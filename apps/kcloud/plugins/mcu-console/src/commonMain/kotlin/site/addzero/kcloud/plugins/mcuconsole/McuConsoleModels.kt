package site.addzero.kcloud.plugins.mcuconsole

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class McuPortSummary(
    val portPath: String = "",
    val portName: String = "",
    val systemPortName: String = "",
    val descriptiveName: String = "",
    val description: String = "",
    val kind: String = "",
)

@Serializable
data class McuPortsResponse(
    val items: List<McuPortSummary> = emptyList(),
)

@Serializable
enum class McuTransportKind {
    SERIAL,
    MODBUS_RTU,
    MODBUS_TCP,
    BLUETOOTH,
    MQTT,
}

@Serializable
enum class McuBluetoothMode {
    BLE,
    CLASSIC,
}

@Serializable
data class McuSerialTransportConfig(
    val portPath: String? = null,
    val baudRate: Int = 115200,
)

@Serializable
data class McuModbusRtuTransportConfig(
    val portPath: String? = null,
    val baudRate: Int = 115200,
    val unitId: Int = 1,
    val timeoutMs: Int = 1000,
)

@Serializable
data class McuModbusTcpTransportConfig(
    val host: String = "",
    val port: Int = 502,
    val unitId: Int = 1,
    val timeoutMs: Int = 1000,
)

@Serializable
data class McuBluetoothTransportConfig(
    val mode: McuBluetoothMode = McuBluetoothMode.BLE,
    val deviceName: String = "",
    val deviceAddress: String = "",
    val serviceUuid: String = "",
    val writeCharacteristicUuid: String = "",
    val notifyCharacteristicUuid: String = "",
)

@Serializable
data class McuMqttTransportConfig(
    val brokerUrl: String = "",
    val clientId: String = "",
    val username: String = "",
    val password: String = "",
    val publishTopic: String = "",
    val subscribeTopic: String = "",
    val qos: Int = 0,
    val keepAliveSeconds: Int = 60,
)

@Serializable
data class McuSessionOpenRequest(
    val portPath: String = "",
    val baudRate: Int = 115200,
)

@Serializable
data class McuSignalRequest(
    val dtrEnabled: Boolean? = null,
    val rtsEnabled: Boolean? = null,
)

@Serializable
data class McuResetRequest(
    val pulseMs: Int = 100,
)

@Serializable
data class McuSessionLinesRequest(
    val limit: Int = 200,
)

@Serializable
data class McuSessionSnapshot(
    val portPath: String? = null,
    val portName: String? = null,
    val baudRate: Int = 115200,
    val isOpen: Boolean = false,
    val dtrEnabled: Boolean = false,
    val rtsEnabled: Boolean = false,
    val latestSeq: Long = 0,
    val lastError: String? = null,
)

@Serializable
enum class McuEventKind {
    SYSTEM,
    TX_FRAME,
    RX_FRAME,
    LOG,
    ERROR,
    FLASH,
}

@Serializable
data class McuEventEnvelope(
    val seq: Long = 0,
    val kind: McuEventKind = McuEventKind.SYSTEM,
    val title: String = "",
    val message: String = "",
    val timestamp: String = "",
    val requestId: String? = null,
    val raw: String? = null,
)

@Serializable
data class McuEventBatchResponse(
    val items: List<McuEventEnvelope> = emptyList(),
    val latestSeq: Long = 0,
)

@Serializable
enum class McuScriptRunState {
    IDLE,
    RUNNING,
    STOPPING,
    ERROR,
}

@Serializable
data class McuScriptExecuteRequest(
    val language: String = "micropython",
    val script: String = "",
    val timeoutMs: Int = 5000,
)

@Serializable
data class McuScriptStopRequest(
    val requestId: String? = null,
)

@Serializable
data class McuScriptStatusResponse(
    val state: McuScriptRunState = McuScriptRunState.IDLE,
    val activeRequestId: String? = null,
    val lastRequestId: String? = null,
    val language: String = "micropython",
    val lastMessage: String? = null,
    val lastFrameType: String? = null,
    val lastPayload: JsonElement? = null,
    val updatedAt: String? = null,
)

@Serializable
enum class McuFlashRunState {
    IDLE,
    RUNNING,
    SUCCESS,
    ERROR,
}

@Serializable
enum class McuFlashRuntimeKind {
    MICROPYTHON,
}

@Serializable
enum class McuFlashStrategyKind {
    SERIAL_ACK_STREAM,
    COMMAND_TEMPLATE,
}

@Serializable
data class McuFlashProfileSummary(
    val id: String = "",
    val title: String = "",
    val runtimeKind: McuFlashRuntimeKind = McuFlashRuntimeKind.MICROPYTHON,
    val strategyKind: McuFlashStrategyKind = McuFlashStrategyKind.COMMAND_TEMPLATE,
    val mcuFamily: String = "generic",
    val description: String = "",
    val artifactLabel: String = "固件路径",
    val artifactHint: String = "",
    val defaultBaudRate: Int = 115200,
    val commandTemplate: String? = null,
    val supportsCommandOverride: Boolean = false,
    val requiresPort: Boolean = true,
    val supportsOnlineDownload: Boolean = false,
    val defaultDownloadUrl: String? = null,
    val downloadUrlHint: String = "",
)

