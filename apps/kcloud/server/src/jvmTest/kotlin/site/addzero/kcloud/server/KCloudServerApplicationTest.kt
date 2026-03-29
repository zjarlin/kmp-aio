package site.addzero.kcloud.server

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
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
import java.io.File
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
    fun `runtime routes list bundles and ensure builtin runtime`() = withEmbeddedDesktopDatasource { testConfig ->
        val fakeGateway = ServerFakeSerialPortGateway(runtimeInstalledInitially = false)
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
            val bundlesResponse = client.get("/api/mcu/runtime/bundles")
            val ensureResponse = client.post("/api/mcu/runtime/ensure") {
                contentType(ContentType.Application.Json)
                setBody("""{"bundleId":"rhai-default-generic"}""")
            }
            val runtimeStatusResponse = client.get("/api/mcu/runtime/status")

            assertEquals(HttpStatusCode.OK, openResponse.status)
            assertEquals(HttpStatusCode.OK, bundlesResponse.status)
            assertEquals(HttpStatusCode.OK, ensureResponse.status)
            assertEquals(HttpStatusCode.OK, runtimeStatusResponse.status)
            assertTrue(bundlesResponse.bodyAsText().contains("\"bundleId\":\"rhai-default-generic\""))
            assertTrue(ensureResponse.bodyAsText().contains("\"state\":\"READY\""))
            assertTrue(runtimeStatusResponse.bodyAsText().contains("\"bundleId\":\"rhai-default-generic\""))
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

    @Test
    fun `system user profile routes read and update current profile`() =
        withEmbeddedDesktopDatasource { testConfig ->
            testApplication {
                environment {
                    config = testConfig
                }
                application {
                    module()
                }

                val readResponse = client.get("/api/system/user/profile")
                val saveResponse = client.put("/api/system/user/profile") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                          "displayName":"桌面测试账号",
                          "email":"desktop@example.com",
                          "avatarLabel":"DT",
                          "locale":"zh-CN",
                          "timeZone":"Asia/Shanghai"
                        }
                        """.trimIndent(),
                    )
                }

                assertEquals(HttpStatusCode.OK, readResponse.status)
                assertEquals(HttpStatusCode.OK, saveResponse.status)
                assertTrue(readResponse.bodyAsText().contains("\"accountKey\":\"desktop-user\""))
                assertTrue(saveResponse.bodyAsText().contains("\"displayName\":\"桌面测试账号\""))
            }
        }

    @Test
    fun `system ai chat routes create session persist messages and delete`() =
        withEmbeddedDesktopDatasource { testConfig ->
            testApplication {
                environment {
                    config = testConfig
                }
                application {
                    module()
                }

                val createSessionResponse = client.post("/api/system/ai-chat/sessions") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"title":"测试对话"}""")
                }
                val sessionId = "\"id\":(\\d+)".toRegex()
                    .find(createSessionResponse.bodyAsText())
                    ?.groupValues
                    ?.getOrNull(1)
                    .orEmpty()
                val sendMessageResponse = client.post("/api/system/ai-chat/sessions/$sessionId/messages") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"content":"先写一条消息"}""")
                }
                val listMessagesResponse = client.get("/api/system/ai-chat/sessions/$sessionId/messages")
                val deleteSessionResponse = client.delete("/api/system/ai-chat/sessions/$sessionId")

                assertEquals(HttpStatusCode.OK, createSessionResponse.status)
                assertEquals(HttpStatusCode.OK, sendMessageResponse.status)
                assertEquals(HttpStatusCode.OK, listMessagesResponse.status)
                assertEquals(HttpStatusCode.OK, deleteSessionResponse.status)
                assertTrue(sessionId.isNotBlank())
                assertTrue(sendMessageResponse.bodyAsText().contains("模型提供方尚未接通"))
                assertTrue(listMessagesResponse.bodyAsText().contains("\"role\":\"assistant\""))
            }
        }

    @Test
    fun `system knowledge base routes create update and delete space document`() =
        withEmbeddedDesktopDatasource { testConfig ->
            testApplication {
                environment {
                    config = testConfig
                }
                application {
                    module()
                }

                val createSpaceResponse = client.post("/api/system/knowledge-base/spaces") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"name":"工程知识","description":"第一批文档"}""")
                }
                val spaceId = "\"id\":(\\d+)".toRegex()
                    .find(createSpaceResponse.bodyAsText())
                    ?.groupValues
                    ?.getOrNull(1)
                    .orEmpty()
                val createDocumentResponse = client.post("/api/system/knowledge-base/spaces/$spaceId/documents") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"title":"接入说明","content":"先记正文"}""")
                }
                val documentId = "\"id\":(\\d+)".toRegex()
                    .find(createDocumentResponse.bodyAsText())
                    ?.groupValues
                    ?.getOrNull(1)
                    .orEmpty()
                val updateDocumentResponse = client.put("/api/system/knowledge-base/documents/$documentId") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"title":"接入说明 v2","content":"补充正文"}""")
                }
                val listDocumentsResponse = client.get("/api/system/knowledge-base/spaces/$spaceId/documents")
                val deleteDocumentResponse = client.delete("/api/system/knowledge-base/documents/$documentId")
                val deleteSpaceResponse = client.delete("/api/system/knowledge-base/spaces/$spaceId")

                assertEquals(HttpStatusCode.OK, createSpaceResponse.status)
                assertEquals(HttpStatusCode.OK, createDocumentResponse.status)
                assertEquals(HttpStatusCode.OK, updateDocumentResponse.status)
                assertEquals(HttpStatusCode.OK, listDocumentsResponse.status)
                assertEquals(HttpStatusCode.OK, deleteDocumentResponse.status)
                assertEquals(HttpStatusCode.OK, deleteSpaceResponse.status)
                assertTrue(spaceId.isNotBlank())
                assertTrue(documentId.isNotBlank())
                assertTrue(updateDocumentResponse.bodyAsText().contains("接入说明 v2"))
                assertTrue(listDocumentsResponse.bodyAsText().contains("\"title\":\"接入说明 v2\""))
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
                "datasources.sqlite.enabled" to "true",
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
    constructor(
        runtimeInstalledInitially: Boolean = true,
    ) {
        runtimeInstalled = runtimeInstalledInitially
    }

    val openedConnections = mutableListOf<ServerFakeSerialPortConnection>()

    @Volatile
    var runtimeInstalled: Boolean = true

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
        return ServerFakeSerialPortConnection(this, portPath, baudRate).also { connection ->
            openedConnections += connection
        }
    }
}

