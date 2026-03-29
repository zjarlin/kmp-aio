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

    @Test
    fun `config center routes create entry list target and preview render`() =
        withEmbeddedDesktopDatasource { testConfig ->
            withConfigCenterBootstrap { _ ->
                testApplication {
                    environment {
                        config = testConfig
                    }
                    application {
                        module()
                    }

                    val createEntryResponse = client.post("/api/config-center/entries") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            """
                            {
                              "key":"demo.value",
                              "namespace":"kcloud",
                              "domain":"SYSTEM",
                              "profile":"default",
                              "valueType":"STRING",
                              "storageMode":"REPO_PLAIN",
                              "value":"demo-1"
                            }
                            """.trimIndent(),
                        )
                    }
                    val createTargetResponse = client.post("/api/config-center/targets") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            """
                            {
                              "name":"Demo Template",
                              "targetKind":"GENERIC_TEXT_TEMPLATE",
                              "outputPath":"",
                              "namespaceFilter":"kcloud",
                              "profile":"default",
                              "templateText":"demo={{demo.value}}",
                              "enabled":true,
                              "sortOrder":50
                            }
                            """.trimIndent(),
                        )
                    }
                    val entriesResponse = client.get("/api/config-center/entries?namespace=kcloud&includeDisabled=true")
                    val targetId = "\"id\":\"([^\"]+)\"".toRegex()
                        .find(createTargetResponse.bodyAsText())
                        ?.groupValues
                        ?.getOrNull(1)
                        .orEmpty()
                    val previewResponse = client.post("/api/config-center/render/$targetId/preview")

                    assertEquals(HttpStatusCode.OK, createEntryResponse.status)
                    assertEquals(HttpStatusCode.OK, createTargetResponse.status)
                    assertEquals(HttpStatusCode.OK, entriesResponse.status)
                    assertEquals(HttpStatusCode.OK, previewResponse.status)
                    assertTrue(targetId.isNotBlank())
                    assertTrue(entriesResponse.bodyAsText().contains("\"key\":\"demo.value\""))
                    assertTrue(previewResponse.bodyAsText().contains("demo=demo-1"))
                }
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
                "datasources.sqlite.driver" to "org.sqlite.JDBC",
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

private inline fun withConfigCenterBootstrap(
    block: (File) -> Unit,
) {
    val tempDatabase = Files.createTempFile("kcloud-config-center-test-", ".sqlite").toFile()
    val previousDbPath = System.getProperty("CONFIG_CENTER_DB_PATH")
    val previousMasterKey = System.getProperty("CONFIG_CENTER_MASTER_KEY")
    val previousAppId = System.getProperty("CONFIG_CENTER_APP_ID")
    System.setProperty("CONFIG_CENTER_DB_PATH", tempDatabase.absolutePath)
    System.setProperty("CONFIG_CENTER_MASTER_KEY", "test-master-key")
    System.setProperty("CONFIG_CENTER_APP_ID", "kcloud")
    try {
        block(tempDatabase)
    } finally {
        restoreSystemProperty("CONFIG_CENTER_DB_PATH", previousDbPath)
        restoreSystemProperty("CONFIG_CENTER_MASTER_KEY", previousMasterKey)
        restoreSystemProperty("CONFIG_CENTER_APP_ID", previousAppId)
        tempDatabase.delete()
    }
}

private fun restoreSystemProperty(
    key: String,
    value: String?,
) {
    if (value == null) {
        System.clearProperty(key)
    } else {
        System.setProperty(key, value)
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
