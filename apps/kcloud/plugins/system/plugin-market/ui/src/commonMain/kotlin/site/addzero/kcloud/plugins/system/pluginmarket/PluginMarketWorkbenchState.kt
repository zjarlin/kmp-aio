package site.addzero.kcloud.plugins.system.pluginmarket

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.model.*
import site.addzero.kcloud.plugins.system.pluginmarket.service.*

@Single
class PluginMarketWorkbenchState(
    private val packageService: PluginPackageService,
    private val fileService: PluginSourceFileService,
    private val discoveryService: PluginDiscoveryService,
    private val deploymentService: PluginDeploymentService,
    private val configService: PluginMarketConfigService,
) {
    val packages = mutableStateListOf<PluginPackageDto>()
    val discoveries = mutableStateListOf<PluginDiscoveryItemDto>()
    val jobs = mutableStateListOf<PluginDeploymentJobDto>()
    val currentFiles = mutableStateListOf<PluginSourceFileDto>()

    var selectedPackageId by mutableStateOf<String?>(null)
    var selectedDiscoveryId by mutableStateOf<String?>(null)
    var selectedFileId by mutableStateOf<String?>(null)
    var selectedJobId by mutableStateOf<String?>(null)
    var searchKeyword by mutableStateOf("")
    var statusMessage by mutableStateOf("插件市场已就绪")
    var statusIsError by mutableStateOf(false)
    var previewText by mutableStateOf("")
    var diffText by mutableStateOf("")
    var logsText by mutableStateOf("")
    var isBusy by mutableStateOf(false)

    var packageName by mutableStateOf("")
    var packagePluginId by mutableStateOf("")
    var packageGroup by mutableStateOf("system")
    var packageDescription by mutableStateOf("")
    var packageVersion by mutableStateOf("0.1.0")
    var packageBasePackage by mutableStateOf("site.addzero.kcloud.plugins")
    var packageModuleDir by mutableStateOf("")
    var packageEnabled by mutableStateOf(true)
    var packagePresetKind by mutableStateOf(PluginPresetKind.BLANK)
    var packageComposeKoinModuleClass by mutableStateOf("")
    var packageServerKoinModuleClass by mutableStateOf("")
    var packageRouteRegistrarImport by mutableStateOf("")
    var packageRouteRegistrarCall by mutableStateOf("")

    var fileRelativePath by mutableStateOf("")
    var fileContent by mutableStateOf("")
    var fileGroup by mutableStateOf("source")

    var config by mutableStateOf(PluginMarketConfigDto())
        private set

    fun updateConfig(transform: (PluginMarketConfigDto) -> PluginMarketConfigDto) {
        config = transform(config)
    }

    suspend fun refreshAll() {
        isBusy = true
        try {
            packages.resetWith(packageService.list())
            discoveries.resetWith(discoveryService.discover())
            jobs.resetWith(deploymentService.listJobs())
            config = configService.read()
            syncSelections()
            updateStatus("已刷新插件市场")
        } catch (error: Throwable) {
            updateStatus(error.message ?: "刷新失败", true)
            throw error
        } finally {
            isBusy = false
        }
    }

    suspend fun selectPackage(id: String?) {
        selectedPackageId = id
        selectedDiscoveryId = null
        selectedJobId = null
        selectedFileId = null
        if (id == null) {
            beginCreatePackage()
            currentFiles.clear()
            previewText = ""
            diffText = ""
            logsText = ""
            return
        }
        val aggregate = packageService.aggregate(id)
        applyAggregate(aggregate)
        previewText = aggregate.files.joinToString("\n") { it.relativePath }
        logsText = aggregate.jobs.firstOrNull()?.stdoutText.orEmpty()
    }

    fun selectDiscovery(id: String?) {
        selectedDiscoveryId = id
        selectedPackageId = null
        selectedJobId = null
        selectedFileId = null
        val discovery = discoveries.firstOrNull { it.discoveryId == id }
        previewText = buildString {
            appendLine("模块路径：${discovery?.moduleDir.orEmpty()}")
            appendLine("Gradle 路径：${discovery?.gradlePath.orEmpty()}")
            if (discovery != null) {
                if (discovery.issues.isEmpty()) {
                    appendLine("结构检查：通过")
                } else {
                    appendLine("结构检查：")
                    discovery.issues.forEach { issue ->
                        appendLine("- $issue")
                    }
                }
            }
        }
        diffText = ""
        logsText = ""
    }

    fun selectFile(id: String?) {
        selectedFileId = id
        val file = currentFiles.firstOrNull { it.id == id }
        fileRelativePath = file?.relativePath.orEmpty()
        fileContent = file?.content.orEmpty()
        fileGroup = file?.fileGroup ?: "source"
        previewText = file?.content.orEmpty()
    }

    fun selectJob(id: String?) {
        selectedJobId = id
        val job = jobs.firstOrNull { it.id == id }
        logsText = buildString {
            appendLine(job?.summaryText.orEmpty())
            job?.stdoutText?.takeIf { it.isNotBlank() }?.let { stdout ->
                appendLine()
                appendLine(stdout)
            }
            job?.stderrText?.takeIf { it.isNotBlank() }?.let { stderr ->
                appendLine()
                appendLine(stderr)
            }
        }
    }

    fun beginCreatePackage() {
        selectedPackageId = null
        packageName = ""
        packagePluginId = ""
        packageGroup = "system"
        packageDescription = ""
        packageVersion = "0.1.0"
        packageBasePackage = "site.addzero.kcloud.plugins"
        packageModuleDir = ""
        packageEnabled = true
        packagePresetKind = PluginPresetKind.BLANK
        packageComposeKoinModuleClass = ""
        packageServerKoinModuleClass = ""
        packageRouteRegistrarImport = ""
        packageRouteRegistrarCall = ""
        currentFiles.clear()
        beginCreateFile()
    }

    fun beginCreateFile() {
        selectedFileId = null
        fileRelativePath = ""
        fileContent = ""
        fileGroup = "source"
    }

    suspend fun savePackage() {
        isBusy = true
        try {
            val selectedId = selectedPackageId
            if (selectedId == null) {
                val aggregate = packageService.create(
                    CreatePluginPackageRequest(
                        pluginId = packagePluginId.trim(),
                        name = packageName.trim(),
                        pluginGroup = packageGroup.trim().ifBlank { null },
                        description = packageDescription.trim().ifBlank { null },
                        version = packageVersion.trim().ifBlank { "0.1.0" },
                        basePackage = packageBasePackage.trim(),
                        moduleDir = packageModuleDir.trim().ifBlank { null },
                        enabled = packageEnabled,
                        presetKind = packagePresetKind,
                        composeKoinModuleClass = packageComposeKoinModuleClass.trim().ifBlank { null },
                        serverKoinModuleClass = packageServerKoinModuleClass.trim().ifBlank { null },
                        routeRegistrarImport = packageRouteRegistrarImport.trim().ifBlank { null },
                        routeRegistrarCall = packageRouteRegistrarCall.trim().ifBlank { null },
                    ),
                )
                applyAggregate(aggregate)
            } else {
                packageService.update(
                    selectedId,
                    UpdatePluginPackageRequest(
                        name = packageName.trim(),
                        pluginGroup = packageGroup.trim().ifBlank { null },
                        description = packageDescription.trim().ifBlank { null },
                        version = packageVersion.trim().ifBlank { "0.1.0" },
                        basePackage = packageBasePackage.trim(),
                        moduleDir = packageModuleDir.trim(),
                        enabled = packageEnabled,
                        composeKoinModuleClass = packageComposeKoinModuleClass.trim().ifBlank { null },
                        serverKoinModuleClass = packageServerKoinModuleClass.trim().ifBlank { null },
                        routeRegistrarImport = packageRouteRegistrarImport.trim().ifBlank { null },
                        routeRegistrarCall = packageRouteRegistrarCall.trim().ifBlank { null },
                    ),
                )
            }
            refreshAll()
            selectedPackageId?.let { selectPackage(it) }
            updateStatus("已保存插件包")
        } catch (error: Throwable) {
            updateStatus(error.message ?: "保存插件包失败", true)
            throw error
        } finally {
            isBusy = false
        }
    }

    suspend fun deleteSelectedPackage() {
        val id = selectedPackageId ?: return
        isBusy = true
        try {
            packageService.delete(id)
            beginCreatePackage()
            refreshAll()
            updateStatus("已删除插件包")
        } catch (error: Throwable) {
            updateStatus(error.message ?: "删除插件包失败", true)
            throw error
        } finally {
            isBusy = false
        }
    }

    suspend fun saveFile() {
        val packageId = selectedPackageId ?: return
        isBusy = true
        try {
            val saved = fileService.save(
                SavePluginSourceFileRequest(
                    packageId = packageId,
                    relativePath = fileRelativePath.trim(),
                    content = fileContent,
                    fileGroup = fileGroup.trim().ifBlank { "source" },
                ),
            )
            refreshCurrentPackageFiles()
            selectedFileId = saved.id
            selectFile(saved.id)
            updateStatus("已保存源码文件")
        } catch (error: Throwable) {
            updateStatus(error.message ?: "保存源码文件失败", true)
            throw error
        } finally {
            isBusy = false
        }
    }

    suspend fun deleteSelectedFile() {
        val id = selectedFileId ?: return
        isBusy = true
        try {
            fileService.delete(id)
            refreshCurrentPackageFiles()
            beginCreateFile()
            updateStatus("已删除源码文件")
        } catch (error: Throwable) {
            updateStatus(error.message ?: "删除源码文件失败", true)
            throw error
        } finally {
            isBusy = false
        }
    }

    suspend fun importSelectedDiscovery() {
        val discovery = discoveries.firstOrNull { it.discoveryId == selectedDiscoveryId } ?: return
        isBusy = true
        try {
            val aggregate = discoveryService.importDiscovered(
                ImportDiscoveredPluginRequest(
                    discoveryId = discovery.discoveryId,
                    managedPluginId = discovery.pluginId,
                    managedName = discovery.pluginId,
                ),
            )
            applyAggregate(aggregate)
            refreshAll()
            updateStatus("已导入发现模块")
        } catch (error: Throwable) {
            updateStatus(error.message ?: "导入发现模块失败", true)
            throw error
        } finally {
            isBusy = false
        }
    }

    suspend fun deploySelectedPackage(runBuild: Boolean? = null) {
        val packageId = selectedPackageId ?: return
        isBusy = true
        try {
            val job = deploymentService.deploy(
                DeployPluginPackageRequest(
                    packageId = packageId,
                    runBuild = runBuild,
                ),
            )
            jobs.prepend(job)
            selectedJobId = job.id
            selectJob(job.id)
            refreshCurrentPackageFiles()
            updateStatus("已导出插件模块")
        } catch (error: Throwable) {
            updateStatus(error.message ?: "导出插件模块失败", true)
            throw error
        } finally {
            isBusy = false
        }
    }

    suspend fun runBuildForSelectedPackage() {
        val packageId = selectedPackageId ?: return
        isBusy = true
        try {
            val job = deploymentService.runBuild(RunPluginBuildRequest(packageId))
            jobs.prepend(job)
            selectedJobId = job.id
            selectJob(job.id)
            updateStatus("已触发验证构建")
        } catch (error: Throwable) {
            updateStatus(error.message ?: "触发验证构建失败", true)
            throw error
        } finally {
            isBusy = false
        }
    }

    suspend fun saveConfig(newConfig: PluginMarketConfigDto = config) {
        isBusy = true
        try {
            config = configService.update(
                UpdatePluginMarketConfigRequest(
                    exportRootDir = newConfig.exportRootDir,
                    gradleCommand = newConfig.gradleCommand,
                    gradleTasks = newConfig.gradleTasks,
                    javaHome = newConfig.javaHome,
                    environmentLines = newConfig.environmentLines,
                    autoBuildEnabled = newConfig.autoBuildEnabled,
                ),
            )
            updateStatus("已保存插件市场配置")
        } catch (error: Throwable) {
            updateStatus(error.message ?: "保存插件市场配置失败", true)
            throw error
        } finally {
            isBusy = false
        }
    }

    private suspend fun refreshCurrentPackageFiles() {
        val packageId = selectedPackageId ?: return
        val aggregate = packageService.aggregate(packageId)
        applyAggregate(aggregate)
    }

    private fun applyAggregate(aggregate: PluginPackageAggregateDto) {
        selectedPackageId = aggregate.pluginPackage.id
        packages.replaceOrAdd(aggregate.pluginPackage) { it.id == aggregate.pluginPackage.id }
        packageName = aggregate.pluginPackage.name
        packagePluginId = aggregate.pluginPackage.pluginId
        packageGroup = aggregate.pluginPackage.pluginGroup.orEmpty()
        packageDescription = aggregate.pluginPackage.description.orEmpty()
        packageVersion = aggregate.pluginPackage.version
        packageBasePackage = aggregate.pluginPackage.basePackage
        packageModuleDir = aggregate.pluginPackage.moduleDir
        packageEnabled = aggregate.pluginPackage.enabled
        packageComposeKoinModuleClass = aggregate.pluginPackage.composeKoinModuleClass.orEmpty()
        packageServerKoinModuleClass = aggregate.pluginPackage.serverKoinModuleClass.orEmpty()
        packageRouteRegistrarImport = aggregate.pluginPackage.routeRegistrarImport.orEmpty()
        packageRouteRegistrarCall = aggregate.pluginPackage.routeRegistrarCall.orEmpty()
        currentFiles.resetWith(aggregate.files)
        jobs.resetWith(aggregate.jobs + jobs.filter { it.packageId != aggregate.pluginPackage.id })
        if (selectedFileId !in currentFiles.map { it.id }) {
            selectedFileId = currentFiles.firstOrNull()?.id
        }
        selectFile(selectedFileId)
    }

    private fun syncSelections() {
        if (selectedPackageId !in packages.map { it.id }) {
            selectedPackageId = packages.firstOrNull()?.id
        }
        if (selectedDiscoveryId !in discoveries.map { it.discoveryId }) {
            selectedDiscoveryId = discoveries.firstOrNull()?.discoveryId
        }
        if (selectedJobId !in jobs.map { it.id }) {
            selectedJobId = jobs.firstOrNull()?.id
        }
    }

    private fun updateStatus(message: String, isError: Boolean = false) {
        statusMessage = message
        statusIsError = isError
    }
}

private fun <T> MutableList<T>.resetWith(items: List<T>) {
    clear()
    addAll(items)
}

private fun <T> MutableList<T>.prepend(item: T) {
    add(0, item)
}

private fun <T> MutableList<T>.replaceOrAdd(item: T, matches: (T) -> Boolean) {
    val index = indexOfFirst(matches)
    if (index >= 0) {
        this[index] = item
    } else {
        add(item)
    }
}
