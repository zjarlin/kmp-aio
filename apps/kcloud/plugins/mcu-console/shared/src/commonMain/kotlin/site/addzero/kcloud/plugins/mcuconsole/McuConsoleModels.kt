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
    val portLocation: String = "",
    val serialNumber: String = "",
    val manufacturer: String = "",
    val vendorId: Int? = null,
    val productId: Int? = null,
    val deviceKey: String = "",
    val remark: String = "",
)

@Serializable
data class McuPortsResponse(
    val items: List<McuPortSummary> = emptyList(),
)

@Serializable
data class McuTransportProfilesResponse(
    val items: List<McuTransportProfileIso> = emptyList(),
)

@Serializable
enum class McuTransportKind {
    SERIAL,
}

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity",
    replaceWith = ReplaceWith("McuModbusSerialParity", "site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity"),
)
typealias McuModbusSerialParity = site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusFrameFormat",
    replaceWith = ReplaceWith("McuModbusFrameFormat", "site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusFrameFormat"),
)
typealias McuModbusFrameFormat = site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusFrameFormat

@Serializable
data class McuSerialTransportConfig(
    val portPath: String? = null,
    val baudRate: Int = 115200,
)

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusCommandConfig",
    replaceWith = ReplaceWith("McuModbusCommandConfig", "site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusCommandConfig"),
)
typealias McuModbusCommandConfig = site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusCommandConfig

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusAtomicAction",
    replaceWith = ReplaceWith("McuModbusAtomicAction", "site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusAtomicAction"),
)
typealias McuModbusAtomicAction = site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusAtomicAction

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioMode",
    replaceWith = ReplaceWith("McuModbusGpioMode", "site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioMode"),
)
typealias McuModbusGpioMode = site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioMode

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse",
    replaceWith = ReplaceWith("McuModbusCommandResponse", "site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse"),
)
typealias McuModbusCommandResponse = site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioWriteRequest",
    replaceWith = ReplaceWith("McuModbusGpioWriteRequest", "site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioWriteRequest"),
)
typealias McuModbusGpioWriteRequest = site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioWriteRequest

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioModeRequest",
    replaceWith = ReplaceWith("McuModbusGpioModeRequest", "site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioModeRequest"),
)
typealias McuModbusGpioModeRequest = site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioModeRequest

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusPwmDutyRequest",
    replaceWith = ReplaceWith("McuModbusPwmDutyRequest", "site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusPwmDutyRequest"),
)
typealias McuModbusPwmDutyRequest = site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusPwmDutyRequest

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusServoAngleRequest",
    replaceWith = ReplaceWith("McuModbusServoAngleRequest", "site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusServoAngleRequest"),
)
typealias McuModbusServoAngleRequest = site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusServoAngleRequest

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusPowerLightsResponse",
    replaceWith = ReplaceWith("McuModbusPowerLightsResponse", "site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusPowerLightsResponse"),
)
typealias McuModbusPowerLightsResponse = site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusPowerLightsResponse

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusDeviceInfoResponse",
    replaceWith = ReplaceWith("McuModbusDeviceInfoResponse", "site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusDeviceInfoResponse"),
)
typealias McuModbusDeviceInfoResponse = site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusDeviceInfoResponse

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsRequest",
    replaceWith = ReplaceWith("McuModbusIndicatorLightsRequest", "site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsRequest"),
)
typealias McuModbusIndicatorLightsRequest = site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsRequest

@Deprecated(
    message = "Use site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsResponse",
    replaceWith = ReplaceWith("McuModbusIndicatorLightsResponse", "site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsResponse"),
)
typealias McuModbusIndicatorLightsResponse = site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsResponse

data class McuSessionOpenRequest(
    val profileKey: String? = null,
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
enum class McuSerialLineEnding {
    LF,
    CRLF,
    CR,
}

@Serializable
data class McuSerialTextSendRequest(
    val text: String = "",
    val appendLineEnding: Boolean = true,
    val lineEnding: McuSerialLineEnding = McuSerialLineEnding.CRLF,
)

@Serializable
data class McuSerialTextSendResponse(
    val accepted: Boolean = false,
    val bytesSent: Int = 0,
    val preview: String = "",
    val lineEnding: McuSerialLineEnding = McuSerialLineEnding.CRLF,
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
    TX_TEXT,
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
    STM32,
}

@Serializable
enum class McuFlashStrategyKind {
    ST_LINK_SWD,
}

@Serializable
data class McuFlashProfileSummary(
    val id: String = "",
    val title: String = "",
    val runtimeKind: McuFlashRuntimeKind = McuFlashRuntimeKind.STM32,
    val strategyKind: McuFlashStrategyKind = McuFlashStrategyKind.ST_LINK_SWD,
    val mcuFamily: String = "stm32",
    val description: String = "",
    val artifactLabel: String = "固件路径",
    val artifactHint: String = "",
    val defaultStartAddress: Long = 0x08000000,
    val connectUnderReset: Boolean = true,
    val supportedChipIds: List<Int> = emptyList(),
)

@Serializable
data class McuFlashProfilesResponse(
    val items: List<McuFlashProfileSummary> = emptyList(),
    val defaultProfileId: String? = null,
)

@Serializable
data class McuFlashProbeSummary(
    val serialNumber: String? = null,
    val productName: String? = null,
    val manufacturerName: String? = null,
    val vendorId: Int = 0,
    val productId: Int = 0,
)

@Serializable
data class McuFlashProbesResponse(
    val items: List<McuFlashProbeSummary> = emptyList(),
)

@Serializable
data class McuFlashRequest(
    val profileId: String = "",
    val firmwarePath: String = "",
    val probeSerialNumber: String? = null,
    val startAddress: Long? = null,
)

@Serializable
data class McuFlashStatusResponse(
    val state: McuFlashRunState = McuFlashRunState.IDLE,
    val profileId: String? = null,
    val profileTitle: String? = null,
    val runtimeKind: McuFlashRuntimeKind? = null,
    val strategyKind: McuFlashStrategyKind? = null,
    val probeSerialNumber: String? = null,
    val probeDescription: String? = null,
    val targetChipId: Int? = null,
    val targetVoltageMillivolts: Int? = null,
    val flashStartAddress: Long? = null,
    val firmwarePath: String? = null,
    val bytesSent: Int = 0,
    val totalBytes: Int = 0,
    val progressPercent: Double = 0.0,
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

/**
 * 设备信息主动轮询请求。
 */
@Serializable
data class McuDeviceInfoPollRequest(
    val timeoutMs: Int = 1200,
)

/**
 * MCU 设备信息轮询结果。
 */
@Serializable
data class McuDeviceInfoResponse(
    val success: Boolean = false,
    val requestId: String? = null,
    val portPath: String? = null,
    val runtime: String? = null,
    val boardName: String? = null,
    val chipModel: String? = null,
    val chipRevision: String? = null,
    val cpuModel: String? = null,
    val cpuCores: Int? = null,
    val cpuFrequencyHz: Int? = null,
    val xtalFrequencyHz: Int? = null,
    val macAddress: String? = null,
    val sdkVersion: String? = null,
    val firmwareVersion: String? = null,
    val flashSizeBytes: Long? = null,
    val heapFreeBytes: Long? = null,
    val heapTotalBytes: Long? = null,
    val lastMessage: String? = null,
    val updatedAt: String? = null,
    val rawPayload: JsonElement? = null,
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
    const val DEVICE_INFO = "vm.device.info"
}

object McuVmFrameTypes {
    const val ACK = "ack"
    const val LOG = "log"
    const val RESULT = "result"
    const val ERROR = "error"
    const val STATUS = "status"
}
