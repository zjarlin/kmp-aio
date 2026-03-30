package site.addzero.kcloud.plugins.mcuconsole.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.*
import kotlin.math.max

@Single
class McuConsoleWorkbenchState(
    private val remoteService: McuConsoleRemoteService,
) {
    var ports by mutableStateOf<List<McuPortSummary>>(emptyList())
        private set
    var flashProfiles by mutableStateOf<List<McuFlashProfileSummary>>(emptyList())
        private set
    var runtimeBundles by mutableStateOf<List<McuRuntimeBundleSummary>>(emptyList())
        private set
    var portQuery by mutableStateOf("")
    var selectedPortPath by mutableStateOf<String?>(null)
        private set
    var selectedFlashProfileId by mutableStateOf<String?>(null)
        private set
    var selectedRuntimeBundleId by mutableStateOf<String?>(null)
        private set
    var selectedScriptExampleId by mutableStateOf<String?>(null)
        private set
    var selectedAtomicCommandId by mutableStateOf<String?>(null)
        private set
    var baudRateText by mutableStateOf("115200")
    var scriptLanguage by mutableStateOf("rhai")
    var session by mutableStateOf(McuSessionSnapshot())
        private set
    var scriptStatus by mutableStateOf(McuScriptStatusResponse())
        private set
    var flashStatus by mutableStateOf(McuFlashStatusResponse())
        private set
    var runtimeStatus by mutableStateOf(McuRuntimeStatusResponse())
        private set
    var events by mutableStateOf<List<McuEventEnvelope>>(emptyList())
        private set
    var widgetInstances by mutableStateOf<List<McuWidgetInstanceState>>(emptyList())
        private set
    var scriptText by mutableStateOf(
        """
        gpio_set(2, true);
        delay_ms(300);
        gpio_set(2, false);
        delay_ms(300);
        """.trimIndent(),
    )
    var firmwarePathText by mutableStateOf("")
    var firmwareDownloadUrlText by mutableStateOf("")
    var flashCommandTemplateText by mutableStateOf("")
    var timeoutMsText by mutableStateOf("5000")
    var feedbackMessage by mutableStateOf<String?>(null)
        private set
    var feedbackIsError by mutableStateOf(false)
        private set
    var isLoadingPorts by mutableStateOf(false)
        private set
    var isSubmitting by mutableStateOf(false)
        private set

    private var lastSeenSeq: Long = 0
    private var widgetInstanceSequence = 0
    private var autoFilledFirmwarePath: String? = null
    private var autoFilledFirmwareDownloadUrl: String? = null

    val filteredPorts: List<McuPortSummary>
        get() {
            val keyword = portQuery.trim()
            if (keyword.isBlank()) {
                return ports
            }
            return ports.filter { port ->
                listOf(
                    port.portName,
                    port.portPath,
                    port.systemPortName,
                    port.descriptiveName,
                    port.description,
                    port.kind,
                ).any { value ->
                    value.contains(keyword, ignoreCase = true)
                }
            }
        }

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
            runtimeStatus.runtimeKind == McuFlashRuntimeKind.RHAI_VM &&
            runtimeStatus.bundleId == selectedRuntimeBundleId

    val canUseWidgets: Boolean
        get() = hasActiveSession && isRuntimeReady && !isSubmitting

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

    fun selectPort(
        portPath: String?,
    ) {
        selectedPortPath = portPath
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
            ports = loadedPorts
            if (session.portPath != null && loadedPorts.any { it.portPath == session.portPath }) {
                selectedPortPath = session.portPath
            } else if (selectedPortPath != null && loadedPorts.any { it.portPath == selectedPortPath }) {
                selectedPortPath = selectedPortPath
            } else {
                selectedPortPath = loadedPorts.firstOrNull()?.portPath
            }
        } catch (throwable: Throwable) {
            reportError(throwable)
        } finally {
            isLoadingPorts = false
        }
    }

    suspend fun refreshSession() {
        try {
            session = remoteService.getSession()
            session.portPath?.let { selectedPortPath = it }
            if (session.baudRate > 0) {
                baudRateText = session.baudRate.toString()
            }
            lastSeenSeq = max(lastSeenSeq, session.latestSeq)
        } catch (throwable: Throwable) {
            reportError(throwable)
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
            val fallback = loadedBundles.firstOrNull { it.bundleId == "rhai-default-generic" }
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
        try {
            applyRuntimeStatus(remoteService.getRuntimeStatus())
        } catch (throwable: Throwable) {
            reportError(throwable)
        }
    }

    suspend fun refreshAll() {
        refreshPorts()
        refreshFlashProfiles()
        refreshRuntimeBundles()
        refreshSession()
        refreshScriptStatus()
        refreshFlashStatus()
        refreshRuntimeStatus()
        if (events.isEmpty()) {
            loadRecentEvents()
        } else {
            pollEvents()
        }
    }

    suspend fun openSession() {
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
            session = remoteService.openSession(
                McuSessionOpenRequest(
                    portPath = portPath,
                    baudRate = baudRate,
                ),
            )
            loadRuntimeBundlesIfNeeded()
            ensureRuntimeInternal(forceReflash = false, showSubmitting = false)
            loadRecentEvents()
            refreshScriptStatus()
            refreshFlashStatus()
            val message = runtimeStatus.lastMessage ?: "串口已打开"
            updateFeedback(message, runtimeStatus.state == McuRuntimeEnsureState.ERROR)
        } catch (throwable: Throwable) {
            reportError(throwable)
        } finally {
            isSubmitting = false
        }
    }

    suspend fun closeSession() {
        runSubmitting("串口已关闭") {
            session = remoteService.closeSession()
            runtimeStatus = McuRuntimeStatusResponse()
            refreshScriptStatus()
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
        val message = throwable.message?.takeIf { it.isNotBlank() } ?: throwable::class.simpleName ?: "操作失败"
        updateFeedback(message, true)
    }

    private fun updateFeedback(
        message: String?,
        isError: Boolean,
    ) {
        feedbackMessage = message
        feedbackIsError = isError
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

private fun McuRuntimeBundleSummary.defaultLanguage(): String {
    return when (runtimeKind) {
        McuFlashRuntimeKind.MICROPYTHON -> "micropython"
        McuFlashRuntimeKind.RHAI_VM -> "rhai"
    }
}

private fun String.escapeScriptText(): String {
    return replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
}
