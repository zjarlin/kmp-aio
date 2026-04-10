package site.addzero.kcloud.plugins.codegencontext.context

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingValueDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataDeviceFunctionDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataFirmwareSyncDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataMqttDefaultsDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataRtuDefaultsDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataTcpDefaultsDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataThingPropertyDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto
import site.addzero.kcloud.plugins.codegencontext.api.external.generated.CodegenContextApi
import site.addzero.kcloud.plugins.codegencontext.api.external.generated.CodegenTemplateApi
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenNodeKind

@KoinViewModel
class CodegenContextViewModel(
    private val contextApi: CodegenContextApi,
    private val templateApi: CodegenTemplateApi,
) : ViewModel() {
    private var draftKeySeed = 0

    var screenState by mutableStateOf(CodegenContextScreenState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val currentSelection = screenState.selectedContextId
            screenState =
                screenState.copy(
                    loading = true,
                    errorMessage = null,
                    statusMessage = null,
                    preview = null,
                    exportResult = null,
                )
            runCatching {
                val templates = templateApi.listProtocolTemplates()
                val contexts = contextApi.listContexts()
                val selectedId = currentSelection ?: contexts.firstOrNull()?.id
                val draft =
                    selectedId?.let { id -> contextApi.getContext(id) } ?: createEmptyDraft(templates.firstOrNull())
                val definitions = loadDefinitions(draft.protocolTemplateId)
                screenState =
                    screenState.copy(
                        loading = false,
                        protocolTemplates = templates,
                        contexts = contexts,
                        selectedContextId = selectedId,
                        availableContextDefinitions = definitions,
                        draft = draft,
                    )
            }.onFailure { throwable ->
                screenState =
                    screenState.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "加载元数据页面失败。",
                    )
            }
        }
    }

    fun selectContext(
        contextId: Long,
    ) {
        viewModelScope.launch {
            runCatching {
                val draft = contextApi.getContext(contextId)
                val definitions = loadDefinitions(draft.protocolTemplateId)
                screenState =
                    screenState.copy(
                        selectedContextId = contextId,
                        availableContextDefinitions = definitions,
                        draft = draft,
                        preview = null,
                        exportResult = null,
                        errorMessage = null,
                        statusMessage = null,
                    )
            }.onFailure { throwable ->
                screenState = screenState.copy(errorMessage = throwable.message ?: "加载元数据草稿失败。")
            }
        }
    }

    fun newContext() {
        viewModelScope.launch {
            val template = screenState.protocolTemplates.firstOrNull()
            val definitions = loadDefinitions(template?.id ?: 0L)
            screenState =
                screenState.copy(
                    selectedContextId = null,
                    availableContextDefinitions = definitions,
                    draft = createEmptyDraft(template),
                    preview = null,
                    exportResult = null,
                    errorMessage = null,
                    statusMessage = "已创建新的元数据草稿。",
                )
        }
    }

    fun save() {
        viewModelScope.launch {
            screenState =
                screenState.copy(
                    saving = true,
                    errorMessage = null,
                    statusMessage = null,
                    exportResult = null,
                )
            runCatching {
                val saved = contextApi.saveContext(screenState.draft)
                val contexts = contextApi.listContexts()
                val definitions = loadDefinitions(saved.protocolTemplateId)
                screenState =
                    screenState.copy(
                        saving = false,
                        contexts = contexts,
                        selectedContextId = saved.id,
                        availableContextDefinitions = definitions,
                        draft = saved,
                        preview = contextApi.previewContext(saved),
                        statusMessage = "元数据草稿已保存。",
                    )
            }.onFailure { throwable ->
                screenState =
                    screenState.copy(
                        saving = false,
                        errorMessage = throwable.message ?: "保存元数据草稿失败。",
                    )
            }
        }
    }

    fun preview() {
        viewModelScope.launch {
            screenState =
                screenState.copy(
                    previewing = true,
                    errorMessage = null,
                    statusMessage = null,
                )
            runCatching {
                val preview = contextApi.previewContext(screenState.draft)
                screenState =
                    screenState.copy(
                        previewing = false,
                        preview = preview,
                        statusMessage = "预检已更新。",
                    )
            }.onFailure { throwable ->
                screenState =
                    screenState.copy(
                        previewing = false,
                        errorMessage = throwable.message ?: "预检失败。",
                    )
            }
        }
    }

    fun exportSelected() {
        viewModelScope.launch {
            screenState =
                screenState.copy(
                    exporting = true,
                    errorMessage = null,
                    statusMessage = null,
                )
            runCatching {
                val saved = contextApi.saveContext(screenState.draft)
                val exportResult = contextApi.exportContext(requireNotNull(saved.id))
                val contexts = contextApi.listContexts()
                val definitions = loadDefinitions(saved.protocolTemplateId)
                screenState =
                    screenState.copy(
                        exporting = false,
                        contexts = contexts,
                        selectedContextId = saved.id,
                        availableContextDefinitions = definitions,
                        draft = saved,
                        preview = contextApi.previewContext(saved),
                        exportResult = exportResult,
                        statusMessage = exportResult.message,
                    )
            }.onFailure { throwable ->
                screenState =
                    screenState.copy(
                        exporting = false,
                        errorMessage = throwable.message ?: "导出失败。",
                    )
            }
        }
    }

    fun deleteSelected() {
        val selectedId = screenState.selectedContextId ?: return
        viewModelScope.launch {
            screenState =
                screenState.copy(
                    deleting = true,
                    errorMessage = null,
                    statusMessage = null,
                )
            runCatching {
                contextApi.deleteContext(selectedId)
                val contexts = contextApi.listContexts()
                val nextSelectedId = contexts.firstOrNull()?.id
                val nextDraft =
                    nextSelectedId?.let { id ->
                        contextApi.getContext(id)
                    } ?: createEmptyDraft(screenState.protocolTemplates.firstOrNull())
                val definitions = loadDefinitions(nextDraft.protocolTemplateId)
                screenState =
                    screenState.copy(
                        deleting = false,
                        contexts = contexts,
                        selectedContextId = nextSelectedId,
                        availableContextDefinitions = definitions,
                        draft = nextDraft,
                        preview = null,
                        exportResult = null,
                        statusMessage = "元数据草稿已删除。",
                    )
            }.onFailure { throwable ->
                screenState =
                    screenState.copy(
                        deleting = false,
                        errorMessage = throwable.message ?: "删除失败。",
                    )
            }
        }
    }

    fun updateDraft(
        transform: (CodegenMetadataDraftDto) -> CodegenMetadataDraftDto,
    ) {
        screenState =
            screenState.copy(
                draft = transform(screenState.draft),
                preview = null,
                exportResult = null,
                errorMessage = null,
            )
    }

    fun selectProtocolTemplate(
        protocolTemplateId: Long?,
    ) {
        viewModelScope.launch {
            runCatching {
                val template = screenState.protocolTemplates.firstOrNull { item -> item.id == protocolTemplateId }
                val definitions = loadDefinitions(protocolTemplateId ?: 0L)
                screenState =
                    screenState.copy(
                        availableContextDefinitions = definitions,
                        draft =
                            screenState.draft.copy(
                                protocolTemplateId = protocolTemplateId ?: 0L,
                                protocolTemplateCode = template?.code,
                                protocolTemplateName = template?.name,
                                exportSettings =
                                    screenState.draft.exportSettings.let { settings ->
                                        if (settings.kotlinClientTransports.isNotEmpty() || template == null) {
                                            settings
                                        } else {
                                            settings.copy(
                                                kotlinClientTransports =
                                                    setOf(template.code.toDefaultKotlinTransport()),
                                            )
                                        }
                                    },
                            ),
                        preview = null,
                        exportResult = null,
                        errorMessage = null,
                    )
            }.onFailure { throwable ->
                screenState = screenState.copy(errorMessage = throwable.message ?: "切换协议模板失败。")
            }
        }
    }

    fun addThingProperty() {
        updateDraft { draft ->
            draft.copy(
                thingProperties =
                    draft.thingProperties +
                            CodegenMetadataThingPropertyDraftDto(
                                key = nextDraftKey("property"),
                                sortIndex = draft.thingProperties.size,
                            ),
            )
        }
    }

    fun updateThingProperty(
        index: Int,
        transform: (CodegenMetadataThingPropertyDraftDto) -> CodegenMetadataThingPropertyDraftDto,
    ) {
        updateDraft { draft ->
            draft.copy(
                thingProperties =
                    draft.thingProperties.mapIndexed { currentIndex, property ->
                        if (currentIndex == index) {
                            transform(property)
                        } else {
                            property
                        }
                    },
            )
        }
    }

    fun removeThingProperty(
        index: Int,
    ) {
        updateDraft { draft ->
            val removed = draft.thingProperties.getOrNull(index)?.key
            draft.copy(
                thingProperties = draft.thingProperties.filterIndexed { currentIndex, _ -> currentIndex != index },
                deviceFunctions =
                    draft.deviceFunctions.map { function ->
                        function.copy(
                            thingPropertyKeys = function.thingPropertyKeys.filterNot { key -> key == removed },
                        )
                    },
            )
        }
    }

    fun addDeviceFunction() {
        updateDraft { draft ->
            draft.copy(
                deviceFunctions =
                    draft.deviceFunctions +
                            CodegenMetadataDeviceFunctionDraftDto(
                                key = nextDraftKey("function"),
                                sortIndex = draft.deviceFunctions.size,
                            ),
            )
        }
    }

    fun updateDeviceFunction(
        index: Int,
        transform: (CodegenMetadataDeviceFunctionDraftDto) -> CodegenMetadataDeviceFunctionDraftDto,
    ) {
        updateDraft { draft ->
            draft.copy(
                deviceFunctions =
                    draft.deviceFunctions.mapIndexed { currentIndex, function ->
                        if (currentIndex == index) {
                            transform(function)
                        } else {
                            function
                        }
                    },
            )
        }
    }

    fun removeDeviceFunction(
        index: Int,
    ) {
        updateDraft { draft ->
            draft.copy(
                deviceFunctions = draft.deviceFunctions.filterIndexed { currentIndex, _ -> currentIndex != index },
            )
        }
    }

    fun toggleFunctionPropertySelection(
        functionIndex: Int,
        thingPropertyKey: String,
    ) {
        updateDeviceFunction(functionIndex) { function ->
            val selected = function.thingPropertyKeys.toMutableSet()
            if (!selected.add(thingPropertyKey)) {
                selected.remove(thingPropertyKey)
            }
            function.copy(thingPropertyKeys = selected.toList())
        }
    }

    fun updateThingPropertyBindingValue(
        propertyIndex: Int,
        definitionCode: String,
        paramCode: String,
        value: String,
    ) {
        val definitions = propertyDefinitions()
        updateThingProperty(propertyIndex) { property ->
            property.copy(
                bindings = updateBindingValue(property.bindings, definitions, definitionCode, paramCode, value),
            )
        }
    }

    fun updateDeviceFunctionBindingValue(
        functionIndex: Int,
        definitionCode: String,
        paramCode: String,
        value: String,
    ) {
        val definitions = methodDefinitions()
        updateDeviceFunction(functionIndex) { function ->
            function.copy(
                bindings = updateBindingValue(function.bindings, definitions, definitionCode, paramCode, value),
            )
        }
    }

    fun toggleKotlinClientTransport(
        transport: CodegenMetadataTransportKind,
    ) {
        updateExportSettings { settings ->
            settings.copy(
                kotlinClientTransports = settings.kotlinClientTransports.toggle(transport),
            )
        }
    }

    fun toggleCExposeTransport(
        transport: CodegenMetadataTransportKind,
    ) {
        updateExportSettings { settings ->
            settings.copy(
                cExposeTransports = settings.cExposeTransports.toggle(transport),
            )
        }
    }

    fun toggleArtifactKind(
        artifactKind: CodegenMetadataArtifactKind,
    ) {
        updateExportSettings { settings ->
            settings.copy(
                artifactKinds = settings.artifactKinds.toggle(artifactKind),
            )
        }
    }

    fun updateFirmwareSync(
        transform: (CodegenMetadataFirmwareSyncDto) -> CodegenMetadataFirmwareSyncDto,
    ) {
        updateExportSettings { settings ->
            settings.copy(firmwareSync = transform(settings.firmwareSync))
        }
    }

    fun updateRtuDefaults(
        transform: (CodegenMetadataRtuDefaultsDraftDto) -> CodegenMetadataRtuDefaultsDraftDto,
    ) {
        updateExportSettings { settings ->
            settings.copy(rtuDefaults = transform(settings.rtuDefaults))
        }
    }

    fun updateTcpDefaults(
        transform: (CodegenMetadataTcpDefaultsDraftDto) -> CodegenMetadataTcpDefaultsDraftDto,
    ) {
        updateExportSettings { settings ->
            settings.copy(tcpDefaults = transform(settings.tcpDefaults))
        }
    }

    fun updateMqttDefaults(
        transform: (CodegenMetadataMqttDefaultsDraftDto) -> CodegenMetadataMqttDefaultsDraftDto,
    ) {
        updateExportSettings { settings ->
            settings.copy(mqttDefaults = transform(settings.mqttDefaults))
        }
    }

    private fun updateExportSettings(
        transform: (CodegenMetadataExportSettingsDto) -> CodegenMetadataExportSettingsDto,
    ) {
        updateDraft { draft ->
            draft.copy(exportSettings = transform(draft.exportSettings))
        }
    }

    private suspend fun loadDefinitions(
        protocolTemplateId: Long,
    ): List<CodegenContextDefinitionDto> =
        if (protocolTemplateId > 0L) {
            contextApi.listContextDefinitions(protocolTemplateId)
        } else {
            emptyList()
        }

    private fun createEmptyDraft(
        template: ProtocolTemplateOptionDto?,
    ): CodegenMetadataDraftDto =
        CodegenMetadataDraftDto(
            protocolTemplateId = template?.id ?: 0L,
            protocolTemplateCode = template?.code,
            protocolTemplateName = template?.name,
            exportSettings =
                CodegenMetadataExportSettingsDto(
                    kotlinClientTransports =
                        template
                            ?.code
                            ?.toDefaultKotlinTransport()
                            ?.let { transport -> setOf(transport) }
                            .orEmpty(),
                ),
        )

    private fun methodDefinitions(): List<CodegenContextDefinitionDto> =
        screenState.availableContextDefinitions.filter { definition -> definition.targetKind == CodegenNodeKind.METHOD }

    private fun propertyDefinitions(): List<CodegenContextDefinitionDto> =
        screenState.availableContextDefinitions.filter { definition -> definition.targetKind == CodegenNodeKind.FIELD }

    private fun nextDraftKey(
        prefix: String,
    ): String {
        draftKeySeed += 1
        return "$prefix-$draftKeySeed"
    }
}

