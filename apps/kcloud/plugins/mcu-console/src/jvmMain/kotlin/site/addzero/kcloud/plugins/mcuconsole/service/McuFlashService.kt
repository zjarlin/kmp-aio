package site.addzero.kcloud.plugins.mcuconsole.service

import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import site.addzero.kcloud.plugins.mcuconsole.McuEventKind
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRunState
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway

class McuFlashService(
    private val gateway: SerialPortGateway,
    private val sessionService: McuConsoleSessionService,
) {
    private val lock = Any()
    private var status = McuFlashStatusResponse()

    suspend fun flash(
        request: McuFlashRequest,
    ): McuFlashStatusResponse = withContext(Dispatchers.IO) {
        val target = resolveTarget(request)
        val firmware = resolveFirmwareFile(request.firmwarePath)

        updateStatus(
            state = McuFlashRunState.RUNNING,
            portPath = target.portPath,
            baudRate = target.baudRate,
            firmwarePath = firmware.absolutePath,
            bytesSent = 0,
            totalBytes = firmware.length().toInt(),
            lastMessage = "烧录开始",
        )

        sessionService.closeSession("烧录前关闭当前串口会话")
        sessionService.appendEvent(
            kind = McuEventKind.FLASH,
            title = "开始烧录",
            message = "${target.portPath} <- ${firmware.name}",
        )

        try {
            gateway.openConnection(
                portPath = target.portPath,
                baudRate = target.baudRate,
            ).use { connection ->
                connection.writeUtf8("START_FLASH\r\n")
                sessionService.appendEvent(
                    kind = McuEventKind.FLASH,
                    title = "烧录握手",
                    message = "已发送 START_FLASH",
                )
                Thread.sleep(500)

                val bytes = firmware.readBytes()
                val progressStep = (bytes.size / 10).coerceAtLeast(1)
                bytes.forEachIndexed { index, byte ->
                    connection.writeBytes(byteArrayOf(byte), 1)
                    readUntilContains(
                        portPath = target.portPath,
                        readBlock = { buffer -> connection.read(buffer, timeoutMs = 1200) },
                        expected = "ACK",
                        timeoutMs = 2_000,
                    )
                    val sent = index + 1
                    updateStatus(
                        state = McuFlashRunState.RUNNING,
                        portPath = target.portPath,
                        baudRate = target.baudRate,
                        firmwarePath = firmware.absolutePath,
                        bytesSent = sent,
                        totalBytes = bytes.size,
                        lastMessage = "烧录中 $sent/${bytes.size}",
                    )
                    if (sent == bytes.size || sent % progressStep == 0) {
                        sessionService.appendEvent(
                            kind = McuEventKind.FLASH,
                            title = "烧录进度",
                            message = "$sent / ${bytes.size}",
                        )
                    }
                }

                connection.writeUtf8("DONE\r\n")
                val response = readUntilContains(
                    portPath = target.portPath,
                    readBlock = { buffer -> connection.read(buffer, timeoutMs = 1200) },
                    expected = "SUCCESS",
                    timeoutMs = 3_000,
                )
                check(response.contains("SUCCESS")) { "烧录失败: 未收到 SUCCESS" }
            }

            sessionService.appendEvent(
                kind = McuEventKind.FLASH,
                title = "烧录完成",
                message = firmware.name,
            )
            updateStatus(
                state = McuFlashRunState.SUCCESS,
                portPath = target.portPath,
                baudRate = target.baudRate,
                firmwarePath = firmware.absolutePath,
                bytesSent = firmware.length().toInt(),
                totalBytes = firmware.length().toInt(),
                lastMessage = "烧录完成",
            )
            return@withContext getStatus()
        } catch (error: Throwable) {
            sessionService.appendEvent(
                kind = McuEventKind.ERROR,
                title = "烧录失败",
                message = error.message ?: "未知错误",
            )
            updateStatus(
                state = McuFlashRunState.ERROR,
                portPath = target.portPath,
                baudRate = target.baudRate,
                firmwarePath = firmware.absolutePath,
                bytesSent = status.bytesSent,
                totalBytes = firmware.length().toInt(),
                lastMessage = error.message ?: "烧录失败",
            )
            throw error
        }
    }

    fun getStatus(): McuFlashStatusResponse {
        synchronized(lock) {
            return status
        }
    }

    private fun updateStatus(
        state: McuFlashRunState,
        portPath: String,
        baudRate: Int,
        firmwarePath: String,
        bytesSent: Int,
        totalBytes: Int,
        lastMessage: String,
    ) {
        synchronized(lock) {
            status = McuFlashStatusResponse(
                state = state,
                portPath = portPath,
                baudRate = baudRate,
                firmwarePath = firmwarePath,
                bytesSent = bytesSent,
                totalBytes = totalBytes,
                lastMessage = lastMessage,
                updatedAt = Instant.now().toString(),
            )
        }
    }

    private fun resolveTarget(
        request: McuFlashRequest,
    ): FlashTarget {
        val session = sessionService.getSessionSnapshot()
        val portPath = request.portPath?.takeIf { it.isNotBlank() }
            ?: session.portPath
            ?: throw IllegalArgumentException("请先选择串口")
        val baudRate = request.baudRate
            ?: session.baudRate
        return FlashTarget(
            portPath = portPath,
            baudRate = baudRate,
        )
    }

    private fun resolveFirmwareFile(
        firmwarePath: String,
    ): File {
        val normalized = firmwarePath.trim()
        require(normalized.isNotBlank()) { "firmwarePath is required" }
        val file = File(normalized)
        require(file.isFile) { "固件不存在: $normalized" }
        return file
    }

    private fun readUntilContains(
        portPath: String,
        readBlock: (ByteArray) -> Int,
        expected: String,
        timeoutMs: Int,
    ): String {
        val startedAt = System.currentTimeMillis()
        val buffer = ByteArray(256)
        val builder = StringBuilder()
        while (System.currentTimeMillis() - startedAt < timeoutMs) {
            val count = readBlock(buffer)
            if (count <= 0) {
                continue
            }
            builder.append(buffer.decodeToString(0, count))
            val content = builder.toString()
            if (content.contains(expected)) {
                return content
            }
        }
        throw IllegalStateException("串口 $portPath 未收到 $expected")
    }

    private data class FlashTarget(
        val portPath: String,
        val baudRate: Int,
    )
}
