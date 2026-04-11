package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataDraftDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind

class CodegenMetadataDraftServiceTest {

    @Test
    fun shouldUseEmptyFirmwareSyncValuesForNewMetadataDraft() {
        val firmwareSync = CodegenMetadataDraftDto().exportSettings.firmwareSync

        assertEquals("", firmwareSync.cOutputProjectDir)
        assertEquals("", firmwareSync.bridgeImplPath)
        assertEquals("", firmwareSync.keilUvprojxPath)
        assertEquals("", firmwareSync.keilTargetName)
        assertEquals("", firmwareSync.keilGroupName)
        assertEquals("", firmwareSync.mxprojectPath)
    }

    @Test
    fun shouldSaveModelingDraftWithoutExplicitExportSelections() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val saved =
                fixture.service.saveContextDraft(
                    genericContextRequest(
                        protocolTemplateId = template.id,
                        availableDefinitions = definitions,
                        code = "CTX_MODEL_ONLY",
                    ).copy(
                        protocolTemplateCode = template.code,
                        protocolTemplateName = template.name,
                    ).toMetadataDraft(definitions).copy(
                        exportSettings =
                            site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto(
                                artifactKinds = emptySet(),
                                kotlinClientTransports = emptySet(),
                                cExposeTransports = emptySet(),
                            ),
                    ),
                )

