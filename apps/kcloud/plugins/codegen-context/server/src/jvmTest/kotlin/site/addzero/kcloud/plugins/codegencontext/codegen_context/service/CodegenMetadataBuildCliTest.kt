package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind

/**
 * 验证代码生成metadata构建命令行相关场景。
 */
class CodegenMetadataBuildCliTest {

    @Test
    /**
     * 处理shouldfallbackrequested传输tofirst可用metadata快照。
     */
    fun shouldFallbackRequestedTransportsToFirstAvailableMetadataSnapshot() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val saved =
                fixture.service.saveContext(
                    genericContextRequest(
                        protocolTemplateId = template.id,
                        availableDefinitions = fixture.service.listContextDefinitions(template.id),
                        code = "CTX_METADATA_BUILD",
                    ),
                )
            val workspaceRoot = createGeneratorWorkspace()

            try {
                fixture.generator.export(
                    context = saved,
                    exportSettings =
                        CodegenMetadataExportSettingsDto(
                            artifactKinds = setOf(CodegenMetadataArtifactKind.METADATA_SNAPSHOT),
                            kotlinClientTransports = setOf(CodegenMetadataTransportKind.RTU),
                        ),
                )

                val serverOutputRoot = workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin")
                val sharedOutputRoot = workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/shared/generated/commonMain/kotlin")
                val metadataRoot = workspaceRoot.resolve("t/Docs/generated/modbus-metadata")

                main(
                    arrayOf(
                        "--driver-class",
                        "org.sqlite.JDBC",
                        "--jdbc-url",
                        fixture.dataSource.connection.use { connection -> connection.metaData.url },
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

    @Test
    /**
     * 处理shouldgenerate合同fromcommittedsqlite快照。
     */
    fun shouldGenerateContractsFromCommittedSqliteSnapshot() {
        val workspaceRoot = createGeneratorWorkspace()

        try {
            val snapshotFile =
                Path.of(
                    checkNotNull(javaClass.getResource("/snapshots/codegen-context-metadata.sqlite")) {
                        "Missing committed sqlite snapshot resource."
                    }.toURI(),
                )
            val serverOutputRoot =
                workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/build/generated/source/codegen-context/jvmMain/kotlin")
            val sharedOutputRoot =
                workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/shared/build/generated/source/codegen-context/commonMain/kotlin")
            val metadataRoot = workspaceRoot.resolve("t/Docs/generated/modbus-metadata")

            main(
                arrayOf(
                    "--driver-class",
                    "org.sqlite.JDBC",
                    "--jdbc-url",
                    "jdbc:sqlite:${snapshotFile.toAbsolutePath().normalize()}",
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
                    "apps/kcloud/plugins/mcu-console/server/build/generated/source/codegen-context/jvmMain/kotlin",
                    "--shared-output-root",
                    "apps/kcloud/plugins/mcu-console/shared/build/generated/source/codegen-context/commonMain/kotlin",
                    "--gateway-output-root",
                    "apps/kcloud/plugins/mcu-console/server/build/generated/source/codegen-context/jvmMain/kotlin",
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
                    .resolve("site/addzero/kcloud/plugins/mcuconsole/modbus/device/DeviceWriteApi.kt")
                    .exists(),
            )
            assertTrue(
                sharedOutputRoot
                    .resolve("site/addzero/kcloud/plugins/mcuconsole/modbus/device/FlashConfigRegisters.kt")
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
        } finally {
            deleteWorkspace(workspaceRoot)
        }
    }
}
