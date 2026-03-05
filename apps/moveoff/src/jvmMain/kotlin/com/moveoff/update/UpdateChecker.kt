package com.moveoff.update

import com.moveoff.event.EventBus
import com.moveoff.event.UIEvent
import com.moveoff.model.UpdateChannel
import com.moveoff.model.UpdateSettings
import com.moveoff.model.VersionInfo
import com.moveoff.storage.getSettingsDirectory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Duration.Companion.hours

/**
 * 更新检查结果
 */
sealed class UpdateCheckResult {
    data class UpdateAvailable(val versionInfo: VersionInfo) : UpdateCheckResult()
    data object UpToDate : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

/**
 * 下载进度
 */
data class DownloadProgress(
    val downloadedBytes: Long,
    val totalBytes: Long,
    val percentage: Int
) {
    @Suppress("unused")
    val isComplete: Boolean get() = downloadedBytes >= totalBytes && totalBytes > 0
}

/**
 * 更新状态
 */
enum class UpdateState {
    IDLE,               // 空闲
    CHECKING,           // 检查中
    DOWNLOADING,        // 下载中
    DOWNLOADED,         // 已下载
    INSTALLING,         // 安装中
    @Suppress("unused")
    INSTALLED,          // 已安装
    ERROR               // 错误
}

/**
 * 自动更新检查器
 */
class UpdateChecker(
    private val currentVersion: String,
    private val updateSettings: UpdateSettings,
    private val checkIntervalHours: Int = 24,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
        }
    }

    private val _updateState = MutableStateFlow(UpdateState.IDLE)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _downloadProgress = MutableStateFlow(DownloadProgress(0, 0, 0))
    val downloadProgress: StateFlow<DownloadProgress> = _downloadProgress.asStateFlow()

    private val _availableUpdate = MutableStateFlow<VersionInfo?>(null)
    val availableUpdate: StateFlow<VersionInfo?> = _availableUpdate.asStateFlow()

    private var checkJob: Job? = null

    companion object {
        // 默认更新服务器，可以通过环境变量覆盖
        private val UPDATE_SERVER_URL = System.getenv("MOVEOFF_UPDATE_URL")
            ?: "https://api.moveoff.addzero.site/updates"

        // GitHub Releases 作为备用更新源
        private const val GITHUB_API_URL = "https://api.github.com/repos/addzero/moveoff/releases/latest"
    }

    /**
     * 启动自动检查
     */
    fun startAutoCheck() {
        if (!updateSettings.checkUpdatesAutomatically) {
            println("自动更新检查已禁用")
            return
        }

        checkJob?.cancel()
        checkJob = scope.launch {
            // 立即检查一次
            checkForUpdate()

            // 定期循环检查
            while (isActive) {
                delay(checkIntervalHours.hours)
                checkForUpdate()
            }
        }
    }

    /**
     * 停止自动检查
     */
    fun stopAutoCheck() {
        checkJob?.cancel()
        checkJob = null
    }

    /**
     * 检查更新
     */
    suspend fun checkForUpdate(): UpdateCheckResult = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.CHECKING

        try {
            val result = fetchLatestVersion()

            when (result) {
                is UpdateCheckResult.UpdateAvailable -> {
                    val version = result.versionInfo

                    // 检查是否被用户跳过
                    if (version.version == updateSettings.skipVersion) {
                        println("版本 ${version.version} 已被用户跳过")
                        _updateState.value = UpdateState.IDLE
                        return@withContext UpdateCheckResult.UpToDate
                    }

                    // 检查是否为强制更新
                    if (version.mandatory && version.minVersion != null) {
                        if (isVersionOlder(currentVersion, version.minVersion)) {
                            println("强制更新要求：当前版本 $currentVersion 低于最低版本 ${version.minVersion}")
                        }
                    }

                    _availableUpdate.value = version
                    _updateState.value = UpdateState.IDLE

                    // 发送通知
                    EventBus.emit(UIEvent.UpdateAvailable(
                        version = version.version,
                        releaseNotes = version.releaseNotes,
                        mandatory = version.mandatory
                    ))

                    // 如果设置为自动下载，开始下载
                    if (updateSettings.downloadUpdatesAutomatically) {
                        downloadUpdate(version)
                    }

                    result
                }
                is UpdateCheckResult.UpToDate -> {
                    _updateState.value = UpdateState.IDLE
                    result
                }
                is UpdateCheckResult.Error -> {
                    _updateState.value = UpdateState.ERROR
                    result
                }
            }
        } catch (e: Exception) {
            println("检查更新失败: ${e.message}")
            e.printStackTrace()
            _updateState.value = UpdateState.ERROR
            UpdateCheckResult.Error(e.message ?: "未知错误")
        }
    }

    /**
     * 获取最新版本信息
     */
    private suspend fun fetchLatestVersion(): UpdateCheckResult {
        return try {
            // 尝试主更新服务器
            val response = client.get("$UPDATE_SERVER_URL/check") {
                parameter("version", currentVersion)
                parameter("channel", updateSettings.updateChannel.name.lowercase())
                parameter("platform", getPlatform())
                parameter("arch", getArchitecture())
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<String>()
                val json = Json { ignoreUnknownKeys = true }
                val versionInfo = json.decodeFromString<VersionInfo>(body)

                if (isNewerVersion(versionInfo.version, currentVersion)) {
                    UpdateCheckResult.UpdateAvailable(versionInfo)
                } else {
                    UpdateCheckResult.UpToDate
                }
            } else {
                // 主服务器失败，尝试 GitHub
                fetchFromGitHub()
            }
        } catch (e: Exception) {
            println("主更新服务器不可用，尝试 GitHub: ${e.message}")
            fetchFromGitHub()
        }
    }

    /**
     * 从 GitHub Releases 获取
     */
    private suspend fun fetchFromGitHub(): UpdateCheckResult {
        return try {
            val response = client.get(GITHUB_API_URL) {
                header("Accept", "application/vnd.github.v3+json")
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<String>()
                val json = Json { ignoreUnknownKeys = true }
                val release = json.decodeFromString<GitHubRelease>(body)

                // 检查是否是预发布版本
                if (release.prerelease && updateSettings.updateChannel == UpdateChannel.STABLE) {
                    return UpdateCheckResult.UpToDate
                }

                val version = release.tagName.removePrefix("v")

                if (isNewerVersion(version, currentVersion)) {
                    // 查找适合当前平台的资源
                    val asset = findPlatformAsset(release.assets)
                        ?: return UpdateCheckResult.Error("未找到适合当前平台的安装包")

                    val versionInfo = VersionInfo(
                        version = version,
                        versionCode = parseVersionCode(version),
                        releaseDate = release.publishedAt,
                        releaseNotes = release.body,
                        downloadUrl = asset.browserDownloadUrl,
                        mandatory = false
                    )
                    UpdateCheckResult.UpdateAvailable(versionInfo)
                } else {
                    UpdateCheckResult.UpToDate
                }
            } else {
                UpdateCheckResult.Error("无法获取版本信息: ${response.status}")
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error("检查更新失败: ${e.message}")
        }
    }

    /**
     * 下载更新
     */
    suspend fun downloadUpdate(versionInfo: VersionInfo): File? = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.DOWNLOADING
        _downloadProgress.value = DownloadProgress(0, 0, 0)

        try {
            val downloadDir = File(getSettingsDirectory(), "updates").apply {
                mkdirs()
            }

            val fileName = getPlatformInstallerName(versionInfo.version)
            val outputFile = File(downloadDir, fileName)

            // 如果文件已存在且完整，直接返回
            if (outputFile.exists() && outputFile.length() > 0) {
                println("更新文件已存在: ${outputFile.absolutePath}")
                _updateState.value = UpdateState.DOWNLOADED
                return@withContext outputFile
            }

            // 下载文件
            val response = client.get(versionInfo.downloadUrl)
            val totalBytes = response.headers["Content-Length"]?.toLongOrNull() ?: -1L

            if (response.status != HttpStatusCode.OK) {
                throw Exception("下载失败: ${response.status}")
            }

            response.bodyAsChannel().apply {
                outputFile.outputStream().use { output ->
                    var downloadedBytes = 0L
                    val buffer = ByteArray(8192)

                    while (!isClosedForRead) {
                        val read = readAvailable(buffer, 0, buffer.size)
                        if (read <= 0) break

                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val percentage = if (totalBytes > 0) {
                            ((downloadedBytes * 100) / totalBytes).toInt()
                        } else 0

                        _downloadProgress.value = DownloadProgress(
                            downloadedBytes = downloadedBytes,
                            totalBytes = totalBytes,
                            percentage = percentage
                        )
                    }
                }
            }

            _updateState.value = UpdateState.DOWNLOADED
            println("更新下载完成: ${outputFile.absolutePath}")

            outputFile
        } catch (e: Exception) {
            println("下载更新失败: ${e.message}")
            e.printStackTrace()
            _updateState.value = UpdateState.ERROR
            null
        }
    }

    /**
     * 安装更新
     */
    fun installUpdate(file: File): Boolean {
        _updateState.value = UpdateState.INSTALLING

        return try {
            when {
                System.getProperty("os.name").contains("Mac", ignoreCase = true) -> {
                    installOnMac(file)
                }
                System.getProperty("os.name").contains("Windows", ignoreCase = true) -> {
                    installOnWindows(file)
                }
                else -> {
                    installOnLinux(file)
                }
            }
        } catch (e: Exception) {
            println("安装更新失败: ${e.message}")
            e.printStackTrace()
            _updateState.value = UpdateState.ERROR
            false
        }
    }

    /**
     * macOS 安装
     */
    private fun installOnMac(file: File): Boolean {
        return when {
            file.name.endsWith(".dmg") -> {
                // 打开 DMG 文件
                ProcessBuilder("open", file.absolutePath).start()
                true
            }
            file.name.endsWith(".pkg") -> {
                // 安装 PKG
                ProcessBuilder("open", "-W", file.absolutePath).start()
                true
            }
            else -> false
        }
    }

    /**
     * Windows 安装
     */
    private fun installOnWindows(file: File): Boolean {
        return when {
            file.name.endsWith(".msi") -> {
                ProcessBuilder("msiexec", "/i", file.absolutePath, "/passive", "/norestart").start()
                true
            }
            file.name.endsWith(".exe") -> {
                ProcessBuilder(file.absolutePath, "/SILENT", "/NORESTART").start()
                true
            }
            else -> false
        }
    }

    /**
     * Linux 安装
     */
    private fun installOnLinux(file: File): Boolean {
        return when {
            file.name.endsWith(".deb") -> {
                ProcessBuilder("sudo", "dpkg", "-i", file.absolutePath).start()
                true
            }
            file.name.endsWith(".AppImage") -> {
                file.setExecutable(true)
                ProcessBuilder(file.absolutePath).start()
                true
            }
            else -> false
        }
    }

    /**
     * 跳过此版本
     */
    fun skipVersion(version: String) {
        // TODO: 保存到设置中
        _availableUpdate.value = null
    }

    /**
     * 稍后提醒
     */
    fun remindLater() {
        _availableUpdate.value = null
    }

    /**
     * 关闭资源
     */
    fun close() {
        stopAutoCheck()
        scope.cancel()
        client.close()
    }

    // ========== 辅助方法 ==========

    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        return compareVersions(newVersion, currentVersion) > 0
    }

    private fun isVersionOlder(version: String, minVersion: String): Boolean {
        return compareVersions(version, minVersion) < 0
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }

    private fun parseVersionCode(version: String): Int {
        val parts = version.split(".")
        val major = parts.getOrElse(0) { "0" }.toIntOrNull() ?: 0
        val minor = parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0
        val patch = parts.getOrElse(2) { "0" }.toIntOrNull() ?: 0
        return major * 10000 + minor * 100 + patch
    }

    private fun getPlatform(): String {
        return when {
            System.getProperty("os.name").contains("Mac", ignoreCase = true) -> "macos"
            System.getProperty("os.name").contains("Windows", ignoreCase = true) -> "windows"
            else -> "linux"
        }
    }

    private fun getArchitecture(): String {
        val arch = System.getProperty("os.arch")
        return when {
            arch.contains("aarch64") || arch.contains("arm64") -> "arm64"
            arch.contains("64") -> "x64"
            else -> "x86"
        }
    }

    private fun getPlatformInstallerName(version: String): String {
        return when {
            System.getProperty("os.name").contains("Mac", ignoreCase = true) ->
                "MoveOff-$version.dmg"
            System.getProperty("os.name").contains("Windows", ignoreCase = true) ->
                "MoveOff-$version.msi"
            else ->
                "MoveOff-$version.deb"
        }
    }

    private fun findPlatformAsset(assets: List<GitHubAsset>): GitHubAsset? {
        val platform = getPlatform()
        val arch = getArchitecture()

        // 优先查找匹配平台和架构的资源
        return assets.find { it.name.contains(platform, ignoreCase = true) &&
                (arch == "x64" || it.name.contains(arch, ignoreCase = true)) }
            ?: assets.find { it.name.contains(platform, ignoreCase = true) }
            ?: assets.firstOrNull()
    }
}

/**
 * GitHub API 响应数据类
 */
@kotlinx.serialization.Serializable
data class GitHubRelease(
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val prerelease: Boolean,
    val assets: List<GitHubAsset>
)

@kotlinx.serialization.Serializable
data class GitHubAsset(
    val name: String,
    val browserDownloadUrl: String,
    val size: Long
)

/**
 * 更新检查器管理器 - 单例
 */
object UpdateCheckerManager {
    private var instance: UpdateChecker? = null

    fun initialize(
        currentVersion: String,
        updateSettings: UpdateSettings
    ): UpdateChecker {
        if (instance == null) {
            instance = UpdateChecker(currentVersion, updateSettings).apply {
                startAutoCheck()
            }
        }
        return instance!!
    }

    fun get(): UpdateChecker {
        return instance ?: throw IllegalStateException("UpdateChecker未初始化")
    }

    fun stop() {
        instance?.close()
        instance = null
    }
}
