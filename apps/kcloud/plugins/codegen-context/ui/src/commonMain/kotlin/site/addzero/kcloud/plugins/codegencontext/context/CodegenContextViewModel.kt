package site.addzero.kcloud.plugins.codegencontext.context

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenFieldDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenGenerationSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenRtuGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenSchemaDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenTcpGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.external.CodegenContextApi
import site.addzero.kcloud.plugins.codegencontext.api.external.CodegenTemplateApi
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType

@KoinViewModel
class CodegenContextViewModel(
    private val contextApi: CodegenContextApi,
    private val templateApi: CodegenTemplateApi,
) : ViewModel() {
    var screenState by mutableStateOf(CodegenContextScreenState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val currentSelection = screenState.selectedContextId
            screenState = screenState.copy(loading = true, errorMessage = null, statusMessage = null, generatedFiles = emptyList())
            runCatching {
                val templates = templateApi.listProtocolTemplates()
                val contexts = contextApi.listContexts()
                val selectedId = currentSelection ?: contexts.firstOrNull()?.id
                val editor =
                    selectedId?.let { contextId ->
                        contextApi.getContext(contextId).toEditor()
                    } ?: CodegenContextEditorState.empty().copy(
                        protocolTemplateId = templates.firstOrNull()?.id,
                    )
                screenState = screenState.copy(
                    loading = false,
                    protocolTemplates = templates,
                    contexts = contexts,
                    selectedContextId = selectedId,
                    editor = editor,
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    loading = false,
                    errorMessage = throwable.message ?: "加载代码生成上下文失败。",
                )
            }
        }
    }

    fun selectContext(
        contextId: Long,
    ) {
        viewModelScope.launch {
            runCatching {
                val detail = contextApi.getContext(contextId)
                screenState = screenState.copy(
                    selectedContextId = contextId,
                    editor = detail.toEditor(),
                    errorMessage = null,
                    statusMessage = null,
                    generatedFiles = emptyList(),
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    errorMessage = throwable.message ?: "加载上下文详情失败。",
                )
            }
        }
    }

    fun newContext() {
        screenState = screenState.copy(
            selectedContextId = null,
            statusMessage = "已创建新的上下文草稿。",
            errorMessage = null,
            generatedFiles = emptyList(),
            editor = CodegenContextEditorState.empty().copy(
                protocolTemplateId = screenState.protocolTemplates.firstOrNull()?.id,
            ),
        )
    }

    fun save() {
        viewModelScope.launch {
            screenState = screenState.copy(saving = true, errorMessage = null, statusMessage = null, generatedFiles = emptyList())
            runCatching {
                val saved = persistEditor()
                screenState = screenState.copy(
                    saving = false,
                    selectedContextId = saved.id,
                    editor = saved.toEditor(),
                    statusMessage = "上下文已保存。",
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    saving = false,
                    errorMessage = throwable.message ?: "保存上下文失败。",
                )
            }
        }
    }

    fun deleteSelected() {
        val selectedId = screenState.selectedContextId ?: return
        viewModelScope.launch {
            screenState = screenState.copy(deleting = true, errorMessage = null, statusMessage = null, generatedFiles = emptyList())
            runCatching {
                contextApi.deleteContext(selectedId)
                val contexts = contextApi.listContexts()
                val nextSelectedId = contexts.firstOrNull()?.id
                val nextEditor =
                    nextSelectedId?.let { contextId -> contextApi.getContext(contextId).toEditor() }
                        ?: CodegenContextEditorState.empty().copy(
                            protocolTemplateId = screenState.protocolTemplates.firstOrNull()?.id,
                        )
                screenState = screenState.copy(
                    deleting = false,
                    contexts = contexts,
                    selectedContextId = nextSelectedId,
                    editor = nextEditor,
                    statusMessage = "上下文已删除。",
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    deleting = false,
                    errorMessage = throwable.message ?: "删除上下文失败。",
                )
            }
        }
    }

    fun generateSelected() {
        viewModelScope.launch {
            screenState = screenState.copy(generating = true, errorMessage = null, statusMessage = null, generatedFiles = emptyList())
            runCatching {
                val saved = persistEditor()
                val response = contextApi.generateContext(requireNotNull(saved.id))
                screenState = screenState.copy(
                    generating = false,
                    selectedContextId = saved.id,
                    editor = saved.toEditor(),
                    statusMessage = response.message,
                    generatedFiles = response.generatedFiles,
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    generating = false,
                    errorMessage = throwable.message ?: "生成契约失败。",
                )
            }
        }
    }

    fun updateContext(
        transform: (CodegenContextEditorState) -> CodegenContextEditorState,
    ) {
        screenState = screenState.copy(editor = transform(screenState.editor))
    }

    fun updateGenerationSettings(
        transform: (CodegenGenerationSettingsEditorState) -> CodegenGenerationSettingsEditorState,
    ) {
        updateContext { editor ->
            editor.copy(generationSettings = transform(editor.generationSettings))
        }
    }

    fun addSchema() {
        addSchema(CodegenSchemaDirection.READ)
    }

    fun addSchema(
        direction: CodegenSchemaDirection,
    ) {
        updateContext { editor ->
            val sortIndex = editor.schemas.size.toString()
            editor.copy(
                schemas =
                    editor.schemas + CodegenSchemaEditorState(
                        sortIndexText = sortIndex,
                        direction = direction,
                        functionCode = direction.allowedFunctionCodes().first(),
                    ),
            )
        }
    }

    fun removeSchema(
        schemaIndex: Int,
    ) {
        updateContext { editor ->
            editor.copy(schemas = editor.schemas.filterIndexed { index, _ -> index != schemaIndex })
        }
    }

    fun updateSchema(
        schemaIndex: Int,
        transform: (CodegenSchemaEditorState) -> CodegenSchemaEditorState,
    ) {
        updateContext { editor ->
            editor.copy(
                schemas =
                    editor.schemas.mapIndexed { index, schema ->
                        if (index == schemaIndex) {
                            transform(schema)
                        } else {
                            schema
                        }
                    },
            )
        }
    }

    fun updateSchemaName(
        schemaIndex: Int,
        value: String,
    ) {
        updateSchema(schemaIndex) { current ->
            val currentAutoMethodName = current.name.toGeneratedMethodName()
            val nextMethodName =
                if (current.methodName.isBlank() || current.methodName == currentAutoMethodName) {
                    value.toGeneratedMethodName()
                } else {
                    current.methodName
                }
            val currentAutoModelName = current.derivedModelName()
            val nextModelName =
                if (current.direction == CodegenSchemaDirection.READ &&
                    (current.modelName.isBlank() || current.modelName == currentAutoModelName)
                ) {
                    value.toGeneratedTypeName(defaultName = "GeneratedModel")
                } else {
                    current.modelName
                }
            current.copy(
                name = value,
                methodName = nextMethodName,
                modelName = nextModelName,
            )
        }
    }

    fun updateSchemaMethodName(
        schemaIndex: Int,
        value: String,
    ) {
        updateSchema(schemaIndex) { current ->
            val currentAutoModelName = current.derivedModelName()
            val nextModelName =
                if (current.direction == CodegenSchemaDirection.READ &&
                    (current.modelName.isBlank() || current.modelName == currentAutoModelName)
                ) {
                    value.toGeneratedTypeName(defaultName = "GeneratedModel")
                } else {
                    current.modelName
                }
            current.copy(
                methodName = value,
                modelName = nextModelName,
            )
        }
    }

    fun updateSchemaDirection(
        schemaIndex: Int,
        direction: CodegenSchemaDirection,
    ) {
        updateSchema(schemaIndex) { current ->
            val nextFunctionCode =
                if (current.functionCode in direction.allowedFunctionCodes()) {
                    current.functionCode
                } else {
                    direction.allowedFunctionCodes().first()
                }
            val nextModelName =
                if (direction == CodegenSchemaDirection.READ && current.modelName.isBlank()) {
                    current.derivedModelName()
                } else {
                    current.modelName
                }
            current.copy(
                direction = direction,
                functionCode = nextFunctionCode,
                modelName = nextModelName,
            )
        }
    }

    fun addField(
        schemaIndex: Int,
    ) {
        updateSchema(schemaIndex) { schema ->
            schema.copy(
                fields =
                    schema.fields + CodegenFieldEditorState(
                        sortIndexText = schema.fields.size.toString(),
                    ),
            )
        }
    }

    fun updateFieldName(
        schemaIndex: Int,
        fieldIndex: Int,
        value: String,
    ) {
        updateField(schemaIndex, fieldIndex) { current ->
            val currentAutoPropertyName = current.name.toGeneratedPropertyName()
            val nextPropertyName =
                if (current.propertyName.isBlank() || current.propertyName == currentAutoPropertyName) {
                    value.toGeneratedPropertyName()
                } else {
                    current.propertyName
                }
            current.copy(
                name = value,
                propertyName = nextPropertyName,
            )
        }
    }

    fun removeField(
        schemaIndex: Int,
        fieldIndex: Int,
    ) {
        updateSchema(schemaIndex) { schema ->
            schema.copy(
                fields = schema.fields.filterIndexed { index, _ -> index != fieldIndex },
            )
        }
    }

    fun updateField(
        schemaIndex: Int,
        fieldIndex: Int,
        transform: (CodegenFieldEditorState) -> CodegenFieldEditorState,
    ) {
        updateSchema(schemaIndex) { schema ->
            schema.copy(
                fields =
                    schema.fields.mapIndexed { index, field ->
                        if (index == fieldIndex) {
                            transform(field)
                        } else {
                            field
                        }
                    },
            )
        }
    }

    private suspend fun persistEditor(): CodegenContextDetailDto {
        val saved = contextApi.saveContext(screenState.editor.toDto())
        val contexts = contextApi.listContexts()
        screenState = screenState.copy(
            contexts = contexts,
            selectedContextId = saved.id,
        )
        return saved
    }
}

