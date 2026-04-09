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
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenGenerationSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenFieldDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenRtuGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenSchemaDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenTcpGenerationDefaultsDto
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
                    ).copy(
                        externalCOutputRoot = "/Volumes/peer-share/board-fw",
                        generationSettings =
                            CodegenGenerationSettingsDto(
                                serverOutputRoot = "/Users/test/server/generated/jvmMain/kotlin",
                                sharedOutputRoot = "/Users/test/shared/generated/commonMain/kotlin",
                                gatewayOutputRoot = "/Users/test/server/generated/jvmMain/kotlin",
                                apiClientOutputRoot = "/Users/test/ui/generated/commonMain/kotlin",
                                apiClientPackageName = "site.addzero.kcloud.plugins.codegencontext.generated.client",
                                springRouteOutputRoot = "/Users/test/server/generated-spring/jvmMain/kotlin",
                                cOutputRoot = "/Users/test/peer/Docs/generated/modbus/c",
                                markdownOutputRoot = "/Users/test/peer/Docs/generated/modbus/markdown",
                                rtuDefaults =
                                    CodegenRtuGenerationDefaultsDto(
                                        portPath = "/dev/ttyUSB9",
                                        unitId = 9,
                                        baudRate = 115200,
                                        dataBits = 8,
                                        stopBits = 1,
                                        parity = "even",
                                        timeoutMs = 2_500,
                                        retries = 4,
                                    ),
                                tcpDefaults =
                                    CodegenTcpGenerationDefaultsDto(
                                        host = "10.0.0.8",
                                        port = 1502,
                                        unitId = 12,
                                        timeoutMs = 3_000,
                                        retries = 5,
                                    ),
                            ),
                    ),
                )

            assertNotNull(saved.id)
            assertEquals("MODBUS_RTU_CLIENT", saved.protocolTemplateCode)
            assertEquals("ModbusRTU", saved.protocolTemplateName)
            assertEquals("/Volumes/peer-share/board-fw", saved.externalCOutputRoot)
            assertEquals("/Users/test/server/generated/jvmMain/kotlin", saved.generationSettings.serverOutputRoot)
            assertEquals("/Users/test/shared/generated/commonMain/kotlin", saved.generationSettings.sharedOutputRoot)
            assertEquals(
                "site.addzero.kcloud.plugins.codegencontext.generated.client",
                saved.generationSettings.apiClientPackageName,
            )
            assertEquals("/dev/ttyUSB9", saved.generationSettings.rtuDefaults.portPath)
            assertEquals(115200, saved.generationSettings.rtuDefaults.baudRate)
            assertEquals("10.0.0.8", saved.generationSettings.tcpDefaults.host)
            assertEquals(1502, saved.generationSettings.tcpDefaults.port)
            assertEquals(2, saved.schemas.size)
            assertTrue(saved.schemas.all { schema -> schema.id != null })
            assertTrue(saved.schemas.flatMap { schema -> schema.fields }.all { field -> field.id != null })

            val readSchema = saved.schemas.first { it.direction == CodegenSchemaDirection.READ }
            val updated =
                fixture.service.saveContext(
                    saved.copy(
                        name = "Updated Context",
                        description = "Updated description",
                        externalCOutputRoot = "/Volumes/peer-share/board-fw-v2",
                        generationSettings =
                            saved.generationSettings.copy(
                                apiClientPackageName = "site.addzero.kcloud.plugins.codegencontext.generated.clientv2",
                                tcpDefaults = saved.generationSettings.tcpDefaults.copy(host = "10.0.0.9"),
                            ),
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
            assertEquals("/Volumes/peer-share/board-fw-v2", updated.externalCOutputRoot)
            assertEquals(
                "site.addzero.kcloud.plugins.codegencontext.generated.clientv2",
                updated.generationSettings.apiClientPackageName,
            )
            assertEquals("10.0.0.9", updated.generationSettings.tcpDefaults.host)
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
    fun shouldRejectRelativeExternalCOutputRoot() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val error =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(
                        baseContextRequest(
                            protocolTemplateId = template.id,
                            code = "CTX_RELATIVE_OUTPUT",
                        ).copy(
                            externalCOutputRoot = "relative/peer-fw",
                        ),
                    )
                }

            assertContains(error.message.orEmpty(), "externalCOutputRoot must be an absolute path")
        }
    }

    @Test
    fun shouldRejectApiClientPackageAndOutputRootWhenConfiguredAlone() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val onlyOutputError =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(
                        baseContextRequest(
                            protocolTemplateId = template.id,
                            code = "CTX_CLIENT_OUTPUT_ONLY",
                        ).copy(
                            generationSettings =
                                CodegenGenerationSettingsDto(
                                    apiClientOutputRoot = "/Users/test/ui/generated/commonMain/kotlin",
                                ),
                        ),
                    )
                }
            assertContains(onlyOutputError.message.orEmpty(), "apiClientPackageName is required")

            val onlyPackageError =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(
                        baseContextRequest(
                            protocolTemplateId = template.id,
                            code = "CTX_CLIENT_PACKAGE_ONLY",
                        ).copy(
                            generationSettings =
                                CodegenGenerationSettingsDto(
                                    apiClientPackageName = "site.addzero.generated.client",
                                ),
                        ),
                    )
                }
            assertContains(onlyPackageError.message.orEmpty(), "apiClientOutputRoot is required")
        }
    }

    @Test
    fun shouldSaveAndGenerateContractsAndReturnGeneratedFileSummary() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val workspaceRoot = createGeneratorWorkspace()
            val apiClientRoot = workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/ui/generated/commonMain/kotlin")
            val springRouteRoot = workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/generated-spring/jvmMain/kotlin")
            val cRoot = workspaceRoot.resolve("mounted/peer-c-project/Docs/generated/modbus-metadata/c")
            val markdownRoot = workspaceRoot.resolve("mounted/peer-c-project/Docs/generated/modbus-metadata/markdown")
            val saved =
                fixture.service.saveContext(
                    baseContextRequest(
                        protocolTemplateId = template.id,
                        code = "CTX_GENERATE",
                    ).copy(
                        generationSettings =
                            CodegenGenerationSettingsDto(
                                serverOutputRoot =
                                    workspaceRoot.resolve(
                                        "apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin",
                                    ).toString(),
                                sharedOutputRoot =
                                    workspaceRoot.resolve(
                                        "apps/kcloud/plugins/mcu-console/shared/generated/commonMain/kotlin",
                                    ).toString(),
                                gatewayOutputRoot =
                                    workspaceRoot.resolve(
                                        "apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin",
                                    ).toString(),
                                apiClientOutputRoot = apiClientRoot.toString(),
                                apiClientPackageName = "site.addzero.kcloud.plugins.mcuconsole.api.external.generated",
                                springRouteOutputRoot = springRouteRoot.toString(),
                                cOutputRoot = cRoot.toString(),
                                markdownOutputRoot = markdownRoot.toString(),
                            ),
                    ),
                )
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
                assertTrue(response.generatedFiles.size >= 8)
                assertContains(response.message, "Generated")
                assertTrue(response.generatedFiles.all { file -> file.startsWith(workspaceRoot.toString()) })
                assertTrue(response.generatedFiles.all { file -> Path.of(file).exists() })
                assertTrue(
                    response.generatedFiles.any { file ->
                        file.endsWith("/site/addzero/kcloud/plugins/mcuconsole/modbus/device/DeviceApi.kt")
                    },
                )
                assertTrue(
                    response.generatedFiles.any { file ->
                        file.endsWith("/site/addzero/kcloud/plugins/mcuconsole/modbus/device/DeviceWriteApi.kt")
                    },
                )
                assertTrue(
                    response.generatedFiles.any { file ->
                        file.endsWith("/site/addzero/kcloud/plugins/mcuconsole/modbus/device/BoardSnapshotRegisters.kt")
                    },
                )
                assertTrue(
                    response.generatedFiles.any { file ->
                        file.endsWith("/GeneratedModbusRtuSpringRoutesSource.kt")
                    },
                )
                assertTrue(
                    response.generatedFiles.any { file ->
                        file.endsWith("/GeneratedModbusRtuKtorfitClient.kt")
                    },
                )
                assertTrue(response.generatedFiles.any { file -> file.endsWith("/generated/modbus/rtu/modbus_rtu_dispatch.c") })
                assertTrue(response.generatedFiles.any { file -> file.endsWith(".rtu.protocol.md") })
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
                            assertContains(payload, "\"interfaceSimpleName\":\"DeviceApi\"")
                            assertContains(payload, "\"interfaceSimpleName\":\"DeviceWriteApi\"")
                        }
                    }
                }
            } finally {
                deleteWorkspace(workspaceRoot)
            }
        }
    }

    @Test
    fun shouldGenerateExternalCAndMarkdownArtifactsWhenConfigured() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val workspaceRoot = createGeneratorWorkspace()
            val externalOutputRoot = workspaceRoot.resolve("mounted/peer-c-project/Docs/generated/modbus-metadata")
            val saved =
                fixture.service.saveContext(
                    baseContextRequest(
                        protocolTemplateId = template.id,
                        code = "CTX_EXTERNAL_OUTPUT",
                    ).copy(
                        externalCOutputRoot = externalOutputRoot.toString(),
                    ),
                )

            try {
                val response =
                    withRepoRoot(workspaceRoot) {
                        fixture.service.generateContracts(saved.id!!)
                    }

                val cDispatch = externalOutputRoot.resolve("c/generated/modbus/rtu/modbus_rtu_dispatch.c")
                val protocolDocDir = externalOutputRoot.resolve("markdown/generated/modbus/protocols")
                assertTrue(cDispatch.exists())
                assertTrue(response.generatedFiles.contains(cDispatch.toString()))
                assertTrue(response.generatedFiles.any { file -> file.startsWith(externalOutputRoot.toString()) })
                assertContains(response.message, "Generated")
                assertTrue(protocolDocDir.toFile().listFiles().orEmpty().any { file -> file.name.endsWith(".rtu.protocol.md") })
            } finally {
                deleteWorkspace(workspaceRoot)
            }
        }
    }

    @Test
    fun shouldGenerateFlashConfigContractsWithU8AndByteArrayFields() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val saved =
                fixture.service.saveContext(
                    baseContextRequest(
                        protocolTemplateId = template.id,
                        code = "CTX_FLASH_CONFIG",
                    ).copy(
                        schemas =
                            listOf(
                                CodegenSchemaDto(
                                    name = "读取 Flash 配置",
                                    description = "读取 Flash 持久化配置。",
                                    sortIndex = 0,
                                    direction = CodegenSchemaDirection.READ,
                                    functionCode = CodegenFunctionCode.READ_HOLDING_REGISTERS,
                                    baseAddress = 200,
                                    methodName = "getFlashConfig",
                                    modelName = "FlashConfig",
                                    fields =
                                        listOf(
                                            CodegenFieldDto(
                                                name = "魔术字",
                                                description = "魔术字。",
                                                sortIndex = 0,
                                                propertyName = "magicWord",
                                                transportType = CodegenTransportType.U32_BE,
                                                registerOffset = 0,
                                            ),
                                            CodegenFieldDto(
                                                name = "端口配置",
                                                description = "24 路端口配置。",
                                                sortIndex = 1,
                                                propertyName = "portConfig",
                                                transportType = CodegenTransportType.BYTE_ARRAY,
                                                registerOffset = 2,
                                                length = 24,
                                            ),
                                            CodegenFieldDto(
                                                name = "从机地址",
                                                description = "Modbus 从机地址。",
                                                sortIndex = 2,
                                                propertyName = "slaveAddress",
                                                transportType = CodegenTransportType.U8,
                                                registerOffset = 14,
                                            ),
                                        ),
                                ),
                                CodegenSchemaDto(
                                    name = "写入 Flash 配置",
                                    description = "写入 Flash 持久化配置。",
                                    sortIndex = 10,
                                    direction = CodegenSchemaDirection.WRITE,
                                    functionCode = CodegenFunctionCode.WRITE_MULTIPLE_REGISTERS,
                                    baseAddress = 200,
                                    methodName = "writeFlashConfig",
                                    fields =
                                        listOf(
                                            CodegenFieldDto(
                                                name = "魔术字",
                                                description = "魔术字。",
                                                sortIndex = 0,
                                                propertyName = "magicWord",
                                                transportType = CodegenTransportType.U32_BE,
                                                registerOffset = 0,
                                            ),
                                            CodegenFieldDto(
                                                name = "端口配置",
                                                description = "24 路端口配置。",
                                                sortIndex = 1,
                                                propertyName = "portConfig",
                                                transportType = CodegenTransportType.BYTE_ARRAY,
                                                registerOffset = 2,
                                                length = 24,
                                            ),
                                            CodegenFieldDto(
                                                name = "从机地址",
                                                description = "Modbus 从机地址。",
                                                sortIndex = 2,
                                                propertyName = "slaveAddress",
                                                transportType = CodegenTransportType.U8,
                                                registerOffset = 14,
                                            ),
                                        ),
                                ),
                            ),
                    ),
                )
            val workspaceRoot = createGeneratorWorkspace()

            try {
                val response =
                    withRepoRoot(workspaceRoot) {
                        fixture.service.generateContracts(saved.id!!)
                    }

                assertTrue(response.generatedFiles.size >= 3)
                val deviceApi =
                    workspaceRoot.resolve(
                        "apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin/" +
                            "site/addzero/kcloud/plugins/mcuconsole/modbus/device/DeviceApi.kt",
                    ).readText()
                val deviceWriteApi =
                    workspaceRoot.resolve(
                        "apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin/" +
                            "site/addzero/kcloud/plugins/mcuconsole/modbus/device/DeviceWriteApi.kt",
                    ).readText()
                val flashRegisters =
                    workspaceRoot.resolve(
                        "apps/kcloud/plugins/mcu-console/shared/generated/commonMain/kotlin/" +
                            "site/addzero/kcloud/plugins/mcuconsole/modbus/device/FlashConfigRegisters.kt",
                    ).readText()

                assertContains(deviceApi, "suspend fun getFlashConfig(): FlashConfigRegisters")
                assertContains(deviceWriteApi, "suspend fun writeFlashConfig(")
                assertContains(deviceWriteApi, "portConfig: ByteArray")
                assertContains(deviceWriteApi, "slaveAddress: Int")
                assertContains(flashRegisters, "val portConfig: ByteArray")
                fixture.dataSource.connection.use { connection ->
                    connection.prepareStatement(
                        """
                        SELECT payload
                        FROM codegen_context_modbus_contract
                        WHERE context_id = ? AND transport = 'rtu'
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setLong(1, saved.id!!)
                        statement.executeQuery().use { resultSet ->
                            assertTrue(resultSet.next())
                            val payload = resultSet.getString("payload")
                            assertContains(payload, "\"codecName\":\"BYTE_ARRAY\"")
                            assertContains(payload, "\"codecName\":\"U8\"")
                            assertContains(payload, "\"valueKind\":\"BYTES\"")
                            assertContains(payload, "\"length\":24")
                        }
                    }
                }
            } finally {
                deleteWorkspace(workspaceRoot)
            }
        }
    }
}
