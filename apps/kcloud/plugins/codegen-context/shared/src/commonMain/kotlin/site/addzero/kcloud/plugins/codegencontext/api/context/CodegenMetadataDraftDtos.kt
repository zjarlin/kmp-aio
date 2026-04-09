package site.addzero.kcloud.plugins.codegencontext.api.context

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataIssueSeverity
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind

@Serializable
/**
 * 表示元数据草稿 RTU 默认参数。
 */
data class CodegenMetadataRtuDefaultsDraftDto(
    val portPath: String = "/dev/ttyUSB0",
    val unitId: String = "1",
    val baudRate: String = "9600",
    val dataBits: String = "8",
    val stopBits: String = "1",
    val parity: String = "none",
    val timeoutMs: String = "1000",
    val retries: String = "2",
)

@Serializable
/**
 * 表示元数据草稿 TCP 默认参数。
 */
data class CodegenMetadataTcpDefaultsDraftDto(
    val host: String = "127.0.0.1",
    val port: String = "502",
    val unitId: String = "1",
    val timeoutMs: String = "1000",
    val retries: String = "2",
)

@Serializable
/**
 * 表示元数据草稿 MQTT 默认参数。
 */
data class CodegenMetadataMqttDefaultsDraftDto(
    val brokerUrl: String = "tcp://127.0.0.1:1883",
    val clientId: String = "modbus-mqtt-client",
    val requestTopic: String = "modbus/request",
    val responseTopic: String = "modbus/response",
    val qos: String = "1",
    val timeoutMs: String = "1000",
    val retries: String = "2",
)

@Serializable
/**
 * 表示固件工程同步配置。
 */
data class CodegenMetadataFirmwareSyncDto(
    val cOutputProjectDir: String = "",
    val bridgeImplPath: String = "Core/Src/modbus",
    val keilUvprojxPath: String = "",
    val keilTargetName: String = "",
    val keilGroupName: String = "",
    val mxprojectPath: String = "",
)

@Serializable
/**
 * 表示元数据导出设置。
 */
data class CodegenMetadataExportSettingsDto(
    val artifactKinds: Set<CodegenMetadataArtifactKind> = CodegenMetadataArtifactKind.entries.toSet(),
    val kotlinClientTransports: Set<CodegenMetadataTransportKind> = CodegenMetadataTransportKind.entries.toSet(),
    val cExposeTransports: Set<CodegenMetadataTransportKind> = CodegenMetadataTransportKind.entries.toSet(),
    val firmwareSync: CodegenMetadataFirmwareSyncDto = CodegenMetadataFirmwareSyncDto(),
    val rtuDefaults: CodegenMetadataRtuDefaultsDraftDto = CodegenMetadataRtuDefaultsDraftDto(),
    val tcpDefaults: CodegenMetadataTcpDefaultsDraftDto = CodegenMetadataTcpDefaultsDraftDto(),
    val mqttDefaults: CodegenMetadataMqttDefaultsDraftDto = CodegenMetadataMqttDefaultsDraftDto(),
)

@Serializable
/**
 * 表示物模型字段草稿。
 */
data class CodegenMetadataThingPropertyDraftDto(
    val key: String = "",
    val id: Long? = null,
    val name: String = "",
    val description: String? = null,
    val sortIndex: Int = 0,
    val nullable: Boolean = false,
    val defaultLiteral: String? = null,
    val bindings: List<CodegenContextBindingDto> = emptyList(),
)

@Serializable
/**
 * 表示设备功能草稿。
 */
data class CodegenMetadataDeviceFunctionDraftDto(
    val key: String = "",
    val id: Long? = null,
    val name: String = "",
    val description: String? = null,
    val sortIndex: Int = 0,
    val thingPropertyKeys: List<String> = emptyList(),
    val bindings: List<CodegenContextBindingDto> = emptyList(),
)

@Serializable
/**
 * 表示元数据草稿。
 */
data class CodegenMetadataDraftDto(
    val id: Long? = null,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val enabled: Boolean = true,
    val consumerTarget: CodegenConsumerTarget = CodegenConsumerTarget.MCU_CONSOLE,
    val protocolTemplateId: Long = 0,
    val protocolTemplateCode: String? = null,
    val protocolTemplateName: String? = null,
    val exportSettings: CodegenMetadataExportSettingsDto = CodegenMetadataExportSettingsDto(),
    val thingProperties: List<CodegenMetadataThingPropertyDraftDto> = emptyList(),
    val deviceFunctions: List<CodegenMetadataDeviceFunctionDraftDto> = emptyList(),
)

@Serializable
/**
 * 表示解析后的物模型字段预览。
 */
data class CodegenMetadataResolvedPropertyDto(
    val key: String,
    val id: Long? = null,
    val name: String,
    val resolvedPropertyName: String? = null,
    val resolvedTypeName: String? = null,
    val layoutSummary: String? = null,
    val usageCount: Int = 0,
)

@Serializable
/**
 * 表示解析后的设备功能预览。
 */
data class CodegenMetadataResolvedFunctionDto(
    val key: String,
    val id: Long? = null,
    val name: String,
    val resolvedMethodName: String? = null,
    val direction: String? = null,
    val requestModelName: String? = null,
    val responseModelName: String? = null,
    val layoutSummary: String? = null,
    val thingPropertyCount: Int = 0,
)

@Serializable
/**
 * 表示导出计划预览项。
 */
data class CodegenMetadataExportPlanDto(
    val artifactKind: CodegenMetadataArtifactKind,
    val transport: CodegenMetadataTransportKind? = null,
    val ready: Boolean,
    val summary: String,
)

@Serializable
/**
 * 表示元数据问题。
 */
data class CodegenMetadataIssueDto(
    val severity: CodegenMetadataIssueSeverity,
    val location: String,
    val message: String,
)

@Serializable
/**
 * 表示元数据草稿预检结果。
 */
data class CodegenMetadataPreviewDto(
    val resolvedProperties: List<CodegenMetadataResolvedPropertyDto> = emptyList(),
    val resolvedFunctions: List<CodegenMetadataResolvedFunctionDto> = emptyList(),
    val exportPlans: List<CodegenMetadataExportPlanDto> = emptyList(),
    val issues: List<CodegenMetadataIssueDto> = emptyList(),
)

@Serializable
/**
 * 表示生成出的产物。
 */
data class CodegenGeneratedArtifactDto(
    val artifactKind: CodegenMetadataArtifactKind,
    val transport: CodegenMetadataTransportKind? = null,
    val path: String,
)

@Serializable
/**
 * 表示 metadata snapshot 导出结果。
 */
data class CodegenMetadataSnapshotResultDto(
    val transport: CodegenMetadataTransportKind,
    val tableName: String,
    val contextCode: String,
    val selected: Boolean = true,
    val queryHint: String,
)

@Serializable
/**
 * 表示工程同步结果。
 */
data class CodegenProjectSyncResultDto(
    val toolId: String,
    val transport: CodegenMetadataTransportKind,
    val updated: Boolean,
    val filePath: String? = null,
    val message: String,
)

@Serializable
/**
 * 表示元数据导出结果。
 */
data class CodegenMetadataExportResultDto(
    val contextId: Long,
    val message: String,
    val metadataSnapshots: List<CodegenMetadataSnapshotResultDto> = emptyList(),
    val generatedArtifacts: List<CodegenGeneratedArtifactDto> = emptyList(),
    val projectSyncResults: List<CodegenProjectSyncResultDto> = emptyList(),
    val issues: List<CodegenMetadataIssueDto> = emptyList(),
)
