package site.addzero.kbox.plugins.tools.storagetool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxAppDataMigrationPlan
import site.addzero.kbox.core.model.KboxComposeProjectConfig
import site.addzero.kbox.core.model.KboxComposeProjectSnapshot
import site.addzero.kbox.core.model.KboxDetectedPackageManager
import site.addzero.kbox.core.model.KboxDotfileCandidate
import site.addzero.kbox.core.model.KboxInstallerArchiveRecord
import site.addzero.kbox.core.model.KboxInstallerCandidate
import site.addzero.kbox.core.model.KboxLargeFileCandidate
import site.addzero.kbox.core.model.KboxOffloadRecord
import site.addzero.kbox.core.model.KboxPackageDiff
import site.addzero.kbox.core.model.KboxPackageProfile
import site.addzero.kbox.core.model.KboxPackageProfileSummary
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.service.KboxAppDataMigrationService
import site.addzero.kbox.core.service.KboxComposeProjectService
import site.addzero.kbox.core.service.KboxDotfileService
import site.addzero.kbox.core.service.KboxHistoryStore
import site.addzero.kbox.core.service.KboxInstallerService
import site.addzero.kbox.core.service.KboxLargeFileService
import site.addzero.kbox.core.service.KboxOffloadService
import site.addzero.kbox.core.service.KboxPackageProfileService
import site.addzero.kbox.core.service.KboxPathService
import site.addzero.kbox.core.service.KboxRemotePathService
import site.addzero.kbox.core.service.KboxSettingsRepository
import site.addzero.kbox.core.support.KboxDefaults
import site.addzero.kbox.plugin.api.KboxPluginManagerService
import java.awt.Desktop
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class KboxStorageHubTab {
    FILES,
    PACKAGE_PROFILES,
    DOTFILES,
    COMPOSE,
    SETTINGS,
}

enum class KboxComposeAction(
    val displayName: String,
) {
    VALIDATE("校验"),
    UP("启动"),
    DOWN("停止"),
    RESTART("重启"),
    PULL("拉取"),
    LOGS("日志"),
    PS("状态"),
}

