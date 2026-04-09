package site.addzero.kcloud.plugins.codegencontext.api.context

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenBindingTargetMode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenClassKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenContextValueType
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenDefinitionSourceKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenNodeKind

@Serializable
/**
 * 表示协议模板选项数据传输对象。
 *
 * @property id 主键 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 */
data class ProtocolTemplateOptionDto(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val sortIndex: Int,
)

@Serializable
/**
 * 表示代码生成上下文摘要数据传输对象。
 *
 * @property id 主键 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property enabled 是否启用。
 * @property consumerTarget 消费目标。
 * @property protocolTemplateId 协议模板 ID。
 * @property protocolTemplateCode 协议模板编码。
 * @property protocolTemplateName 协议模板名称。
 */
data class CodegenContextSummaryDto(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val enabled: Boolean,
    val consumerTarget: CodegenConsumerTarget,
    val protocolTemplateId: Long,
    val protocolTemplateCode: String,
    val protocolTemplateName: String,
)

@Serializable
/**
 * 表示代码生成RTU生成默认数据传输对象。
 *
 * @property portPath 端口路径。
 * @property unitId 单元 ID。
 * @property baudRate 波特率。
 * @property dataBits 数据位。
 * @property stopBits 停止位。
 * @property parity 校验位。
 * @property timeoutMs 超时时间（毫秒）。
 * @property retries 重试次数。
 */
data class CodegenRtuGenerationDefaultsDto(
    val portPath: String = "/dev/ttyUSB0",
    val unitId: Int = 1,
    val baudRate: Int = 9600,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    val parity: String = "none",
    val timeoutMs: Long = 1_000,
    val retries: Int = 2,
)

@Serializable
/**
 * 表示代码生成TCP生成默认数据传输对象。
 *
 * @property host 主机地址。
 * @property port 端口。
 * @property unitId 单元 ID。
 * @property timeoutMs 超时时间（毫秒）。
 * @property retries 重试次数。
 */
data class CodegenTcpGenerationDefaultsDto(
    val host: String = "127.0.0.1",
    val port: Int = 502,
    val unitId: Int = 1,
    val timeoutMs: Long = 1_000,
    val retries: Int = 2,
)

@Serializable
/**
 * 表示代码生成MQTT生成默认数据传输对象。
 *
 * @property brokerUrl 代理地址。
 * @property clientId 客户端 ID。
 * @property requestTopic 请求主题。
 * @property responseTopic 响应主题。
 * @property qos QoS 等级。
 * @property timeoutMs 超时时间（毫秒）。
 * @property retries 重试次数。
 */
data class CodegenMqttGenerationDefaultsDto(
    val brokerUrl: String = "tcp://127.0.0.1:1883",
    val clientId: String = "modbus-mqtt-client",
    val requestTopic: String = "modbus/request",
    val responseTopic: String = "modbus/response",
    val qos: Int = 1,
    val timeoutMs: Long = 1_000,
    val retries: Int = 2,
)

@Serializable
/**
 * 表示代码生成设置数据传输对象。
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
data class CodegenGenerationSettingsDto(
    val serverOutputRoot: String? = null,
    val sharedOutputRoot: String? = null,
    val gatewayOutputRoot: String? = null,
    val apiClientOutputRoot: String? = null,
    val apiClientPackageName: String? = null,
    val springRouteOutputRoot: String? = null,
    val cOutputRoot: String? = null,
    val markdownOutputRoot: String? = null,
    val rtuDefaults: CodegenRtuGenerationDefaultsDto = CodegenRtuGenerationDefaultsDto(),
    val tcpDefaults: CodegenTcpGenerationDefaultsDto = CodegenTcpGenerationDefaultsDto(),
    val mqttDefaults: CodegenMqttGenerationDefaultsDto = CodegenMqttGenerationDefaultsDto(),
)

@Serializable
/**
 * 表示代码生成上下文参数定义数据传输对象。
 *
 * @property id 主键 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 * @property valueType 取值类型。
 * @property required 是否必填。
 * @property defaultValue 默认值。
 * @property enumOptions 枚举选项列表。
 * @property placeholder 占位提示。
 */
data class CodegenContextParamDefinitionDto(
    val id: Long? = null,
    val code: String,
    val name: String,
    val description: String? = null,
    val sortIndex: Int = 0,
    val valueType: CodegenContextValueType,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val enumOptions: List<String> = emptyList(),
    val placeholder: String? = null,
)

@Serializable
/**
 * 表示代码生成上下文定义数据传输对象。
 *
 * @property id 主键 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 * @property targetKind 目标类型。
 * @property bindingTargetMode 绑定目标模式。
 * @property sourceKind 来源类型。
 * @property params 参数定义列表。
 */