            assertNotNull(saved.id)
            assertEquals("CTX_MODEL_ONLY", saved.code)
            assertEquals(template.id, saved.protocolTemplateId)
            assertTrue(saved.deviceFunctions.isNotEmpty())
            assertTrue(saved.thingProperties.isNotEmpty())
        }
    }

    @Test
    fun shouldSaveAndLoadMetadataDraftSelections() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val firmwareProjectDir = Files.createTempDirectory("codegen-metadata-save-")
            try {
                val saved =
                    fixture.service.saveContextDraft(
                        genericContextRequest(
                            protocolTemplateId = template.id,
                            availableDefinitions = definitions,
                            code = "CTX_METADATA_DRAFT",
                        ).copy(
                            protocolTemplateCode = template.code,
                            protocolTemplateName = template.name,
                        ).toMetadataDraft(definitions).copy(
                            exportSettings =
                                site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto(
                                    artifactKinds =
                                        setOf(
                                            CodegenMetadataArtifactKind.METADATA_SNAPSHOT,
                                            CodegenMetadataArtifactKind.C_SERVICE_CONTRACT,
                                        ),
                                    kotlinClientTransports =
                                        setOf(
                                            CodegenMetadataTransportKind.RTU,
                                            CodegenMetadataTransportKind.MQTT,
                                        ),
                                    cExposeTransports = setOf(CodegenMetadataTransportKind.TCP),
                                    firmwareSync =
                                        site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataFirmwareSyncDto(
                                            cOutputProjectDir = firmwareProjectDir.toString(),
                                            bridgeImplPath = "Core/Src/modbus",
                                        ),
                                ),
                        ),
                    )

                assertNotNull(saved.id)
                assertEquals(
                    setOf(CodegenMetadataTransportKind.RTU, CodegenMetadataTransportKind.MQTT),
                    saved.exportSettings.kotlinClientTransports,
                )
                assertEquals(setOf(CodegenMetadataTransportKind.TCP), saved.exportSettings.cExposeTransports)
                assertEquals(
                    setOf(
                        CodegenMetadataArtifactKind.METADATA_SNAPSHOT,
                        CodegenMetadataArtifactKind.C_SERVICE_CONTRACT,
                    ),
                    saved.exportSettings.artifactKinds,
                )

                val reloaded = fixture.service.getContextDraft(saved.id!!)
                assertEquals(saved.exportSettings, reloaded.exportSettings)
            } finally {
                firmwareProjectDir.toFile().deleteRecursively()
            }
        }
    }

    @Test
    fun shouldPreviewResolvedNamesFromMetadataDraft() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val draft =
                genericContextRequest(
                    protocolTemplateId = template.id,
                    availableDefinitions = definitions,
                    code = "CTX_METADATA_PREVIEW",
                ).copy(
                    protocolTemplateCode = template.code,
                    protocolTemplateName = template.name,
                ).toMetadataDraft(definitions)

            val preview = fixture.service.previewContextDraft(draft)

            assertTrue(preview.resolvedFunctions.isNotEmpty())
            assertTrue(preview.resolvedProperties.isNotEmpty())
            assertTrue(preview.resolvedFunctions.all { item -> !item.resolvedMethodName.isNullOrBlank() })
            assertTrue(preview.resolvedProperties.all { item -> !item.resolvedPropertyName.isNullOrBlank() })
            assertTrue(preview.resolvedProperties.all { item -> !item.resolvedTypeName.isNullOrBlank() })
            assertTrue(preview.exportPlans.isNotEmpty())
        }
    }

    @Test
    fun shouldExportFixedWorkbenchArtifactsToCAndMarkdown() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val firmwareProjectDir = createHomeScopedTempDirectory("codegen-metadata-export-")
            try {
                val firmwareProjectDirTokenPath = firmwareProjectDir.toHomeTokenPath("\$HOME")
                val saved =
                    fixture.service.saveContextDraft(
                        genericContextRequest(
                            protocolTemplateId = template.id,
                            availableDefinitions = definitions,
                            code = "CTX_METADATA_EXPORT",
                        ).copy(
                            protocolTemplateCode = template.code,
                            protocolTemplateName = template.name,
                        ).toMetadataDraft(definitions).copy(
                            exportSettings =
                                site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto(
                                    artifactKinds =
                                        setOf(
                                            CodegenMetadataArtifactKind.METADATA_SNAPSHOT,
                                            CodegenMetadataArtifactKind.C_SERVICE_CONTRACT,
                                            CodegenMetadataArtifactKind.MARKDOWN_PROTOCOL,
                                        ),
                                    kotlinClientTransports = setOf(CodegenMetadataTransportKind.RTU),
                                    cExposeTransports = setOf(CodegenMetadataTransportKind.TCP),
                                    firmwareSync =
                                        site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataFirmwareSyncDto(
                                            cOutputProjectDir = firmwareProjectDirTokenPath,
                                            bridgeImplPath = "Core/Src/modbus",
                                        ),
                                ),
                        ),
                    )

                val result = fixture.service.exportContext(saved.id!!)

                assertTrue(result.metadataSnapshots.isEmpty())
                assertTrue(result.generatedArtifacts.isNotEmpty())
                assertEquals(CodegenMetadataTransportKind.entries.toSet(), result.generatedArtifacts.mapNotNull { it.transport }.toSet())
                assertEquals(
                    setOf(
                        CodegenMetadataArtifactKind.C_SERVICE_CONTRACT,
                        CodegenMetadataArtifactKind.C_TRANSPORT_CONTRACT,
                        CodegenMetadataArtifactKind.MARKDOWN_PROTOCOL,
                    ),
                    result.generatedArtifacts.map { artifact -> artifact.artifactKind }.toSet(),
                )
                assertTrue(result.generatedArtifacts.all { artifact -> java.nio.file.Path.of(artifact.path).exists() })
            } finally {
                firmwareProjectDir.toFile().deleteRecursively()
            }
        }
    }

    @Test
    fun shouldBackfillLegacyExternalOutputsToMetadataDraftSelections() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val legacyOutputRoot = Files.createTempDirectory("codegen-metadata-legacy")
            try {
                val saved =
                    fixture.service.saveContext(
                        genericContextRequest(
                            protocolTemplateId = template.id,
                            availableDefinitions = fixture.service.listContextDefinitions(template.id),
                            code = "CTX_METADATA_LEGACY",
                        ).copy(
                            externalCOutputRoot = legacyOutputRoot.toString(),
                        ),
                    )

                val draft = fixture.service.getContextDraft(saved.id!!)

                assertEquals(setOf(CodegenMetadataTransportKind.RTU), draft.exportSettings.kotlinClientTransports)
                assertEquals(CodegenMetadataTransportKind.entries.toSet(), draft.exportSettings.cExposeTransports)
                assertEquals(
                    setOf(
                        CodegenMetadataArtifactKind.METADATA_SNAPSHOT,
                        CodegenMetadataArtifactKind.C_SERVICE_CONTRACT,
                        CodegenMetadataArtifactKind.C_TRANSPORT_CONTRACT,
                        CodegenMetadataArtifactKind.MARKDOWN_PROTOCOL,
                    ),
                    draft.exportSettings.artifactKinds,
                )
                assertEquals(legacyOutputRoot.toString(), draft.exportSettings.firmwareSync.cOutputProjectDir)
            } finally {
                legacyOutputRoot.toFile().deleteRecursively()
            }
        }
    }

    @Test
    fun shouldResolveTransportDefaultsFromNodeIdAndKeepExplicitOverrides() {
        CodegenContextTestFixture().use { fixture ->
            seedHostConfigNodeDefaults(fixture)
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val draft =
                genericContextRequest(
                    protocolTemplateId = template.id,
                    availableDefinitions = definitions,
                    code = "CTX_METADATA_NODE_DEFAULTS",
                ).copy(
                    protocolTemplateCode = template.code,
                    protocolTemplateName = template.name,
                ).toMetadataDraft(definitions).copy(
                    nodeId = "device/101/301",
                )

            val saved = fixture.service.saveContextDraft(draft)

            assertEquals("/dev/ttyUSB9", saved.exportSettings.rtuDefaults.portPath)
            assertEquals("9", saved.exportSettings.rtuDefaults.unitId)
            assertEquals("19200", saved.exportSettings.rtuDefaults.baudRate)
            assertEquals("10.20.30.40", saved.exportSettings.tcpDefaults.host)
            assertEquals("1502", saved.exportSettings.tcpDefaults.port)
            assertEquals("9", saved.exportSettings.tcpDefaults.unitId)
            assertEquals("tcp://broker.local:1884", saved.exportSettings.mqttDefaults.brokerUrl)
            assertEquals("gateway-node-301", saved.exportSettings.mqttDefaults.clientId)
            assertEquals("factory/gateway/request", saved.exportSettings.mqttDefaults.requestTopic)
            assertEquals("factory/gateway/response", saved.exportSettings.mqttDefaults.responseTopic)

            val overridden = fixture.service.saveContextDraft(
                saved.copy(
                    exportSettings =
                        saved.exportSettings.copy(
                            tcpDefaults = saved.exportSettings.tcpDefaults.copy(host = "manual-host"),
                        ),
                ),
            )

            assertEquals("manual-host", overridden.exportSettings.tcpDefaults.host)
            assertEquals("1502", overridden.exportSettings.tcpDefaults.port)
        }
    }
}

