package site.addzero.kcloud.server

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.koin.dsl.module
import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortConnection
import site.addzero.kcloud.module
import java.nio.file.Files

class KCloudServerApplicationTest {
    @Test
    fun `lists fake serial ports`() = withEmbeddedDesktopDatasource { testConfig ->
        val fakeGateway = ServerFakeSerialPortGateway()
        testApplication {
            environment {
                config = testConfig
            }
            application {
                module(
                    overrideModules = listOf(
                        module {
                            single<SerialPortGateway> { fakeGateway }
                        },
                    ),
                )
            }

            val response = client.get("/api/mcu/ports")
            val body = response.bodyAsText()

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(body.contains("\"portPath\":\"COM9\""))
        }
    }

    @Test
    fun `opens session resets device and executes script`() = withEmbeddedDesktopDatasource { testConfig ->
        val fakeGateway = ServerFakeSerialPortGateway()
        testApplication {
            environment {
                config = testConfig
            }
            application {
                module(
                    overrideModules = listOf(
                        module {
                            single<SerialPortGateway> { fakeGateway }
                        },
                    ),
                )
            }

            val openResponse = client.post("/api/mcu/session/open") {
                contentType(ContentType.Application.Json)
                setBody("""{"portPath":"COM9","baudRate":115200}""")
            }
            val resetResponse = client.post("/api/mcu/session/reset") {
                contentType(ContentType.Application.Json)
                setBody("""{"pulseMs":25}""")
            }
            val scriptResponse = client.post("/api/mcu/script/execute") {
                contentType(ContentType.Application.Json)
                setBody("""{"script":"println(1);","timeoutMs":3000}""")
            }
            Thread.sleep(120)
            val eventsResponse = client.get("/api/mcu/events?afterSeq=0")
            val statusResponse = client.get("/api/mcu/script/status")

            assertEquals(HttpStatusCode.OK, openResponse.status)
            assertEquals(HttpStatusCode.OK, resetResponse.status)
            assertEquals(HttpStatusCode.OK, scriptResponse.status)
            assertEquals(HttpStatusCode.OK, eventsResponse.status)
            assertEquals(HttpStatusCode.OK, statusResponse.status)
            assertTrue(openResponse.bodyAsText().contains("\"isOpen\":true"))
            assertTrue(eventsResponse.bodyAsText().contains("会话已打开"))
            assertTrue(eventsResponse.bodyAsText().contains("设备复位"))
            assertTrue(statusResponse.bodyAsText().contains("\"state\":\"RUNNING\""))
        }
    }
}

private inline fun withEmbeddedDesktopDatasource(
    block: (MapApplicationConfig) -> Unit,
) {
    val tempDatabase = Files.createTempFile("kcloud-server-test-", ".db").toFile()
    val previousEmbeddedFlag = System.getProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY)
    System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
    try {
        block(
            MapApplicationConfig(
                "datasources.sqlite.url" to "jdbc:sqlite:${tempDatabase.absolutePath}",
                "datasources.sqlite.driver" to "org.sqlite.SQLiteDriver",
            ),
        )
    } finally {
        if (previousEmbeddedFlag == null) {
            System.clearProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY)
        } else {
            System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, previousEmbeddedFlag)
        }
        tempDatabase.delete()
    }
}

private const val VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY = "vibepocket.embedded.desktop"

private class ServerFakeSerialPortGateway : SerialPortGateway {
    val openedConnections = mutableListOf<ServerFakeSerialPortConnection>()

    override fun listPorts(): List<McuPortSummary> {
        return listOf(
            McuPortSummary(
                portPath = "COM9",
                portName = "Server Mock Port",
                systemPortName = "COM9",
            ),
        )
    }

    override fun openConnection(
        portPath: String,
        baudRate: Int,
    ): SerialPortConnection {
        return ServerFakeSerialPortConnection(portPath, baudRate).also { connection ->
            openedConnections += connection
        }
    }
}

private class ServerFakeSerialPortConnection(
    override val portPath: String,
    override val baudRate: Int,
) : SerialPortConnection {
    override val portName: String = "Server-Fake-$portPath"

    private val pendingReads = ArrayDeque<ByteArray>()

    override var isOpen: Boolean = true
        private set

    override fun writeUtf8(
        text: String,
    ) {
        val requestId = REQUEST_ID_REGEX.find(text)?.groupValues?.getOrNull(1).orEmpty()
        if (text.contains("\"command\":\"vm.execute\"")) {
            pendingReads += (
                """{"requestId":"$requestId","type":"ack","success":true,"message":"ready"}""" + "\n"
                ).toByteArray()
        }
    }

    override fun writeBytes(
        bytes: ByteArray,
        length: Int,
    ) {
    }

    override fun read(
        buffer: ByteArray,
        timeoutMs: Int,
    ): Int {
        val next = pendingReads.removeFirstOrNull() ?: return 0
        val count = min(buffer.size, next.size)
        next.copyInto(buffer, endIndex = count)
        if (count < next.size) {
            pendingReads.addFirst(next.copyOfRange(count, next.size))
        }
        return count
    }

    override fun setDtr(
        enabled: Boolean,
    ) {
    }

    override fun setRts(
        enabled: Boolean,
    ) {
    }

    override fun close() {
        isOpen = false
    }

    private companion object {
        val REQUEST_ID_REGEX = Regex("\"requestId\":\"([^\"]+)\"")
    }
}
