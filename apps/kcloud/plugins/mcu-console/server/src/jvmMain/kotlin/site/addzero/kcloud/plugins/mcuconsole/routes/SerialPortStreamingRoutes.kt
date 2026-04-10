package site.addzero.kcloud.plugins.mcuconsole.routes

import io.ktor.http.ContentType
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import site.addzero.serial.SerialFlowControl
import site.addzero.serial.SerialParity
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialPortTool
import site.addzero.serial.SerialStopBits
import kotlin.text.toIntOrNull

/**
 * 注册 MCU 串口日志 SSE 路由。
 *
 * 这里使用手写 Ktor 路由而不是 Spring 注解 controller，
 * 是为了避免把 `ApplicationCall`/SSE 低层细节带进 controller2api 生成链路。
 */
fun Route.registerMcuConsoleSerialStreamingRoutes() {
    get("/mcu-console/router/ports/logs/sse") {
        val serialConfig =
            SerialPortConfig(
                portName = call.requireQueryParameter("portName"),
                baudRate = call.queryParameter("baudRate")?.toIntOrNull() ?: 115200,
                dataBits = call.queryParameter("dataBits")?.toIntOrNull() ?: 8,
                stopBits = call.queryParameter("stopBits")?.toSerialStopBits() ?: SerialStopBits.ONE,
                parity = call.queryParameter("parity")?.toSerialParity() ?: SerialParity.NONE,
                flowControl = call.queryParameter("flowControl")?.toSerialFlowControl() ?: SerialFlowControl.NONE,
                readTimeoutMs = call.queryParameter("readTimeoutMs")?.toIntOrNull() ?: 200,
                writeTimeoutMs = call.queryParameter("writeTimeoutMs")?.toIntOrNull() ?: 1_000,
                openSafetySleepTimeMs = call.queryParameter("openSafetySleepTimeMs")?.toIntOrNull() ?: 0,
            )
        val heartbeatIntervalMs = call.queryParameter("heartbeatIntervalMs")?.toLongOrNull() ?: 15_000
        val pollIntervalMs = call.queryParameter("pollIntervalMs")?.toLongOrNull() ?: 100

        call.respondTextWriter(contentType = ContentType.Text.EventStream) {
            SerialPortTool.open(serialConfig).use { connection ->
                var lastEmissionTime = System.currentTimeMillis()
                val pending = StringBuilder()
                while (currentCoroutineContext().isActive && connection.isOpen) {
                    val bytes = connection.readAvailable()
                    if (bytes.isNotEmpty()) {
                        pending.append(bytes.decodeToString())
                        val lines = drainCompletedLines(pending)
                        for (line in lines) {
                            lastEmissionTime = System.currentTimeMillis()
                            write(line.toSseFrame())
                            flush()
                        }
                        continue
                    }

                    if (heartbeatIntervalMs > 0 && System.currentTimeMillis() - lastEmissionTime >= heartbeatIntervalMs) {
                        lastEmissionTime = System.currentTimeMillis()
                        write(": keep-alive\n\n")
                        flush()
                        continue
                    }

                    delay(pollIntervalMs)
                }

                if (pending.isNotEmpty()) {
                    write(pending.toString().toSseFrame())
                    flush()
                }
            }
        }
    }
}

/**
 * 读取必填 query 参数。
 */
private fun io.ktor.server.application.ApplicationCall.requireQueryParameter(name: String): String =
    queryParameter(name)?.takeIf { it.isNotBlank() }
        ?: error("缺少必填参数：$name")

/**
 * 读取可选 query 参数。
 */
private fun io.ktor.server.application.ApplicationCall.queryParameter(name: String): String? =
    request.queryParameters[name]

/**
 * 转换停止位参数。
 */
private fun String.toSerialStopBits(): SerialStopBits =
    when (uppercase()) {
        "ONE" -> SerialStopBits.ONE
        "ONE_POINT_FIVE" -> SerialStopBits.ONE_POINT_FIVE
        "TWO" -> SerialStopBits.TWO
        else -> error("不支持的 stopBits：$this")
    }

/**
 * 转换校验位参数。
 */
private fun String.toSerialParity(): SerialParity =
    when (uppercase()) {
        "NONE" -> SerialParity.NONE
        "EVEN" -> SerialParity.EVEN
        "ODD" -> SerialParity.ODD
        "MARK" -> SerialParity.MARK
        "SPACE" -> SerialParity.SPACE
        else -> error("不支持的 parity：$this")
    }

/**
 * 转换流控参数。
 */
private fun String.toSerialFlowControl(): SerialFlowControl =
    when (uppercase()) {
        "NONE" -> SerialFlowControl.NONE
        "RTS_CTS" -> SerialFlowControl.RTS_CTS
        "DTR_DSR" -> SerialFlowControl.DTR_DSR
        "XON_XOFF" -> SerialFlowControl.XON_XOFF
        else -> error("不支持的 flowControl：$this")
    }

/**
 * 把文本编码成 SSE 帧。
 */
private fun String.toSseFrame(): String {
    val normalized = replace("\r\n", "\n").replace('\r', '\n')
    return buildString {
        normalized.split('\n').forEach { line ->
            append("data: ")
            append(line)
            append('\n')
        }
        append('\n')
    }
}

/**
 * 从待处理缓冲区提取完整日志行。
 */
private fun drainCompletedLines(
    pending: StringBuilder,
): List<String> {
    val lines = mutableListOf<String>()
    while (true) {
        val newlineIndex = pending.indexOf("\n")
        if (newlineIndex < 0) {
            return lines
        }
        lines += pending.substring(0, newlineIndex).removeSuffix("\r")
        pending.delete(0, newlineIndex + 1)
    }
}
