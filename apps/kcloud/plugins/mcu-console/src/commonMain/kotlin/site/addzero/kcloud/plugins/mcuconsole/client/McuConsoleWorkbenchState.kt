package site.addzero.kcloud.plugins.mcuconsole.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import site.addzero.kcloud.plugins.mcuconsole.McuEventEnvelope
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary
import site.addzero.kcloud.plugins.mcuconsole.McuResetRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptRunState
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuSessionLinesRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
import site.addzero.kcloud.plugins.mcuconsole.McuSignalRequest

class McuConsoleWorkbenchState(
    private val remoteService: McuConsoleRemoteService,
) {
    var ports by mutableStateOf<List<McuPortSummary>>(emptyList())
        private set
    var portQuery by mutableStateOf("")
    var selectedPortPath by mutableStateOf<String?>(null)
        private set
    var baudRateText by mutableStateOf("115200")
    var session by mutableStateOf(McuSessionSnapshot())
        private set
    var scriptStatus by mutableStateOf(McuScriptStatusResponse())
        private set
    var flashStatus by mutableStateOf(McuFlashStatusResponse())
        private set
    var events by mutableStateOf<List<McuEventEnvelope>>(emptyList())
        private set
    var scriptText by mutableStateOf(
        """
        gpio_set(2, true);
        delay_ms(300);
        gpio_set(2, false);
        delay_ms(300);
        uart_send("hello from kcloud");
        """.trimIndent(),
    )
    var firmwarePathText by mutableStateOf("")
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

    fun selectPort(
        portPath: String?,
    ) {
        selectedPortPath = portPath
    }

    fun clearVisibleEvents() {
        events = emptyList()
        lastSeenSeq = session.latestSeq
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

    suspend fun refreshAll() {
        refreshPorts()
        refreshSession()
        refreshScriptStatus()
        refreshFlashStatus()
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
        runSubmitting("串口已打开") {
            session = remoteService.openSession(
                McuSessionOpenRequest(
                    portPath = portPath,
                    baudRate = baudRate,
                ),
            )
            loadRecentEvents()
            refreshScriptStatus()
        }
    }

    suspend fun closeSession() {
        runSubmitting("串口已关闭") {
            session = remoteService.closeSession()
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
        val timeoutMs = timeoutMsText.toIntOrNull()
            ?: run {
                updateFeedback("超时设置无效", true)
                return
            }
        val normalizedScript = scriptText.trim()
        if (normalizedScript.isBlank()) {
            updateFeedback("脚本不能为空", true)
            return
        }
        runSubmitting("脚本已下发") {
            scriptStatus = remoteService.executeScript(
                McuScriptExecuteRequest(
                    script = normalizedScript,
                    timeoutMs = timeoutMs,
                ),
            )
            pollEvents()
        }
    }

    suspend fun stopScript() {
        runSubmitting("已发送停止请求") {
            scriptStatus = remoteService.stopScript()
            pollEvents()
        }
    }

    suspend fun startFlash() {
        val firmwarePath = firmwarePathText.trim()
        if (firmwarePath.isBlank()) {
            updateFeedback("固件路径不能为空", true)
            return
        }
        val baudRate = baudRateText.toIntOrNull()
            ?: run {
                updateFeedback("波特率无效", true)
                return
            }
        runSubmitting("烧录完成") {
            flashStatus = remoteService.startFlash(
                McuFlashRequest(
                    firmwarePath = firmwarePath,
                    portPath = selectedPortPath ?: session.portPath,
                    baudRate = baudRate,
                ),
            )
            refreshSession()
            loadRecentEvents()
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

    val hasActiveSession: Boolean
        get() = session.isOpen

    val isScriptRunning: Boolean
        get() = scriptStatus.state == McuScriptRunState.RUNNING || scriptStatus.state == McuScriptRunState.STOPPING
}
