package site.addzero.kcloud.plugins.mcuconsole.client

import site.addzero.kcloud.plugins.mcuconsole.McuBluetoothMode
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso
import site.addzero.kcloud.plugins.mcuconsole.McuEventEnvelope
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfileSummary
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuModbusAtomicAction
import site.addzero.kcloud.plugins.mcuconsole.McuModbusFrameFormat
import site.addzero.kcloud.plugins.mcuconsole.McuModbusGpioMode
import site.addzero.kcloud.plugins.mcuconsole.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeBundleSummary
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuSerialLineEnding
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProbeResponse
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProfileIso

data class McuConsoleUiState(
    val devices: List<McuPortSummary> = emptyList(),
    val deviceDraft: McuDeviceProfileIso? = null,
    val transportProfiles: List<McuTransportProfileIso> = emptyList(),
    val transportDraft: McuTransportProfileIso = defaultTransportDraft(),
    val flashProfiles: List<McuFlashProfileSummary> = emptyList(),
    val runtimeBundles: List<McuRuntimeBundleSummary> = emptyList(),
    val transportProbes: Map<McuTransportKind, McuTransportProbeResponse> = emptyMap(),
    val events: List<McuEventEnvelope> = emptyList(),
    val widgetInstances: List<McuWidgetInstanceState> = emptyList(),
    val session: McuSessionSnapshot = McuSessionSnapshot(),
    val scriptStatus: McuScriptStatusResponse = McuScriptStatusResponse(),
    val flashStatus: McuFlashStatusResponse = McuFlashStatusResponse(),
    val runtimeStatus: McuRuntimeStatusResponse = McuRuntimeStatusResponse(),
    val modbusLastExecution: McuModbusExecutionResult = McuModbusExecutionResult(),
    val selection: McuConsoleSelectionState = McuConsoleSelectionState(),
    val modbus: McuConsoleModbusState = McuConsoleModbusState(),
    val bluetooth: McuConsoleBluetoothState = McuConsoleBluetoothState(),
    val serialConsole: McuConsoleSerialConsoleState = McuConsoleSerialConsoleState(),
    val panelControl: McuConsolePanelControlState = McuConsolePanelControlState(),
    val scriptEditor: McuConsoleScriptEditorState = McuConsoleScriptEditorState(),
    val flashEditor: McuConsoleFlashEditorState = McuConsoleFlashEditorState(),
    val feedback: McuConsoleFeedbackState = McuConsoleFeedbackState(),
    val loading: McuConsoleLoadingState = McuConsoleLoadingState(),
)

data class McuConsoleSelectionState(
    val portQuery: String = "",
    val selectedPortPath: String? = null,
    val selectedPortDeviceKey: String? = null,
    val selectedFlashProfileId: String? = null,
    val selectedRuntimeBundleId: String? = null,
    val selectedScriptExampleId: String? = null,
    val selectedAtomicCommandId: String? = null,
    val selectedTransportKind: McuTransportKind = McuTransportKind.SERIAL,
    val selectedModbusAtomicAction: McuModbusAtomicAction = McuModbusAtomicAction.GPIO_WRITE,
    val activeSessionTransportKind: McuTransportKind = McuTransportKind.SERIAL,
)

data class McuConsoleModbusState(
    val connectionName: String = "RTU 主站",
    val frameFormat: McuModbusFrameFormat = McuModbusFrameFormat.RTU,
    val gpioWritePinText: String = "2",
    val gpioWriteHigh: Boolean = true,
    val gpioModePinText: String = "2",
    val gpioMode: McuModbusGpioMode = McuModbusGpioMode.OUTPUT,
    val pwmPinText: String = "4",
    val pwmDutyText: String = "32768",
    val servoPinText: String = "12",
    val servoAngleText: String = "90",
)

data class McuConsoleBluetoothState(
    val mode: McuBluetoothMode = McuBluetoothMode.BLE,
    val deviceNameText: String = "",
    val deviceAddressText: String = "",
    val serviceUuidText: String = "",
    val writeCharacteristicUuidText: String = "",
    val notifyCharacteristicUuidText: String = "",
)

data class McuConsoleSerialConsoleState(
    val commandText: String = """
        import panel_control as p
        p.s(9527)
        p.b(2)
        p.l(0, True)
        """.trimIndent(),
    val appendLineEnding: Boolean = true,
    val lineEnding: McuSerialLineEnding = McuSerialLineEnding.CRLF,
)

data class McuConsolePanelControlState(
    val moduleText: String = "panel_control",
    val displayValueText: String = "9527",
    val beepTimesText: String = "2",
    val ledIndexText: String = "0",
)

data class McuConsoleScriptEditorState(
    val language: String = "micropython",
    val scriptText: String = """
        from machine import Pin
        from time import sleep_ms
        led = Pin(2, Pin.OUT)
        led.value(1)
        sleep_ms(300)
        led.value(0)
        """.trimIndent(),
    val timeoutMsText: String = "5000",
)

data class McuConsoleFlashEditorState(
    val firmwarePathText: String = "",
    val firmwareDownloadUrlText: String = "",
    val flashCommandTemplateText: String = "",
)

data class McuConsoleFeedbackState(
    val message: String? = null,
    val isError: Boolean = false,
)

data class McuConsoleLoadingState(
    val isLoadingPorts: Boolean = false,
    val isSubmitting: Boolean = false,
)

internal fun defaultTransportDraft(
    kind: McuTransportKind = McuTransportKind.SERIAL,
): McuTransportProfileIso {
    return McuTransportProfileIso(
        name = kind.defaultDraftName(),
        transportKind = kind,
        baudRate = 115200,
        unitId = 1,
        dataBits = 8,
        stopBits = 1,
        parity = McuModbusSerialParity.NONE,
        timeoutMs = 1000,
        retries = 2,
        host = when (kind) {
            McuTransportKind.MODBUS_TCP -> "192.168.1.10"
            McuTransportKind.MQTT -> "tcp://127.0.0.1:1883"
            else -> null
        },
        port = when (kind) {
            McuTransportKind.MODBUS_TCP -> 502
            McuTransportKind.MQTT -> 1883
            else -> null
        },
        clientId = "kcloud-mcu-client",
        publishTopic = "devices/mcu/tx",
        subscribeTopic = "devices/mcu/rx",
        qos = 0,
        keepAliveSeconds = 60,
    )
}

internal fun McuTransportKind.defaultDraftName(): String {
    return when (this) {
        McuTransportKind.SERIAL -> "串口连接"
        McuTransportKind.MODBUS_RTU -> "RTU 主站"
        McuTransportKind.MODBUS_TCP -> "TCP 主站"
        McuTransportKind.BLUETOOTH -> "蓝牙连接"
        McuTransportKind.MQTT -> "MQTT 连接"
    }
}
