package site.addzero.kcloud.plugins.codegencontext.context

import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextSummaryDto
import site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType

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
    val schemas: List<CodegenSchemaEditorState> = emptyList(),
) {
    companion object {
        fun empty(): CodegenContextEditorState = CodegenContextEditorState()
    }
}

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
)

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

data class CodegenTcpGenerationDefaultsEditorState(
    val host: String = "127.0.0.1",
    val portText: String = "502",
    val unitIdText: String = "1",
    val timeoutMsText: String = "1000",
    val retriesText: String = "2",
)

data class CodegenSchemaEditorState(
    val id: Long? = null,
    val name: String = "",
    val description: String = "",
    val sortIndexText: String = "0",
    val direction: CodegenSchemaDirection = CodegenSchemaDirection.READ,
    val functionCode: CodegenFunctionCode = CodegenFunctionCode.READ_COILS,
    val baseAddressText: String = "0",
    val methodName: String = "",
    val modelName: String = "",
    val fields: List<CodegenFieldEditorState> = emptyList(),
)

data class CodegenFieldEditorState(
    val id: Long? = null,
    val name: String = "",
    val description: String = "",
    val sortIndexText: String = "0",
    val propertyName: String = "",
    val transportType: CodegenTransportType = CodegenTransportType.BOOL_COIL,
    val registerOffsetText: String = "0",
    val bitOffsetText: String = "0",
    val lengthText: String = "1",
    val translationHint: String = "",
    val defaultLiteral: String = "",
)