@Single
class KboxStorageToolState(
    private val settingsRepository: KboxSettingsRepository,
    private val installerService: KboxInstallerService,
    private val largeFileService: KboxLargeFileService,
    private val offloadService: KboxOffloadService,
    private val historyStore: KboxHistoryStore,
    private val pathService: KboxPathService,
    private val remotePathService: KboxRemotePathService,
    private val migrationService: KboxAppDataMigrationService,
    private val dotfileService: KboxDotfileService,
    private val composeProjectService: KboxComposeProjectService,
    private val packageProfileService: KboxPackageProfileService,
    private val pluginManagerService: KboxPluginManagerService,
) {
    val installerCandidates = mutableStateListOf<KboxInstallerCandidate>()
    val largeFileCandidates = mutableStateListOf<KboxLargeFileCandidate>()
    val installerHistory = mutableStateListOf<KboxInstallerArchiveRecord>()
    val offloadHistory = mutableStateListOf<KboxOffloadRecord>()
    val selectedInstallerPaths = mutableStateListOf<String>()
    val selectedLargeFilePaths = mutableStateListOf<String>()

    val detectedPackageManagers = mutableStateListOf<KboxDetectedPackageManager>()
    val packageProfiles = mutableStateListOf<KboxPackageProfileSummary>()
    val packageDiffs = mutableStateListOf<KboxPackageDiff>()

    val dotfileCandidates = mutableStateListOf<KboxDotfileCandidate>()

    val composeProjects = mutableStateListOf<KboxComposeProjectSnapshot>()

    var draft by mutableStateOf(KboxDefaults.defaultSettings().toDraft())
        private set
    var currentTab by mutableStateOf(KboxStorageHubTab.FILES)
    var isBusy by mutableStateOf(false)
        private set
    var statusText by mutableStateOf("等待加载")
        private set
    var statusIsError by mutableStateOf(false)
        private set

    var activeAppDataDir by mutableStateOf(pathService.appDataDir().absolutePath)
        private set
    var configuredAppDataDirPreview by mutableStateOf(activeAppDataDir)
        private set
    var migrationPreviewText by mutableStateOf("")
        private set
    var remoteAppDataPreview by mutableStateOf(remotePathService.remoteAppDataDir(currentSettings().ssh))
        private set

    var exportProfileName by mutableStateOf(defaultProfileName())
    var selectedPackageProfileFileName by mutableStateOf("")
        private set
    var selectedPackageProfile by mutableStateOf<KboxPackageProfile?>(null)
        private set

    var selectedDotfileTargetPath by mutableStateOf("")
        private set
    var dotfileManualPath by mutableStateOf("")
    var dotfileManualName by mutableStateOf("")

    var composeProjectDirInput by mutableStateOf("")
    var composeProjectNameInput by mutableStateOf("")
    var selectedComposeProjectId by mutableStateOf("")
        private set
    var selectedComposeFile by mutableStateOf("")
        private set
    var composeYamlText by mutableStateOf("")
        private set
    var composeEnvText by mutableStateOf("")
        private set
    var composeCommandOutput by mutableStateOf("")
        private set

    val selectedDotfile: KboxDotfileCandidate?
        get() = dotfileCandidates.firstOrNull { candidate ->
            candidate.targetPath == selectedDotfileTargetPath
        }

    val selectedComposeProject: KboxComposeProjectSnapshot?
        get() = composeProjects.firstOrNull { project ->
            project.config.projectId == selectedComposeProjectId
        }

    suspend fun load() {
        runAction("环境资产已加载") {
            val settings = withContext(Dispatchers.IO) {
                settingsRepository.load()
            }
            draft = settings.toDraft()
            refreshPreviews(settings)
            refreshPackageManagersInternal()
            reloadHistoryInternal()
            refreshPackageProfilesInternal()
            refreshDotfilesInternal()
            refreshComposeProjectsInternal()
        }
    }

    suspend fun saveSettings() {
        runAction("设置已保存") {
            val saved = withContext(Dispatchers.IO) {
                settingsRepository.save(currentSettings())
            }
            pluginManagerService.refresh()
            draft = saved.toDraft()
            refreshPreviews(saved)
            refreshPackageManagersInternal()
            reloadHistoryInternal()
            refreshPackageProfilesInternal()
            refreshDotfilesInternal()
            refreshComposeProjectsInternal()
        }
    }

    suspend fun scanInstallers() {
        runAction("安装包扫描完成") {
            val scanned = withContext(Dispatchers.IO) {
                installerService.scan(currentSettings())
            }
            installerCandidates.replaceAll(scanned)
            selectedInstallerPaths.clear()
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
                (result.archived + installerHistory).take(50),
            )
            val rescanned = withContext(Dispatchers.IO) {
                installerService.scan(currentSettings())
            }
            installerCandidates.replaceAll(rescanned)
            selectedInstallerPaths.clear()
            val skippedSuffix = if (result.skipped.isEmpty()) {
                ""
            } else {
                "，跳过 ${result.skipped.size} 个"
            }
            statusText = "安装包归档完成，已移动 ${result.archived.size} 个$skippedSuffix"
        }
    }

    suspend fun deleteSelectedInstallers(
        includeAll: Boolean,
    ) {
        val candidates = selectedInstallers(includeAll)
        require(candidates.isNotEmpty()) {
            "请先选择要删除的安装包"
        }
        runAction("安装包已删除") {
            val result = withContext(Dispatchers.IO) {
                installerService.deleteFiles(candidates.map { candidate -> candidate.sourcePath })
            }
            val rescanned = withContext(Dispatchers.IO) {
                installerService.scan(currentSettings())
            }
            installerCandidates.replaceAll(rescanned)
            selectedInstallerPaths.clear()
            val skippedSuffix = if (result.skipped.isEmpty()) {
                ""
            } else {
                "，失败 ${result.skipped.size} 个"
            }
            statusText = "安装包已删除 ${result.deleted.size} 个$skippedSuffix"
        }
    }

    suspend fun scanLargeFiles() {
        runAction("大文件扫描完成") {
            val scanned = withContext(Dispatchers.IO) {
                largeFileService.scan(currentSettings())
            }
            largeFileCandidates.replaceAll(scanned)
            selectedLargeFilePaths.clear()
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
                (result.uploaded + offloadHistory).take(50),
            )
            val rescanned = withContext(Dispatchers.IO) {
                largeFileService.scan(currentSettings())
            }
            largeFileCandidates.replaceAll(rescanned)
            selectedLargeFilePaths.clear()
            val skippedSuffix = if (result.skipped.isEmpty()) {
                ""
            } else {
                "，跳过 ${result.skipped.size} 个"
            }
            statusText = "远端迁移完成，已发送 ${result.uploaded.size} 个$skippedSuffix"
        }
    }

    suspend fun openContainingFolder(
        absolutePath: String,
    ) {
        runAction("目录已打开") {
            withContext(Dispatchers.IO) {
                val targetFile = File(absolutePath).absoluteFile
                val openFile = if (targetFile.isDirectory) {
                    targetFile
                } else {
                    targetFile.parentFile ?: error("目标没有父目录：$absolutePath")
                }
                check(Desktop.isDesktopSupported()) {
                    "当前环境不支持打开目录"
                }
                Desktop.getDesktop().open(openFile)
            }
        }
    }

    suspend fun refreshPackageProfiles() {
        runAction("包清单已刷新") {
            refreshPackageManagersInternal()
            refreshPackageProfilesInternal()
            refreshSelectedProfileDiffInternal()
        }
    }

    suspend fun exportPackageProfile() {
        runAction("包清单导出完成") {
            withContext(Dispatchers.IO) {
                packageProfileService.exportProfile(exportProfileName)
            }
            refreshPackageProfilesInternal()
            refreshSelectedProfileDiffInternal()
            exportProfileName = defaultProfileName()
        }
    }

    suspend fun importSelectedPackageProfile() {
        val fileName = selectedPackageProfileFileName.ifBlank {
            error("请先选择一个包清单")
        }
        runAction("包清单导入完成") {
            val result = withContext(Dispatchers.IO) {
                packageProfileService.importProfile(fileName)
            }
            refreshSelectedProfileDiffInternal()
            val failedCount = result.entries.sumOf { entry -> entry.failedPackages.size }
            val installedCount = result.entries.sumOf { entry -> entry.installedPackages.size }
            statusText = if (failedCount == 0) {
                "包清单导入完成，新增安装 ${installedCount} 个"
            } else {
                "包清单导入完成，新增安装 ${installedCount} 个，失败 ${failedCount} 个"
            }
        }
    }

    fun selectPackageProfile(
        fileName: String,
    ) {
        selectedPackageProfileFileName = fileName
        if (fileName.isBlank()) {
            selectedPackageProfile = null
            packageDiffs.clear()
            return
        }
        val profile = packageProfileService.readProfile(fileName)
        selectedPackageProfile = profile
        packageDiffs.replaceAll(packageProfileService.diffProfile(profile))
    }

    suspend fun refreshDotfiles() {
        runAction("Dotfile 列表已刷新") {
            refreshDotfilesInternal()
        }
    }

    suspend fun importSelectedDotfile() {
        val candidate = selectedDotfile ?: error("请先选择一个 dotfile")
        runAction("Dotfile 已托管") {
            withContext(Dispatchers.IO) {
                dotfileService.importTarget(
                    targetPath = candidate.targetPath,
                    logicalName = candidate.logicalName,
                )
            }
            refreshDotfilesInternal()
        }
    }

    suspend fun importManualDotfile() {
        runAction("Dotfile 已托管") {
            withContext(Dispatchers.IO) {
                dotfileService.importTarget(
                    targetPath = dotfileManualPath,
                    logicalName = dotfileManualName,
                )
            }
            dotfileManualPath = ""
            dotfileManualName = ""
            refreshDotfilesInternal()
        }
    }

    suspend fun relinkSelectedDotfile() {
        val candidate = selectedDotfile ?: error("请先选择一个 dotfile")
        runAction("符号链接已重建") {
            withContext(Dispatchers.IO) {
                dotfileService.rebuildLink(candidate.targetPath)
            }
            refreshDotfilesInternal()
        }
    }

    suspend fun removeSelectedDotfile() {
        val candidate = selectedDotfile ?: error("请先选择一个 dotfile")
        runAction("已取消托管") {
            withContext(Dispatchers.IO) {
                dotfileService.removeManagedTarget(candidate.targetPath)
            }
            refreshDotfilesInternal()
        }
    }

    fun selectDotfile(
        targetPath: String,
    ) {
        selectedDotfileTargetPath = targetPath
    }

    suspend fun refreshComposeProjects() {
        runAction("Compose 项目已刷新") {
            refreshComposeProjectsInternal()
        }
    }

    suspend fun registerComposeProject() {
        runAction("Compose 项目已注册") {
            withContext(Dispatchers.IO) {
                composeProjectService.registerProject(
                    directory = composeProjectDirInput,
                    name = composeProjectNameInput,
                )
            }
            composeProjectDirInput = ""
            composeProjectNameInput = ""
            refreshComposeProjectsInternal()
        }
    }

    suspend fun removeSelectedComposeProject() {
        val project = selectedComposeProject ?: error("请先选择一个 Compose 项目")
        runAction("Compose 项目已移除") {
            withContext(Dispatchers.IO) {
                composeProjectService.removeProject(project.config.projectId)
            }
            refreshComposeProjectsInternal()
        }
    }

    suspend fun saveSelectedComposeFile() {
        val project = selectedComposeProject ?: error("请先选择一个 Compose 项目")
        val fileName = selectedComposeFile.ifBlank {
            error("请先选择一个 Compose 文件")
        }
        runAction("Compose 文件已保存") {
            withContext(Dispatchers.IO) {
                composeProjectService.saveComposeFile(
                    projectId = project.config.projectId,
                    composeFile = fileName,
                    content = composeYamlText,
                )
            }
            refreshComposeProjectsInternal()
        }
    }

    suspend fun saveSelectedEnvFile() {
        val project = selectedComposeProject ?: error("请先选择一个 Compose 项目")
        runAction(".env 已保存") {
            withContext(Dispatchers.IO) {
                composeProjectService.saveEnvFile(
                    projectId = project.config.projectId,
                    content = composeEnvText,
                )
            }
        }
    }

    suspend fun runComposeAction(
        action: KboxComposeAction,
    ) {
        val project = selectedComposeProject ?: error("请先选择一个 Compose 项目")
        runAction("Compose 命令执行完成") {
            val result = withContext(Dispatchers.IO) {
                when (action) {
                    KboxComposeAction.VALIDATE -> composeProjectService.validateConfig(project.config.projectId)
                    KboxComposeAction.UP -> composeProjectService.upDetached(project.config.projectId)
                    KboxComposeAction.DOWN -> composeProjectService.down(project.config.projectId)
                    KboxComposeAction.RESTART -> composeProjectService.restart(project.config.projectId)
                    KboxComposeAction.PULL -> composeProjectService.pull(project.config.projectId)
                    KboxComposeAction.LOGS -> composeProjectService.logs(project.config.projectId)
                    KboxComposeAction.PS -> composeProjectService.ps(project.config.projectId)
                }
            }
            composeCommandOutput = buildComposeCommandOutput(action, result)
            statusText = if (result.success) {
                "Compose ${action.displayName} 执行成功"
            } else {
                "Compose ${action.displayName} 执行失败"
            }
            statusIsError = !result.success
            refreshComposeProjectsInternal()
        }
    }

    fun selectComposeProject(
        projectId: String,
    ) {
        selectedComposeProjectId = projectId
        loadSelectedComposeFileContent()
    }

    fun selectComposeFile(
        composeFile: String,
    ) {
        selectedComposeFile = composeFile
        loadSelectedComposeFileContent()
    }

    fun updateComposeYamlText(
        value: String,
    ) {
        composeYamlText = value
    }

    fun updateComposeEnvText(
        value: String,
    ) {
        composeEnvText = value
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

    private suspend fun reloadHistoryInternal() {
        val installers = withContext(Dispatchers.IO) {
            historyStore.readInstallerHistory().takeLast(50).reversed()
        }
        val offloads = withContext(Dispatchers.IO) {
            historyStore.readOffloadHistory().takeLast(50).reversed()
        }
        installerHistory.replaceAll(installers)
        offloadHistory.replaceAll(offloads)
    }

    private suspend fun refreshPackageManagersInternal() {
        val managers = withContext(Dispatchers.IO) {
            packageProfileService.detectManagers()
        }
        detectedPackageManagers.replaceAll(managers)
    }

    private suspend fun refreshPackageProfilesInternal() {
        val profiles = withContext(Dispatchers.IO) {
            packageProfileService.listProfiles()
        }
        packageProfiles.replaceAll(profiles)
        if (profiles.none { profile -> profile.fileName == selectedPackageProfileFileName }) {
            selectedPackageProfileFileName = profiles.firstOrNull()?.fileName.orEmpty()
        }
        refreshSelectedProfileDiffInternal()
    }

    private suspend fun refreshSelectedProfileDiffInternal() {
        val selectedFileName = selectedPackageProfileFileName
        if (selectedFileName.isBlank()) {
            selectedPackageProfile = null
            packageDiffs.clear()
            return
        }
        val profile = withContext(Dispatchers.IO) {
            packageProfileService.readProfile(selectedFileName)
        }
        val diffs = withContext(Dispatchers.IO) {
            packageProfileService.diffProfile(profile)
        }
        selectedPackageProfile = profile
        packageDiffs.replaceAll(diffs)
    }

    private suspend fun refreshDotfilesInternal() {
        val candidates = withContext(Dispatchers.IO) {
            dotfileService.discoverCandidates()
        }
        dotfileCandidates.replaceAll(candidates)
        if (candidates.none { candidate -> candidate.targetPath == selectedDotfileTargetPath }) {
            selectedDotfileTargetPath = candidates.firstOrNull()?.targetPath.orEmpty()
        }
    }

    private suspend fun refreshComposeProjectsInternal() {
        val projects = withContext(Dispatchers.IO) {
            composeProjectService.listProjects()
        }
        composeProjects.replaceAll(projects)
        if (projects.none { project -> project.config.projectId == selectedComposeProjectId }) {
            selectedComposeProjectId = projects.firstOrNull()?.config?.projectId.orEmpty()
        }
        loadSelectedComposeFileContent()
    }

    private fun loadSelectedComposeFileContent() {
        val project = selectedComposeProject
        if (project == null) {
            selectedComposeFile = ""
            composeYamlText = ""
            composeEnvText = ""
            composeCommandOutput = ""
            return
        }
        val files = composeProjectService.readProjectFiles(project.config.projectId)
        if (selectedComposeFile.isBlank() || !project.config.composeFiles.contains(selectedComposeFile)) {
            selectedComposeFile = project.config.composeFiles.firstOrNull().orEmpty()
        }
        composeYamlText = files.composeFileContent[selectedComposeFile].orEmpty()
        composeEnvText = files.envContent
    }

    private fun refreshPreviews(
        settings: KboxSettings,
    ) {
        activeAppDataDir = pathService.appDataDir().absolutePath
        configuredAppDataDirPreview = pathService.resolveAppDataDir(
            localAppDataOverride = settings.localAppDataOverride,
            createDirectories = false,
        ).absolutePath
        migrationPreviewText = formatMigrationPreview(
            migrationService.preview(settings.localAppDataOverride),
        )
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
            installerCandidates.filter { candidate -> selectedInstallerPaths.contains(candidate.sourcePath) }
        }
    }

    private fun selectedLargeFiles(
        includeAll: Boolean,
    ): List<KboxLargeFileCandidate> {
        return if (includeAll) {
            largeFileCandidates.toList()
        } else {
            largeFileCandidates.filter { candidate -> selectedLargeFilePaths.contains(candidate.sourcePath) }
        }
    }

    private suspend fun runAction(
        defaultSuccessText: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        statusIsError = false
        statusText = ""
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

    private fun buildComposeCommandOutput(
        action: KboxComposeAction,
        result: site.addzero.kbox.core.model.KboxCommandResult,
    ): String {
        return buildString {
            appendLine("动作：${action.displayName}")
            appendLine("命令：${result.commandLine}")
            appendLine("工作目录：${result.workingDirectory}")
            appendLine("退出码：${result.exitCode}")
            appendLine("耗时：${result.durationMillis} ms")
            appendLine()
            append(result.output.ifBlank { "无输出" })
        }
    }

    private fun formatMigrationPreview(
        plan: KboxAppDataMigrationPlan,
    ): String {
        return buildString {
            appendLine("当前目录：${plan.currentPath}")
            appendLine("目标目录：${plan.targetPath}")
            appendLine("需要迁移：${if (plan.needsMigration) "是" else "否"}")
            if (plan.warnings.isNotEmpty()) {
                appendLine("提示：${plan.warnings.joinToString("；")}")
            }
            if (plan.blockers.isNotEmpty()) {
                appendLine("阻塞：${plan.blockers.joinToString("；")}")
            }
        }.trim()
    }

    private fun defaultProfileName(): String {
        return "profile-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())}"
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
