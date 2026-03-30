package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.coroutines.*
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortConnection
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList

class McuConsoleSessionService(
    private val gateway: SerialPortGateway,
    private val protocolCodec: McuVmProtocolCodec,
    private val settingsService: McuConsoleSettingsService? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val frameListeners = CopyOnWriteArrayList<(McuVmIncomingFrame) -> Unit>()
    private val events = mutableListOf<McuEventEnvelope>()
    private val lock = Any()

    private var sequence = 0L
    private var activeConnection: SerialPortConnection? = null
    private var readerJob: Job? = null
    private var snapshot = McuSessionSnapshot()

    fun listPorts(): McuPortsResponse {
        return McuPortsResponse(
            items = settingsService?.mergeLivePorts(gateway.listPorts()) ?: gateway.listPorts(),
        )
    }

    fun getSessionSnapshot(): McuSessionSnapshot {
        synchronized(lock) {
            return snapshot.copy(latestSeq = sequence)
        }
    }

    fun openSession(
        request: McuSessionOpenRequest,
    ): McuSessionSnapshot {
        require(request.portPath.isNotBlank()) { "portPath is required" }
        closeSession("切换串口会话")

        val connection = gateway.openConnection(
            portPath = request.portPath,
            baudRate = request.baudRate,
        )

        synchronized(lock) {
            activeConnection = connection
            snapshot = McuSessionSnapshot(
                portPath = connection.portPath,
                portName = connection.portName,
                baudRate = connection.baudRate,
                isOpen = true,
                dtrEnabled = false,
                rtsEnabled = false,
                latestSeq = sequence,
            )
        }

        appendEvent(
            kind = McuEventKind.SYSTEM,
            title = "会话已打开",
            message = "${connection.portPath} @ ${connection.baudRate}",
        )
        settingsService?.markTransportProfileUsed(request.profileKey)
        startReader(connection)
        return getSessionSnapshot()
    }

    fun closeSession(
        reason: String = "串口已关闭",
    ): McuSessionSnapshot {
        val connectionToClose: SerialPortConnection?
        val readerToCancel: Job?
        synchronized(lock) {
            connectionToClose = activeConnection
            readerToCancel = readerJob
            activeConnection = null
            readerJob = null
            snapshot = McuSessionSnapshot(
                latestSeq = sequence,
            )
        }

        if (readerToCancel != null) {
            runBlocking {
                readerToCancel.cancelAndJoin()
            }
        }
        connectionToClose?.close()
        if (connectionToClose != null) {
            appendEvent(
                kind = McuEventKind.SYSTEM,
                title = "会话已关闭",
                message = reason,
            )
        }
        return getSessionSnapshot()
    }

    fun resetSession(
        request: McuResetRequest,
    ): McuSessionSnapshot {
        val connection = requireConnection()
        connection.setDtr(true)
        Thread.sleep(request.pulseMs.toLong())
        connection.setDtr(false)
        synchronized(lock) {
            snapshot = snapshot.copy(
                dtrEnabled = false,
                latestSeq = sequence,
            )
        }
        appendEvent(
            kind = McuEventKind.SYSTEM,
            title = "设备复位",
            message = "DTR 脉冲 ${request.pulseMs}ms",
        )
        return getSessionSnapshot()
    }

    fun updateSignals(
        request: McuSignalRequest,
    ): McuSessionSnapshot {
        val connection = requireConnection()
        request.dtrEnabled?.let { enabled ->
            connection.setDtr(enabled)
        }
        request.rtsEnabled?.let { enabled ->
            connection.setRts(enabled)
        }
        synchronized(lock) {
            snapshot = snapshot.copy(
                dtrEnabled = request.dtrEnabled ?: snapshot.dtrEnabled,
                rtsEnabled = request.rtsEnabled ?: snapshot.rtsEnabled,
                latestSeq = sequence,
            )
        }
        appendEvent(
            kind = McuEventKind.SYSTEM,
            title = "线路状态变更",
            message = "DTR=${request.dtrEnabled ?: snapshot.dtrEnabled}, RTS=${request.rtsEnabled ?: snapshot.rtsEnabled}",
        )
        return getSessionSnapshot()
    }

    fun readRecentEvents(
        request: McuSessionLinesRequest,
    ): McuEventBatchResponse {
        synchronized(lock) {
            return McuEventBatchResponse(
                items = events.takeLast(request.limit.coerceAtLeast(1)),
                latestSeq = sequence,
            )
        }
    }

    fun readEvents(
        afterSeq: Long,
    ): McuEventBatchResponse {
        synchronized(lock) {
            return McuEventBatchResponse(
                items = events.filter { event -> event.seq > afterSeq },
                latestSeq = sequence,
            )
        }
    }

    fun registerFrameListener(
        listener: (McuVmIncomingFrame) -> Unit,
    ): AutoCloseable {
        frameListeners += listener
        return AutoCloseable {
            frameListeners -= listener
        }
    }

    fun sendVmFrame(
        frame: McuVmOutgoingFrame,
    ) {
        val encoded = protocolCodec.encode(frame)
        val connection = requireConnection()
        connection.writeUtf8(encoded)
        appendEvent(
            kind = McuEventKind.TX_FRAME,
            title = frame.command,
            message = "已发送 ${frame.command}",
            requestId = frame.requestId,
            raw = encoded.trim(),
        )
    }

    fun sendSerialText(
        request: McuSerialTextSendRequest,
    ): McuSerialTextSendResponse {
        require(request.text.isNotEmpty() || request.appendLineEnding) { "text is required" }
        val payload = request.text.normalizeLineEndings(request.lineEnding)
            .let { normalized ->
                if (request.appendLineEnding) {
                    normalized + request.lineEnding.sequence()
                } else {
                    normalized
                }
            }
        val bytes = payload.toByteArray(Charsets.UTF_8)
        val connection = requireConnection()
        connection.writeUtf8(payload)
        appendEvent(
            kind = McuEventKind.TX_TEXT,
            title = "串口直发",
            message = "已发送 ${bytes.size} bytes",
            raw = payload.escapeForEventLog(),
        )
        return McuSerialTextSendResponse(
            accepted = true,
            bytesSent = bytes.size,
            preview = payload.previewForTransport(),
            lineEnding = request.lineEnding,
        )
    }

    fun appendEvent(
        kind: McuEventKind,
        title: String,
        message: String,
        requestId: String? = null,
        raw: String? = null,
    ): McuEventEnvelope {
        synchronized(lock) {
            sequence += 1
            val event = McuEventEnvelope(
                seq = sequence,
                kind = kind,
                title = title,
                message = message,
                timestamp = Instant.now().toString(),
                requestId = requestId,
                raw = raw,
            )
            events += event
            if (events.size > 800) {
                events.removeAt(0)
            }
            snapshot = snapshot.copy(
                latestSeq = sequence,
                lastError = if (kind == McuEventKind.ERROR) message else snapshot.lastError,
            )
            return event
        }
    }

    private fun startReader(
        connection: SerialPortConnection,
    ) {
        val job = scope.launch {
            val buffer = ByteArray(1024)
            var remainder = ""
            while (true) {
                if (!isActiveConnection(connection)) {
                    break
                }
                val count = try {
                    connection.read(buffer, timeoutMs = 250)
                } catch (error: Throwable) {
                    appendEvent(
                        kind = McuEventKind.ERROR,
                        title = "串口读取失败",
                        message = error.message ?: "未知错误",
                    )
                    forceCloseConnection()
                    break
                }
                if (count <= 0) {
                    continue
                }
                val chunk = remainder + buffer.decodeToString(0, count)
                val extracted = protocolCodec.extractFrames(chunk)
                remainder = extracted.remainder
                extracted.frames.forEach(::handleIncomingLine)
            }
            if (remainder.isNotBlank()) {
                handleIncomingLine(remainder.trim())
            }
        }
        synchronized(lock) {
            readerJob = job
        }
    }

    private fun handleIncomingLine(
        line: String,
    ) {
        if (line.isBlank()) {
            return
        }
        val frame = protocolCodec.decodeOrNull(line)
        if (frame == null) {
            appendEvent(
                kind = McuEventKind.LOG,
                title = "串口日志",
                message = line,
                raw = line,
            )
            return
        }
        appendEvent(
            kind = McuEventKind.RX_FRAME,
            title = frame.type,
            message = frame.message ?: frame.payload?.toString().orEmpty(),
            requestId = frame.requestId,
            raw = line,
        )
        frameListeners.forEach { listener ->
            runCatching {
                listener(frame)
            }
        }
    }

    private fun forceCloseConnection() {
        val connectionToClose: SerialPortConnection?
        synchronized(lock) {
            connectionToClose = activeConnection
            activeConnection = null
            readerJob = null
            snapshot = McuSessionSnapshot(
                latestSeq = sequence,
                lastError = "串口连接已断开",
            )
        }
        connectionToClose?.close()
    }

    private fun requireConnection(): SerialPortConnection {
        synchronized(lock) {
            return requireNotNull(activeConnection) { "当前没有活动串口会话" }
        }
    }

    private fun isActiveConnection(
        connection: SerialPortConnection,
    ): Boolean {
        synchronized(lock) {
            return activeConnection === connection
        }
    }
}

private fun McuSerialLineEnding.sequence(): String {
    return when (this) {
        McuSerialLineEnding.LF -> "\n"
        McuSerialLineEnding.CRLF -> "\r\n"
        McuSerialLineEnding.CR -> "\r"
    }
}

private fun String.normalizeLineEndings(
    lineEnding: McuSerialLineEnding,
): String {
    val normalized = replace("\r\n", "\n").replace('\r', '\n')
    return normalized.replace("\n", lineEnding.sequence())
}

private fun String.escapeForEventLog(): String {
    return replace("\r", "\\r")
        .replace("\n", "\\n\n")
}

private fun String.previewForTransport(
    limit: Int = 160,
): String {
    val escaped = replace("\r", "\\r")
        .replace("\n", "\\n")
    return if (escaped.length <= limit) {
        escaped
    } else {
        escaped.take(limit) + "..."
    }
}
