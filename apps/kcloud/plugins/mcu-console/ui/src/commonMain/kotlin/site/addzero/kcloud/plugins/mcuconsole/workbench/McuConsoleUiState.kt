package site.addzero.kcloud.plugins.mcuconsole.workbench

import site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso
import site.addzero.kcloud.plugins.mcuconsole.McuEventEnvelope
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProbeSummary
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfileSummary
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeBundleSummary
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuSerialLineEnding
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProfileIso
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusFrameFormat
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusAtomicAction
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioMode
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusDeviceInfoResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusPowerLightsResponse

data class McuConsoleUiState(
    val devices: List<McuPortSummary> = emptyList(),
    val deviceDraft: McuDeviceProfileIso? = null,
    val transportProfiles: List<McuTransportProfileIso> = emptyList(),
    val transportDraft: McuTransportProfileIso = defaultTransportDraft(),
    val flashProfiles: List<McuFlashProfileSummary> = emptyList(),
    val flashProbes: List<McuFlashProbeSummary> = emptyList(),
    val runtimeBundles: List<McuRuntimeBundleSummary> = emptyList(),
    val events: List<McuEventEnvelope> = emptyList(),
    val widgetInstances: List<McuWidgetInstanceState> = emptyList(),
    val session: McuSessionSnapshot = McuSessionSnapshot(),
    val scriptStatus: McuScriptStatusResponse = McuScriptStatusResponse(),
    val flashStatus: McuFlashStatusResponse = McuFlashStatusResponse(),
    val runtimeStatus: McuRuntimeStatusResponse = McuRuntimeStatusResponse(),
    val devicePowerLights: McuModbusPowerLightsResponse = McuModbusPowerLightsResponse(),
    val deviceInfo: McuModbusDeviceInfoResponse = McuModbusDeviceInfoResponse(),
    val modbusLastExecution: McuModbusExecutionResult = McuModbusExecutionResult(),
    val selection: McuConsoleSelectionState = McuConsoleSelectionState(),
    val modbus: McuConsoleModbusState = McuConsoleModbusState(),
    val serialConsole: McuConsoleSerialConsoleState = McuConsoleSerialConsoleState(),
    val panelControl: McuConsolePanelControlState = McuConsolePanelControlState(),
    val probe: McuConsoleProbeState = McuConsoleProbeState(),
    val customSerialActions: List<McuCustomSerialActionState> = defaultCustomSerialActions(),
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
    val selectedFlashProbeSerialNumber: String? = null,
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

data class McuConsoleProbeState(
    val pinMapFilesText: String = "boot.py,main.py,panel_control.py",
    val gpioSnapshotPinsText: String = "12,14,15,16,17,18,25,26,27,32,33",
    val i2cSdaText: String = "21",
    val i2cSclText: String = "22",
)

data class McuCustomSerialActionState(
    val id: String,
    val labelText: String,
    val scriptText: String,
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
        transportKind = McuTransportKind.SERIAL,
        baudRate = 115200,
        unitId = 1,
        dataBits = 8,
        stopBits = 1,
        parity = McuModbusSerialParity.NONE,
        timeoutMs = 1000,
        retries = 2,
    )
}

internal fun McuTransportKind.defaultDraftName(): String {
    return "串口连接"
}

internal fun defaultCustomSerialActions(): List<McuCustomSerialActionState> {
    return listOf(
        McuCustomSerialActionState(
            id = "rgb-red",
            labelText = "RGB 红灯",
            scriptText = """
                from machine import Pin
                from neopixel import NeoPixel
                rgb = NeoPixel(Pin(25, Pin.OUT), 5)
                for i in range(5):
                    rgb[i] = (255, 0, 0)
                rgb.write()
            """.trimIndent(),
        ),
        McuCustomSerialActionState(
            id = "rgb-off",
            labelText = "RGB 熄灭",
            scriptText = """
                from machine import Pin
                from neopixel import NeoPixel
                rgb = NeoPixel(Pin(25, Pin.OUT), 5)
                for i in range(5):
                    rgb[i] = (0, 0, 0)
                rgb.write()
            """.trimIndent(),
        ),
        McuCustomSerialActionState(
            id = "relay-on",
            labelText = "继电器吸合",
            scriptText = """
                from machine import Pin
                Pin(14, Pin.OUT).value(1)
            """.trimIndent(),
        ),
        McuCustomSerialActionState(
            id = "relay-off",
            labelText = "继电器断开",
            scriptText = """
                from machine import Pin
                Pin(14, Pin.OUT).value(0)
            """.trimIndent(),
        ),
    )
}
