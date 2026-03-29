package site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm

import kotlinx.serialization.json.*
import site.addzero.kcloud.plugins.mcuconsole.*

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