private fun CodegenContextDetailDto.toEditor(): CodegenContextEditorState {
    return CodegenContextEditorState(
        id = id,
        code = code,
        name = name,
        description = description.orEmpty(),
        enabled = enabled,
        consumerTarget = consumerTarget,
        protocolTemplateId = protocolTemplateId,
        externalCOutputRoot = externalCOutputRoot.orEmpty(),
        generationSettings =
            CodegenGenerationSettingsEditorState(
                serverOutputRoot = generationSettings.serverOutputRoot.orEmpty(),
                sharedOutputRoot = generationSettings.sharedOutputRoot.orEmpty(),
                gatewayOutputRoot = generationSettings.gatewayOutputRoot.orEmpty(),
                apiClientOutputRoot = generationSettings.apiClientOutputRoot.orEmpty(),
                apiClientPackageName = generationSettings.apiClientPackageName.orEmpty(),
                springRouteOutputRoot = generationSettings.springRouteOutputRoot.orEmpty(),
                cOutputRoot = generationSettings.cOutputRoot.orEmpty(),
                markdownOutputRoot = generationSettings.markdownOutputRoot.orEmpty(),
                rtuDefaults =
                    CodegenRtuGenerationDefaultsEditorState(
                        portPath = generationSettings.rtuDefaults.portPath,
                        unitIdText = generationSettings.rtuDefaults.unitId.toString(),
                        baudRateText = generationSettings.rtuDefaults.baudRate.toString(),
                        dataBitsText = generationSettings.rtuDefaults.dataBits.toString(),
                        stopBitsText = generationSettings.rtuDefaults.stopBits.toString(),
                        parity = generationSettings.rtuDefaults.parity,
                        timeoutMsText = generationSettings.rtuDefaults.timeoutMs.toString(),
                        retriesText = generationSettings.rtuDefaults.retries.toString(),
                    ),
                tcpDefaults =
                    CodegenTcpGenerationDefaultsEditorState(
                        host = generationSettings.tcpDefaults.host,
                        portText = generationSettings.tcpDefaults.port.toString(),
                        unitIdText = generationSettings.tcpDefaults.unitId.toString(),
                        timeoutMsText = generationSettings.tcpDefaults.timeoutMs.toString(),
                        retriesText = generationSettings.tcpDefaults.retries.toString(),
                    ),
            ),
        schemas =
            schemas.map { schema ->
                CodegenSchemaEditorState(
                    id = schema.id,
                    name = schema.name,
                    description = schema.description.orEmpty(),
                    sortIndexText = schema.sortIndex.toString(),
                    direction = schema.direction,
                    functionCode = schema.functionCode,
                    baseAddressText = schema.baseAddress.toString(),
                    methodName = schema.methodName,
                    modelName = schema.modelName.orEmpty(),
                    fields =
                        schema.fields.map { field ->
                            CodegenFieldEditorState(
                                id = field.id,
                                name = field.name,
                                description = field.description.orEmpty(),
                                sortIndexText = field.sortIndex.toString(),
                                propertyName = field.propertyName,
                                transportType = field.transportType,
                                registerOffsetText = field.registerOffset.toString(),
                                bitOffsetText = field.bitOffset.toString(),
                                lengthText = field.length.toString(),
                                translationHint = field.translationHint.orEmpty(),
                                defaultLiteral = field.defaultLiteral.orEmpty(),
                            )
                        },
                )
            },
    )
}

