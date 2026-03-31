package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoPollRequest
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoResponse
import site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
import site.addzero.kcloud.plugins.mcuconsole.McuVmFrameTypes
import site.addzero.kcloud.plugins.mcuconsole.McuVmIncomingFrame
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import java.time.Instant
import java.util.UUID

/**
 * 负责通过 MCU VM 协议主动轮询设备基础信息。
 */
@Single
class McuDeviceInfoService(
    private val sessionService: McuConsoleSessionService,
    private val protocolCodec: McuVmProtocolCodec,
) {
    private val lock = Any()
    private var status = McuDeviceInfoResponse()

    /**
     * 返回最近一次设备信息轮询结果。
     */
    fun getStatus(): McuDeviceInfoResponse {
        synchronized(lock) {
            return status
        }
    }

    /**
     * 主动向当前串口会话发送设备信息查询命令，并等待匹配响应。
     */
    suspend fun pollDeviceInfo(
        request: McuDeviceInfoPollRequest,
    ): McuDeviceInfoResponse {
        val snapshot = sessionService.getSessionSnapshot()
        if (!snapshot.isOpen || snapshot.portPath.isNullOrBlank()) {
            return updateStatus(
                buildFailureResponse(
                    requestId = null,
                    portPath = snapshot.portPath,
                    message = "请先打开串口会话后再轮询设备信息",
                ),
            )
        }

        val requestId = UUID.randomUUID().toString()
        val frames = Channel<McuVmIncomingFrame>(capacity = Channel.UNLIMITED)
        val registration = sessionService.registerFrameListener { frame ->
            frames.trySendBlocking(frame)
        }
        return try {
            sessionService.sendVmFrame(protocolCodec.buildDeviceInfoFrame(requestId))
            val matchedFrame = awaitDeviceInfoFrame(
                requestId = requestId,
                frames = frames,
                timeoutMs = request.timeoutMs.coerceAtLeast(100).toLong(),
            )
            if (matchedFrame == null) {
                updateStatus(
                    buildFailureResponse(
                        requestId = requestId,
                        portPath = snapshot.portPath,
                        message = "设备未在 ${request.timeoutMs.coerceAtLeast(100)}ms 内返回设备信息",
                    ),
                )
            } else {
                updateStatus(buildResponse(frame = matchedFrame, snapshot = snapshot, requestId = requestId))
            }
        } catch (throwable: Throwable) {
            updateStatus(
                buildFailureResponse(
                    requestId = requestId,
                    portPath = snapshot.portPath,
                    message = throwable.message ?: "设备信息轮询失败",
                ),
            )
        } finally {
            registration.close()
            frames.close()
        }
    }

    /**
     * 在限定时间内等待与当前请求匹配的设备信息帧。
     */
    private suspend fun awaitDeviceInfoFrame(
        requestId: String,
        frames: Channel<McuVmIncomingFrame>,
        timeoutMs: Long,
    ): McuVmIncomingFrame? {
        return withTimeoutOrNull(timeoutMs) {
            var matched: McuVmIncomingFrame? = null
            while (matched == null) {
                val frame = frames.receive()
                if (frame.requestId != null && frame.requestId != requestId) {
                    continue
                }
                if (!shouldAcceptFrame(frame)) {
                    continue
                }
                matched = frame
            }
            matched
        }
    }

    /**
     * 过滤掉只表示“已收到”但尚未携带设备信息的 ACK 帧。
     */
    private fun shouldAcceptFrame(
        frame: McuVmIncomingFrame,
    ): Boolean {
        if (frame.type == McuVmFrameTypes.LOG) {
            return false
        }
        if (frame.type == McuVmFrameTypes.ACK && !containsDeviceInfoPayload(frame.payload)) {
            return false
        }
        return true
    }

    /**
     * 判断当前帧负载里是否已经包含可映射的设备信息字段。
     */
    private fun containsDeviceInfoPayload(
        payload: JsonElement?,
    ): Boolean {
        val keys = setOf(
            "chipModel",
            "chip",
            "cpuModel",
            "cpu",
            "cpuFrequencyHz",
            "xtalFrequencyHz",
            "xtalMhz",
            "macAddress",
            "mac",
            "sdkVersion",
            "firmwareVersion",
        )
        return collectPayloadSources(payload)
            .any { source -> source.keys.any(keys::contains) }
    }

    /**
     * 把设备回包映射为稳定的后端响应结构。
     */
    private fun buildResponse(
        frame: McuVmIncomingFrame,
        snapshot: McuSessionSnapshot,
        requestId: String,
    ): McuDeviceInfoResponse {
        val sources = collectPayloadSources(frame.payload)
        return McuDeviceInfoResponse(
            success = frame.success,
            requestId = frame.requestId ?: requestId,
            portPath = snapshot.portPath,
            runtime = sources.readString("runtime", "runtimeName", "platform"),
            boardName = sources.readString("boardName", "board", "machine"),
            chipModel = sources.readString("chipModel", "chip", "chipName", "socModel"),
            chipRevision = sources.readString("chipRevision", "revision", "chipRev"),
            cpuModel = sources.readString("cpuModel", "cpu", "arch"),
            cpuCores = sources.readInt("cpuCores", "cores", "coreCount"),
            cpuFrequencyHz = sources.readInt("cpuFrequencyHz", "cpuHz", "cpuFreqHz", "cpu_freq_hz")
                ?: sources.readMegahertz("cpuFrequencyMhz", "cpuMhz", "cpu_freq_mhz"),
            xtalFrequencyHz = sources.readInt("xtalFrequencyHz", "xtalHz", "xtal_freq_hz")
                ?: sources.readMegahertz("xtalFrequencyMhz", "xtalMhz", "xtal_freq_mhz"),
            macAddress = sources.readString("macAddress", "mac", "staMac", "wifiMac"),
            sdkVersion = sources.readString("sdkVersion", "sdk", "idfVersion"),
            firmwareVersion = sources.readString("firmwareVersion", "firmware", "version"),
            flashSizeBytes = sources.readLong("flashSizeBytes", "flashSize", "flash_size"),
            heapFreeBytes = sources.readLong("heapFreeBytes", "freeHeap", "heapFree", "heap_free"),
            heapTotalBytes = sources.readLong("heapTotalBytes", "totalHeap", "heapTotal", "heap_total"),
            lastMessage = frame.message ?: if (frame.success) "设备信息已刷新" else "设备返回失败帧",
            updatedAt = Instant.now().toString(),
            rawPayload = frame.payload,
        )
    }

    /**
     * 构建统一的失败响应，避免把轮询失败直接暴露成接口异常。
     */
    private fun buildFailureResponse(
        requestId: String?,
        portPath: String?,
        message: String,
    ): McuDeviceInfoResponse {
        return McuDeviceInfoResponse(
            success = false,
            requestId = requestId,
            portPath = portPath,
            lastMessage = message,
            updatedAt = Instant.now().toString(),
        )
    }

    /**
     * 更新最近一次轮询状态并返回新值。
     */
    private fun updateStatus(
        next: McuDeviceInfoResponse,
    ): McuDeviceInfoResponse {
        synchronized(lock) {
            status = next
            return status
        }
    }

    /**
     * 把可能嵌套在不同 key 下的设备信息对象全部收集出来，供字段别名回填。
     */
    private fun collectPayloadSources(
        payload: JsonElement?,
    ): List<JsonObject> {
        val root = payload.asJsonObjectOrNull() ?: return emptyList()
        val nested = listOf("deviceInfo", "device", "info", "system")
            .mapNotNull { key -> root[key].asJsonObjectOrNull() }
        return buildList {
            add(root)
            addAll(nested)
        }
    }
}

