package site.addzero.kbox.plugins.tools.storagetool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxInstallerArchiveRecord
import site.addzero.kbox.core.model.KboxInstallerCandidate
import site.addzero.kbox.core.model.KboxLargeFileCandidate
import site.addzero.kbox.core.model.KboxOffloadRecord
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.service.KboxHistoryStore
import site.addzero.kbox.core.service.KboxInstallerService
import site.addzero.kbox.core.service.KboxLargeFileService
import site.addzero.kbox.core.service.KboxOffloadService
import site.addzero.kbox.core.service.KboxPathService
import site.addzero.kbox.core.service.KboxRemotePathService
import site.addzero.kbox.core.service.KboxSettingsRepository
import site.addzero.kbox.core.support.KboxDefaults

@Single
class KboxStorageToolState(
    private val settingsRepository: KboxSettingsRepository,
    private val installerService: KboxInstallerService,
    private val largeFileService: KboxLargeFileService,
    private val offloadService: KboxOffloadService,
    private val historyStore: KboxHistoryStore,
    private val pathService: KboxPathService,
    private val remotePathService: KboxRemotePathService,
) {
    val installerCandidates = mutableStateListOf<KboxInstallerCandidate>()
    val largeFileCandidates = mutableStateListOf<KboxLargeFileCandidate>()
    val installerHistory = mutableStateListOf<KboxInstallerArchiveRecord>()
    val offloadHistory = mutableStateListOf<KboxOffloadRecord>()
    val selectedInstallerPaths = mutableStateListOf<String>()
    val selectedLargeFilePaths = mutableStateListOf<String>()

    var draft by mutableStateOf(KboxDefaults.defaultSettings().toDraft())
        private set
    var isBusy by mutableStateOf(false)
        private set
    var statusText by mutableStateOf("等待扫描")
        private set
    var statusIsError by mutableStateOf(false)
        private set
    var localAppDataDir by mutableStateOf(pathService.appDataDir().absolutePath)
        private set
    var remoteAppDataPreview by mutableStateOf(remotePathService.remoteAppDataDir(currentSettings().ssh))
        private set

    suspend fun load() {
        runAction("配置已加载") {
            val settings = withContext(Dispatchers.IO) {
                settingsRepository.load()
            }
            draft = settings.toDraft()
            refreshPreviews(settings)
            reloadHistory()
        }
    }

    suspend fun saveSettings() {
        runAction("配置已保存") {
            val saved = withContext(Dispatchers.IO) {
                settingsRepository.save(currentSettings())
            }
            draft = saved.toDraft()
            refreshPreviews(saved)
        }
    }

    suspend fun scanInstallers() {
        runAction("安装包扫描完成") {
            val settings = currentSettings()
            val scanned = withContext(Dispatchers.IO) {
                installerService.scan(settings)
            }
            installerCandidates.replaceAll(scanned)
            selectedInstallerPaths.clear()
            refreshPreviews(settings)
            statusText = "安装包扫描完成，共 ${scanned.size} 个候选文件"
        }
    }

    suspend fun collectSelectedInstallers(
        includeAll: Boolean,
    ) {
        val candidates = selectedInstallers(includeAll)
        require(candidates.isNotEmpty()) {
            "请先选择要归档的安装包"
        }
        runAction("安装包归档完成") {
            val result = withContext(Dispatchers.IO) {
                installerService.collect(candidates)
            }
            installerHistory.replaceAll(
                (result.archived + installerHistory).take(30),
            )
            scanInstallers()
            val skippedSuffix = if (result.skipped.isEmpty()) {
                ""
            } else {
                "，跳过 ${result.skipped.size} 个"
            }
            statusText = "安装包归档完成，已移动 ${result.archived.size} 个$skippedSuffix"
        }
    }

    suspend fun scanLargeFiles() {
        runAction("大文件扫描完成") {
            val settings = currentSettings()
            val scanned = withContext(Dispatchers.IO) {
                largeFileService.scan(settings)
            }
            largeFileCandidates.replaceAll(scanned)
            selectedLargeFilePaths.clear()
            refreshPreviews(settings)
            statusText = "大文件扫描完成，共 ${scanned.size} 个候选文件"
        }
    }

    suspend fun testSsh() {
        runAction("SSH 连接成功") {
            withContext(Dispatchers.IO) {
                offloadService.validate(currentSettings())
            }
        }
    }

    suspend fun offloadSelectedLargeFiles(
        includeAll: Boolean,
    ) {
        val candidates = selectedLargeFiles(includeAll)
        require(candidates.isNotEmpty()) {
            "请先选择要发送的大文件"
        }
        runAction("远端迁移完成") {
            val result = withContext(Dispatchers.IO) {
                offloadService.offload(
                    settings = currentSettings(),
                    candidates = candidates,
                )
            }
            offloadHistory.replaceAll(
                (result.uploaded + offloadHistory).take(30),
            )
            scanLargeFiles()
            val skippedSuffix = if (result.skipped.isEmpty()) {
                ""
            } else {
                "，跳过 ${result.skipped.size} 个"
            }
            statusText = "远端迁移完成，已发送 ${result.uploaded.size} 个$skippedSuffix"
        }
    }

    fun updateDraft(
        transform: (KboxSettingsDraft) -> KboxSettingsDraft,
    ) {
        draft = transform(draft)
        refreshPreviews(currentSettings())
    }

    fun toggleInstallerSelection(
        sourcePath: String,
        selected: Boolean,
    ) {
        selectedInstallerPaths.toggleValue(sourcePath, selected)
    }

    fun toggleLargeFileSelection(
        sourcePath: String,
        selected: Boolean,
    ) {
        selectedLargeFilePaths.toggleValue(sourcePath, selected)
    }

    fun remoteAbsolutePathOf(
        candidate: KboxLargeFileCandidate,
    ): String {
        return remotePathService.remoteAbsolutePath(
            config = currentSettings().ssh,
            relativePath = candidate.remoteRelativePath,
        )
    }

    private suspend fun reloadHistory() {
        val installers = withContext(Dispatchers.IO) {
            historyStore.readInstallerHistory().takeLast(30).reversed()
        }
        val offloads = withContext(Dispatchers.IO) {
            historyStore.readOffloadHistory().takeLast(30).reversed()
        }
        installerHistory.replaceAll(installers)
        offloadHistory.replaceAll(offloads)
    }

    private fun refreshPreviews(
        settings: KboxSettings,
    ) {
        localAppDataDir = pathService.appDataDir().absolutePath
        remoteAppDataPreview = remotePathService.remoteAppDataDir(settings.ssh)
    }

    private fun currentSettings(): KboxSettings {
        return KboxDefaults.normalize(draft.toSettings())
    }

    private fun selectedInstallers(
        includeAll: Boolean,
    ): List<KboxInstallerCandidate> {
        return if (includeAll) {
            installerCandidates.toList()
        } else {
            installerCandidates.filter { selectedInstallerPaths.contains(it.sourcePath) }
        }
    }

    private fun selectedLargeFiles(
        includeAll: Boolean,
    ): List<KboxLargeFileCandidate> {
        return if (includeAll) {
            largeFileCandidates.toList()
        } else {
            largeFileCandidates.filter { selectedLargeFilePaths.contains(it.sourcePath) }
        }
    }

    private suspend fun runAction(
        defaultSuccessText: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        statusIsError = false
        try {
            block()
            if (!statusIsError && statusText.isBlank()) {
                statusText = defaultSuccessText
            }
        } catch (error: Throwable) {
            statusIsError = true
            statusText = error.message ?: "执行失败"
        } finally {
            isBusy = false
        }
    }

    private fun <T> SnapshotStateList<T>.replaceAll(
        values: List<T>,
    ) {
        clear()
        addAll(values)
    }

    private fun SnapshotStateList<String>.toggleValue(
        value: String,
        selected: Boolean,
    ) {
        if (selected && !contains(value)) {
            add(value)
        }
        if (!selected) {
            remove(value)
        }
    }
}
