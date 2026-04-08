package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenFieldDto
import site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.common.BusinessValidationException
import site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.common.NotFoundException
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType

class CodegenContextServiceTest {

    @Test
    fun shouldPersistContextCrudWithNestedSchemasAndHardProtocolRelation() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val saved =
                fixture.service.saveContext(
                    baseContextRequest(
                        protocolTemplateId = template.id,
                        code = "CTX_CRUD",
                    ),
                )

            assertNotNull(saved.id)
            assertEquals("MODBUS_RTU_CLIENT", saved.protocolTemplateCode)
            assertEquals("Modbus RTU Client", saved.protocolTemplateName)
            assertEquals(2, saved.schemas.size)
            assertTrue(saved.schemas.all { schema -> schema.id != null })
            assertTrue(saved.schemas.flatMap { schema -> schema.fields }.all { field -> field.id != null })

            val readSchema = saved.schemas.first { it.direction == CodegenSchemaDirection.READ }
            val updated =
                fixture.service.saveContext(
                    saved.copy(
                        name = "Updated Context",
                        description = "Updated description",
                        schemas =
                            listOf(
                                readSchema.copy(
                                    name = "Read Updated Snapshot",
                                    description = "Updated read schema",
                                    methodName = "readUpdatedSnapshot",
                                    modelName = "UpdatedSnapshot",
                                    fields =
                                        listOf(
                                            readSchema.fields.first().copy(
                                                name = "Updated Uptime",
                                                propertyName = "updatedUptimeMs",
                                            ),
                                            CodegenFieldDto(
                                                name = "Voltage",
                                                description = "Voltage register",
                                                sortIndex = 10,
                                                propertyName = "voltageMv",
                                                transportType = CodegenTransportType.U16,
                                                registerOffset = 4,
                                            ),
                                        ),
                                ),
                            ),
                    ),
                )

            assertEquals("Updated Context", updated.name)
            assertEquals(1, updated.schemas.size)
            assertEquals("readUpdatedSnapshot", updated.schemas.single().methodName)
            assertEquals(2, updated.schemas.single().fields.size)
            assertEquals(
                listOf("updatedUptimeMs", "voltageMv"),
                updated.schemas.single().fields.map { it.propertyName },
            )
            val updatedId = requireNotNull(updated.id)

            val listedCodes = fixture.service.listContexts().map { it.code }
            assertContains(listedCodes, "CTX_CRUD")

            fixture.service.deleteContext(updatedId)

            assertFailsWith<NotFoundException> {
                fixture.service.getContext(updatedId)
            }
            assertTrue(
                fixture.service.listContexts().none { it.code == "CTX_CRUD" },
            )
        }
    }

    @Test
    fun shouldRejectDuplicateMethodNamesInSameContext() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val base = baseContextRequest(protocolTemplateId = template.id, code = "CTX_DUP_METHOD")
            val duplicateRequest =
                base.copy(
                    schemas = listOf(base.schemas[0], base.schemas[1].copy(methodName = base.schemas[0].methodName)),
                )

            val error =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(duplicateRequest)
                }

            assertContains(error.message.orEmpty(), "Duplicate schema methodName")
        }
    }

    @Test
    fun shouldRejectReadSchemaWithoutModelName() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val request =
                baseContextRequest(protocolTemplateId = template.id, code = "CTX_MISSING_MODEL").copy(
                    schemas =
                        listOf(
                            baseContextRequest(template.id).schemas.first().copy(modelName = null),
                        ),
                )

            val error =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(request)
                }

            assertContains(error.message.orEmpty(), "must define modelName")
        }
    }

    @Test
    fun shouldRejectOverlappingFieldSpans() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val request =
                baseContextRequest(protocolTemplateId = template.id, code = "CTX_OVERLAP").copy(
                    schemas =
                        listOf(
                            baseContextRequest(template.id).schemas.first().copy(
                                fields =
                                    listOf(
                                        CodegenFieldDto(
                                            name = "Value A",
                                            sortIndex = 0,
                                            propertyName = "valueA",
                                            transportType = CodegenTransportType.U32_BE,
                                            registerOffset = 0,
                                        ),
                                        CodegenFieldDto(
                                            name = "Value B",
                                            sortIndex = 1,
                                            propertyName = "valueB",
                                            transportType = CodegenTransportType.U16,
                                            registerOffset = 1,
                                        ),
                                    ),
                            ),
                        ),
                )

            val error =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(request)
                }

            assertContains(error.message.orEmpty(), "overlapping field layout")
        }
    }

    @Test
    fun shouldRejectInvalidTransportAndFunctionCodeCombination() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val request =
                baseContextRequest(protocolTemplateId = template.id, code = "CTX_INVALID_TRANSPORT").copy(
                    schemas =
                        listOf(
                            baseContextRequest(template.id).schemas.first().copy(
                                direction = CodegenSchemaDirection.WRITE,
                                functionCode = CodegenFunctionCode.WRITE_SINGLE_COIL,
                                modelName = null,
                                fields =
                                    listOf(
                                        CodegenFieldDto(
                                            name = "Threshold",
                                            sortIndex = 0,
                                            propertyName = "threshold",
                                            transportType = CodegenTransportType.U16,
                                            registerOffset = 0,
                                        ),
                                    ),
                            ),
                        ),
                )

            val error =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(request)
                }

            assertContains(error.message.orEmpty(), "must be BOOL_COIL")
        }
    }

    @Test
    fun shouldSaveAndGenerateContractsAndReturnGeneratedFileSummary() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val saved =
                fixture.service.saveContext(
                    baseContextRequest(
                        protocolTemplateId = template.id,
                        code = "CTX_GENERATE",
                    ),
                )
            val workspaceRoot = createGeneratorWorkspace()
            val manualContractFile =
                workspaceRoot.resolve(
                    "apps/kcloud/plugins/mcu-console/server/src/jvmMain/kotlin/" +
                        "site/addzero/kcloud/plugins/mcuconsole/modbus/device/DeviceApi.kt",
                )
            manualContractFile.writeText("manual sentinel")

            try {
                val response =
                    withRepoRoot(workspaceRoot) {
                        fixture.service.generateContracts(saved.id!!)
                    }

                assertEquals(saved.id, response.contextId)
                assertEquals(3, response.generatedFiles.size)
                assertContains(response.message, "Generated 3 contract artifacts")
                assertTrue(response.generatedFiles.all { file -> file.startsWith(workspaceRoot.toString()) })
                assertTrue(response.generatedFiles.all { file -> Path.of(file).exists() })
                assertEquals("manual sentinel", manualContractFile.readText())
                fixture.dataSource.connection.use { connection ->
                    connection.prepareStatement(
                        """
                        SELECT selected, payload
                        FROM codegen_context_modbus_contract
                        WHERE context_id = ? AND transport = 'rtu'
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setLong(1, saved.id!!)
                        statement.executeQuery().use { resultSet ->
                            assertTrue(resultSet.next())
                            assertEquals(1, resultSet.getInt("selected"))
                            val payload = resultSet.getString("payload")
                            assertContains(payload, "\"interfaceSimpleName\": \"DeviceApi\"")
                            assertContains(payload, "\"interfaceSimpleName\": \"DeviceWriteApi\"")
                        }
                    }
                }
            } finally {
                deleteWorkspace(workspaceRoot)
            }
        }
    }
}
