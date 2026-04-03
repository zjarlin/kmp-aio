package site.addzero.kcloud.plugins.mcuconsole.workbench

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusCommandConfig
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusFrameFormat
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusAtomicAction
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioMode
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioModeRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioWriteRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusPwmDutyRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusServoAngleRequest
import kotlin.math.max

@Single
class McuConsoleWorkbenchState(
    private val remoteService: McuConsoleRemoteService,
) {
    var uiState by mutableStateOf(McuConsoleUiState())
        private set

    var ports: List<McuPortSummary>
        get() = uiState.devices
        private set(value) {
            updateUiState { copy(devices = value) }
        }

    var deviceDraft: McuDeviceProfileIso?
        get() = uiState.deviceDraft
        private set(value) {
            updateUiState { copy(deviceDraft = value) }
        }

    var transportProfiles: List<McuTransportProfileIso>
        get() = uiState.transportProfiles
        private set(value) {
            updateUiState { copy(transportProfiles = value) }
        }

    var transportDraft: McuTransportProfileIso
        get() = uiState.transportDraft
        private set(value) {
            updateUiState { copy(transportDraft = value) }
        }

    var flashProfiles: List<McuFlashProfileSummary>
        get() = uiState.flashProfiles
        private set(value) {
            updateUiState { copy(flashProfiles = value) }
        }

    var runtimeBundles: List<McuRuntimeBundleSummary>
        get() = uiState.runtimeBundles
        private set(value) {
            updateUiState { copy(runtimeBundles = value) }
        }

    var portQuery: String
        get() = uiState.selection.portQuery
        set(value) {
            updateSelection { copy(portQuery = value) }
        }

    var selectedPortPath: String?
        get() = uiState.selection.selectedPortPath
        private set(value) {
            updateSelection { copy(selectedPortPath = value) }
        }

    var selectedPortDeviceKey: String?
        get() = uiState.selection.selectedPortDeviceKey
        private set(value) {
            updateSelection { copy(selectedPortDeviceKey = value) }
        }

    var selectedPortRemarkDraft: String
        get() = uiState.deviceDraft?.remark.orEmpty()
        set(value) {
            val nextDraft = (deviceDraft ?: selectedPort.toDeviceDraft()).copy(remark = value)
            deviceDraft = nextDraft
        }

    var selectedFlashProfileId: String?
        get() = uiState.selection.selectedFlashProfileId
        private set(value) {
            updateSelection { copy(selectedFlashProfileId = value) }
        }

    var selectedRuntimeBundleId: String?
        get() = uiState.selection.selectedRuntimeBundleId
        private set(value) {
            updateSelection { copy(selectedRuntimeBundleId = value) }
        }

    var selectedScriptExampleId: String?
        get() = uiState.selection.selectedScriptExampleId
        private set(value) {
            updateSelection { copy(selectedScriptExampleId = value) }
        }

    var selectedAtomicCommandId: String?
        get() = uiState.selection.selectedAtomicCommandId
        private set(value) {
            updateSelection { copy(selectedAtomicCommandId = value) }
        }

    var selectedTransportKind: McuTransportKind
        get() = uiState.selection.selectedTransportKind
        private set(value) {
            updateSelection { copy(selectedTransportKind = value) }
        }

    var selectedModbusAtomicAction: McuModbusAtomicAction
        get() = uiState.selection.selectedModbusAtomicAction
        private set(value) {
            updateSelection { copy(selectedModbusAtomicAction = value) }
        }

    var activeSessionTransportKind: McuTransportKind
        get() = uiState.selection.activeSessionTransportKind
        private set(value) {
            updateSelection { copy(activeSessionTransportKind = value) }
        }

    var modbusConnectionNameText: String
        get() = transportDraft.name
        set(value) {
            updateTransportDraft {
                copy(name = value.ifBlank { selectedTransportKind.defaultDraftName() })
            }
        }

    var baudRateText: String
        get() = transportDraft.baudRate?.toString().orEmpty()
        set(value) {
            updateTransportDraft { copy(baudRate = value.toIntOrNull()) }
        }

    var modbusRtuUnitIdText: String
        get() = transportDraft.unitId?.toString().orEmpty()
        set(value) {
            updateTransportDraft { copy(unitId = value.toIntOrNull()) }
        }

    var modbusRtuTimeoutMsText: String
        get() = transportDraft.timeoutMs?.toString().orEmpty()
        set(value) {
            updateTransportDraft { copy(timeoutMs = value.toIntOrNull()) }
        }

    var modbusRtuDataBitsText: String
        get() = transportDraft.dataBits?.toString().orEmpty()
        set(value) {
            updateTransportDraft { copy(dataBits = value.toIntOrNull()) }
        }

    var modbusRtuStopBitsText: String
        get() = transportDraft.stopBits?.toString().orEmpty()
        set(value) {
            updateTransportDraft { copy(stopBits = value.toIntOrNull()) }
        }

    var modbusRtuParity: McuModbusSerialParity
        get() = transportDraft.parity ?: McuModbusSerialParity.NONE
        set(value) {
            updateTransportDraft { copy(parity = value) }
        }

    var modbusFrameFormat: McuModbusFrameFormat
        get() = uiState.modbus.frameFormat
        set(value) {
            updateModbusState { copy(frameFormat = value) }
        }

    var modbusRtuRetriesText: String
        get() = transportDraft.retries?.toString().orEmpty()
        set(value) {
            updateTransportDraft { copy(retries = value.toIntOrNull()) }
        }

    var modbusGpioWritePinText: String
        get() = uiState.modbus.gpioWritePinText
        set(value) {
            updateModbusState { copy(gpioWritePinText = value) }
        }

    var modbusGpioWriteHigh: Boolean
        get() = uiState.modbus.gpioWriteHigh
        set(value) {
            updateModbusState { copy(gpioWriteHigh = value) }
        }

    var modbusGpioModePinText: String
        get() = uiState.modbus.gpioModePinText
        set(value) {
            updateModbusState { copy(gpioModePinText = value) }
        }

    var modbusGpioMode: McuModbusGpioMode
        get() = uiState.modbus.gpioMode
        set(value) {
            updateModbusState { copy(gpioMode = value) }
        }

    var modbusPwmPinText: String
        get() = uiState.modbus.pwmPinText
        set(value) {
            updateModbusState { copy(pwmPinText = value) }
        }

    var modbusPwmDutyText: String
        get() = uiState.modbus.pwmDutyText
        set(value) {
            updateModbusState { copy(pwmDutyText = value) }
        }

    var modbusServoPinText: String
        get() = uiState.modbus.servoPinText
        set(value) {
            updateModbusState { copy(servoPinText = value) }
        }

    var modbusServoAngleText: String
        get() = uiState.modbus.servoAngleText
        set(value) {
            updateModbusState { copy(servoAngleText = value) }
        }

    var scriptLanguage: String
        get() = uiState.scriptEditor.language
        set(value) {
            updateScriptEditor { copy(language = value) }
        }

    var session: McuSessionSnapshot
        get() = uiState.session
        private set(value) {
            updateUiState { copy(session = value) }
        }

    var scriptStatus: McuScriptStatusResponse
        get() = uiState.scriptStatus
        private set(value) {
            updateUiState { copy(scriptStatus = value) }
        }

    var flashStatus: McuFlashStatusResponse
        get() = uiState.flashStatus
        private set(value) {
            updateUiState { copy(flashStatus = value) }
        }

    var runtimeStatus: McuRuntimeStatusResponse
        get() = uiState.runtimeStatus
        private set(value) {
            updateUiState { copy(runtimeStatus = value) }
        }

    var modbusLastExecution: McuModbusExecutionResult
        get() = uiState.modbusLastExecution
        private set(value) {
            updateUiState { copy(modbusLastExecution = value) }
        }

    var events: List<McuEventEnvelope>
        get() = uiState.events
        private set(value) {
            updateUiState { copy(events = value) }
        }

    var widgetInstances: List<McuWidgetInstanceState>
        get() = uiState.widgetInstances
        private set(value) {
            updateUiState { copy(widgetInstances = value) }
        }

    var scriptText: String
        get() = uiState.scriptEditor.scriptText
        set(value) {
            updateScriptEditor { copy(scriptText = value) }
        }

    var serialCommandText: String
        get() = uiState.serialConsole.commandText
        set(value) {
            updateSerialConsoleState { copy(commandText = value) }
        }

    var serialCommandAppendLineEnding: Boolean
        get() = uiState.serialConsole.appendLineEnding
        set(value) {
            updateSerialConsoleState { copy(appendLineEnding = value) }
        }

    var serialCommandLineEnding: McuSerialLineEnding
        get() = uiState.serialConsole.lineEnding
        set(value) {
            updateSerialConsoleState { copy(lineEnding = value) }
        }

    var panelControlModuleText: String
        get() = uiState.panelControl.moduleText
        set(value) {
            updatePanelControlState { copy(moduleText = value) }
        }

    var panelDisplayValueText: String
        get() = uiState.panelControl.displayValueText
        set(value) {
            updatePanelControlState { copy(displayValueText = value) }
        }

    var panelBeepTimesText: String
        get() = uiState.panelControl.beepTimesText
        set(value) {
            updatePanelControlState { copy(beepTimesText = value) }
        }

    var panelLedIndexText: String
        get() = uiState.panelControl.ledIndexText
        set(value) {
            updatePanelControlState { copy(ledIndexText = value) }
        }

    var customSerialActions: List<McuCustomSerialActionState>
        get() = uiState.customSerialActions
        private set(value) {
            updateUiState { copy(customSerialActions = value) }
        }

    var probePinMapFilesText: String
        get() = uiState.probe.pinMapFilesText
        set(value) {
            updateProbeState { copy(pinMapFilesText = value) }
        }

    var probeGpioSnapshotPinsText: String
        get() = uiState.probe.gpioSnapshotPinsText
        set(value) {
            updateProbeState { copy(gpioSnapshotPinsText = value) }
        }

    var probeI2cSdaText: String
        get() = uiState.probe.i2cSdaText
        set(value) {
            updateProbeState { copy(i2cSdaText = value) }
        }

    var probeI2cSclText: String
        get() = uiState.probe.i2cSclText
        set(value) {
            updateProbeState { copy(i2cSclText = value) }
        }

    var firmwarePathText: String
        get() = uiState.flashEditor.firmwarePathText
        set(value) {
            updateFlashEditor { copy(firmwarePathText = value) }
        }

    var firmwareDownloadUrlText: String
        get() = uiState.flashEditor.firmwareDownloadUrlText
        set(value) {
            updateFlashEditor { copy(firmwareDownloadUrlText = value) }
        }

    var flashCommandTemplateText: String
        get() = uiState.flashEditor.flashCommandTemplateText
        set(value) {
            updateFlashEditor { copy(flashCommandTemplateText = value) }
        }

    var timeoutMsText: String
        get() = uiState.scriptEditor.timeoutMsText
        set(value) {
            updateScriptEditor { copy(timeoutMsText = value) }
        }

    var feedbackMessage: String?
        get() = uiState.feedback.message
        private set(value) {
            updateFeedbackState { copy(message = value) }
        }

    var feedbackIsError: Boolean
        get() = uiState.feedback.isError
        private set(value) {
            updateFeedbackState { copy(isError = value) }
        }

    var isLoadingPorts: Boolean
        get() = uiState.loading.isLoadingPorts
        private set(value) {
            updateLoadingState { copy(isLoadingPorts = value) }
        }

    var isSubmitting: Boolean
        get() = uiState.loading.isSubmitting
        private set(value) {
            updateLoadingState { copy(isSubmitting = value) }
        }

    private var lastSeenSeq: Long = 0
    private var widgetInstanceSequence = 0
    private var autoFilledFirmwarePath: String? = null
    private var autoFilledFirmwareDownloadUrl: String? = null

    private inline fun updateUiState(
        transform: McuConsoleUiState.() -> McuConsoleUiState,
    ) {
        uiState = uiState.transform()
    }

    private inline fun updateSelection(
        transform: McuConsoleSelectionState.() -> McuConsoleSelectionState,
    ) {
        updateUiState {
            copy(selection = selection.transform())
        }
    }

    private inline fun updateTransportDraft(
        transform: McuTransportProfileIso.() -> McuTransportProfileIso,
    ) {
        updateUiState {
            copy(transportDraft = transportDraft.transform())
        }
    }

    private inline fun updateModbusState(
        transform: McuConsoleModbusState.() -> McuConsoleModbusState,
    ) {
        updateUiState {
            copy(modbus = modbus.transform())
        }
    }

    private inline fun updateSerialConsoleState(
        transform: McuConsoleSerialConsoleState.() -> McuConsoleSerialConsoleState,
    ) {
        updateUiState {
            copy(serialConsole = serialConsole.transform())
        }
    }

    private inline fun updatePanelControlState(
        transform: McuConsolePanelControlState.() -> McuConsolePanelControlState,
    ) {
        updateUiState {
            copy(panelControl = panelControl.transform())
        }
    }

    private inline fun updateProbeState(
        transform: McuConsoleProbeState.() -> McuConsoleProbeState,
    ) {
        updateUiState {
            copy(probe = probe.transform())
        }
    }

    private inline fun updateCustomSerialAction(
        index: Int,
        transform: McuCustomSerialActionState.() -> McuCustomSerialActionState,
    ) {
        val current = customSerialActions
        if (index !in current.indices) {
            return
        }
        customSerialActions = current.mapIndexed { currentIndex, action ->
            if (currentIndex == index) {
                action.transform()
            } else {
                action
            }
        }
    }

    private inline fun updateScriptEditor(
        transform: McuConsoleScriptEditorState.() -> McuConsoleScriptEditorState,
    ) {
        updateUiState {
            copy(scriptEditor = scriptEditor.transform())
        }
    }

    private inline fun updateFlashEditor(
        transform: McuConsoleFlashEditorState.() -> McuConsoleFlashEditorState,
    ) {
        updateUiState {
            copy(flashEditor = flashEditor.transform())
        }
    }

    private inline fun updateFeedbackState(
        transform: McuConsoleFeedbackState.() -> McuConsoleFeedbackState,
    ) {
        updateUiState {
            copy(feedback = feedback.transform())
        }
    }

    private inline fun updateLoadingState(
        transform: McuConsoleLoadingState.() -> McuConsoleLoadingState,
    ) {
        updateUiState {
            copy(loading = loading.transform())
        }
    }

    val filteredPorts: List<McuPortSummary>
        get() {
            val source = ports.sortedByWorkbenchPriority()
            val keyword = portQuery.trim()
            if (keyword.isBlank()) {
                return source
            }
            return source.filter { port ->
                listOf(
                    port.portName,
                    port.portPath,
                    port.systemPortName,
                    port.descriptiveName,
                    port.description,
                    port.serialNumber,
                    port.manufacturer,
                    port.remark,
                    port.deviceKey,
                    port.portLocation,
                    port.kind,
                ).any { value ->
                    value.contains(keyword, ignoreCase = true)
                }
            }
        }

    val selectedPort: McuPortSummary?
        get() = ports.firstOrNull { it.portPath == selectedPortPath }

    val selectedFlashProfile: McuFlashProfileSummary?
        get() = flashProfiles.firstOrNull { it.id == selectedFlashProfileId }

    val selectedRuntimeBundle: McuRuntimeBundleSummary?
        get() = runtimeBundles.firstOrNull { it.bundleId == selectedRuntimeBundleId }

    val selectedScriptExample: McuScriptExample?
        get() = selectedRuntimeBundle?.scriptExamples?.firstOrNull { it.id == selectedScriptExampleId }

    val selectedAtomicCommand: McuAtomicCommandDefinition?
        get() = selectedRuntimeBundle?.atomicCommands?.firstOrNull { it.id == selectedAtomicCommandId }

    val hasActiveSession: Boolean
        get() = session.isOpen

    val isScriptRunning: Boolean
        get() = scriptStatus.state == McuScriptRunState.RUNNING || scriptStatus.state == McuScriptRunState.STOPPING

    val isRuntimeReady: Boolean
        get() = runtimeStatus.state == McuRuntimeEnsureState.READY &&
            runtimeStatus.bundleId == selectedRuntimeBundleId

    val canUseWidgets: Boolean
        get() = hasActiveSession &&
            activeSessionTransportKind == McuTransportKind.SERIAL &&
            isRuntimeReady &&
            !isSubmitting

    val supportsSelectedTransportConnection: Boolean
        get() = selectedTransportKind == McuTransportKind.SERIAL

    val canOpenSelectedTransportSession: Boolean
        get() = !isSubmitting &&
            !session.isOpen &&
            selectedPortPath != null &&
            baudRateText.toIntOrNull() != null

    val openSessionActionLabel: String
        get() = "打开串口终端"

    val canEnsureRuntime: Boolean
        get() = session.isOpen &&
            activeSessionTransportKind == McuTransportKind.SERIAL &&
            selectedRuntimeBundle != null

    val canControlSerialLines: Boolean
        get() = session.isOpen &&
            activeSessionTransportKind == McuTransportKind.SERIAL

    val canSendDirectSerialText: Boolean
        get() = session.isOpen &&
            activeSessionTransportKind == McuTransportKind.SERIAL &&
            !isSubmitting

    val selectedTransportNotice: String
        get() = "控制台只保留本机串口自动发现；Modbus 页面复用当前串口参数执行独立 RTU 原子操作。"

    val canStartFlash: Boolean
        get() {
            val profile = selectedFlashProfile ?: return false
            if (firmwarePathText.isBlank()) {
                return false
            }
            if (profile.requiresPort && (selectedPortPath ?: session.portPath).isNullOrBlank()) {
                return false
            }
            return true
        }

    val canDownloadFirmwareOnline: Boolean
        get() {
            val profile = selectedFlashProfile ?: return false
            if (!profile.supportsOnlineDownload && firmwareDownloadUrlText.isBlank()) {
                return false
            }
            return firmwareDownloadUrlText.isNotBlank() || !profile.defaultDownloadUrl.isNullOrBlank()
        }

    val canExecuteSelectedModbusAction: Boolean
        get() = !isSubmitting &&
            modbusFrameFormat == McuModbusFrameFormat.RTU &&
            hasValidModbusRtuConfigForSubmit() &&
            hasValidSelectedModbusActionForSubmit()

    fun selectPort(
        portPath: String?,
    ) {
        selectedPortPath = portPath
        val selected = ports.firstOrNull { it.portPath == portPath }
        selectedPortDeviceKey = selected?.deviceKey?.takeIf { it.isNotBlank() }
        deviceDraft = selected.toDeviceDraft(deviceDraft)
        if (selected != null) {
            updateTransportDraft {
                copy(
                    deviceKey = selected.deviceKey.ifBlank { null },
                    portPathHint = selected.portPath.ifBlank { null },
                )
            }
        }
    }

    fun updateSelectedPortRemarkDraft(
        value: String,
    ) {
        selectedPortRemarkDraft = value
    }

    fun selectTransport(
        kind: McuTransportKind,
    ) {
        selectedTransportKind = kind
        updateTransportDraft {
            copy(
                transportKind = kind,
                name = name.ifBlank { kind.defaultDraftName() },
            )
        }
    }

    fun selectModbusAtomicAction(
        action: McuModbusAtomicAction,
    ) {
        selectedModbusAtomicAction = action
    }

    fun selectFlashProfile(
        profileId: String,
    ) {
        val profile = flashProfiles.firstOrNull { it.id == profileId } ?: return
        selectedFlashProfileId = profile.id
        baudRateText = profile.defaultBaudRate.toString()
        flashCommandTemplateText = profile.commandTemplate.orEmpty()
        val suggestedDownloadUrl = profile.defaultDownloadUrl.orEmpty()
        if (firmwareDownloadUrlText.isBlank() || firmwareDownloadUrlText == autoFilledFirmwareDownloadUrl) {
            firmwareDownloadUrlText = suggestedDownloadUrl
            autoFilledFirmwareDownloadUrl = suggestedDownloadUrl.ifBlank { null }
        }
    }

    fun selectRuntimeBundle(
        bundleId: String,
    ) {
        val bundle = runtimeBundles.firstOrNull { it.bundleId == bundleId } ?: return
        val changed = selectedRuntimeBundleId != bundle.bundleId
        selectedRuntimeBundleId = bundle.bundleId
        if (changed && runtimeStatus.bundleId != bundle.bundleId) {
            runtimeStatus = McuRuntimeStatusResponse(
                state = McuRuntimeEnsureState.IDLE,
                bundleId = bundle.bundleId,
                bundleTitle = bundle.title,
                runtimeKind = bundle.runtimeKind,
                mcuFamily = bundle.mcuFamily,
                defaultFlashProfileId = bundle.defaultFlashProfileId,
                baudRate = bundle.defaultBaudRate,
                lastMessage = "已切换到 ${bundle.title}，尚未验证运行时",
            )
            updateFeedback(runtimeStatus.lastMessage, false)
        }
        if (changed) {
            widgetInstances = emptyList()
            selectedScriptExampleId = null
            selectedAtomicCommandId = null
        }
        scriptLanguage = bundle.defaultLanguage()
        syncBundleDefaults(bundle)
        if (changed && bundle.scriptExamples.isNotEmpty()) {
            val defaultExample = bundle.scriptExamples.first()
            selectedScriptExampleId = defaultExample.id
            scriptText = defaultExample.script
            scriptLanguage = defaultExample.language
        }
    }

    fun selectScriptExample(
        exampleId: String,
    ) {
        val example = selectedRuntimeBundle?.scriptExamples?.firstOrNull { it.id == exampleId } ?: return
        selectedScriptExampleId = example.id
        scriptLanguage = example.language
        scriptText = example.script
    }

    fun selectAtomicCommand(
        commandId: String,
    ) {
        val command = selectedRuntimeBundle?.atomicCommands?.firstOrNull { it.id == commandId } ?: return
        selectedAtomicCommandId = command.id
        if (command.exampleScript.isNotBlank()) {
            scriptText = command.exampleScript
        }
    }

    fun addWidgetFromTemplate(
        templateId: String,
    ) {
        val bundle = selectedRuntimeBundle ?: return
        val template = bundle.widgetTemplates.firstOrNull { it.id == templateId } ?: return
        widgetInstanceSequence += 1
        widgetInstances = widgetInstances + McuWidgetInstanceState(
            instanceId = "widget-${widgetInstanceSequence}",
            templateId = template.id,
            bundleId = bundle.bundleId,
            title = template.title,
            kind = template.kind,
            language = bundle.defaultLanguage(),
            scriptTemplate = template.scriptTemplate,
            bindings = template.bindings,
            values = template.bindings.associate { binding ->
                binding.key to binding.defaultValue
            },
        )
    }

    fun removeWidgetInstance(
        instanceId: String,
    ) {
        widgetInstances = widgetInstances.filterNot { it.instanceId == instanceId }
    }

    fun updateWidgetValue(
        instanceId: String,
        key: String,
        value: String,
    ) {
        widgetInstances = widgetInstances.map { widget ->
            if (widget.instanceId == instanceId) {
                widget.copy(values = widget.values + (key to value))
            } else {
                widget
            }
        }
    }

    fun clearVisibleEvents() {
        events = emptyList()
        lastSeenSeq = session.latestSeq
    }

    fun previewWidgetScript(
        instanceId: String,
    ): String {
        val widget = widgetInstances.firstOrNull { it.instanceId == instanceId } ?: return ""
        return buildWidgetScript(widget).orEmpty()
    }

    suspend fun refreshPorts() {
        isLoadingPorts = true
        try {
            val loadedPorts = remoteService.listPorts()
                .sortedByWorkbenchPriority()
            ports = loadedPorts
            val nextSelectedPort = when {
                session.portPath != null -> loadedPorts.firstOrNull { it.portPath == session.portPath }
                selectedPortPath != null -> loadedPorts.firstOrNull { it.portPath == selectedPortPath }
                selectedPortDeviceKey != null -> {
                    loadedPorts.firstOrNull { it.deviceKey == selectedPortDeviceKey }
                }
                else -> null
            } ?: loadedPorts.firstOrNull()
            selectPort(nextSelectedPort?.portPath)
        } catch (throwable: Throwable) {
            reportError(throwable)
        } finally {
            isLoadingPorts = false
        }
    }

    suspend fun refreshSession() {
        try {
            session = remoteService.getSession()
            session.portPath?.let { portPath ->
                selectPort(portPath)
            }
            if (session.baudRate > 0) {
                baudRateText = session.baudRate.toString()
            }
            lastSeenSeq = max(lastSeenSeq, session.latestSeq)
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun refreshTransportProfiles() {
        try {
            val loadedProfiles = remoteService.listTransportProfiles()
            transportProfiles = loadedProfiles
            val currentProfile = transportDraft.profileKey
                .takeIf { it.isNotBlank() }
                ?.let { profileKey ->
                    loadedProfiles.firstOrNull { profile -> profile.profileKey == profileKey }
                }
            if (currentProfile != null) {
                transportDraft = currentProfile
                selectedTransportKind = currentProfile.transportKind
            }
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun saveSelectedPortRemark() {
        val selected = selectedPort
            ?: run {
                updateFeedback("请先选择串口，再保存备注", true)
                return
            }
        val deviceKey = selected.deviceKey.trim()
        if (deviceKey.isBlank()) {
            updateFeedback("当前串口缺少稳定设备标识，无法可靠保存备注", true)
            return
        }
        runSubmitting(
            if (selectedPortRemarkDraft.isBlank()) {
                "串口备注已清空"
            } else {
                "串口备注已保存"
            },
        ) {
            val savedDraft = remoteService.saveDeviceProfile(
                (deviceDraft ?: selected.toDeviceDraft()).copy(
                    deviceKey = deviceKey,
                    serialNumber = selected.serialNumber.ifBlank { null },
                    manufacturer = selected.manufacturer.ifBlank { null },
                    vendorId = selected.vendorId,
                    productId = selected.productId,
                    lastPortPath = selected.portPath.ifBlank { null },
                    lastPortName = selected.portName.ifBlank { null },
                    remark = selectedPortRemarkDraft.ifBlank { null },
                ),
            )
            deviceDraft = savedDraft
            refreshPorts()
            val nextSelected = ports.firstOrNull { port ->
                port.deviceKey == deviceKey
            } ?: ports.firstOrNull { port ->
                port.portPath == selected.portPath
            }
            selectPort(nextSelected?.portPath)
        }
    }

    suspend fun loadRecentEvents(
        limit: Int = 200,
    ) {
        try {
            val response = remoteService.readRecentLines(McuSessionLinesRequest(limit = limit))
            events = response.items
            lastSeenSeq = max(response.latestSeq, response.items.lastOrNull()?.seq ?: 0)
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun pollEvents() {
        try {
            val response = remoteService.readEvents(lastSeenSeq)
            if (response.items.isNotEmpty()) {
                events = (events + response.items).takeLast(400)
                lastSeenSeq = max(response.latestSeq, response.items.last().seq)
            } else {
                lastSeenSeq = max(lastSeenSeq, response.latestSeq)
            }
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun refreshScriptStatus() {
        try {
            scriptStatus = remoteService.getScriptStatus()
            syncWidgetResult(scriptStatus)
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun refreshFlashProfiles() {
        try {
            val loadedProfiles = remoteService.listFlashProfiles()
            flashProfiles = loadedProfiles
            val selected = loadedProfiles.firstOrNull { it.id == selectedFlashProfileId }
            val fallback = selectedRuntimeBundle
                ?.defaultFlashProfileId
                ?.let { bundleProfileId ->
                    loadedProfiles.firstOrNull { it.id == bundleProfileId }
                }
                ?: loadedProfiles.firstOrNull()
            val nextProfile = selected ?: fallback
            if (nextProfile != null) {
                val shouldResetCommand = selectedFlashProfileId == null || selected == null
                selectedFlashProfileId = nextProfile.id
                if (shouldResetCommand || flashCommandTemplateText.isBlank()) {
                    flashCommandTemplateText = nextProfile.commandTemplate.orEmpty()
                }
                if (baudRateText.isBlank() || baudRateText == "115200") {
                    baudRateText = nextProfile.defaultBaudRate.toString()
                }
            }
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun refreshFlashStatus() {
        try {
            flashStatus = remoteService.getFlashStatus()
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun refreshRuntimeBundles() {
        try {
            val loadedBundles = remoteService.listRuntimeBundles()
            runtimeBundles = loadedBundles
            val fallback = loadedBundles.firstOrNull { it.bundleId == "micropython-default-generic" }
                ?: loadedBundles.firstOrNull()
            val nextBundle = loadedBundles.firstOrNull { it.bundleId == selectedRuntimeBundleId } ?: fallback
            nextBundle?.let { bundle ->
                selectRuntimeBundle(bundle.bundleId)
            }
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun loadRuntimeBundlesIfNeeded() {
        if (runtimeBundles.isEmpty()) {
            refreshRuntimeBundles()
        }
    }

    suspend fun refreshRuntimeStatus() {
        if (!session.isOpen) {
            val selectedBundle = selectedRuntimeBundle
            runtimeStatus = McuRuntimeStatusResponse(
                state = McuRuntimeEnsureState.IDLE,
                bundleId = selectedBundle?.bundleId,
                bundleTitle = selectedBundle?.title,
                runtimeKind = selectedBundle?.runtimeKind,
                mcuFamily = selectedBundle?.mcuFamily,
                defaultFlashProfileId = selectedBundle?.defaultFlashProfileId,
                baudRate = selectedBundle?.defaultBaudRate,
            )
            return
        }
        try {
            applyRuntimeStatus(remoteService.getRuntimeStatus())
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun refreshAll() {
        refreshPorts()
        refreshTransportProfiles()
        refreshFlashProfiles()
        refreshRuntimeBundles()
        refreshSession()
        refreshFlashStatus()
        if (session.isOpen) {
            refreshRuntimeStatus()
        }
        if (events.isEmpty()) {
            loadRecentEvents()
        } else {
            pollEvents()
        }
    }

    suspend fun saveCurrentTransportProfile() {
        runSubmitting("连接配置已保存") {
            persistTransportDraft(selectedTransportKind)
        }
    }

    fun applyTransportProfile(
        profileKey: String,
    ) {
        val profile = transportProfiles.firstOrNull { item -> item.profileKey == profileKey } ?: return
        transportDraft = profile
        selectedTransportKind = profile.transportKind
        profile.deviceKey
            ?.takeIf { key -> key.isNotBlank() }
            ?.let { deviceKey ->
                val matchedPort = ports.firstOrNull { port -> port.deviceKey == deviceKey }
                    ?: profile.portPathHint?.let { portPath ->
                        ports.firstOrNull { port -> port.portPath == portPath }
                    }
                selectPort(matchedPort?.portPath)
            }
    }

    suspend fun deleteTransportProfile(
        profileKey: String,
    ) {
        runSubmitting("连接配置已删除") {
            val updatedProfiles = remoteService.deleteTransportProfile(profileKey)
            transportProfiles = updatedProfiles
            if (transportDraft.profileKey == profileKey) {
                transportDraft = defaultTransportDraft(selectedTransportKind)
            }
        }
    }

    suspend fun openSession() {
        openSerialBackedSession(
            transportKind = McuTransportKind.SERIAL,
            initializeRuntime = true,
            successMessage = null,
        )
    }

    suspend fun closeSession() {
        runSubmitting("串口已关闭") {
            session = remoteService.closeSession()
            runtimeStatus = McuRuntimeStatusResponse()
            scriptStatus = McuScriptStatusResponse()
            activeSessionTransportKind = McuTransportKind.SERIAL
        }
    }

    suspend fun resetSession() {
        runSubmitting("已发送复位脉冲") {
            session = remoteService.resetSession(McuResetRequest())
        }
    }

    suspend fun updateDtr(
        enabled: Boolean,
    ) {
        runSubmitting(if (enabled) "DTR 已开启" else "DTR 已关闭") {
            session = remoteService.updateSignals(
                McuSignalRequest(
                    dtrEnabled = enabled,
                ),
            )
        }
    }

    suspend fun updateRts(
        enabled: Boolean,
    ) {
        runSubmitting(if (enabled) "RTS 已开启" else "RTS 已关闭") {
            session = remoteService.updateSignals(
                McuSignalRequest(
                    rtsEnabled = enabled,
                ),
            )
        }
    }

    suspend fun sendSerialCommand() {
        val command = serialCommandText.trimEnd()
        if (command.isBlank()) {
            updateFeedback("串口命令不能为空", true)
            return
        }
        sendSerialPayload(
            text = command,
            appendLineEnding = serialCommandAppendLineEnding,
            successMessage = "串口命令已发送",
        )
    }

    suspend fun openReplSession() {
        selectTransport(McuTransportKind.SERIAL)
        if (session.isOpen && activeSessionTransportKind == McuTransportKind.SERIAL) {
            updateFeedback("串口终端已连接", false)
            return
        }
        openSession()
    }

    suspend fun sendReplText(
        text: String = serialCommandText,
        appendLineEnding: Boolean = true,
        clearInput: Boolean = true,
        successMessage: String = "终端输入已发送",
    ) {
        sendSerialPayload(
            text = text,
            appendLineEnding = appendLineEnding,
            successMessage = successMessage,
            clearInput = clearInput,
        )
    }

    suspend fun sendReplNewLine() {
        sendReplText(
            text = "",
            appendLineEnding = true,
            clearInput = true,
            successMessage = "已发送空行",
        )
    }

    suspend fun sendReplInterrupt() {
        sendReplText(
            text = "\u0003",
            appendLineEnding = false,
            clearInput = false,
            successMessage = "已发送 Ctrl+C",
        )
    }

    suspend fun sendPanelDisplayCommand() {
        val value = panelDisplayValueText.trim()
        if (value.isBlank()) {
            updateFeedback("显示内容不能为空", true)
            return
        }
        sendPanelControlCommand(
            commandLines = listOf("p.s(${value.toPythonLiteralOrNumber()})"),
            successMessage = "数码管命令已发送",
        )
    }

    suspend fun sendPanelBeepCommand() {
        val times = requireIntInRange(
            value = panelBeepTimesText,
            label = "蜂鸣器次数",
            min = 1,
            max = 20,
        ) ?: return
        sendPanelControlCommand(
            commandLines = listOf("p.b($times)"),
            successMessage = "蜂鸣器命令已发送",
        )
    }

    suspend fun sendPanelLedCommand(
        enabled: Boolean,
    ) {
        val index = requireIntInRange(
            value = panelLedIndexText,
            label = "LED 序号",
            min = 0,
            max = 7,
        ) ?: return
        sendPanelControlCommand(
            commandLines = listOf("p.l($index, ${if (enabled) "True" else "False"})"),
            successMessage = if (enabled) {
                "LED 点亮命令已发送"
            } else {
                "LED 熄灭命令已发送"
            },
        )
    }

    suspend fun sendPanelAllLedCommand(
        enabled: Boolean,
    ) {
        sendPanelControlCommand(
            commandLines = listOf("p.${if (enabled) "all_on" else "all_off"}()"),
            successMessage = if (enabled) {
                "全亮命令已发送"
            } else {
                "全灭命令已发送"
            },
        )
    }

    suspend fun sendPanelClearDisplayCommand() {
        sendPanelControlCommand(
            commandLines = listOf("p.c()"),
            successMessage = "清屏命令已发送",
        )
    }

    suspend fun probeKnownPinMap() {
        val files = parseProbeFileNames(probePinMapFilesText)
        if (files.isEmpty()) {
            updateFeedback("至少填写一个探测文件名", true)
            return
        }
        sendGeneratedSerialScript(
            script = buildKnownPinMapProbeScript(files),
            successMessage = "已发送已知引脚探测脚本",
        )
    }

    suspend fun probeGpioSnapshot() {
        val pins = parseProbePinList(probeGpioSnapshotPinsText, "GPIO 快照引脚")
            ?: return
        if (pins.isEmpty()) {
            updateFeedback("至少填写一个 GPIO 引脚", true)
            return
        }
        sendGeneratedSerialScript(
            script = buildGpioSnapshotProbeScript(pins),
            successMessage = "已发送 GPIO 快照探测脚本",
        )
    }

    suspend fun probeI2cDevices() {
        val sda = requireIntInRange(
            value = probeI2cSdaText,
            label = "I2C SDA",
            min = 0,
            max = 39,
        ) ?: return
        val scl = requireIntInRange(
            value = probeI2cSclText,
            label = "I2C SCL",
            min = 0,
            max = 39,
        ) ?: return
        sendGeneratedSerialScript(
            script = buildI2cProbeScript(sda = sda, scl = scl),
            successMessage = "已发送 I2C 扫描脚本",
        )
    }

    fun updateCustomSerialActionLabel(
        index: Int,
        value: String,
    ) {
        updateCustomSerialAction(index) {
            copy(labelText = value)
        }
    }

    fun updateCustomSerialActionScript(
        index: Int,
        value: String,
    ) {
        updateCustomSerialAction(index) {
            copy(scriptText = value)
        }
    }

    suspend fun runCustomSerialAction(
        index: Int,
    ) {
        val action = customSerialActions.getOrNull(index)
            ?: return
        val script = action.scriptText.trim()
        if (script.isBlank()) {
            updateFeedback("自定义脚本不能为空", true)
            return
        }
        val successMessage = action.labelText.trim()
            .ifBlank { "自定义脚本已发送" }
        sendGeneratedSerialScript(
            script = script,
            successMessage = "$successMessage 已发送",
        )
    }

    suspend fun executeSelectedModbusAction() {
        val config = buildModbusRtuConfigForSubmit() ?: return
        when (selectedModbusAtomicAction) {
            McuModbusAtomicAction.GPIO_WRITE -> {
                val pin = requireIntInRange(
                    value = modbusGpioWritePinText,
                    label = "GPIO 引脚",
                    min = 0,
                    max = 255,
                ) ?: return
                val high = modbusGpioWriteHigh
                runModbusOperation(
                    action = McuModbusAtomicAction.GPIO_WRITE,
                    config = config,
                    parameters = listOf(
                        "pin" to pin.toString(),
                        "value" to if (high) "HIGH" else "LOW",
                    ),
                ) {
                    remoteService.gpioWrite(
                        McuModbusGpioWriteRequest(
                            portPath = config.portPath,
                            unitId = config.unitId,
                            baudRate = config.baudRate,
                            dataBits = config.dataBits,
                            stopBits = config.stopBits,
                            parity = config.parity,
                            timeoutMs = config.timeoutMs,
                            retries = config.retries,
                            pin = pin,
                            high = high,
                        ),
                    )
                }
            }

            McuModbusAtomicAction.GPIO_MODE -> {
                val pin = requireIntInRange(
                    value = modbusGpioModePinText,
                    label = "GPIO 引脚",
                    min = 0,
                    max = 255,
                ) ?: return
                val mode = modbusGpioMode
                runModbusOperation(
                    action = McuModbusAtomicAction.GPIO_MODE,
                    config = config,
                    parameters = listOf(
                        "pin" to pin.toString(),
                        "mode" to mode.displayName(),
                    ),
                ) {
                    remoteService.gpioMode(
                        McuModbusGpioModeRequest(
                            portPath = config.portPath,
                            unitId = config.unitId,
                            baudRate = config.baudRate,
                            dataBits = config.dataBits,
                            stopBits = config.stopBits,
                            parity = config.parity,
                            timeoutMs = config.timeoutMs,
                            retries = config.retries,
                            pin = pin,
                            mode = mode.code(),
                        ),
                    )
                }
            }

            McuModbusAtomicAction.PWM_DUTY -> {
                val pin = requireIntInRange(
                    value = modbusPwmPinText,
                    label = "PWM 引脚",
                    min = 0,
                    max = 255,
                ) ?: return
                val dutyU16 = requireIntInRange(
                    value = modbusPwmDutyText,
                    label = "PWM 占空比",
                    min = 0,
                    max = 65535,
                ) ?: return
                runModbusOperation(
                    action = McuModbusAtomicAction.PWM_DUTY,
                    config = config,
                    parameters = listOf(
                        "pin" to pin.toString(),
                        "dutyU16" to dutyU16.toString(),
                    ),
                ) {
                    remoteService.pwmDuty(
                        McuModbusPwmDutyRequest(
                            portPath = config.portPath,
                            unitId = config.unitId,
                            baudRate = config.baudRate,
                            dataBits = config.dataBits,
                            stopBits = config.stopBits,
                            parity = config.parity,
                            timeoutMs = config.timeoutMs,
                            retries = config.retries,
                            pin = pin,
                            dutyU16 = dutyU16,
                        ),
                    )
                }
            }

            McuModbusAtomicAction.SERVO_ANGLE -> {
                val pin = requireIntInRange(
                    value = modbusServoPinText,
                    label = "舵机引脚",
                    min = 0,
                    max = 255,
                ) ?: return
                val angle = requireIntInRange(
                    value = modbusServoAngleText,
                    label = "舵机角度",
                    min = 0,
                    max = 180,
                ) ?: return
                runModbusOperation(
                    action = McuModbusAtomicAction.SERVO_ANGLE,
                    config = config,
                    parameters = listOf(
                        "pin" to pin.toString(),
                        "angle" to angle.toString(),
                    ),
                ) {
                    remoteService.servoAngle(
                        McuModbusServoAngleRequest(
                            portPath = config.portPath,
                            unitId = config.unitId,
                            baudRate = config.baudRate,
                            dataBits = config.dataBits,
                            stopBits = config.stopBits,
                            parity = config.parity,
                            timeoutMs = config.timeoutMs,
                            retries = config.retries,
                            pin = pin,
                            angle = angle,
                        ),
                    )
                }
            }
        }
    }

    suspend fun executeScript() {
        val normalizedScript = scriptText.trim()
        if (normalizedScript.isBlank()) {
            updateFeedback("脚本不能为空", true)
            return
        }
        val timeoutMs = timeoutMsText.toIntOrNull()
            ?: run {
                updateFeedback("超时设置无效", true)
                return
            }
        runSubmitting("脚本已下发") {
            scriptStatus = remoteService.executeScript(
                McuScriptExecuteRequest(
                    language = scriptLanguage,
                    script = normalizedScript,
                    timeoutMs = timeoutMs,
                ),
            )
            syncWidgetResult(scriptStatus)
            pollEvents()
        }
    }

    suspend fun executeWidget(
        instanceId: String,
    ) {
        val widget = widgetInstances.firstOrNull { it.instanceId == instanceId }
            ?: run {
                updateFeedback("控件实例不存在", true)
                return
            }
        if (!canUseWidgets) {
            updateFeedback("运行时未就绪", true)
            return
        }
        val timeoutMs = timeoutMsText.toIntOrNull()
            ?: run {
                updateFeedback("超时设置无效", true)
                return
            }
        val script = buildWidgetScript(widget)
            ?: run {
                updateFeedback("控件参数不完整", true)
                return
            }
        runSubmitting("${widget.title} 已执行") {
            scriptText = script
            scriptLanguage = widget.language
            scriptStatus = remoteService.executeScript(
                McuScriptExecuteRequest(
                    language = widget.language,
                    script = script,
                    timeoutMs = timeoutMs,
                ),
            )
            widgetInstances = widgetInstances.map { current ->
                if (current.instanceId == widget.instanceId) {
                    current.copy(
                        activeRequestId = scriptStatus.activeRequestId,
                        lastRequestId = scriptStatus.lastRequestId ?: scriptStatus.activeRequestId,
                        lastMessage = scriptStatus.lastMessage,
                    )
                } else {
                    current
                }
            }
            syncWidgetResult(scriptStatus)
            pollEvents()
        }
    }

    suspend fun stopScript() {
        runSubmitting("已发送停止请求") {
            scriptStatus = remoteService.stopScript()
            syncWidgetResult(scriptStatus)
            pollEvents()
        }
    }

    suspend fun startFlash() {
        runSubmitting("烧录完成") {
            startFlashInternal()
        }
    }

    suspend fun downloadFirmwareOnline(
        flashAfterDownload: Boolean = false,
    ) {
        val profile = selectedFlashProfile
            ?: run {
                updateFeedback("请先选择烧录能力包", true)
                return
            }
        runSubmitting(
            if (flashAfterDownload) {
                "在线下载并烧录完成"
            } else {
                "固件已下载"
            },
        ) {
            val response = remoteService.downloadFlashFirmware(
                McuFlashDownloadRequest(
                    profileId = profile.id,
                    downloadUrl = firmwareDownloadUrlText.trim().takeIf { it.isNotBlank() },
                ),
            )
            applyFirmwareDownload(response)
            if (flashAfterDownload) {
                startFlashInternal()
            } else {
                loadRecentEvents()
            }
        }
    }

    suspend fun ensureRuntime(
        forceReflash: Boolean = false,
    ) {
        ensureRuntimeInternal(forceReflash = forceReflash, showSubmitting = true)
    }

    private suspend fun ensureRuntimeInternal(
        forceReflash: Boolean,
        showSubmitting: Boolean,
    ) {
        val bundleId = selectedRuntimeBundleId ?: runtimeBundles.firstOrNull()?.bundleId
            ?: run {
                updateFeedback("没有可用运行时包", true)
                return
            }
        val execute: suspend () -> Unit = {
            applyRuntimeStatus(
                remoteService.ensureRuntime(
                    McuRuntimeEnsureRequest(
                        bundleId = bundleId,
                        forceReflash = forceReflash,
                    ),
                ),
            )
            session = remoteService.getSession()
            refreshFlashStatus()
            pollEvents()
        }
        if (showSubmitting) {
            isSubmitting = true
            try {
                execute()
                updateFeedback(
                    runtimeStatus.lastMessage ?: "运行时已就绪",
                    runtimeStatus.state == McuRuntimeEnsureState.ERROR,
                )
            } catch (throwable: Throwable) {
                reportError(throwable)
            } finally {
                isSubmitting = false
            }
        } else {
            execute()
        }
    }

    private fun applyRuntimeStatus(
        nextStatus: McuRuntimeStatusResponse,
    ) {
        runtimeStatus = nextStatus
        nextStatus.bundleId
            ?.takeIf { it.isNotBlank() && selectedRuntimeBundleId == null }
            ?.let { bundleId ->
            if (runtimeBundles.any { it.bundleId == bundleId } && selectedRuntimeBundleId != bundleId) {
                selectRuntimeBundle(bundleId)
            }
        }
        nextStatus.artifactPath
            ?.takeIf { it.isNotBlank() }
            ?.let { artifactPath ->
                if (firmwarePathText.isBlank() || firmwarePathText == autoFilledFirmwarePath) {
                    firmwarePathText = artifactPath
                    autoFilledFirmwarePath = artifactPath
                }
            }
        nextStatus.defaultFlashProfileId
            ?.takeIf { it.isNotBlank() && flashProfiles.any { profile -> profile.id == it } }
            ?.let { profileId ->
                if (selectedFlashProfileId != profileId) {
                    selectFlashProfile(profileId)
                }
            }
        nextStatus.baudRate?.takeIf { it > 0 }?.let { baudRate ->
            baudRateText = baudRate.toString()
        }
    }

    private fun syncBundleDefaults(
        bundle: McuRuntimeBundleSummary,
    ) {
        if (flashProfiles.any { it.id == bundle.defaultFlashProfileId }) {
            selectFlashProfile(bundle.defaultFlashProfileId)
        } else if (baudRateText.isBlank() || baudRateText == "115200") {
            baudRateText = bundle.defaultBaudRate.toString()
        }
    }

    private fun syncWidgetResult(
        nextStatus: McuScriptStatusResponse,
    ) {
        val requestId = nextStatus.lastRequestId ?: nextStatus.activeRequestId ?: return
        widgetInstances = widgetInstances.map { widget ->
            if (widget.lastRequestId == requestId || widget.activeRequestId == requestId) {
                widget.copy(
                    activeRequestId = if (nextStatus.state == McuScriptRunState.RUNNING || nextStatus.state == McuScriptRunState.STOPPING) {
                        requestId
                    } else {
                        null
                    },
                    lastRequestId = requestId,
                    lastFrameType = nextStatus.lastFrameType,
                    lastPayloadText = nextStatus.lastPayload?.toString(),
                    lastMessage = nextStatus.lastMessage,
                )
            } else {
                widget
            }
        }
    }

    private suspend fun startFlashInternal() {
        val firmwarePath = firmwarePathText.trim()
        if (firmwarePath.isBlank()) {
            throw IllegalArgumentException("固件路径不能为空")
        }
        val baudRate = baudRateText.toIntOrNull()
            ?: throw IllegalArgumentException("波特率无效")
        if (selectedFlashProfile?.requiresPort == true &&
            (selectedPortPath ?: session.portPath).isNullOrBlank()
        ) {
            throw IllegalArgumentException("请先选择串口")
        }
        flashStatus = remoteService.startFlash(
            McuFlashRequest(
                profileId = selectedFlashProfile?.id.orEmpty(),
                firmwarePath = firmwarePath,
                portPath = selectedPortPath ?: session.portPath,
                baudRate = baudRate,
                commandTemplate = flashCommandTemplateText
                    .trim()
                    .takeIf {
                        selectedFlashProfile?.strategyKind == McuFlashStrategyKind.COMMAND_TEMPLATE &&
                            it.isNotBlank()
                    },
            ),
        )
        refreshSession()
        refreshRuntimeStatus()
        loadRecentEvents()
    }

    private fun applyFirmwareDownload(
        response: McuFlashDownloadResponse,
    ) {
        response.resolvedUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { resolvedUrl ->
                firmwareDownloadUrlText = resolvedUrl
                autoFilledFirmwareDownloadUrl = resolvedUrl
            }
        response.downloadPath
            ?.takeIf { it.isNotBlank() }
            ?.let { downloadPath ->
                firmwarePathText = downloadPath
                autoFilledFirmwarePath = downloadPath
            }
        response.profileId
            ?.takeIf { it.isNotBlank() && flashProfiles.any { profile -> profile.id == it } }
            ?.let { profileId ->
                if (selectedFlashProfileId != profileId) {
                    selectFlashProfile(profileId)
                }
            }
        response.lastMessage?.takeIf { it.isNotBlank() }?.let { message ->
            updateFeedback(message, false)
        }
    }

    private fun buildWidgetScript(
        widget: McuWidgetInstanceState,
    ): String? {
        var rendered = widget.scriptTemplate
        widget.bindings.forEach { binding ->
            val resolvedValue = resolveBindingValue(binding, widget.values[binding.key]) ?: return null
            rendered = rendered.replace("{{${binding.key}}}", resolvedValue)
        }
        return rendered.trim()
    }

    private fun resolveBindingValue(
        binding: McuWidgetBinding,
        rawValue: String?,
    ): String? {
        val normalized = rawValue?.ifBlank { null } ?: binding.defaultValue.takeIf { it.isNotBlank() }
        if (binding.required && normalized == null) {
            return null
        }
        return when (binding.fieldKind) {
            McuWidgetFieldKind.BOOLEAN -> normalized?.lowercase() ?: "false"
            McuWidgetFieldKind.INTEGER -> normalized ?: "0"
            McuWidgetFieldKind.NUMBER -> normalized ?: "0"
            else -> (normalized ?: "").escapeScriptText()
        }
    }

    private suspend fun runSubmitting(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        isSubmitting = true
        try {
            block()
            updateFeedback(successMessage, false)
        } catch (throwable: Throwable) {
            reportError(throwable)
        } finally {
            isSubmitting = false
        }
    }

    private fun reportError(
        throwable: Throwable,
    ) {
        val message = throwable.userFacingMessage()
        updateFeedback(message, true)
    }

    private fun updateFeedback(
        message: String?,
        isError: Boolean,
    ) {
        feedbackMessage = message
        feedbackIsError = isError
    }

    private suspend fun sendPanelControlCommand(
        commandLines: List<String>,
        successMessage: String,
    ) {
        if (!canSendDirectSerialText) {
            updateFeedback("请先打开串口会话", true)
            return
        }
        val moduleName = panelControlModuleText.trim()
        if (moduleName.isBlank()) {
            updateFeedback("panel_control 模块名不能为空", true)
            return
        }
        val script = buildString {
            append("import ")
            append(moduleName)
            append(" as p\n")
            commandLines.forEach { line ->
                append(line)
                append('\n')
            }
        }.trimEnd()
        sendGeneratedSerialScript(
            script = script,
            successMessage = successMessage,
        )
    }

    private suspend fun sendGeneratedSerialScript(
        script: String,
        successMessage: String,
    ) {
        if (!canSendDirectSerialText) {
            updateFeedback("请先打开串口会话", true)
            return
        }
        serialCommandText = script
        sendSerialPayload(
            text = script,
            appendLineEnding = true,
            successMessage = successMessage,
        )
    }

    /**
     * REPL 允许空行和纯空格，因此这里保留原文，不做 trim。
     */
    private suspend fun sendSerialPayload(
        text: String,
        appendLineEnding: Boolean,
        successMessage: String,
        clearInput: Boolean = false,
    ) {
        if (!canSendDirectSerialText) {
            updateFeedback("请先打开串口会话", true)
            return
        }
        if (text.isEmpty() && !appendLineEnding) {
            updateFeedback("发送内容不能为空", true)
            return
        }
        runSubmitting(successMessage) {
            remoteService.sendSerialText(
                McuSerialTextSendRequest(
                    text = text,
                    appendLineEnding = appendLineEnding,
                    lineEnding = serialCommandLineEnding,
                ),
            )
            if (clearInput) {
                serialCommandText = ""
            }
            pollEvents()
        }
    }

    fun selectedModbusActionReferenceRows(): List<Pair<String, String>> {
        return when (selectedModbusAtomicAction) {
            McuModbusAtomicAction.GPIO_WRITE -> listOf(
                "动作" to selectedModbusAtomicAction.displayName(),
                "operationId" to "gpio-write",
                "寄存器" to "1024",
                "说明" to "写 GPIO 电平",
            )

            McuModbusAtomicAction.GPIO_MODE -> listOf(
                "动作" to selectedModbusAtomicAction.displayName(),
                "operationId" to "gpio-mode",
                "寄存器" to "1026",
                "说明" to "切换 GPIO 模式",
            )

            McuModbusAtomicAction.PWM_DUTY -> listOf(
                "动作" to selectedModbusAtomicAction.displayName(),
                "operationId" to "pwm-duty",
                "寄存器" to "1028",
                "说明" to "写 PWM duty_u16",
            )

            McuModbusAtomicAction.SERVO_ANGLE -> listOf(
                "动作" to selectedModbusAtomicAction.displayName(),
                "operationId" to "servo-angle",
                "寄存器" to "1030",
                "说明" to "写舵机目标角度",
            )
        }
    }

    fun modbusLastResultRows(): List<Pair<String, String>> {
        val execution = modbusLastExecution
        val response = execution.response
        if (execution.action == null || response == null) {
            return listOf(
                "状态" to "尚未执行",
                "说明" to "请先在左侧配置 RTU 参数并执行一个原子动作",
            )
        }
        return listOf(
            "动作" to execution.action.displayName(),
            "接受" to response.accepted.toString(),
            "摘要" to response.summary,
            "串口" to execution.config?.portPath.orEmpty(),
            "波特率" to execution.config?.baudRate?.toString().orEmpty(),
            "UnitId" to execution.config?.unitId?.toString().orEmpty(),
            "超时" to execution.config?.timeoutMs?.let { "${it}ms" }.orEmpty(),
        ) + execution.parameters
    }

    fun transportSummaryRows(): List<Pair<String, String>> {
        return listOf(
            "模式" to selectedTransportKind.displayName(),
            "串口" to selectedPortPath.orEmpty().ifBlank { "未选择" },
            "波特率" to baudRateText.ifBlank { "115200" },
            "状态" to if (session.isOpen) "终端已打开" else "待连接",
        )
    }

    fun modbusConnectionSummaryRows(): List<Pair<String, String>> {
        return listOf(
            "连接别名" to modbusConnectionNameText,
            "串口" to selectedPortPath.orEmpty().ifBlank { "请先从左侧选择串口" },
            "帧格式" to modbusFrameFormat.displayName(),
            "波特率" to baudRateText.ifBlank { "115200" },
            "UnitId" to modbusRtuUnitIdText,
            "dataBits" to modbusRtuDataBitsText,
            "stopBits" to modbusRtuStopBitsText,
            "parity" to modbusRtuParity.displayName(),
            "超时" to "${modbusRtuTimeoutMsText}ms",
            "重试" to modbusRtuRetriesText,
            "状态" to if (session.isOpen) "可直接复用当前串口" else "将按本页参数独立执行",
        )
    }

    private fun hasValidModbusRtuConfigForSubmit(): Boolean {
        val portPath = selectedPortPath?.trim()
        if (portPath.isNullOrBlank()) {
            return false
        }
        val baudRate = baudRateText.toIntOrNull() ?: return false
        val unitId = modbusRtuUnitIdText.toIntOrNull() ?: return false
        val dataBits = modbusRtuDataBitsText.toIntOrNull() ?: return false
        val stopBits = modbusRtuStopBitsText.toIntOrNull() ?: return false
        val timeoutMs = modbusRtuTimeoutMsText.toLongOrNull() ?: return false
        val retries = modbusRtuRetriesText.toIntOrNull() ?: return false
        return baudRate > 0 &&
            unitId in 1..247 &&
            dataBits in 5..8 &&
            stopBits in 1..2 &&
            timeoutMs > 0 &&
            retries >= 0
    }

    private fun hasValidSelectedModbusActionForSubmit(): Boolean {
        return when (selectedModbusAtomicAction) {
            McuModbusAtomicAction.GPIO_WRITE -> modbusGpioWritePinText.toIntOrNull() in 0..255
            McuModbusAtomicAction.GPIO_MODE -> modbusGpioModePinText.toIntOrNull() in 0..255
            McuModbusAtomicAction.PWM_DUTY -> {
                val pin = modbusPwmPinText.toIntOrNull()
                val duty = modbusPwmDutyText.toIntOrNull()
                pin in 0..255 && duty in 0..65535
            }

            McuModbusAtomicAction.SERVO_ANGLE -> {
                val pin = modbusServoPinText.toIntOrNull()
                val angle = modbusServoAngleText.toIntOrNull()
                pin in 0..255 && angle in 0..180
            }
        }
    }

    private fun buildModbusRtuConfigForSubmit(): McuModbusCommandConfig? {
        if (modbusFrameFormat != McuModbusFrameFormat.RTU) {
            updateFeedback("当前后端只支持 Modbus RTU，ASCII 帧格式尚未接通", true)
            return null
        }
        val portPath = selectedPortPath?.trim()?.takeIf { it.isNotBlank() }
            ?: run {
                updateFeedback("请先选择 Modbus 串口", true)
                return null
            }
        val baudRate = requirePositiveInt(baudRateText, "Modbus 波特率") ?: return null
        val unitId = requireIntInRange(
            value = modbusRtuUnitIdText,
            label = "Modbus UnitId",
            min = 1,
            max = 247,
        ) ?: return null
        val dataBits = requireIntInRange(
            value = modbusRtuDataBitsText,
            label = "Modbus dataBits",
            min = 5,
            max = 8,
        ) ?: return null
        val stopBits = requireIntInRange(
            value = modbusRtuStopBitsText,
            label = "Modbus stopBits",
            min = 1,
            max = 2,
        ) ?: return null
        val timeoutMs = requirePositiveLong(modbusRtuTimeoutMsText, "Modbus 超时") ?: return null
        val retries = requireNonNegativeInt(modbusRtuRetriesText, "Modbus 重试次数") ?: return null
        return McuModbusCommandConfig(
            portPath = portPath,
            baudRate = baudRate,
            unitId = unitId,
            dataBits = dataBits,
            stopBits = stopBits,
            parity = modbusRtuParity,
            timeoutMs = timeoutMs,
            retries = retries,
        )
    }

    private suspend fun runModbusOperation(
        action: McuModbusAtomicAction,
        config: McuModbusCommandConfig,
        parameters: List<Pair<String, String>>,
        block: suspend () -> McuModbusCommandResponse,
    ) {
        isSubmitting = true
        try {
            val response = block()
            modbusLastExecution = McuModbusExecutionResult(
                action = action,
                config = config,
                parameters = parameters,
                response = response,
            )
            updateFeedback(
                response.summary.ifBlank { "${action.displayName()} 已下发" },
                !response.accepted,
            )
        } catch (throwable: Throwable) {
            reportError(throwable)
        } finally {
            isSubmitting = false
        }
    }

    private suspend fun openSerialBackedSession(
        transportKind: McuTransportKind,
        initializeRuntime: Boolean,
        successMessage: String?,
    ) {
        val portPath = selectedPortPath?.takeIf { it.isNotBlank() }
            ?: run {
                updateFeedback("请先选择串口", true)
                return
            }
        val baudRate = baudRateText.toIntOrNull()
            ?: run {
                updateFeedback("波特率无效", true)
                return
        }
        isSubmitting = true
        try {
            val savedProfile = persistTransportDraft(transportKind)
            session = remoteService.openSession(
                McuSessionOpenRequest(
                    profileKey = savedProfile.profileKey,
                    portPath = portPath,
                    baudRate = baudRate,
                ),
            )
            activeSessionTransportKind = transportKind
            if (initializeRuntime) {
                loadRuntimeBundlesIfNeeded()
                ensureRuntimeInternal(forceReflash = false, showSubmitting = false)
                val message = successMessage ?: runtimeStatus.lastMessage ?: "串口已打开"
                updateFeedback(message, runtimeStatus.state == McuRuntimeEnsureState.ERROR)
            } else {
                runtimeStatus = McuRuntimeStatusResponse(
                    lastMessage = "当前会话未自动执行运行时探测",
                )
                updateFeedback(successMessage ?: "串口已打开", false)
            }
            loadRecentEvents()
            refreshFlashStatus()
        } catch (throwable: Throwable) {
            reportError(throwable)
        } finally {
            isSubmitting = false
        }
    }

    private suspend fun persistTransportDraft(
        kind: McuTransportKind,
    ): McuTransportProfileIso {
        val selected = selectedPort
        val saved = remoteService.saveTransportProfile(
            transportDraft.copy(
                name = transportDraft.name.ifBlank { McuTransportKind.SERIAL.defaultDraftName() },
                transportKind = McuTransportKind.SERIAL,
                deviceKey = selected?.deviceKey?.ifBlank { null } ?: transportDraft.deviceKey,
                portPathHint = selected?.portPath?.ifBlank { null } ?: transportDraft.portPathHint,
            ),
        )
        transportDraft = saved
        selectedTransportKind = saved.transportKind
        transportProfiles = remoteService.listTransportProfiles()
        return saved
    }

    private fun requirePositiveInt(
        value: String,
        label: String,
    ): Int? {
        val parsed = value.toIntOrNull()
            ?: run {
                updateFeedback("$label 无效", true)
                return null
            }
        if (parsed <= 0) {
            updateFeedback("$label 必须大于 0", true)
            return null
        }
        return parsed
    }

    private fun requirePositiveLong(
        value: String,
        label: String,
    ): Long? {
        val parsed = value.toLongOrNull()
            ?: run {
                updateFeedback("$label 无效", true)
                return null
            }
        if (parsed <= 0) {
            updateFeedback("$label 必须大于 0", true)
            return null
        }
        return parsed
    }

    private fun requireNonNegativeInt(
        value: String,
        label: String,
    ): Int? {
        val parsed = value.toIntOrNull()
            ?: run {
                updateFeedback("$label 无效", true)
                return null
            }
        if (parsed < 0) {
            updateFeedback("$label 不能小于 0", true)
            return null
        }
        return parsed
    }

    private fun requireIntInRange(
        value: String,
        label: String,
        min: Int,
        max: Int,
    ): Int? {
        val parsed = value.toIntOrNull()
            ?: run {
                updateFeedback("$label 无效", true)
                return null
            }
        if (parsed !in min..max) {
            updateFeedback("$label 必须在 $min..$max 之间", true)
            return null
        }
        return parsed
    }

    private fun parseProbeFileNames(
        raw: String,
    ): List<String> {
        return raw.split(',', '\n', ';')
            .map { value -> value.trim() }
            .filter { value -> value.isNotBlank() }
            .distinct()
    }

    private fun parseProbePinList(
        raw: String,
        label: String,
    ): List<Int>? {
        val normalized = raw.split(',', '\n', ';', ' ')
            .map { value -> value.trim() }
            .filter { value -> value.isNotBlank() }
        if (normalized.isEmpty()) {
            return emptyList()
        }
        val pins = mutableListOf<Int>()
        normalized.forEach { value ->
            val parsed = value.toIntOrNull()
                ?: run {
                    updateFeedback("$label 存在无效值: $value", true)
                    return null
                }
            if (parsed !in 0..39) {
                updateFeedback("$label 必须在 0..39 之间", true)
                return null
            }
            pins += parsed
        }
        return pins.distinct()
    }

    private fun String.toPythonLiteralOrNumber(): String {
        val normalized = trim()
        return normalized.toIntOrNull()?.toString()
            ?: "\"${normalized.escapePythonString()}\""
    }
}

data class McuWidgetInstanceState(
    val instanceId: String,
    val templateId: String,
    val bundleId: String,
    val title: String,
    val kind: McuWidgetTemplateKind,
    val language: String,
    val scriptTemplate: String,
    val bindings: List<McuWidgetBinding>,
    val values: Map<String, String> = emptyMap(),
    val activeRequestId: String? = null,
    val lastRequestId: String? = null,
    val lastFrameType: String? = null,
    val lastPayloadText: String? = null,
    val lastMessage: String? = null,
)

data class McuModbusExecutionResult(
    val action: McuModbusAtomicAction? = null,
    val config: McuModbusCommandConfig? = null,
    val parameters: List<Pair<String, String>> = emptyList(),
    val response: McuModbusCommandResponse? = null,
)

private fun McuRuntimeBundleSummary.defaultLanguage(): String {
    return "micropython"
}

private fun List<McuPortSummary>.sortedByWorkbenchPriority(): List<McuPortSummary> {
    return sortedWith(
        compareByDescending<McuPortSummary> { port -> port.workbenchPriority() }
            .thenBy { port -> port.portPath.lowercase() },
    )
}

private fun McuPortSummary.workbenchPriority(): Int {
    var score = 0
    if (serialNumber.isNotBlank()) {
        score += 400
    }
    if (vendorId != null || productId != null) {
        score += 260
    }
    if (portPath.startsWith("/dev/cu.")) {
        score += 120
    }
    if (portPath.startsWith("COM", ignoreCase = true)) {
        score += 120
    }
    if (portPath.startsWith("/dev/tty.")) {
        score += 30
    }
    if (portPath.contains("usb", ignoreCase = true) ||
        portName.contains("usb", ignoreCase = true) ||
        description.contains("usb", ignoreCase = true)
    ) {
        score += 80
    }
    if (portPath.contains("uart", ignoreCase = true) ||
        portName.contains("uart", ignoreCase = true) ||
        description.contains("uart", ignoreCase = true)
    ) {
        score += 40
    }
    if (portPath.contains("Bluetooth-Incoming-Port", ignoreCase = true)) {
        score -= 500
    }
    if (portPath.contains("debug-console", ignoreCase = true)) {
        score -= 300
    }
    if (descriptiveName.contains("Dial-In", ignoreCase = true)) {
        score -= 80
    }
    return score
}

fun McuTransportKind.displayName(): String {
    return when (this) {
        McuTransportKind.SERIAL -> "串口"
    }
}

fun McuModbusSerialParity.displayName(): String {
    return when (this) {
        McuModbusSerialParity.NONE -> "NONE"
        McuModbusSerialParity.EVEN -> "EVEN"
        McuModbusSerialParity.ODD -> "ODD"
    }
}

fun McuModbusFrameFormat.displayName(): String {
    return when (this) {
        McuModbusFrameFormat.RTU -> "RTU"
        McuModbusFrameFormat.ASCII -> "ASCII"
    }
}

fun McuSerialLineEnding.displayName(): String {
    return when (this) {
        McuSerialLineEnding.LF -> "LF"
        McuSerialLineEnding.CRLF -> "CRLF"
        McuSerialLineEnding.CR -> "CR"
    }
}

fun McuModbusAtomicAction.displayName(): String {
    return when (this) {
        McuModbusAtomicAction.GPIO_WRITE -> "GPIO 电平"
        McuModbusAtomicAction.GPIO_MODE -> "GPIO 模式"
        McuModbusAtomicAction.PWM_DUTY -> "PWM 占空比"
        McuModbusAtomicAction.SERVO_ANGLE -> "舵机角度"
    }
}

fun McuModbusGpioMode.displayName(): String {
    return when (this) {
        McuModbusGpioMode.INPUT -> "输入"
        McuModbusGpioMode.OUTPUT -> "输出"
        McuModbusGpioMode.INPUT_PULL_UP -> "上拉输入"
    }
}

private fun McuModbusGpioMode.code(): Int {
    return when (this) {
        McuModbusGpioMode.INPUT -> 0
        McuModbusGpioMode.OUTPUT -> 1
        McuModbusGpioMode.INPUT_PULL_UP -> 2
    }
}

private fun McuPortSummary?.toDeviceDraft(
    current: McuDeviceProfileIso? = null,
): McuDeviceProfileIso {
    if (this == null) {
        return current ?: McuDeviceProfileIso()
    }
    return (current ?: McuDeviceProfileIso()).copy(
        deviceKey = deviceKey,
        serialNumber = serialNumber.ifBlank { null },
        manufacturer = manufacturer.ifBlank { null },
        vendorId = vendorId,
        productId = productId,
        remark = current?.remark ?: remark.ifBlank { null },
        lastPortPath = portPath.ifBlank { null },
        lastPortName = portName.ifBlank { null },
    )
}

private fun buildKnownPinMapProbeScript(
    files: List<String>,
): String {
    val renderedFiles = files.joinToString(", ") { file ->
        "\"${file.escapePythonString()}\""
    }
    return """
        import os
        FILES = [$renderedFiles]
        print("=== PROBE KNOWN PIN MAP ===")
        for name in FILES:
            try:
                lines = open(name).read().splitlines()
            except Exception as e:
                print("%s: missing (%s)" % (name, e))
                continue
            print("--- %s ---" % name)
            for idx, line in enumerate(lines, 1):
                text = line.strip()
                lower = text.lower()
                if (not text) or text.startswith("#"):
                    continue
                if "pin" in lower or "neopixel" in lower or "tm1637" in lower or "softi2c" in lower or "i2c(" in lower or "spi(" in lower or "uart(" in lower:
                    print("%s:%d %s" % (name, idx, text))
        print("=== FILES ===")
        print(os.listdir())
    """.trimIndent()
}

private fun buildGpioSnapshotProbeScript(
    pins: List<Int>,
): String {
    val renderedPins = pins.joinToString(", ")
    return """
        from machine import Pin
        PINS = [$renderedPins]
        print("=== PROBE GPIO SNAPSHOT ===")
        for pin in PINS:
            try:
                value = Pin(pin, Pin.IN).value()
                print("GPIO%d=%d" % (pin, value))
            except Exception as e:
                print("GPIO%d ERR %s" % (pin, e))
    """.trimIndent()
}

private fun buildI2cProbeScript(
    sda: Int,
    scl: Int,
): String {
    return """
        from machine import Pin, SoftI2C
        SDA_PIN = $sda
        SCL_PIN = $scl
        print("=== PROBE I2C SCAN ===")
        print("SDA=%d SCL=%d" % (SDA_PIN, SCL_PIN))
        try:
            bus = SoftI2C(sda=Pin(SDA_PIN), scl=Pin(SCL_PIN), freq=100000)
            devices = bus.scan()
            print("I2C devices:", [hex(value) for value in devices])
        except Exception as e:
            print("I2C ERR %s" % e)
    """.trimIndent()
}

private fun String.escapeScriptText(): String {
    return replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
}

private fun String.escapePythonString(): String {
    return replace("\\", "\\\\")
        .replace("\"", "\\\"")
}

private fun Throwable.userFacingMessage(): String {
    return generateSequence(this) { it.cause }
        .mapNotNull { cause ->
            cause.message
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.takeUnless(String::looksLikeInternalImplementationName)
                ?: cause::class.simpleName
                    ?.takeUnless(String::looksLikeInternalImplementationName)
        }
        .firstOrNull()
        ?: "操作失败"
}

private fun String.looksLikeInternalImplementationName(): Boolean {
    val value = trim()
    if (value.isEmpty()) {
        return false
    }
    if ("ApiImpl" in value) {
        return true
    }
    return value.contains('/') && value.contains('$') && !value.contains(' ')
}
