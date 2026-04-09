package site.addzero.kcloud.plugins.codegencontext.context

import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextSummaryDto
import site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenContextValueType
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenNodeKind

/**
 * 表示代码生成上下文界面状态。
 *
 * @property loading 加载状态。
 * @property saving saving。
 * @property generating generating。
 * @property deleting deleting。
 * @property errorMessage 错误消息。
 * @property statusMessage 状态消息。
 * @property generatedFiles 生成文件列表。
 * @property protocolTemplates 协议模板。
 * @property contexts 上下文。
 * @property selectedContextId 选中上下文 ID。
 * @property editor editor。
 */
data class CodegenContextScreenState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val generating: Boolean = false,
    val deleting: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val generatedFiles: List<String> = emptyList(),
    val protocolTemplates: List<ProtocolTemplateOptionDto> = emptyList(),
    val contexts: List<CodegenContextSummaryDto> = emptyList(),
    val selectedContextId: Long? = null,
    val editor: CodegenContextEditorState = CodegenContextEditorState.empty(),
) {
    val selectedContext: CodegenContextSummaryDto?
        get() = contexts.firstOrNull { item -> item.id == selectedContextId }
}

/**
 * 表示代码生成上下文editor状态。
 *
 * @property id 主键 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property enabled 是否启用。
 * @property consumerTarget 消费目标。
 * @property protocolTemplateId 协议模板 ID。
 * @property externalCOutputRoot 外部 C 输出根目录。
 * @property generationSettings 生成设置。
 * @property availableContextDefinitions 可用上下文定义列表。
 * @property methods 方法。
 * @property properties 属性。
 */
data class CodegenContextEditorState(
    val id: Long? = null,
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val enabled: Boolean = true,
    val consumerTarget: CodegenConsumerTarget = CodegenConsumerTarget.MCU_CONSOLE,
    val protocolTemplateId: Long? = null,
    val externalCOutputRoot: String = "",
    val generationSettings: CodegenGenerationSettingsEditorState = CodegenGenerationSettingsEditorState(),
    val availableContextDefinitions: List<CodegenContextDefinitionDto> = emptyList(),
    val methods: List<CodegenMethodEditorState> = emptyList(),
    val properties: List<CodegenPropertyEditorState> = emptyList(),
) {
    companion object {
        /**
         * 处理empty。
         */
        fun empty(): CodegenContextEditorState = CodegenContextEditorState()
    }

    /**
     * 处理方法定义。
     */
    fun methodDefinitions(): List<CodegenContextDefinitionDto> =
        availableContextDefinitions.filter { definition -> definition.targetKind == CodegenNodeKind.METHOD }

    /**
     * 处理属性定义。
     */
    fun propertyDefinitions(): List<CodegenContextDefinitionDto> =
        availableContextDefinitions.filter { definition -> definition.targetKind == CodegenNodeKind.FIELD }
}

/**
 * 表示代码生成设置editor状态。
 *
 * @property serverOutputRoot 服务端输出根目录。
 * @property sharedOutputRoot 共享输出根目录。
 * @property gatewayOutputRoot 网关输出根目录。
 * @property apiClientOutputRoot API 客户端输出根目录。
 * @property apiClientPackageName API 客户端包名。
 * @property springRouteOutputRoot Spring 路由输出根目录。
 * @property cOutputRoot C 输出根目录。
 * @property markdownOutputRoot Markdown 输出根目录。
 * @property rtuDefaults RTU 默认值配置。
 * @property tcpDefaults TCP 默认值配置。
 * @property mqttDefaults MQTT 默认值配置。
 */
data class CodegenGenerationSettingsEditorState(
    val serverOutputRoot: String = "",
    val sharedOutputRoot: String = "",
    val gatewayOutputRoot: String = "",
    val apiClientOutputRoot: String = "",
    val apiClientPackageName: String = "",
    val springRouteOutputRoot: String = "",
    val cOutputRoot: String = "",
    val markdownOutputRoot: String = "",
    val rtuDefaults: CodegenRtuGenerationDefaultsEditorState = CodegenRtuGenerationDefaultsEditorState(),
    val tcpDefaults: CodegenTcpGenerationDefaultsEditorState = CodegenTcpGenerationDefaultsEditorState(),
    val mqttDefaults: CodegenMqttGenerationDefaultsEditorState = CodegenMqttGenerationDefaultsEditorState(),
)

/**
 * 表示代码生成RTU生成默认editor状态。
 *
 * @property portPath 端口路径。
 * @property unitIdText 单元 ID文本。
 * @property baudRateText 波特率文本。
 * @property dataBitsText 数据位文本。
 * @property stopBitsText 停止位文本。
 * @property parity 校验位。
 * @property timeoutMsText 超时时间（毫秒）文本。
 * @property retriesText 重试次数文本。
 */
data class CodegenRtuGenerationDefaultsEditorState(
    val portPath: String = "/dev/ttyUSB0",
    val unitIdText: String = "1",
    val baudRateText: String = "9600",
    val dataBitsText: String = "8",
    val stopBitsText: String = "1",
    val parity: String = "none",
    val timeoutMsText: String = "1000",
    val retriesText: String = "2",
)

