package site.addzero.kcloud.plugins.codegencontext.context

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenClassDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingValueDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextParamDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenGenerationSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMethodDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMqttGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenPropertyDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenRtuGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenTcpGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.external.generated.CodegenContextApi
import site.addzero.kcloud.plugins.codegencontext.api.external.generated.CodegenTemplateApi
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenClassKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenNodeKind

private const val MODBUS_OPERATION_DEFINITION_CODE = "MODBUS_OPERATION"
private const val MODBUS_FIELD_DEFINITION_CODE = "MODBUS_FIELD"
private const val METHOD_DIRECTION_PARAM = "direction"
private const val METHOD_FUNCTION_CODE_PARAM = "functionCode"
private const val METHOD_BASE_ADDRESS_PARAM = "baseAddress"
private const val FIELD_TRANSPORT_TYPE_PARAM = "transportType"
private const val FIELD_REGISTER_OFFSET_PARAM = "registerOffset"
private const val FIELD_BIT_OFFSET_PARAM = "bitOffset"
private const val FIELD_LENGTH_PARAM = "length"
private const val FIELD_TRANSLATION_HINT_PARAM = "translationHint"
private const val FIELD_DEFAULT_LITERAL_PARAM = "defaultLiteral"
private const val PROPERTY_CATALOG_CLASS_NAME = "CodegenPropertyCatalog"

@KoinViewModel
/**
 * 管理代码生成上下文界面的状态与交互逻辑。
 *
 * @property contextApi 上下文API。
 * @property templateApi 模板API。
 */