@Serializable
data class McuFlashProfilesResponse(
    val items: List<McuFlashProfileSummary> = emptyList(),
    val defaultProfileId: String? = null,
)

@Serializable
data class McuFlashRequest(
    val profileId: String = "",
    val firmwarePath: String = "",
    val portPath: String? = null,
    val baudRate: Int? = null,
    val commandTemplate: String? = null,
)

@Serializable
data class McuFlashDownloadRequest(
    val profileId: String = "",
    val downloadUrl: String? = null,
)

@Serializable
data class McuFlashDownloadResponse(
    val profileId: String? = null,
    val profileTitle: String? = null,
    val runtimeKind: McuFlashRuntimeKind? = null,
    val resolvedUrl: String? = null,
    val downloadPath: String? = null,
    val commandPreview: String? = null,
    val lastMessage: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class McuFlashStatusResponse(
    val state: McuFlashRunState = McuFlashRunState.IDLE,
    val profileId: String? = null,
    val profileTitle: String? = null,
    val runtimeKind: McuFlashRuntimeKind? = null,
    val strategyKind: McuFlashStrategyKind? = null,
    val portPath: String? = null,
    val baudRate: Int = 115200,
    val firmwarePath: String? = null,
    val bytesSent: Int = 0,
    val totalBytes: Int = 0,
    val commandPreview: String? = null,
    val lastMessage: String? = null,
    val updatedAt: String? = null,
)

@Serializable
enum class McuRuntimeEnsureState {
    IDLE,
    PROBING,
    INITIALIZING,
    READY,
    ERROR,
}

@Serializable
data class McuAtomicCommandDefinition(
    val id: String = "",
    val title: String = "",
    val signature: String = "",
    val description: String = "",
    val exampleScript: String = "",
)

@Serializable
data class McuScriptExample(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val language: String = "micropython",
    val script: String = "",
)

@Serializable
enum class McuWidgetFieldKind {
    TEXT,
    MULTILINE,
    BOOLEAN,
    INTEGER,
    NUMBER,
    SELECT,
}

@Serializable
data class McuWidgetBinding(
    val key: String = "",
    val label: String = "",
    val fieldKind: McuWidgetFieldKind = McuWidgetFieldKind.TEXT,
    val defaultValue: String = "",
    val required: Boolean = true,
    val placeholder: String = "",
    val options: List<String> = emptyList(),
    val min: Double? = null,
    val max: Double? = null,
    val step: Double? = null,
)

@Serializable
enum class McuWidgetTemplateKind {
    ACTION_BUTTON,
    BOOLEAN_SWITCH,
    PWM_SLIDER,
    VALUE_CARD,
    TEXT_SEND,
}

@Serializable
data class McuWidgetTemplate(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val kind: McuWidgetTemplateKind = McuWidgetTemplateKind.ACTION_BUTTON,
    val scriptTemplate: String = "",
    val bindings: List<McuWidgetBinding> = emptyList(),
)

@Serializable
data class McuRuntimeBundleSummary(
    val bundleId: String = "",
    val title: String = "",
    val runtimeKind: McuFlashRuntimeKind = McuFlashRuntimeKind.MICROPYTHON,
    val mcuFamily: String = "generic",
    val defaultFlashProfileId: String = "",
    val defaultBaudRate: Int = 115200,
    val artifactRelativePath: String = "",
    val atomicCommands: List<McuAtomicCommandDefinition> = emptyList(),
    val scriptExamples: List<McuScriptExample> = emptyList(),
    val widgetTemplates: List<McuWidgetTemplate> = emptyList(),
)

@Serializable
data class McuRuntimeBundlesResponse(
    val items: List<McuRuntimeBundleSummary> = emptyList(),
    val defaultBundleId: String? = null,
)

@Serializable
data class McuRuntimeEnsureRequest(
    val bundleId: String = "",
    val forceReflash: Boolean = false,
)

@Serializable
data class McuRuntimeStatusResponse(
    val state: McuRuntimeEnsureState = McuRuntimeEnsureState.IDLE,
    val bundleId: String? = null,
    val bundleTitle: String? = null,
    val runtimeKind: McuFlashRuntimeKind? = null,
    val mcuFamily: String? = null,
    val defaultFlashProfileId: String? = null,
    val baudRate: Int? = null,
    val artifactPath: String? = null,
    val lastMessage: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class McuVmOutgoingFrame(
    val requestId: String = "",
    val command: String = "",
    val payload: JsonElement = JsonObject(emptyMap()),
)

@Serializable
data class McuVmIncomingFrame(
    val requestId: String? = null,
    val type: String = "",
    val success: Boolean = true,
    val message: String? = null,
    val payload: JsonElement? = null,
)

@Serializable
data class McuVmExecutePayload(
    val language: String = "micropython",
    val script: String = "",
    val timeoutMs: Int = 5000,
)

object McuVmCommands {
    const val EXECUTE = "vm.execute"
    const val STOP = "vm.stop"
    const val STATUS = "vm.status"
    const val PING = "vm.ping"
}

object McuVmFrameTypes {
    const val ACK = "ack"
    const val LOG = "log"
    const val RESULT = "result"
    const val ERROR = "error"
    const val STATUS = "status"
}
