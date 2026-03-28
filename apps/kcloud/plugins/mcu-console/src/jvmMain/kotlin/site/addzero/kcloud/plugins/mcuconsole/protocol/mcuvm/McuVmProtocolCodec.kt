package site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStopRequest
import site.addzero.kcloud.plugins.mcuconsole.McuVmCommands
import site.addzero.kcloud.plugins.mcuconsole.McuVmExecutePayload
import site.addzero.kcloud.plugins.mcuconsole.McuVmIncomingFrame
import site.addzero.kcloud.plugins.mcuconsole.McuVmOutgoingFrame

class McuVmProtocolCodec(
    private val json: Json,
) {
    fun encode(
        frame: McuVmOutgoingFrame,
    ): String {
        return json.encodeToString(frame) + "\n"
    }

    fun decodeOrNull(
        rawLine: String,
    ): McuVmIncomingFrame? {
        return runCatching {
            json.decodeFromString<McuVmIncomingFrame>(rawLine)
        }.getOrNull()
    }

    fun buildExecuteFrame(
        requestId: String,
        request: McuScriptExecuteRequest,
    ): McuVmOutgoingFrame {
        return McuVmOutgoingFrame(
            requestId = requestId,
            command = McuVmCommands.EXECUTE,
            payload = json.encodeToJsonElement(
                McuVmExecutePayload(
                    language = request.language,
                    script = request.script,
                    timeoutMs = request.timeoutMs,
                ),
            ),
        )
    }

    fun buildStopFrame(
        requestId: String,
        request: McuScriptStopRequest,
    ): McuVmOutgoingFrame {
        return McuVmOutgoingFrame(
            requestId = requestId,
            command = McuVmCommands.STOP,
            payload = buildJsonObject {
                request.requestId?.takeIf { it.isNotBlank() }?.let { targetRequestId ->
                    put("targetRequestId", targetRequestId)
                }
            },
        )
    }

    fun buildStatusFrame(
        requestId: String,
    ): McuVmOutgoingFrame {
        return buildCommandFrame(requestId = requestId, command = McuVmCommands.STATUS)
    }

    fun buildPingFrame(
        requestId: String,
    ): McuVmOutgoingFrame {
        return buildCommandFrame(requestId = requestId, command = McuVmCommands.PING)
    }

    fun buildCommandFrame(
        requestId: String,
        command: String,
        payload: JsonElement = JsonObject(emptyMap()),
    ): McuVmOutgoingFrame {
        return McuVmOutgoingFrame(
            requestId = requestId,
            command = command,
            payload = payload,
        )
    }

    fun extractFrames(
        buffer: String,
    ): DecodedFrameChunk {
        if (buffer.isEmpty()) {
            return DecodedFrameChunk()
        }
        val normalized = buffer.replace("\r\n", "\n")
        val lines = normalized.split('\n')
        val frames = lines.dropLast(1).mapNotNull { line ->
            line.trim().takeIf { it.isNotEmpty() }
        }
        val remainder = lines.lastOrNull().orEmpty()
        return DecodedFrameChunk(
            frames = frames,
            remainder = remainder,
        )
    }
}

data class DecodedFrameChunk(
    val frames: List<String> = emptyList(),
    val remainder: String = "",
)