/**
 * 表示代码生成TCP生成默认editor状态。
 *
 * @property host 主机地址。
 * @property portText 端口文本。
 * @property unitIdText 单元 ID文本。
 * @property timeoutMsText 超时时间（毫秒）文本。
 * @property retriesText 重试次数文本。
 */
data class CodegenTcpGenerationDefaultsEditorState(
    val host: String = "127.0.0.1",
    val portText: String = "502",
    val unitIdText: String = "1",
    val timeoutMsText: String = "1000",
    val retriesText: String = "2",
)

/**
 * 表示代码生成MQTT生成默认editor状态。
 *
 * @property brokerUrl 代理地址。
 * @property clientId 客户端 ID。
 * @property requestTopic 请求主题。
 * @property responseTopic 响应主题。
 * @property qosText QoS文本。
 * @property timeoutMsText 超时时间（毫秒）文本。
 * @property retriesText 重试次数文本。
 */
data class CodegenMqttGenerationDefaultsEditorState(
    val brokerUrl: String = "tcp://127.0.0.1:1883",
    val clientId: String = "modbus-mqtt-client",
    val requestTopic: String = "modbus/request",
    val responseTopic: String = "modbus/response",
    val qosText: String = "1",
    val timeoutMsText: String = "1000",
    val retriesText: String = "2",
)

/**
 * 表示代码生成方法editor状态。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndexText 排序序号文本。
 * @property methodName 方法名。
 * @property selectedPropertyKeys 选中属性keys。
 * @property bindings 绑定列表。
 */
data class CodegenMethodEditorState(
    val id: Long? = null,
    val name: String = "",
    val description: String = "",
    val sortIndexText: String = "0",
    val methodName: String = "",
    val selectedPropertyKeys: List<String> = emptyList(),
    val bindings: List<CodegenContextBindingEditorState> = emptyList(),
)

/**
 * 表示代码生成属性editor状态。
 *
 * @property editorKey editorkey。
 * @property id 主键 ID。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndexText 排序序号文本。
 * @property propertyName 属性名。
 * @property typeName 类型名。
 * @property nullable 是否可空。
 * @property defaultLiteral 默认字面量。
 * @property bindings 绑定列表。
 */
data class CodegenPropertyEditorState(
    val editorKey: String,
    val id: Long? = null,
    val name: String = "",
    val description: String = "",
    val sortIndexText: String = "0",
    val propertyName: String = "",
    val typeName: String = "",
    val nullable: Boolean = false,
    val defaultLiteral: String = "",
    val bindings: List<CodegenContextBindingEditorState> = emptyList(),
)

/**
 * 表示代码生成上下文绑定editor状态。
 *
 * @property id 主键 ID。
 * @property definitionId 定义 ID。
 * @property definitionCode 定义编码。
 * @property sortIndexText 排序序号文本。
 * @property values 绑定值列表。
 */
data class CodegenContextBindingEditorState(
    val id: Long? = null,
    val definitionId: Long? = null,
    val definitionCode: String,
    val sortIndexText: String = "0",
    val values: List<CodegenContextBindingValueEditorState> = emptyList(),
)

/**
 * 表示代码生成上下文绑定值editor状态。
 *
 * @property id 主键 ID。
 * @property paramDefinitionId 参数定义 ID。
 * @property paramCode 参数编码。
 * @property value 值。
 */
data class CodegenContextBindingValueEditorState(
    val id: Long? = null,
    val paramDefinitionId: Long? = null,
    val paramCode: String,
    val value: String = "",
)

/**
 * 处理代码生成方法editor状态。
 *
 * @param definitionCode 定义编码。
 * @param paramCode 参数编码。
 */
fun CodegenMethodEditorState.bindingValue(
    definitionCode: String,
    paramCode: String,
): String? {
    return bindings.firstOrNull { binding -> binding.definitionCode == definitionCode }
        ?.values
        ?.firstOrNull { value -> value.paramCode == paramCode }
        ?.value
        ?.takeIf(String::isNotBlank)
}

/**
 * 处理代码生成属性editor状态。
 *
 * @param definitionCode 定义编码。
 * @param paramCode 参数编码。
 */
fun CodegenPropertyEditorState.bindingValue(
    definitionCode: String,
    paramCode: String,
): String? {
    return bindings.firstOrNull { binding -> binding.definitionCode == definitionCode }
        ?.values
        ?.firstOrNull { value -> value.paramCode == paramCode }
        ?.value
        ?.takeIf(String::isNotBlank)
}

/**
 * 处理代码生成上下文值类型。
 */
fun CodegenContextValueType.inputPlaceholder(): String =
    when (this) {
        CodegenContextValueType.STRING -> "请输入文本"
        CodegenContextValueType.TEXT -> "请输入多行说明"
        CodegenContextValueType.INT -> "请输入整数"
        CodegenContextValueType.LONG -> "请输入长整数"
        CodegenContextValueType.DECIMAL -> "请输入小数"
        CodegenContextValueType.BOOLEAN -> "true / false"
        CodegenContextValueType.ENUM -> "请选择"
        CodegenContextValueType.PATH -> "请输入绝对路径"
    }
