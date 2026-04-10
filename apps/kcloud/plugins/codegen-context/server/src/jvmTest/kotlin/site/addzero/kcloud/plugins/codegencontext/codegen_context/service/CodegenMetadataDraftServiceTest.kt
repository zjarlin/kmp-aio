package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind

class CodegenMetadataDraftServiceTest {

    @Test
    fun shouldSaveAndLoadMetadataDraftSelections() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
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
                                        cOutputProjectDir = "/tmp/firmware-project",
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
    fun shouldExportSelectedMetadataAndCArtifactsSeparately() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val firmwareProjectDir = Files.createTempDirectory("codegen-metadata-export")
            try {
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
                                            cOutputProjectDir = firmwareProjectDir.toString(),
                                            bridgeImplPath = "Core/Src/modbus",
                                        ),
                                ),
                        ),
                    )

                val result = fixture.service.exportContext(saved.id!!)

                assertEquals(1, result.metadataSnapshots.size)
                assertEquals(CodegenMetadataTransportKind.RTU, result.metadataSnapshots.single().transport)
                assertTrue(result.generatedArtifacts.isNotEmpty())
                assertTrue(result.generatedArtifacts.all { artifact -> artifact.transport == CodegenMetadataTransportKind.TCP })
                assertEquals(
                    setOf(
                        CodegenMetadataArtifactKind.C_SERVICE_CONTRACT,
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
}