class CodegenContextViewModel(
    private val contextApi: CodegenContextApi,
    private val templateApi: CodegenTemplateApi,
) : ViewModel() {
    private var editorKeySeed = 0

    var screenState by mutableStateOf(CodegenContextScreenState())
        private set

    init {
        refresh()
    }

    /**
     * 刷新当前界面数据。
     */
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
                        toEditor(contextApi.getContext(contextId))
                    } ?: run {
                        val protocolTemplateId = templates.firstOrNull()?.id
                        val definitions =
                            if (protocolTemplateId == null) {
                                emptyList()
                            } else {
                                contextApi.listContextDefinitions(protocolTemplateId)
                            }
                        CodegenContextEditorState.empty().copy(
                            protocolTemplateId = protocolTemplateId,
                            availableContextDefinitions = definitions,
                        )
                    }
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
                    errorMessage = throwable.message ?: "加载功能定义失败。",
                )
            }
        }
    }

    /**
     * 选择上下文。
     *
     * @param contextId 上下文 ID。
     */
    fun selectContext(
        contextId: Long,
    ) {
        viewModelScope.launch {
            runCatching {
                val detail = contextApi.getContext(contextId)
                screenState = screenState.copy(
                    selectedContextId = contextId,
                    editor = toEditor(detail),
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

    /**
     * 处理new上下文。
     */
    fun newContext() {
        viewModelScope.launch {
            val protocolTemplateId = screenState.protocolTemplates.firstOrNull()?.id
            val definitions = protocolTemplateId?.let { templateId -> runCatching { contextApi.listContextDefinitions(templateId) }.getOrDefault(emptyList()) }.orEmpty()
            screenState = screenState.copy(
                selectedContextId = null,
                statusMessage = "已创建新的上下文草稿。",
                errorMessage = null,
                generatedFiles = emptyList(),
                editor = CodegenContextEditorState.empty().copy(
                    protocolTemplateId = protocolTemplateId,
                    availableContextDefinitions = definitions,
                ),
            )
        }
    }

    /**
     * 处理保存。
     */
    fun save() {
        viewModelScope.launch {
            screenState = screenState.copy(saving = true, errorMessage = null, statusMessage = null, generatedFiles = emptyList())
            runCatching {
                val saved = persistEditor()
                screenState = screenState.copy(
                    saving = false,
                    selectedContextId = saved.id,
                    editor = toEditor(saved),
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

    /**
     * 删除选中。
     */
    fun deleteSelected() {
        val selectedId = screenState.selectedContextId ?: return
        viewModelScope.launch {
            screenState = screenState.copy(deleting = true, errorMessage = null, statusMessage = null, generatedFiles = emptyList())
            runCatching {
                contextApi.deleteContext(selectedId)
                val contexts = contextApi.listContexts()
                val nextSelectedId = contexts.firstOrNull()?.id
                val nextEditor =
                    nextSelectedId?.let { contextId -> toEditor(contextApi.getContext(contextId)) }
                        ?: run {
                            val protocolTemplateId = screenState.protocolTemplates.firstOrNull()?.id
                            val definitions =
                                if (protocolTemplateId == null) {
                                    emptyList()
                                } else {
                                    contextApi.listContextDefinitions(protocolTemplateId)
                                }
                            CodegenContextEditorState.empty().copy(
                                protocolTemplateId = protocolTemplateId,
                                availableContextDefinitions = definitions,
                            )
                        }
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

    /**
     * 处理generate选中。
     */
    fun generateSelected() {
        viewModelScope.launch {
            screenState = screenState.copy(generating = true, errorMessage = null, statusMessage = null, generatedFiles = emptyList())
            runCatching {
                val saved = persistEditor()
                val response = contextApi.generateContext(requireNotNull(saved.id))
                screenState = screenState.copy(
                    generating = false,
                    selectedContextId = saved.id,
                    editor = toEditor(saved),
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

    /**
     * 更新上下文。
     *
     * @param transform 转换函数。
     */
    fun updateContext(
        transform: (CodegenContextEditorState) -> CodegenContextEditorState,
    ) {
        screenState = screenState.copy(editor = transform(screenState.editor))
    }

    /**
     * 更新生成设置。
     *
     * @param transform 转换函数。
     */
    fun updateGenerationSettings(
        transform: (CodegenGenerationSettingsEditorState) -> CodegenGenerationSettingsEditorState,
    ) {
        updateContext { editor ->
            editor.copy(generationSettings = transform(editor.generationSettings))
        }
    }

    /**
     * 选择协议模板。
     *
     * @param protocolTemplateId 协议模板 ID。
     */
    fun selectProtocolTemplate(
        protocolTemplateId: Long?,
    ) {
        viewModelScope.launch {
            if (protocolTemplateId == null) {
                updateContext { editor ->
                    editor.copy(
                        protocolTemplateId = null,
                        availableContextDefinitions = emptyList(),
                        methods = editor.methods.map { method -> method.copy(bindings = emptyList()) },
                        properties = editor.properties.map { property -> property.copy(bindings = emptyList()) },
                    )
                }
                return@launch
            }
            runCatching {
                val definitions = contextApi.listContextDefinitions(protocolTemplateId)
                updateContext { editor ->
                    editor.copy(
                        protocolTemplateId = protocolTemplateId,
                        availableContextDefinitions = definitions,
                        methods =
                            editor.methods.map { method ->
                                method.copy(
                                    bindings = alignBindings(method.bindings, definitions.forTarget(CodegenNodeKind.METHOD)),
                                )
                            },
                        properties =
                            editor.properties.map { property ->
                                property.copy(
                                    bindings = alignBindings(property.bindings, definitions.forTarget(CodegenNodeKind.FIELD)),
                                )
                            },
                    )
                }
            }.onFailure { throwable ->
                screenState = screenState.copy(errorMessage = throwable.message ?: "加载协议上下文定义失败。")
            }
        }
    }

    /**
     * 处理add方法。
     */
    fun addMethod() {
        updateContext { editor ->
            editor.copy(
                methods =
                    editor.methods + createMethodEditorState(
                        definitions = editor.methodDefinitions(),
                        name = "",
                        description = "",
                        sortIndex = editor.methods.size,
                    ),
            )
        }
    }

    /**
     * 处理import方法from剪贴板。
     */
    fun importMethodsFromClipboard() {
        val clipboardText = CodegenClipboardBridge.readText().orEmpty()
        val importedLines = clipboardText.toImportLines()
        if (importedLines.isEmpty()) {
            screenState = screenState.copy(statusMessage = "剪贴板里没有可导入的方法描述。")
            return
        }
        updateContext { editor ->
            val appended =
                importedLines.mapIndexed { offset, line ->
                    createMethodEditorState(
                        definitions = editor.methodDefinitions(),
                        name = line,
                        description = line,
                        sortIndex = editor.methods.size + offset,
                        preferWrite = line.looksLikeWriteMethod(),
                    )
                }
            editor.copy(methods = editor.methods + appended)
        }
        screenState = screenState.copy(statusMessage = "已从剪贴板导入 ${importedLines.size} 个方法。")
    }

    /**
     * 处理remove方法。
     *
     * @param methodIndex 方法序号。
     */
    fun removeMethod(
        methodIndex: Int,
    ) {
        updateContext { editor ->
            editor.copy(methods = editor.methods.filterIndexed { index, _ -> index != methodIndex })
        }
    }

    /**
     * 更新方法。
     *
     * @param methodIndex 方法序号。
     * @param transform 转换函数。
     */
    fun updateMethod(
        methodIndex: Int,
        transform: (CodegenMethodEditorState) -> CodegenMethodEditorState,
    ) {
        updateContext { editor ->
            editor.copy(
                methods =
                    editor.methods.mapIndexed { index, method ->
                        if (index == methodIndex) {
                            transform(method)
                        } else {
                            method
                        }
                    },
            )
        }
    }

    /**
     * 更新方法名称。
     *
     * @param methodIndex 方法序号。
     * @param value 待解析的值。
     */
    fun updateMethodName(
        methodIndex: Int,
        value: String,
    ) {
        updateMethod(methodIndex) { current ->
            current.copy(
                name = value,
                description = current.description.ifBlank { value },
            )
        }
    }

    /**
     * 处理toggle方法属性selection。
     *
     * @param methodIndex 方法序号。
     * @param propertyKey 属性key。
     */
    fun toggleMethodPropertySelection(
        methodIndex: Int,
        propertyKey: String,
    ) {
        updateMethod(methodIndex) { current ->
            val nextKeys =
                if (propertyKey in current.selectedPropertyKeys) {
                    current.selectedPropertyKeys.filterNot { key -> key == propertyKey }
                } else {
                    current.selectedPropertyKeys + propertyKey
                }
            current.copy(selectedPropertyKeys = nextKeys.distinct())
        }
    }

    /**
     * 更新方法绑定值。
     *
     * @param methodIndex 方法序号。
     * @param definitionCode 定义编码。
     * @param paramCode 参数编码。
     * @param value 待解析的值。
     */
    fun updateMethodBindingValue(
        methodIndex: Int,
        definitionCode: String,
        paramCode: String,
        value: String,
    ) {
        updateMethod(methodIndex) { current ->
            current.copy(
                bindings = current.bindings.updateBindingValue(definitionCode, paramCode, value),
            )
        }
    }

    /**
     * 处理add属性。
     */
    fun addProperty() {
        updateContext { editor ->
            editor.copy(
                properties =
                    editor.properties + createPropertyEditorState(
                        definitions = editor.propertyDefinitions(),
                        editorKey = nextEditorKey("property"),
                        name = "",
                        description = "",
                        sortIndex = editor.properties.size,
                    ),
            )
        }
    }

    /**
     * 处理import属性from剪贴板。
     */
    fun importPropertiesFromClipboard() {
        val clipboardText = CodegenClipboardBridge.readText().orEmpty()
        val importedLines = clipboardText.toImportLines()
        if (importedLines.isEmpty()) {
            screenState = screenState.copy(statusMessage = "剪贴板里没有可导入的字段描述。")
            return
        }
        updateContext { editor ->
            val appended =
                importedLines.mapIndexed { offset, line ->
                    createPropertyEditorState(
                        definitions = editor.propertyDefinitions(),
                        editorKey = nextEditorKey("property"),
                        name = line,
                        description = line,
                        sortIndex = editor.properties.size + offset,
                    )
                }
            editor.copy(properties = editor.properties + appended)
        }
        screenState = screenState.copy(statusMessage = "已从剪贴板导入 ${importedLines.size} 个字段。")
    }

    /**
     * 处理remove属性。
     *
     * @param propertyIndex 属性序号。
     */
    fun removeProperty(
        propertyIndex: Int,
    ) {
        updateContext { editor ->
            val removed = editor.properties.getOrNull(propertyIndex) ?: return@updateContext editor
            editor.copy(
                properties = editor.properties.filterIndexed { index, _ -> index != propertyIndex },
                methods =
                    editor.methods.map { method ->
                        method.copy(
                            selectedPropertyKeys = method.selectedPropertyKeys.filterNot { key -> key == removed.editorKey },
                        )
                    },
            )
        }
    }

    /**
     * 更新属性。
     *
     * @param propertyIndex 属性序号。
     * @param transform 转换函数。
     */
    fun updateProperty(
        propertyIndex: Int,
        transform: (CodegenPropertyEditorState) -> CodegenPropertyEditorState,
    ) {
        updateContext { editor ->
            editor.copy(
                properties =
                    editor.properties.mapIndexed { index, property ->
                        if (index == propertyIndex) {
                            transform(property)
                        } else {
                            property
                        }
                    },
            )
        }
    }

    /**
     * 更新属性名。
     *
     * @param propertyIndex 属性序号。
     * @param value 待解析的值。
     */
    fun updatePropertyName(
        propertyIndex: Int,
        value: String,
    ) {
        updateProperty(propertyIndex) { current ->
            current.copy(
                name = value,
                description = current.description.ifBlank { value },
            )
        }
    }

    /**
     * 更新属性绑定值。
     *
     * @param propertyIndex 属性序号。
     * @param definitionCode 定义编码。
     * @param paramCode 参数编码。
     * @param value 待解析的值。
     */
    fun updatePropertyBindingValue(
        propertyIndex: Int,
        definitionCode: String,
        paramCode: String,
        value: String,
    ) {
        updateProperty(propertyIndex) { current ->
            val nextBindings = current.bindings.updateBindingValue(definitionCode, paramCode, value)
            current.copy(
                bindings = nextBindings,
                defaultLiteral =
                    if (definitionCode == MODBUS_FIELD_DEFINITION_CODE && paramCode == FIELD_DEFAULT_LITERAL_PARAM) {
                        value
                    } else {
                        current.defaultLiteral
                    },
            )
        }
    }

    /**
     * 处理persisteditor。
     */
    private suspend fun persistEditor(): CodegenContextDetailDto {
        val saved = contextApi.saveContext(screenState.editor.toDto())
        val contexts = contextApi.listContexts()
        screenState = screenState.copy(
            contexts = contexts,
            selectedContextId = saved.id,
        )
        return saved
    }

    /**
     * 将当前对象转换为editor。
     *
     * @param detail 详情。
     */
    private fun toEditor(
        detail: CodegenContextDetailDto,
    ): CodegenContextEditorState {
        editorKeySeed = 0
        return detail.toGenericEditor().copy(
            id = detail.id,
            code = detail.code,
            name = detail.name,
            description = detail.description.orEmpty(),
            enabled = detail.enabled,
            consumerTarget = detail.consumerTarget,
            protocolTemplateId = detail.protocolTemplateId,
            externalCOutputRoot = detail.externalCOutputRoot.orEmpty(),
            generationSettings =
                CodegenGenerationSettingsEditorState(
                    serverOutputRoot = detail.generationSettings.serverOutputRoot.orEmpty(),
                    sharedOutputRoot = detail.generationSettings.sharedOutputRoot.orEmpty(),
                    gatewayOutputRoot = detail.generationSettings.gatewayOutputRoot.orEmpty(),
                    apiClientOutputRoot = detail.generationSettings.apiClientOutputRoot.orEmpty(),
                    apiClientPackageName = detail.generationSettings.apiClientPackageName.orEmpty(),
                    springRouteOutputRoot = detail.generationSettings.springRouteOutputRoot.orEmpty(),
                    cOutputRoot = detail.generationSettings.cOutputRoot.orEmpty(),
                    markdownOutputRoot = detail.generationSettings.markdownOutputRoot.orEmpty(),
                    rtuDefaults =
                        CodegenRtuGenerationDefaultsEditorState(
                            portPath = detail.generationSettings.rtuDefaults.portPath,
                            unitIdText = detail.generationSettings.rtuDefaults.unitId.toString(),
                            baudRateText = detail.generationSettings.rtuDefaults.baudRate.toString(),
                            dataBitsText = detail.generationSettings.rtuDefaults.dataBits.toString(),
                            stopBitsText = detail.generationSettings.rtuDefaults.stopBits.toString(),
                            parity = detail.generationSettings.rtuDefaults.parity,
                            timeoutMsText = detail.generationSettings.rtuDefaults.timeoutMs.toString(),
                            retriesText = detail.generationSettings.rtuDefaults.retries.toString(),
                        ),
                    tcpDefaults =
                        CodegenTcpGenerationDefaultsEditorState(
                            host = detail.generationSettings.tcpDefaults.host,
                            portText = detail.generationSettings.tcpDefaults.port.toString(),
                            unitIdText = detail.generationSettings.tcpDefaults.unitId.toString(),
                            timeoutMsText = detail.generationSettings.tcpDefaults.timeoutMs.toString(),
                            retriesText = detail.generationSettings.tcpDefaults.retries.toString(),
                        ),
                    mqttDefaults =
                        CodegenMqttGenerationDefaultsEditorState(
                            brokerUrl = detail.generationSettings.mqttDefaults.brokerUrl,
                            clientId = detail.generationSettings.mqttDefaults.clientId,
                            requestTopic = detail.generationSettings.mqttDefaults.requestTopic,
                            responseTopic = detail.generationSettings.mqttDefaults.responseTopic,
                            qosText = detail.generationSettings.mqttDefaults.qos.toString(),
                            timeoutMsText = detail.generationSettings.mqttDefaults.timeoutMs.toString(),
                            retriesText = detail.generationSettings.mqttDefaults.retries.toString(),
                        ),
                ),
            availableContextDefinitions = detail.availableContextDefinitions,
        )
    }

    /**
     * 处理代码生成上下文详情数据传输对象。
     */
    private fun CodegenContextDetailDto.toGenericEditor(): CodegenContextEditorState {
        val propertyDefinitions = availableContextDefinitions.forTarget(CodegenNodeKind.FIELD)
        val methodDefinitions = availableContextDefinitions.forTarget(CodegenNodeKind.METHOD)
        val modelClasses = classes.filter { codegenClass -> codegenClass.classKind == CodegenClassKind.MODEL }
        val propertyCatalog =
            modelClasses.firstOrNull { codegenClass -> codegenClass.className == PROPERTY_CATALOG_CLASS_NAME }
                ?.properties
                .orEmpty()
        val propertyPool = linkedMapOf<String, CodegenPropertyEditorState>()
        if (propertyCatalog.isNotEmpty()) {
            propertyCatalog.forEach { property ->
                val editor = property.toEditor(nextEditorKey("property"), propertyDefinitions)
                propertyPool[editor.editorKey] = editor
            }
        }
        val propertyKeyByName = mutableMapOf<String, String>()
        propertyPool.values.forEach { property ->
            propertyKeyByName[property.propertyName] = property.editorKey
        }
        /**
         * 确保属性。
         *
         * @param property 属性。
         */
        fun ensureProperty(property: CodegenPropertyDto): String {
            val existingKey = propertyKeyByName[property.propertyName]
            if (existingKey != null) {
                return existingKey
            }
            val editor = property.toEditor(nextEditorKey("property"), propertyDefinitions)
            propertyPool[editor.editorKey] = editor
            propertyKeyByName[editor.propertyName] = editor.editorKey
            return editor.editorKey
        }

        val methods =
            classes.filter { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }
                .flatMap { serviceClass ->
                    serviceClass.methods.map { method ->
                        val selectedPropertyKeys =
                            method.boundModelProperties(modelClasses)
                                .map(::ensureProperty)
                        CodegenMethodEditorState(
                            id = method.id,
                            name = method.name,
                            description = method.description.orEmpty(),
                            sortIndexText = method.sortIndex.toString(),
                            methodName = method.methodName,
                            selectedPropertyKeys = selectedPropertyKeys,
                            bindings = alignBindings(method.bindings.toEditorStates(), methodDefinitions),
                        )
                    }
                }
                .sortedBy { method -> method.sortIndexText.toIntOrNull() ?: 0 }

        return CodegenContextEditorState(
            availableContextDefinitions = availableContextDefinitions,
            methods = methods,
            properties = propertyPool.values.sortedBy { property -> property.sortIndexText.toIntOrNull() ?: 0 },
        )
    }

    /**
     * 处理代码生成上下文editor状态。
     */
    private fun CodegenContextEditorState.toDto(): CodegenContextDetailDto {
        val propertyPool = properties.associateBy(CodegenPropertyEditorState::editorKey)
        val serviceMethods =
            methods.map { method ->
                CodegenMethodDto(
                    id = method.id,
                    name = method.name,
                    description = method.description.takeIf { it.isNotBlank() },
                    sortIndex = method.sortIndexText.toIntOrNull() ?: 0,
                    methodName = method.methodName.trim(),
                    requestClassName = null,
                    responseClassName = null,
                    bindings = method.bindings.toDto(),
                )
            }

        val propertyCatalogClass =
            CodegenClassDto(
                name = "字段目录",
                description = "上位机维护的复用字段池。",
                sortIndex = 0,
                classKind = CodegenClassKind.MODEL,
                className = PROPERTY_CATALOG_CLASS_NAME,
                properties = properties.map { property -> property.toDto() },
            )

        val methodModelClasses =
            methods.flatMapIndexed { index, method ->
                val selectedProperties =
                    method.selectedPropertyKeys.mapNotNull { key -> propertyPool[key] }
                val direction = method.bindingValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_DIRECTION_PARAM).orEmpty()
                when (direction) {
                    "WRITE" ->
                        listOf(
                            CodegenClassDto(
                                name = "${method.name.ifBlank { "未命名方法" }}请求实体",
                                description = method.description.takeIf { it.isNotBlank() },
                                sortIndex = 100 + index * 10,
                                classKind = CodegenClassKind.MODEL,
                                className = "",
                                properties = selectedProperties.map(CodegenPropertyEditorState::toDto),
                            ),
                        )

                    else ->
                        listOf(
                            CodegenClassDto(
                                name = "${method.name.ifBlank { "未命名方法" }}响应实体",
                                description = method.description.takeIf { it.isNotBlank() },
                                sortIndex = 100 + index * 10,
                                classKind = CodegenClassKind.MODEL,
                                className = "",
                                properties = selectedProperties.map(CodegenPropertyEditorState::toDto),
                            ),
                        )
                }
            }

        val serviceClass =
            CodegenClassDto(
                name = "设备契约服务",
                description = "由上位机元数据编辑器维护的方法集合。",
                sortIndex = 10,
                classKind = CodegenClassKind.SERVICE,
                className = "GeneratedDeviceContractService",
                methods = serviceMethods,
            )

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
                    mqttDefaults =
                        CodegenMqttGenerationDefaultsDto(
                            brokerUrl = generationSettings.mqttDefaults.brokerUrl,
                            clientId = generationSettings.mqttDefaults.clientId,
                            requestTopic = generationSettings.mqttDefaults.requestTopic,
                            responseTopic = generationSettings.mqttDefaults.responseTopic,
                            qos = generationSettings.mqttDefaults.qosText.toIntOrNull() ?: 1,
                            timeoutMs = generationSettings.mqttDefaults.timeoutMsText.toLongOrNull() ?: 1_000,
                            retries = generationSettings.mqttDefaults.retriesText.toIntOrNull() ?: 2,
                        ),
                ),
            availableContextDefinitions = availableContextDefinitions,
            classes = listOf(propertyCatalogClass, serviceClass) + methodModelClasses,
        )
    }

    /**
     * 创建方法editor状态。
     *
     * @param definitions 定义。
     * @param name 名称。
     * @param description 描述。
     * @param sortIndex 目标排序序号。
     * @param forcedMethodName forced方法名称。
     * @param preferWrite preferwrite。
     */
    private fun createMethodEditorState(
        definitions: List<CodegenContextDefinitionDto>,
        name: String,
        description: String,
        sortIndex: Int,
        forcedMethodName: String = "",
        preferWrite: Boolean = false,
    ): CodegenMethodEditorState {
        val direction = if (preferWrite) "WRITE" else "READ"
        return CodegenMethodEditorState(
            name = name,
            description = description,
            sortIndexText = sortIndex.toString(),
            methodName = forcedMethodName,
            bindings =
                alignBindings(
                    bindings =
                        listOf(
                            CodegenContextBindingEditorState(
                                definitionCode = MODBUS_OPERATION_DEFINITION_CODE,
                                values =
                                    listOf(
                                        CodegenContextBindingValueEditorState(paramCode = METHOD_DIRECTION_PARAM, value = direction),
                                        CodegenContextBindingValueEditorState(
                                            paramCode = METHOD_FUNCTION_CODE_PARAM,
                                            value = if (preferWrite) "WRITE_MULTIPLE_REGISTERS" else "READ_HOLDING_REGISTERS",
                                        ),
                                        CodegenContextBindingValueEditorState(paramCode = METHOD_BASE_ADDRESS_PARAM, value = "0"),
                                    ),
                            ),
                        ),
                    definitions = definitions,
                ),
        )
    }

    /**
     * 创建属性editor状态。
     *
     * @param definitions 定义。
     * @param editorKey editorkey。
     * @param name 名称。
     * @param description 描述。
     * @param sortIndex 目标排序序号。
     * @param forcedPropertyName forced属性名。
     */
    private fun createPropertyEditorState(
        definitions: List<CodegenContextDefinitionDto>,
        editorKey: String,
        name: String,
        description: String,
        sortIndex: Int,
        forcedPropertyName: String = "",
    ): CodegenPropertyEditorState {
        return CodegenPropertyEditorState(
            editorKey = editorKey,
            name = name,
            description = description,
            sortIndexText = sortIndex.toString(),
            propertyName = forcedPropertyName,
            typeName = "",
            bindings =
                alignBindings(
                    bindings =
                        listOf(
                            CodegenContextBindingEditorState(
                                definitionCode = MODBUS_FIELD_DEFINITION_CODE,
                                values =
                                    listOf(
                                        CodegenContextBindingValueEditorState(paramCode = FIELD_TRANSPORT_TYPE_PARAM, value = "U16"),
                                        CodegenContextBindingValueEditorState(paramCode = FIELD_REGISTER_OFFSET_PARAM, value = sortIndex.toString()),
                                        CodegenContextBindingValueEditorState(paramCode = FIELD_BIT_OFFSET_PARAM, value = "0"),
                                        CodegenContextBindingValueEditorState(paramCode = FIELD_LENGTH_PARAM, value = "1"),
                                        CodegenContextBindingValueEditorState(paramCode = FIELD_TRANSLATION_HINT_PARAM, value = ""),
                                        CodegenContextBindingValueEditorState(paramCode = FIELD_DEFAULT_LITERAL_PARAM, value = ""),
                                    ),
                            ),
                        ),
                    definitions = definitions,
                ),
        )
    }

    /**
     * 处理nexteditorkey。
     *
     * @param prefix prefix。
     */
    private fun nextEditorKey(
        prefix: String,
    ): String {
        editorKeySeed += 1
        return "$prefix-$editorKeySeed"
    }
}

internal expect object CodegenClipboardBridge {
    /**
     * 读取text。
     */
    fun readText(): String?
}

/**
 * 处理列表。
 *
 * @param targetKind 目标类型。
 */
private fun List<CodegenContextDefinitionDto>.forTarget(
    targetKind: CodegenNodeKind,
): List<CodegenContextDefinitionDto> =
    filter { definition -> definition.targetKind == targetKind }
        .sortedBy(CodegenContextDefinitionDto::sortIndex)

/**
 * 处理align绑定。
 *
 * @param bindings 绑定。
 * @param definitions 定义。
 */
private fun alignBindings(
    bindings: List<CodegenContextBindingEditorState>,
    definitions: List<CodegenContextDefinitionDto>,
): List<CodegenContextBindingEditorState> {
    return definitions.mapIndexed { index, definition ->
        val existing =
            bindings.firstOrNull { binding ->
                binding.definitionId == definition.id || binding.definitionCode == definition.code
            }
        CodegenContextBindingEditorState(
            id = existing?.id,
            definitionId = definition.id,
            definitionCode = definition.code,
            sortIndexText = existing?.sortIndexText ?: index.toString(),
            values =
                definition.params.sortedBy(CodegenContextParamDefinitionDto::sortIndex).map { param ->
                    val existingValue =
                        existing?.values?.firstOrNull { value ->
                            value.paramDefinitionId == param.id || value.paramCode == param.code
                        }
                    CodegenContextBindingValueEditorState(
                        id = existingValue?.id,
                        paramDefinitionId = param.id,
                        paramCode = param.code,
                        value = existingValue?.value ?: param.defaultValue.orEmpty(),
                    )
                },
        )
    }
}

/**
 * 处理列表。
 *
 * @param definitionCode 定义编码。
 * @param paramCode 参数编码。
 * @param value 待解析的值。
 */
private fun List<CodegenContextBindingEditorState>.updateBindingValue(
    definitionCode: String,
    paramCode: String,
    value: String,
): List<CodegenContextBindingEditorState> =
    map { binding ->
        if (binding.definitionCode != definitionCode) {
            binding
        } else {
            binding.copy(
                values =
                    binding.values.map { item ->
                        if (item.paramCode == paramCode) {
                            item.copy(value = value)
                        } else {
                            item
                        }
                    },
            )
        }
    }

/**
 * 处理列表。
 *
 * @param definitionCode 定义编码。
 * @param paramCode 参数编码。
 */
private fun List<CodegenContextBindingEditorState>.bindingValue(
    definitionCode: String,
    paramCode: String,
): String? =
    firstOrNull { binding -> binding.definitionCode == definitionCode }
        ?.values
        ?.firstOrNull { value -> value.paramCode == paramCode }
        ?.value
        ?.takeIf(String::isNotBlank)

/**
 * 处理列表。
 */
private fun List<CodegenContextBindingEditorState>.toDto(): List<CodegenContextBindingDto> =
    map { binding ->
        CodegenContextBindingDto(
            id = binding.id,
            definitionId = binding.definitionId,
            definitionCode = binding.definitionCode,
            sortIndex = binding.sortIndexText.toIntOrNull() ?: 0,
            values =
                binding.values.map { value ->
                    CodegenContextBindingValueDto(
                        id = value.id,
                        paramDefinitionId = value.paramDefinitionId,
                        paramCode = value.paramCode,
                        value = value.value.takeIf { text -> text.isNotBlank() },
                    )
                },
        )
    }

/**
 * 处理代码生成属性数据传输对象。
 *
 * @param editorKey editorkey。
 * @param definitions 定义。
 */
private fun CodegenPropertyDto.toEditor(
    editorKey: String,
    definitions: List<CodegenContextDefinitionDto>,
): CodegenPropertyEditorState =
    CodegenPropertyEditorState(
        editorKey = editorKey,
        id = id,
        name = name,
        description = description.orEmpty(),
        sortIndexText = sortIndex.toString(),
        propertyName = propertyName,
        typeName = typeName,
        nullable = nullable,
        defaultLiteral = defaultLiteral.orEmpty(),
        bindings = alignBindings(bindings.toEditorStates(), definitions),
    )

/**
 * 处理代码生成方法数据传输对象。
 *
 * @param modelClasses 模型类。
 */
private fun CodegenMethodDto.boundModelProperties(
    modelClasses: List<CodegenClassDto>,
): List<CodegenPropertyDto> {
    val direction = bindings.firstOrNull { binding -> binding.definitionCode == MODBUS_OPERATION_DEFINITION_CODE }
        ?.values
        ?.firstOrNull { value -> value.paramCode == METHOD_DIRECTION_PARAM }
        ?.value
    val targetClassName =
        if (direction == "WRITE") {
            requestClassName
        } else {
            responseClassName
        }
    val targetClass = modelClasses.firstOrNull { model -> model.className == targetClassName }
    return targetClass?.properties.orEmpty()
}

/**
 * 处理列表。
 */
private fun List<CodegenContextBindingDto>.toEditorStates(): List<CodegenContextBindingEditorState> =
    map { binding ->
        CodegenContextBindingEditorState(
            id = binding.id,
            definitionId = binding.definitionId,
            definitionCode = binding.definitionCode,
            sortIndexText = binding.sortIndex.toString(),
            values =
                binding.values.map { value ->
                    CodegenContextBindingValueEditorState(
                        id = value.id,
                        paramDefinitionId = value.paramDefinitionId,
                        paramCode = value.paramCode,
                        value = value.value.orEmpty(),
                    )
                },
        )
    }

/**
 * 处理代码生成属性editor状态。
 */
private fun CodegenPropertyEditorState.toDto(): CodegenPropertyDto =
    CodegenPropertyDto(
        id = id,
        name = name,
        description = description.takeIf { it.isNotBlank() },
        sortIndex = sortIndexText.toIntOrNull() ?: 0,
        propertyName = propertyName.trim(),
        typeName = typeName.trim(),
        nullable = nullable,
        defaultLiteral = defaultLiteral.takeIf { it.isNotBlank() },
        bindings = bindings.toDto(),
    )

/**
 * 处理string。
 */
private fun String.looksLikeWriteMethod(): Boolean {
    val normalized = trim()
    return listOf("写", "设", "更", "改", "下发", "保存", "同步", "控制").any { token -> token in normalized }
}

/**
 * 处理string。
 */
private fun String.toImportLines(): List<String> =
    lineSequence()
        .map { line ->
            line.trim()
                .replace(Regex("^[\\-•*\\d.、()（）\\[\\]]+\\s*"), "")
                .trim()
        }.filter(String::isNotBlank)
        .toList()
