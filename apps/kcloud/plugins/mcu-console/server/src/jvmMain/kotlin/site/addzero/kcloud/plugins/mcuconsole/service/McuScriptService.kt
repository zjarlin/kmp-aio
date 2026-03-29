package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import java.time.Instant
import java.util.*

class McuScriptService(
    private val sessionService: McuConsoleSessionService,
    private val protocolCodec: McuVmProtocolCodec,
) {
    private val lock = Any()
    private var status = McuScriptStatusResponse()

    init {
        sessionService.registerFrameListener(::handleIncomingFrame)
    }

    fun execute(
        request: McuScriptExecuteRequest,
    ): McuScriptStatusResponse {
        require(request.script.isNotBlank()) { "script is required" }
        val requestId = UUID.randomUUID().toString()
        sessionService.sendVmFrame(protocolCodec.buildExecuteFrame(requestId, request))
        synchronized(lock) {
            status = status.copy(
                state = McuScriptRunState.RUNNING,
                activeRequestId = requestId,
                lastRequestId = requestId,
                language = request.language,
                lastMessage = "脚本已下发",
                lastFrameType = null,
                lastPayload = null,
                updatedAt = Instant.now().toString(),
            )
            return status
        }
    }

    fun stop(
        request: McuScriptStopRequest,
    ): McuScriptStatusResponse {
        val requestId = UUID.randomUUID().toString()
        sessionService.sendVmFrame(protocolCodec.buildStopFrame(requestId, request))
        synchronized(lock) {
            status = status.copy(
                state = McuScriptRunState.STOPPING,
                lastRequestId = request.requestId ?: status.lastRequestId,
                lastMessage = "已发送停止请求",
                lastFrameType = null,
                lastPayload = null,
                updatedAt = Instant.now().toString(),
            )
            return status
        }
    }

    fun queryStatus(): McuScriptStatusResponse {
        synchronized(lock) {
            return status
        }
    }

    private fun handleIncomingFrame(
        frame: McuVmIncomingFrame,
    ) {
        synchronized(lock) {
            val message = listOfNotNull(
                frame.message?.takeIf { it.isNotBlank() },
                frame.payload?.toString()?.takeIf { it.isNotBlank() && it != "null" },
            ).firstOrNull()

            status = when (frame.type) {
                McuVmFrameTypes.ACK -> status.copy(
                    state = McuScriptRunState.RUNNING,
                    activeRequestId = frame.requestId ?: status.activeRequestId,
                    lastRequestId = frame.requestId ?: status.lastRequestId,
                    lastMessage = message ?: "设备已确认",
                    lastFrameType = frame.type,
                    lastPayload = frame.payload,
                    updatedAt = Instant.now().toString(),
                )

                McuVmFrameTypes.LOG -> status.copy(
                    lastMessage = message ?: "脚本正在输出日志",
                    lastFrameType = frame.type,
                    lastPayload = frame.payload,
                    updatedAt = Instant.now().toString(),
                )

                McuVmFrameTypes.RESULT -> status.copy(
                    state = McuScriptRunState.IDLE,
                    activeRequestId = null,
                    lastRequestId = frame.requestId ?: status.lastRequestId,
                    lastMessage = message ?: "脚本执行完成",
                    lastFrameType = frame.type,
                    lastPayload = frame.payload,
                    updatedAt = Instant.now().toString(),
                )

                McuVmFrameTypes.ERROR -> status.copy(
                    state = McuScriptRunState.ERROR,
                    activeRequestId = null,
                    lastRequestId = frame.requestId ?: status.lastRequestId,
                    lastMessage = message ?: "设备返回错误",
                    lastFrameType = frame.type,
                    lastPayload = frame.payload,
                    updatedAt = Instant.now().toString(),
                )

                McuVmFrameTypes.STATUS -> status.copy(
                    state = resolveState(frame) ?: status.state,
                    activeRequestId = frame.requestId ?: status.activeRequestId,
                    lastRequestId = frame.requestId ?: status.lastRequestId,
                    lastMessage = message ?: "状态已刷新",
                    lastFrameType = frame.type,
                    lastPayload = frame.payload,
                    updatedAt = Instant.now().toString(),
                )

                else -> status.copy(
                    lastMessage = message ?: status.lastMessage,
                    lastFrameType = frame.type,
                    lastPayload = frame.payload ?: status.lastPayload,
                    updatedAt = Instant.now().toString(),
                )
            }
        }
    }

    private fun resolveState(
        frame: McuVmIncomingFrame,
    ): McuScriptRunState? {
        val normalized = runCatching {
            frame.payload
                ?.jsonObject
                ?.get("state")
                ?.jsonPrimitive
                ?.content
                ?.trim()
                ?.uppercase()
        }.getOrNull()
        return normalized?.let { value ->
            runCatching {
                enumValueOf<McuScriptRunState>(value)
            }.getOrNull()
        }
    }
}
