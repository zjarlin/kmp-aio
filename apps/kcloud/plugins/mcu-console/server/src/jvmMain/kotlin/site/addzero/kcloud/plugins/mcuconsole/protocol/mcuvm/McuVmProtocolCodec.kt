package site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm

import kotlinx.serialization.json.*
import site.addzero.kcloud.plugins.mcuconsole.*

class McuVmProtocolCodec(
    private val json: Json,
) {
    /**
     * 把 MCU VM 出站帧编码为单行 JSON，便于串口逐行发送。
     */
    fun encode(
        frame: McuVmOutgoingFrame,
    ): String {
        return json.encodeToString(frame) + "\n"
    }

    /**
     * 尝试把串口收到的单行文本解析为 MCU VM 入站帧。
     */
    fun decodeOrNull(
        rawLine: String,
    ): McuVmIncomingFrame? {
        return runCatching {
            json.decodeFromString<McuVmIncomingFrame>(rawLine)
        }.getOrNull()
    }

    /**
     * 构建脚本执行命令帧。
     */
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

    /**
     * 构建脚本停止命令帧。
     */
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

    /**
     * 构建运行时状态查询命令帧。
     */
    fun buildStatusFrame(
        requestId: String,
    ): McuVmOutgoingFrame {
        return buildCommandFrame(requestId = requestId, command = McuVmCommands.STATUS)
    }

    /**
     * 构建运行时存活探测命令帧。
     */
    fun buildPingFrame(
        requestId: String,
    ): McuVmOutgoingFrame {
        return buildCommandFrame(requestId = requestId, command = McuVmCommands.PING)
    }

    /**
     * 构建设备基础信息查询命令帧。
     */
    fun buildDeviceInfoFrame(
        requestId: String,
    ): McuVmOutgoingFrame {
        return buildCommandFrame(requestId = requestId, command = McuVmCommands.DEVICE_INFO)
    }

    /**
     * 构建通用命令帧。
     */
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

    /**
     * 从串口缓冲中切分出完整帧和残余半包。
     */
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

/**
 * 一次串口切帧后的结果块。
 */
data class DecodedFrameChunk(
    val frames: List<String> = emptyList(),
    val remainder: String = "",
)