data class CodegenContextDefinitionDto(
    val id: Long? = null,
    val code: String,
    val name: String,
    val description: String? = null,
    val sortIndex: Int = 0,
    val targetKind: CodegenNodeKind,
    val bindingTargetMode: CodegenBindingTargetMode = CodegenBindingTargetMode.SINGLE,
    val sourceKind: CodegenDefinitionSourceKind = CodegenDefinitionSourceKind.BUILTIN,
    val params: List<CodegenContextParamDefinitionDto> = emptyList(),
)

@Serializable
/**
 * 表示代码生成上下文绑定值数据传输对象。
 *
 * @property id 主键 ID。
 * @property paramDefinitionId 参数定义 ID。
 * @property paramCode 参数编码。
 * @property value 值。
 */
data class CodegenContextBindingValueDto(
    val id: Long? = null,
    val paramDefinitionId: Long? = null,
    val paramCode: String,
    val value: String? = null,
)

@Serializable
/**
 * 表示代码生成上下文绑定数据传输对象。
 *
 * @property id 主键 ID。
 * @property definitionId 定义 ID。
 * @property definitionCode 定义编码。
 * @property sortIndex 排序序号。
 * @property values 绑定值列表。
 */
data class CodegenContextBindingDto(
    val id: Long? = null,
    val definitionId: Long? = null,
    val definitionCode: String,
    val sortIndex: Int = 0,
    val values: List<CodegenContextBindingValueDto> = emptyList(),
)

@Serializable
/**
 * 表示代码生成属性数据传输对象。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 * @property propertyName 属性名。
 * @property typeName 类型名。
 * @property nullable 是否可空。
 * @property defaultLiteral 默认字面量。
 * @property bindings 绑定列表。
 */
data class CodegenPropertyDto(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val sortIndex: Int = 0,
    val propertyName: String,
    val typeName: String,
    val nullable: Boolean = false,
    val defaultLiteral: String? = null,
    val bindings: List<CodegenContextBindingDto> = emptyList(),
)

@Serializable
/**
 * 表示代码生成方法数据传输对象。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 * @property methodName 方法名。
 * @property requestClassName 请求类名。
 * @property responseClassName 响应类名。
 * @property bindings 绑定列表。
 */
data class CodegenMethodDto(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val sortIndex: Int = 0,
    val methodName: String,
    val requestClassName: String? = null,
    val responseClassName: String? = null,
    val bindings: List<CodegenContextBindingDto> = emptyList(),
)

@Serializable
/**
 * 表示代码生成类数据传输对象。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 * @property classKind 类类型。
 * @property className 类名。
 * @property packageName 包名。
 * @property bindings 绑定列表。
 * @property methods 方法。
 * @property properties 属性。
 */
data class CodegenClassDto(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val sortIndex: Int = 0,
    val classKind: CodegenClassKind,
    val className: String,
    val packageName: String? = null,
    val bindings: List<CodegenContextBindingDto> = emptyList(),
    val methods: List<CodegenMethodDto> = emptyList(),
    val properties: List<CodegenPropertyDto> = emptyList(),
)

@Serializable
/**
 * 表示代码生成上下文详情数据传输对象。
 *
 * @property id 主键 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property enabled 是否启用。
 * @property consumerTarget 消费目标。
 * @property protocolTemplateId 协议模板 ID。
 * @property protocolTemplateCode 协议模板编码。
 * @property protocolTemplateName 协议模板名称。
 * @property externalCOutputRoot 外部 C 输出根目录。
 * @property generationSettings 生成设置。
 * @property availableContextDefinitions 可用上下文定义列表。
 * @property classes 类定义列表。
 */
data class CodegenContextDetailDto(
    val id: Long? = null,
    val code: String,
    val name: String,
    val description: String? = null,
    val enabled: Boolean = true,
    val consumerTarget: CodegenConsumerTarget = CodegenConsumerTarget.MCU_CONSOLE,
    val protocolTemplateId: Long,
    val protocolTemplateCode: String? = null,
    val protocolTemplateName: String? = null,
    val externalCOutputRoot: String? = null,
    val generationSettings: CodegenGenerationSettingsDto = CodegenGenerationSettingsDto(),
    val availableContextDefinitions: List<CodegenContextDefinitionDto> = emptyList(),
    val classes: List<CodegenClassDto> = emptyList(),
)

@Serializable
/**
 * 表示generatecontracts响应数据传输对象。
 *
 * @property contextId 上下文 ID。
 * @property generatedFiles 生成文件列表。
 * @property message 提示消息。
 */
data class GenerateContractsResponseDto(
    val contextId: Long,
    val generatedFiles: List<String>,
    val message: String,
)
