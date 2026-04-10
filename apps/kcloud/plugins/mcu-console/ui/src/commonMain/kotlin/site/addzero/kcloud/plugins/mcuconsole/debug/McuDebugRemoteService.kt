package site.addzero.kcloud.plugins.mcuconsole.debug

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialPortConfig
import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialPortDescriptor

@Single
/**
 * 提供 MCU 串口日志远程服务。
 *
 * @property httpClient HTTP 客户端。
 */
class McuDebugRemoteService(
    private val httpClient: HttpClient,
) {
    /**
     * 获取当前机器可见串口。
     */
    suspend fun listPorts(): List<McuSerialPortDescriptor> {
        return httpClient.get("/mcu-console/router/ports/list").body()
    }

    /**
     * 连接串口日志 SSE。
     */
    fun streamSerialLogs(
        config: McuSerialPortConfig,
    ): Flow<String> =
        channelFlow {
            httpClient.prepareGet("/mcu-console/router/ports/logs/sse") {
                parameter("portName", config.portName)
                parameter("baudRate", config.baudRate)
                parameter("dataBits", config.dataBits)
                parameter("stopBits", config.stopBits.name)
                parameter("parity", config.parity.name)
                parameter("flowControl", config.flowControl.name)
                parameter("readTimeoutMs", config.readTimeoutMs)
                parameter("writeTimeoutMs", config.writeTimeoutMs)
                parameter("openSafetySleepTimeMs", config.openSafetySleepTimeMs)
            }.execute { response ->
                val channel = response.bodyAsChannel()
                val dataLines = mutableListOf<String>()

                suspend fun flushDataLines() {
                    if (dataLines.isEmpty()) {
                        return
                    }
                    send(dataLines.joinToString("\n"))
                    dataLines.clear()
                }

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    when {
                        line.startsWith("data:") -> {
                            dataLines += line.removePrefix("data:").trimStart()
                        }

                        line.isBlank() -> {
                            flushDataLines()
                        }
                    }
                }
                flushDataLines()
            }
        }
}

/**
 * 生成易读错误消息。
 */
internal fun Throwable.readableMcuDebugMessage(): String =
    when (this) {
        is HttpRequestTimeoutException -> "连接串口日志超时"
        is ResponseException -> "后台返回错误：${response.status}"
        else -> message ?: this::class.simpleName ?: "未知错误"
    }
