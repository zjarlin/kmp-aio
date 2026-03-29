package site.addzero.kcloud.plugins.system.configcenter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.configcenter.spec.ConfigDomain
import site.addzero.configcenter.spec.ConfigEntryDto
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigStorageMode
import site.addzero.configcenter.spec.ConfigTargetDto
import site.addzero.configcenter.spec.ConfigTargetKind
import site.addzero.configcenter.spec.ConfigTargetMutationRequest
import site.addzero.configcenter.spec.ConfigValueType

@Single
class ConfigCenterWorkbenchState(
    private val remoteService: ConfigCenterRemoteService,
) {
    var isBusy by mutableStateOf(false)
    var message by mutableStateOf("")
    var previewText by mutableStateOf("")

    var entryKeyword by mutableStateOf("")
    var namespaceFilter by mutableStateOf("")
    var profileFilter by mutableStateOf("default")
    var domainFilter by mutableStateOf<ConfigDomain?>(null)

    val entries = mutableStateListOf<ConfigEntryDto>()
    val targets = mutableStateListOf<ConfigTargetDto>()

    var selectedEntryId by mutableStateOf<String?>(null)
    var selectedTargetId by mutableStateOf<String?>(null)

    var entryKey by mutableStateOf("")
    var entryNamespace by mutableStateOf("kcloud")
    var entryProfile by mutableStateOf("default")
    var entryDomain by mutableStateOf(ConfigDomain.SYSTEM)
    var entryValueType by mutableStateOf(ConfigValueType.STRING)
    var entryStorageMode by mutableStateOf(ConfigStorageMode.REPO_PLAIN)
    var entryValue by mutableStateOf("")
    var entryDescription by mutableStateOf("")
    var entryEnabled by mutableStateOf(true)
    var entryTagsText by mutableStateOf("")

    var targetName by mutableStateOf("")
    var targetKind by mutableStateOf(ConfigTargetKind.KTOR_HOCON)
    var targetOutputPath by mutableStateOf("")
    var targetNamespaceFilter by mutableStateOf("kcloud")
    var targetProfile by mutableStateOf("default")
    var targetTemplateText by mutableStateOf("")
    var targetEnabled by mutableStateOf(true)
    var targetSortOrderText by mutableStateOf("10")

    private var loaded = false

    suspend fun ensureLoaded() {
        if (loaded) {
            return
        }
        refreshAll()
        loaded = true
    }

    suspend fun refreshAll() {
        refreshEntries()
        refreshTargets()
    }

    suspend fun refreshEntries() {
        runBusy("已刷新配置项") {
            val result = remoteService.listEntries(
                ConfigQuery(
                    namespace = namespaceFilter.ifBlank { null },
                    domain = domainFilter,
                    profile = profileFilter.ifBlank { "default" },
                    keyword = entryKeyword.ifBlank { null },
                    includeDisabled = true,
                ),
            )
            entries.resetWith(result)
            selectedEntryId?.let { selectedId ->
                result.firstOrNull { it.id == selectedId }?.let(::selectEntry)
            }
        }
    }

    suspend fun saveEntry() {
        require(entryKey.isNotBlank()) {
            "配置键不能为空"
        }
        require(entryNamespace.isNotBlank()) {
            "命名空间不能为空"
        }
        runBusy("已保存配置项") {
            val saved = remoteService.saveEntry(
                ConfigMutationRequest(
                    id = selectedEntryId,
                    key = entryKey.trim(),
                    namespace = entryNamespace.trim(),
                    domain = entryDomain,
                    profile = entryProfile.ifBlank { "default" },
                    valueType = entryValueType,
                    storageMode = entryStorageMode,
                    value = entryValue,
                    description = entryDescription.ifBlank { null },
                    tags = entryTagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    enabled = entryEnabled,
                ),
            )
            selectedEntryId = saved.id
            refreshEntries()
        }
    }

    suspend fun deleteSelectedEntry() {
        val id = selectedEntryId ?: return
        runBusy("已删除配置项") {
            remoteService.deleteEntry(id)
            beginCreateEntry()
            refreshEntries()
        }
    }

    fun beginCreateEntry() {
        selectedEntryId = null
        entryKey = ""
        entryNamespace = namespaceFilter.ifBlank { "kcloud" }
        entryProfile = profileFilter.ifBlank { "default" }
        entryDomain = ConfigDomain.SYSTEM
        entryValueType = ConfigValueType.STRING
        entryStorageMode = ConfigStorageMode.REPO_PLAIN
        entryValue = ""
        entryDescription = ""
        entryEnabled = true
        entryTagsText = ""
    }

    fun selectEntry(
        entry: ConfigEntryDto,
    ) {
        selectedEntryId = entry.id
        entryKey = entry.key
        entryNamespace = entry.namespace
        entryProfile = entry.profile
        entryDomain = entry.domain
        entryValueType = entry.valueType
        entryStorageMode = entry.storageMode
        entryValue = entry.value.orEmpty()
        entryDescription = entry.description.orEmpty()
        entryEnabled = entry.enabled
        entryTagsText = entry.tags.joinToString(", ")
    }

    suspend fun refreshTargets() {
        runBusy("已刷新渲染目标") {
            val result = remoteService.listTargets()
            targets.resetWith(result)
            selectedTargetId?.let { selectedId ->
                result.firstOrNull { it.id == selectedId }?.let(::selectTarget)
            }
        }
    }

    suspend fun saveTarget() {
        require(targetName.isNotBlank()) {
            "目标名称不能为空"
        }
        runBusy("已保存渲染目标") {
            val saved = remoteService.saveTarget(
                ConfigTargetMutationRequest(
                    id = selectedTargetId,
                    name = targetName.trim(),
                    targetKind = targetKind,
                    outputPath = targetOutputPath.trim(),
                    namespaceFilter = targetNamespaceFilter.ifBlank { null },
                    profile = targetProfile.ifBlank { "default" },
                    templateText = targetTemplateText.ifBlank { null },
                    enabled = targetEnabled,
                    sortOrder = targetSortOrderText.toIntOrNull() ?: 0,
                ),
            )
            selectedTargetId = saved.id
            refreshTargets()
        }
    }

    suspend fun deleteSelectedTarget() {
        val id = selectedTargetId ?: return
        runBusy("已删除渲染目标") {
            remoteService.deleteTarget(id)
            beginCreateTarget()
            refreshTargets()
        }
    }

    fun beginCreateTarget() {
        selectedTargetId = null
        targetName = ""
        targetKind = ConfigTargetKind.KTOR_HOCON
        targetOutputPath = ""
        targetNamespaceFilter = "kcloud"
        targetProfile = "default"
        targetTemplateText = ""
        targetEnabled = true
        targetSortOrderText = "10"
    }

    fun selectTarget(
        target: ConfigTargetDto,
    ) {
        selectedTargetId = target.id
        targetName = target.name
        targetKind = target.targetKind
        targetOutputPath = target.outputPath
        targetNamespaceFilter = target.namespaceFilter.orEmpty()
        targetProfile = target.profile
        targetTemplateText = target.templateText.orEmpty()
        targetEnabled = target.enabled
        targetSortOrderText = target.sortOrder.toString()
    }

    suspend fun previewSelectedTarget() {
        val id = selectedTargetId ?: return
        runBusy("已更新预览") {
            previewText = remoteService.previewTarget(id)
        }
    }

    suspend fun exportSelectedTarget() {
        val id = selectedTargetId ?: return
        runBusy("已导出渲染目标") {
            val result = remoteService.exportTarget(id)
            previewText = result.content
        }
    }

    suspend fun loadBootstrapSummary() {
        runBusy("") {
            val dbPath = remoteService.readBootstrapValue("CONFIG_CENTER_DB_PATH").value.orEmpty()
            val appId = remoteService.readBootstrapValue("CONFIG_CENTER_APP_ID").value.orEmpty()
            val profile = remoteService.readBootstrapValue("CONFIG_CENTER_PROFILE").value.orEmpty()
            message = listOf(
                "db=$dbPath",
                "appId=$appId",
                "profile=$profile",
            ).filter { it.isNotBlank() }.joinToString(" | ")
        }
    }

    private suspend fun runBusy(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        runCatching {
            block()
        }.onSuccess {
            if (successMessage.isNotBlank()) {
                message = successMessage
            }
        }.onFailure {
            message = it.message ?: "操作失败"
        }
        isBusy = false
    }
}

private fun <T> MutableList<T>.resetWith(
    data: List<T>,
) {
    clear()
    addAll(data)
}