/**
 * 把 `JsonElement` 安全转换为 `JsonObject`。
 */
private fun JsonElement?.asJsonObjectOrNull(): JsonObject? {
    return runCatching {
        this?.jsonObject
    }.getOrNull()
}

/**
 * 读取首个命中的字符串字段。
 */
private fun List<JsonObject>.readString(
    vararg names: String,
): String? {
    return names.firstNotNullOfOrNull { name ->
        this.asSequence()
            .mapNotNull { source ->
                runCatching {
                    source[name]?.jsonPrimitive?.content?.trim()
                }.getOrNull()
            }
            .firstOrNull { value -> value.isNotBlank() }
    }
}

/**
 * 读取首个命中的整数值字段。
 */
private fun List<JsonObject>.readInt(
    vararg names: String,
): Int? {
    return names.firstNotNullOfOrNull { name ->
        this.asSequence()
            .mapNotNull { source ->
                runCatching {
                    source[name]?.jsonPrimitive?.content?.trim()?.replace("_", "")?.toInt()
                }.getOrNull()
            }
            .firstOrNull()
    }
}

/**
 * 读取首个命中的长整数值字段。
 */
private fun List<JsonObject>.readLong(
    vararg names: String,
): Long? {
    return names.firstNotNullOfOrNull { name ->
        this.asSequence()
            .mapNotNull { source ->
                runCatching {
                    source[name]?.jsonPrimitive?.content?.trim()?.replace("_", "")?.toLong()
                }.getOrNull()
            }
            .firstOrNull()
    }
}

/**
 * 读取 MHz 单位字段并统一换算成 Hz。
 */
private fun List<JsonObject>.readMegahertz(
    vararg names: String,
): Int? {
    return readInt(*names)?.let { value ->
        value * 1_000_000
    }
}