private fun seedHostConfigNodeDefaults(
    fixture: CodegenContextTestFixture,
) {
    fixture.dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.execute(
                """
                INSERT INTO host_config_project (
                    id, name, description, remark, sort_index, created_at, updated_at
                ) VALUES (
                    101, '测试工程', '用于 codegen-context 默认参数测试', NULL, 0, 1775750400000, 1775750400000
                )
                """.trimIndent(),
            )
            statement.execute(
                """
                INSERT INTO host_config_protocol_instance (
                    id, protocol_template_id, name, polling_interval_ms, transport_type, host, tcp_port, port_name, baud_rate,
                    data_bits, stop_bits, parity, response_timeout_ms, created_at, updated_at
                ) VALUES (
                    201, 1, '测试协议', 1000, 'RTU', '10.20.30.40', 1502, '/dev/ttyUSB9', 19200,
                    8, 1, 'EVEN', 2500, 1775750400000, 1775750400000
                )
                """.trimIndent(),
            )
            statement.execute(
                """
                INSERT INTO host_config_project_protocol (
                    id, project_id, protocol_id, sort_index, created_at, updated_at
                ) VALUES (
                    202, 101, 201, 0, 1775750400000, 1775750400000
                )
                """.trimIndent(),
            )
            statement.execute(
                """
                INSERT INTO host_config_device (
                    id, protocol_id, device_type_id, name, station_no, request_interval_ms, write_interval_ms,
                    byte_order2, byte_order4, float_order, batch_analog_start, batch_analog_length,
                    batch_digital_start, batch_digital_length, disabled, sort_index, created_at, updated_at
                ) VALUES (
                    301, 201, 1, '测试设备', 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0,
                    1775750400000, 1775750400000
                )
                """.trimIndent(),
            )
            statement.execute(
                """
                INSERT INTO host_config_project_modbus_server_config (
                    id, project_id, transport_type, enabled, tcp_port, port_name, baud_rate, data_bits, stop_bits,
                    parity, station_no, created_at, updated_at
                ) VALUES (
                    401, 101, 'RTU', 1, NULL, '/dev/ttyUSB7', 9600, 8, 1, 'NONE', 1, 1775750400000, 1775750400000
                )
                """.trimIndent(),
            )
            statement.execute(
                """
                INSERT INTO host_config_project_modbus_server_config (
                    id, project_id, transport_type, enabled, tcp_port, port_name, baud_rate, data_bits, stop_bits,
                    parity, station_no, created_at, updated_at
                ) VALUES (
                    402, 101, 'TCP', 1, 1502, NULL, NULL, NULL, NULL, NULL, 3, 1775750400000, 1775750400000
                )
                """.trimIndent(),
            )
            statement.execute(
                """
                INSERT INTO host_config_project_mqtt_config (
                    id, project_id, enabled, breakpoint_resume, gateway_name, vendor, host, port, topic, gateway_id,
                    auth_enabled, username, password_encrypted, tls_enabled, cert_file_ref, client_id, keep_alive_sec,
                    qos, report_period_sec, precision_value, value_change_ratio_enabled, cloud_control_disabled,
                    created_at, updated_at
                ) VALUES (
                    501, 101, 1, 0, '测试网关', 'addzero', 'broker.local', 1884, 'factory/gateway', 'gw-301',
                    0, NULL, NULL, 0, NULL, 'gateway-node-301', 60, 1, 10, NULL, 0, 0, 1775750400000, 1775750400000
                )
                """.trimIndent(),
            )
        }
    }
}