private class ServerFakeSerialPortConnection(
    private val gateway: ServerFakeSerialPortGateway,
    override val portPath: String,
    override val baudRate: Int,
) : SerialPortConnection {
    override val portName: String = "Server-Fake-$portPath"

    private val pendingReads = ArrayDeque<ByteArray>()
    private var flashMode = false

    override var isOpen: Boolean = true
        private set

    override fun writeUtf8(
        text: String,
    ) {
        val requestId = REQUEST_ID_REGEX.find(text)?.groupValues?.getOrNull(1).orEmpty()
        when {
            gateway.runtimeInstalled && text.contains("\"command\":\"vm.execute\"") -> {
                pendingReads += (
                    """{"requestId":"$requestId","type":"ack","success":true,"message":"ready"}""" + "\n"
                    ).toByteArray()
            }

            gateway.runtimeInstalled && text.contains("\"command\":\"vm.ping\"") -> {
                pendingReads += (
                    """{"requestId":"$requestId","type":"ack","success":true,"message":"runtime-ready","payload":{"runtime":"rhai"}}""" + "\n"
                    ).toByteArray()
            }

            gateway.runtimeInstalled && text.contains("\"command\":\"vm.status\"") -> {
                pendingReads += (
                    """{"requestId":"$requestId","type":"status","success":true,"message":"runtime-ready","payload":{"state":"IDLE","runtime":"rhai"}}""" + "\n"
                    ).toByteArray()
            }

            text == "START_FLASH\r\n" -> {
                flashMode = true
            }

            text == "DONE\r\n" -> {
                gateway.runtimeInstalled = true
                flashMode = false
                pendingReads += "SUCCESS".toByteArray()
            }
        }
    }

    override fun writeBytes(
        bytes: ByteArray,
        length: Int,
    ) {
        if (flashMode) {
            pendingReads += (
                "ACK"
                ).toByteArray()
        }
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
