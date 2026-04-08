package site.addzero.kcloud.plugins.codegencontext.api.context

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType

@Serializable
data class ProtocolTemplateOptionDto(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val sortIndex: Int,
)

@Serializable
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
data class CodegenFieldDto(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val sortIndex: Int = 0,
    val propertyName: String,
    val transportType: CodegenTransportType,
    val registerOffset: Int = 0,
    val bitOffset: Int = 0,
    val length: Int = 1,
    val translationHint: String? = null,
    val defaultLiteral: String? = null,
)

@Serializable
data class CodegenSchemaDto(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val sortIndex: Int = 0,
    val direction: CodegenSchemaDirection,
    val functionCode: CodegenFunctionCode,
    val baseAddress: Int,
    val methodName: String,
    val modelName: String? = null,
    val fields: List<CodegenFieldDto> = emptyList(),
)

@Serializable
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
    val schemas: List<CodegenSchemaDto> = emptyList(),
)

@Serializable
data class GenerateContractsResponseDto(
    val contextId: Long,
    val generatedFiles: List<String>,
    val message: String,
)