private fun updateBindingValue(
    bindings: List<CodegenContextBindingDto>,
    definitions: List<CodegenContextDefinitionDto>,
    definitionCode: String,
    paramCode: String,
    value: String,
): List<CodegenContextBindingDto> {
    val definition = definitions.firstOrNull { item -> item.code == definitionCode }
    val paramDefinition = definition?.params?.firstOrNull { item -> item.code == paramCode }
    val normalizedValue = value.trim().takeIf(String::isNotBlank)
    val bindingIndex =
        bindings.indexOfFirst { binding -> binding.definitionCode == definitionCode || binding.definitionId == definition?.id }
    if (bindingIndex < 0 && normalizedValue == null) {
        return bindings
    }
    val mutableBindings = bindings.toMutableList()
    val currentBinding =
        if (bindingIndex >= 0) {
            mutableBindings[bindingIndex]
        } else {
            CodegenContextBindingDto(
                definitionId = definition?.id,
                definitionCode = definitionCode,
                sortIndex = mutableBindings.size,
            )
        }
    val valueIndex =
        currentBinding.values.indexOfFirst { current ->
            current.paramCode == paramCode || current.paramDefinitionId == paramDefinition?.id
        }
    val mutableValues = currentBinding.values.toMutableList()
    if (valueIndex >= 0) {
        if (normalizedValue == null) {
            mutableValues.removeAt(valueIndex)
        } else {
            mutableValues[valueIndex] =
                mutableValues[valueIndex].copy(
                    paramDefinitionId = paramDefinition?.id ?: mutableValues[valueIndex].paramDefinitionId,
                    paramCode = paramCode,
                    value = normalizedValue,
                )
        }
    } else if (normalizedValue != null) {
        mutableValues +=
            CodegenContextBindingValueDto(
                paramDefinitionId = paramDefinition?.id,
                paramCode = paramCode,
                value = normalizedValue,
            )
    }
    val normalizedBinding =
        currentBinding.copy(
            definitionId = definition?.id ?: currentBinding.definitionId,
            values = mutableValues,
        )
    if (normalizedBinding.values.isEmpty()) {
        if (bindingIndex >= 0) {
            mutableBindings.removeAt(bindingIndex)
        }
    } else if (bindingIndex >= 0) {
        mutableBindings[bindingIndex] = normalizedBinding
    } else {
        mutableBindings += normalizedBinding
    }
    return mutableBindings.mapIndexed { index, binding ->
        binding.copy(sortIndex = index)
    }
}

private fun <T> Set<T>.toggle(
    value: T,
): Set<T> =
    if (value in this) {
        this - value
    } else {
        this + value
    }

private fun String.toDefaultKotlinTransport(): CodegenMetadataTransportKind =
    when (this) {
        "MODBUS_TCP_CLIENT" -> CodegenMetadataTransportKind.TCP
        else -> CodegenMetadataTransportKind.RTU
    }