private fun CodegenContextEditorState.toDto(): CodegenContextDetailDto {
    return CodegenContextDetailDto(
        id = id,
        code = code,
        name = name,
        description = description.takeIf { it.isNotBlank() },
        enabled = enabled,
        consumerTarget = consumerTarget,
        protocolTemplateId = protocolTemplateId ?: 0L,
        externalCOutputRoot = externalCOutputRoot.takeIf { it.isNotBlank() },
        generationSettings =
            CodegenGenerationSettingsDto(
                serverOutputRoot = generationSettings.serverOutputRoot.takeIf { it.isNotBlank() },
                sharedOutputRoot = generationSettings.sharedOutputRoot.takeIf { it.isNotBlank() },
                gatewayOutputRoot = generationSettings.gatewayOutputRoot.takeIf { it.isNotBlank() },
                apiClientOutputRoot = generationSettings.apiClientOutputRoot.takeIf { it.isNotBlank() },
                apiClientPackageName = generationSettings.apiClientPackageName.takeIf { it.isNotBlank() },
                springRouteOutputRoot = generationSettings.springRouteOutputRoot.takeIf { it.isNotBlank() },
                cOutputRoot = generationSettings.cOutputRoot.takeIf { it.isNotBlank() },
                markdownOutputRoot = generationSettings.markdownOutputRoot.takeIf { it.isNotBlank() },
                rtuDefaults =
                    CodegenRtuGenerationDefaultsDto(
                        portPath = generationSettings.rtuDefaults.portPath,
                        unitId = generationSettings.rtuDefaults.unitIdText.toIntOrNull() ?: 1,
                        baudRate = generationSettings.rtuDefaults.baudRateText.toIntOrNull() ?: 9600,
                        dataBits = generationSettings.rtuDefaults.dataBitsText.toIntOrNull() ?: 8,
                        stopBits = generationSettings.rtuDefaults.stopBitsText.toIntOrNull() ?: 1,
                        parity = generationSettings.rtuDefaults.parity,
                        timeoutMs = generationSettings.rtuDefaults.timeoutMsText.toLongOrNull() ?: 1_000,
                        retries = generationSettings.rtuDefaults.retriesText.toIntOrNull() ?: 2,
                    ),
                tcpDefaults =
                    CodegenTcpGenerationDefaultsDto(
                        host = generationSettings.tcpDefaults.host,
                        port = generationSettings.tcpDefaults.portText.toIntOrNull() ?: 502,
                        unitId = generationSettings.tcpDefaults.unitIdText.toIntOrNull() ?: 1,
                        timeoutMs = generationSettings.tcpDefaults.timeoutMsText.toLongOrNull() ?: 1_000,
                        retries = generationSettings.tcpDefaults.retriesText.toIntOrNull() ?: 2,
                    ),
            ),
        schemas =
            schemas.map { schema ->
                CodegenSchemaDto(
                    id = schema.id,
                    name = schema.name,
                    description = schema.description.takeIf { it.isNotBlank() },
                    sortIndex = schema.sortIndexText.toIntOrNull() ?: 0,
                    direction = schema.direction,
                    functionCode = schema.functionCode,
                    baseAddress = schema.baseAddressText.toIntOrNull() ?: 0,
                    methodName = schema.methodName.takeIf { it.isNotBlank() } ?: schema.name.toGeneratedMethodName(),
                    modelName =
                        when (schema.direction) {
                            CodegenSchemaDirection.READ ->
                                (schema.modelName.takeIf { it.isNotBlank() }
                                    ?: schema.derivedModelName())

                            CodegenSchemaDirection.WRITE -> schema.modelName.takeIf { it.isNotBlank() }
                        },
                    fields =
                        schema.fields.map { field ->
                            CodegenFieldDto(
                                id = field.id,
                                name = field.name,
                                description = field.description.takeIf { it.isNotBlank() },
                                sortIndex = field.sortIndexText.toIntOrNull() ?: 0,
                                propertyName = field.propertyName.takeIf { it.isNotBlank() } ?: field.name.toGeneratedPropertyName(),
                                transportType = field.transportType,
                                registerOffset = field.registerOffsetText.toIntOrNull() ?: 0,
                                bitOffset = field.bitOffsetText.toIntOrNull() ?: 0,
                                length = field.lengthText.toIntOrNull() ?: 1,
                                translationHint = field.translationHint.takeIf { it.isNotBlank() },
                                defaultLiteral = field.defaultLiteral.takeIf { it.isNotBlank() },
                            )
                        },
                )
            },
    )
}

private fun CodegenSchemaDirection.allowedFunctionCodes(): List<CodegenFunctionCode> {
    return when (this) {
        CodegenSchemaDirection.READ ->
            listOf(
                CodegenFunctionCode.READ_COILS,
                CodegenFunctionCode.READ_DISCRETE_INPUTS,
                CodegenFunctionCode.READ_INPUT_REGISTERS,
                CodegenFunctionCode.READ_HOLDING_REGISTERS,
            )

        CodegenSchemaDirection.WRITE ->
            listOf(
                CodegenFunctionCode.WRITE_SINGLE_COIL,
                CodegenFunctionCode.WRITE_MULTIPLE_COILS,
                CodegenFunctionCode.WRITE_SINGLE_REGISTER,
                CodegenFunctionCode.WRITE_MULTIPLE_REGISTERS,
            )
    }
}

private fun CodegenSchemaEditorState.derivedModelName(): String {
    val source = methodName.takeIf { it.isNotBlank() } ?: name
    return source.toGeneratedTypeName(defaultName = "GeneratedModel")
}
