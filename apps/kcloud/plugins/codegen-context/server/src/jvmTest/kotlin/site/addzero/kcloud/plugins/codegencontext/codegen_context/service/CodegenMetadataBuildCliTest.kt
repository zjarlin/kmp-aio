package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class CodegenMetadataBuildCliTest {

    @Test
    fun shouldFallbackRequestedTransportsToFirstAvailableMetadataSnapshot() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val saved =
                fixture.service.saveContext(
                    baseContextRequest(
                        protocolTemplateId = template.id,
                        code = "CTX_METADATA_BUILD",
                    ),
                )
            val workspaceRoot = createGeneratorWorkspace()

            try {
                withRepoRoot(workspaceRoot) {
                    fixture.service.generateContracts(saved.id!!)
                }

                val serverOutputRoot = workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin")
                val sharedOutputRoot = workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/shared/generated/commonMain/kotlin")
                val metadataRoot = workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/generated/modbus-metadata")

                main(
                    arrayOf(
                        "--driver-class",
                        "com.mysql.cj.jdbc.Driver",
                        "--jdbc-url",
                        fixture.dataSource.connection.use { connection -> connection.metaData.url },
                        "--username",
                        "root",
                        "--password",
                        "test123456",
                        "--query",
                        """
                        SELECT payload
                        FROM codegen_context_modbus_contract
                        WHERE consumer_target = 'MCU_CONSOLE'
                          AND enabled = 1
                          AND selected = 1
                          AND transport = '${'$'}{transport}'
                        ORDER BY updated_at DESC
                        LIMIT 1
                        """.trimIndent(),
                        "--json-column",
                        "payload",
                        "--transport",
                        "rtu,tcp,mqtt",
                        "--skip-missing-transports",
                        "true",
                        "--workspace-root",
                        workspaceRoot.toString(),
                        "--server-output-root",
                        serverOutputRoot.toString(),
                        "--shared-output-root",
                        sharedOutputRoot.toString(),
                        "--gateway-output-root",
                        serverOutputRoot.toString(),
                        "--c-output-root",
                        metadataRoot.resolve("c").toString(),
                        "--markdown-output-root",
                        metadataRoot.resolve("markdown").toString(),
                    ),
                )

                assertTrue(
                    serverOutputRoot
                        .resolve("site/addzero/kcloud/plugins/mcuconsole/modbus/device/DeviceApi.kt")
                        .exists(),
                )
                assertTrue(
                    serverOutputRoot
                        .resolve("site/addzero/esp32_host_computer/generated/modbus/rtu/GeneratedModbusRtu.kt")
                        .exists(),
                )
                assertTrue(
                    serverOutputRoot
                        .resolve("site/addzero/esp32_host_computer/generated/modbus/tcp/GeneratedModbusTcp.kt")
                        .exists(),
                )
                assertTrue(
                    serverOutputRoot
                        .resolve("site/addzero/esp32_host_computer/generated/modbus/mqtt/GeneratedModbusMqtt.kt")
                        .exists(),
                )
                assertTrue(
                    metadataRoot
                        .resolve("c/generated/modbus/rtu/modbus_rtu_dispatch.c")
                        .exists(),
                )
                assertTrue(
                    metadataRoot
                        .resolve("c/generated/modbus/tcp/modbus_tcp_dispatch.c")
                        .exists(),
                )
                assertTrue(
                    metadataRoot
                        .resolve("c/generated/modbus/mqtt/modbus_mqtt_dispatch.c")
                        .exists(),
                )

                val markdownFiles = Files.walk(metadataRoot.resolve("markdown")).use { paths -> paths.toList() }
                assertTrue(markdownFiles.any { path -> path.fileName.toString().endsWith(".rtu.protocol.md") })
                assertTrue(markdownFiles.any { path -> path.fileName.toString().endsWith(".tcp.protocol.md") })
                assertTrue(markdownFiles.any { path -> path.fileName.toString().endsWith(".mqtt.protocol.md") })

                val mqttGateway =
                    serverOutputRoot
                        .resolve("site/addzero/esp32_host_computer/generated/modbus/mqtt/GeneratedModbusMqtt.kt")
                        .readText()
                assertContains(mqttGateway, "ModbusMqttExecutor")
                assertContains(mqttGateway, "class GeneratedModbusMqttKoinModule")
            } finally {
                deleteWorkspace(workspaceRoot)
            }
        }
    }
}
