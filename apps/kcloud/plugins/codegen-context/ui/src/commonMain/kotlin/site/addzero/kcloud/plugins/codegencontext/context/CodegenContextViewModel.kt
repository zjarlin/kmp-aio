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
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenSchemaDto
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
                    errorMessage = throwable.message ?: "Failed to load codegen contexts.",
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
                    errorMessage = throwable.message ?: "Failed to load context detail.",
                )
            }
        }
    }

    fun newContext() {
        screenState = screenState.copy(
            selectedContextId = null,
            statusMessage = "New context draft created.",
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
                    statusMessage = "Context saved.",
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    saving = false,
                    errorMessage = throwable.message ?: "Failed to save context.",
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
                    statusMessage = "Context deleted.",
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    deleting = false,
                    errorMessage = throwable.message ?: "Failed to delete context.",
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
                    errorMessage = throwable.message ?: "Failed to generate contracts.",
                )
            }
        }
    }

    fun updateContext(
        transform: (CodegenContextEditorState) -> CodegenContextEditorState,
    ) {
        screenState = screenState.copy(editor = transform(screenState.editor))
    }

    fun addSchema() {
        updateContext { editor ->
            editor.copy(
                schemas =
                    editor.schemas + CodegenSchemaEditorState(
                        sortIndexText = editor.schemas.size.toString(),
                        direction = CodegenSchemaDirection.READ,
                        functionCode = CodegenFunctionCode.READ_COILS,
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
                    methodName = schema.methodName,
                    modelName = schema.modelName.takeIf { it.isNotBlank() },
                    fields =
                        schema.fields.map { field ->
                            CodegenFieldDto(
                                id = field.id,
                                name = field.name,
                                description = field.description.takeIf { it.isNotBlank() },
                                sortIndex = field.sortIndexText.toIntOrNull() ?: 0,
                                propertyName = field.propertyName,
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
