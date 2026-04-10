package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenClassDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingValueDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenGenerationSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMethodDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMqttGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenPropertyDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenRtuGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenTcpGenerationDefaultsDto
import site.addzero.kmp.exp.BusinessValidationException
import site.addzero.kmp.exp.NotFoundException
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenClassKind

/**
 * 验证代码生成上下文服务相关场景。
 */
class CodegenContextServiceTest {

    @Test
    /**
     * 处理shouldpersistgeneric类方法属性上下文graph。
     */
    fun shouldPersistGenericClassMethodPropertyContextGraph() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val saved =
                fixture.service.saveContext(
                    genericContextRequest(
                        protocolTemplateId = template.id,
                        availableDefinitions = definitions,
                        code = "CTX_GENERIC_CRUD",
                    ),
                )

            assertNotNull(saved.id)
            assertEquals(3, saved.classes.size)
            assertEquals(2, saved.availableContextDefinitions.size)
            assertEquals(
                listOf("MODBUS_OPERATION", "MODBUS_FIELD"),
                saved.availableContextDefinitions.map { definition -> definition.code },
            )
            val serviceClass = saved.classes.first { codegenClass -> codegenClass.className == "DeviceContract" }
            assertEquals(2, serviceClass.methods.size)
            assertTrue(saved.classes.all { codegenClass -> codegenClass.id != null })
            assertTrue(saved.classes.flatMap(CodegenClassDto::methods).all { method -> method.id != null })
            assertTrue(saved.classes.flatMap(CodegenClassDto::properties).all { property -> property.id != null })
        }
    }

    @Test
    /**
     * 处理shouldpersist上下文crudwithnested类graphandhard协议relation。
     */
    fun shouldPersistContextCrudWithNestedClassGraphAndHardProtocolRelation() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val saved =
                fixture.service.saveContext(
                    genericContextRequest(
                        protocolTemplateId = template.id,
                        availableDefinitions = definitions,
                        code = "CTX_CRUD",
                    ).copy(
                        externalCOutputRoot = "/Volumes/peer-share/board-fw",
                        generationSettings =
                            CodegenGenerationSettingsDto(
                                serverOutputRoot = "/Users/test/server/generated/jvmMain/kotlin",
                                sharedOutputRoot = "/Users/test/shared/generated/commonMain/kotlin",
                                gatewayOutputRoot = "/Users/test/server/generated/jvmMain/kotlin",
                                apiClientOutputRoot = "/Users/test/api/build/generated/source/controller2api/commonMain/kotlin",
                                apiClientPackageName = "site.addzero.kcloud.plugins.codegencontext.generated.client",
                                springRouteOutputRoot = "/Users/test/server/build/generated/source/spring2ktor/jvmMain/kotlin",
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
                                mqttDefaults =
                                    CodegenMqttGenerationDefaultsDto(
                                        brokerUrl = "tcp://10.0.0.7:1883",
                                        clientId = "board-gateway",
                                        requestTopic = "board/request",
                                        responseTopic = "board/response",
                                        qos = 2,
                                        timeoutMs = 4_000,
                                        retries = 6,
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
            assertEquals("tcp://10.0.0.7:1883", saved.generationSettings.mqttDefaults.brokerUrl)
            assertEquals("board-gateway", saved.generationSettings.mqttDefaults.clientId)

            val updated =
                fixture.service.saveContext(
                    buildUpdatedCrudRequest(
                        protocolTemplateId = template.id,
                        availableDefinitions = definitions,
                        id = saved.id,
                        code = saved.code,
                    ),
                )

            assertEquals("Updated Context", updated.name)
            assertEquals("Updated description", updated.description)
            assertEquals("/Volumes/peer-share/board-fw-v2", updated.externalCOutputRoot)
            assertEquals(
                "site.addzero.kcloud.plugins.codegencontext.generated.clientv2",
                updated.generationSettings.apiClientPackageName,
            )
            assertEquals("10.0.0.9", updated.generationSettings.tcpDefaults.host)
            assertEquals(2, updated.classes.size)

            val serviceClass = updated.classes.first { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }
            assertEquals(1, serviceClass.methods.size)
            assertEquals("readUpdatedSnapshot", serviceClass.methods.single().methodName)

            val modelClass = updated.classes.first { codegenClass -> codegenClass.classKind == CodegenClassKind.MODEL }
            assertEquals("UpdatedSnapshot", modelClass.className)
            assertEquals(listOf("updatedUptimeMs", "voltageMv"), modelClass.properties.map(CodegenPropertyDto::propertyName))

            val listedCodes = fixture.service.listContexts().map { it.code }
            assertContains(listedCodes, "CTX_CRUD")

            val updatedId = requireNotNull(updated.id)
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
    /**
     * 处理shouldaccepttilde路径for外部输出根目录。
     */
    fun shouldAcceptTildePathForExternalOutputRoot() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val homeScopedDir = createHomeScopedTempDirectory("codegen-context-external-root-")
            try {
                val saved =
                    fixture.service.saveContext(
                        genericContextRequest(
                            protocolTemplateId = template.id,
                            availableDefinitions = definitions,
                            code = "CTX_TILDE_ROOT",
                        ).copy(
                            externalCOutputRoot = homeScopedDir.toHomeTokenPath("~"),
                        ),
                    )

                assertEquals(homeScopedDir.toHomeTokenPath("~"), saved.externalCOutputRoot)
            } finally {
                homeScopedDir.toFile().deleteRecursively()
            }
        }
    }

    @Test
    /**
     * 处理shouldrejectduplicate方法名称insame上下文。
     */
    fun shouldRejectDuplicateMethodNamesInSameContext() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val base = genericContextRequest(protocolTemplateId = template.id, availableDefinitions = definitions, code = "CTX_DUP_METHOD")
            val serviceClass = base.classes.first { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }
            val duplicateRequest =
                base.replaceServiceClass(
                    serviceClass.copy(
                        methods =
                            listOf(
                                serviceClass.methods[0],
                                serviceClass.methods[1].copy(methodName = serviceClass.methods[0].methodName),
                            ),
                    ),
                )

            val error =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(duplicateRequest)
                }

            assertContains(error.message.orEmpty(), "duplicate methodName values")
        }
    }

    @Test
    /**
     * 处理shouldderiveread响应类whenmissing。
     */
    fun shouldDeriveReadResponseClassWhenMissing() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val base = genericContextRequest(protocolTemplateId = template.id, availableDefinitions = definitions, code = "CTX_MISSING_MODEL")
            val serviceClass = base.classes.first { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }
            val request =
                base.replaceServiceClass(
                    serviceClass.copy(
                        methods =
                            serviceClass.methods.map { method ->
                                if (method.methodName == "readBoardSnapshot") {
                                    method.copy(responseClassName = null)
                                } else {
                                    method
                                }
                            },
                    ),
                ).replaceModelClass(
                    className = "BoardSnapshot",
                ) { model ->
                    model.copy(
                        name = "读取板卡快照响应实体",
                        className = "",
                    )
                }

            val saved = fixture.service.saveContext(request)
            val savedServiceClass = saved.classes.first { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }
            assertEquals("readBoardSnapshot", savedServiceClass.methods.first().methodName)
            assertEquals("ReadBoardSnapshotResponse", savedServiceClass.methods.first().responseClassName)
        }
    }

    @Test
    /**
     * 处理shouldderiveidentifiersonsavewhenuileavesthemblank。
     */
    fun shouldDeriveIdentifiersOnSaveWhenUiLeavesThemBlank() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val saved =
                fixture.service.saveContext(
                    CodegenContextDetailDto(
                        code = "CTX_SERVER_DERIVE",
                        name = "Server Derived Context",
                        description = "验证服务端自动补齐标识符。",
                        protocolTemplateId = template.id,
                        availableContextDefinitions = definitions,
                        classes =
                            listOf(
                                CodegenClassDto(
                                    name = "设备读写服务",
                                    description = "由服务端补齐方法名。",
                                    sortIndex = 0,
                                    classKind = CodegenClassKind.SERVICE,
                                    className = "GeneratedDeviceContractService",
                                    methods =
                                        listOf(
                                            CodegenMethodDto(
                                                name = "温度读取",
                                                description = "读取当前温度。",
                                                sortIndex = 0,
                                                methodName = "",
                                                responseClassName = null,
                                                bindings =
                                                    listOf(
                                                        binding(
                                                            MODBUS_OPERATION_DEFINITION_CODE,
                                                            "direction" to "READ",
                                                            "functionCode" to "READ_HOLDING_REGISTERS",
                                                            "baseAddress" to "10",
                                                        ),
                                                    ),
                                            ),
                                            CodegenMethodDto(
                                                name = "温度设置",
                                                description = "写入目标温度。",
                                                sortIndex = 10,
                                                methodName = "",
                                                requestClassName = null,
                                                bindings =
                                                    listOf(
                                                        binding(
                                                            MODBUS_OPERATION_DEFINITION_CODE,
                                                            "direction" to "WRITE",
                                                            "functionCode" to "WRITE_MULTIPLE_REGISTERS",
                                                            "baseAddress" to "30",
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                                CodegenClassDto(
                                    name = "温度读取响应实体",
                                    description = "服务端应补齐响应类名。",
                                    sortIndex = 10,
                                    classKind = CodegenClassKind.MODEL,
                                    className = "",
                                    properties =
                                        listOf(
                                            CodegenPropertyDto(
                                                name = "温度",
                                                description = "当前温度。",
                                                sortIndex = 0,
                                                propertyName = "",
                                                typeName = "",
                                                bindings =
                                                    listOf(
                                                        binding(
                                                            MODBUS_FIELD_DEFINITION_CODE,
                                                            "transportType" to "U16",
                                                            "registerOffset" to "0",
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                                CodegenClassDto(
                                    name = "温度设置请求实体",
                                    description = "服务端应补齐请求类名。",
                                    sortIndex = 20,
                                    classKind = CodegenClassKind.MODEL,
                                    className = "",
                                    properties =
                                        listOf(
                                            CodegenPropertyDto(
                                                name = "备注",
                                                description = "写入备注。",
                                                sortIndex = 0,
                                                propertyName = "",
                                                typeName = "",
                                                bindings =
                                                    listOf(
                                                        binding(
                                                            MODBUS_FIELD_DEFINITION_CODE,
                                                            "transportType" to "STRING_UTF8",
                                                            "registerOffset" to "1",
                                                            "length" to "2",
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            ),
                    ),
                )

            val serviceClass = saved.classes.first { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }
            assertEquals(listOf("wenDuDuQu", "wenDuSheZhi"), serviceClass.methods.map(CodegenMethodDto::methodName))
            assertEquals("WenDuDuQuResponse", serviceClass.methods[0].responseClassName)
            assertEquals("WenDuSheZhiRequest", serviceClass.methods[1].requestClassName)

            val readModel = saved.classes.first { codegenClass -> codegenClass.name == "温度读取响应实体" }
            assertEquals("WenDuDuQuResponse", readModel.className)
            assertEquals("wenDu", readModel.properties.single().propertyName)
            assertEquals("Int", readModel.properties.single().typeName)

            val writeModel = saved.classes.first { codegenClass -> codegenClass.name == "温度设置请求实体" }
            assertEquals("WenDuSheZhiRequest", writeModel.className)
            assertEquals("beiZhu", writeModel.properties.single().propertyName)
            assertEquals("String", writeModel.properties.single().typeName)
        }
    }

    @Test
    /**
     * 处理shouldrejectoverlapping字段spans。
     */
    fun shouldRejectOverlappingFieldSpans() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val base = genericContextRequest(protocolTemplateId = template.id, availableDefinitions = definitions, code = "CTX_OVERLAP")
            val request =
                base.replaceModelClass(
                    className = "BoardSnapshot",
                    transform = { model ->
                        model.copy(
                            properties =
                                listOf(
                                    model.properties[0],
                                    model.properties[1].copy(
                                        propertyName = "valueB",
                                        bindings =
                                            listOf(
                                                binding(
                                                    MODBUS_FIELD_DEFINITION_CODE,
                                                    "transportType" to "U16",
                                                    "registerOffset" to "1",
                                                ),
                                            ),
                                    ),
                                ),
                        )
                    },
                )

            val error =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(request)
                }

            assertContains(error.message.orEmpty(), "overlapping field layout")
        }
    }

    @Test
    /**
     * 处理shouldrejectinvalid传输andfunction编码combination。
     */
    fun shouldRejectInvalidTransportAndFunctionCodeCombination() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val base = genericContextRequest(protocolTemplateId = template.id, availableDefinitions = definitions, code = "CTX_INVALID_TRANSPORT")
            val withSingleCoilService =
                base
                    .replaceServiceClass(
                        base.classes.first { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }.copy(
                            methods =
                                listOf(
                                    CodegenMethodDto(
                                        name = "写入单个线圈",
                                        description = "非法写线圈示例。",
                                        sortIndex = 0,
                                        methodName = "writeSingleCoil",
                                        requestClassName = "WriteSingleCoilRequest",
                                        bindings =
                                            listOf(
                                                binding(
                                                    MODBUS_OPERATION_DEFINITION_CODE,
                                                    "direction" to "WRITE",
                                                    "functionCode" to "WRITE_SINGLE_COIL",
                                                    "baseAddress" to "0",
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    )
            val request =
                withSingleCoilService
                    .copy(
                        classes =
                            withSingleCoilService.classes.filterNot { codegenClass -> codegenClass.className == "WriteBoardConfigRequest" } +
                                CodegenClassDto(
                                    name = "非法线圈请求",
                                    description = "故意使用错误类型。",
                                    sortIndex = 20,
                                    classKind = CodegenClassKind.MODEL,
                                    className = "WriteSingleCoilRequest",
                                    properties =
                                        listOf(
                                            CodegenPropertyDto(
                                                name = "Threshold",
                                                sortIndex = 0,
                                                propertyName = "threshold",
                                                typeName = "Int",
                                                bindings =
                                                    listOf(
                                                        binding(
                                                            MODBUS_FIELD_DEFINITION_CODE,
                                                            "transportType" to "U16",
                                                            "registerOffset" to "0",
                                                        ),
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
    /**
     * 处理shouldrejectrelative外部 C 输出根目录。
     */
    fun shouldRejectRelativeExternalCOutputRoot() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val error =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(
                        genericContextRequest(
                            protocolTemplateId = template.id,
                            availableDefinitions = fixture.service.listContextDefinitions(template.id),
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
    /**
     * 处理shouldrejectAPI 客户端包and输出根目录whenconfiguredalone。
     */
    fun shouldRejectApiClientPackageAndOutputRootWhenConfiguredAlone() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val onlyOutputError =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(
                        genericContextRequest(
                            protocolTemplateId = template.id,
                            availableDefinitions = definitions,
                            code = "CTX_CLIENT_OUTPUT_ONLY",
                        ).copy(
                            generationSettings =
                                CodegenGenerationSettingsDto(
                                    apiClientOutputRoot = "/Users/test/api/build/generated/source/controller2api/commonMain/kotlin",
                                ),
                        ),
                    )
                }
            assertContains(onlyOutputError.message.orEmpty(), "apiClientPackageName is required")

            val onlyPackageError =
                assertFailsWith<BusinessValidationException> {
                    fixture.service.saveContext(
                        genericContextRequest(
                            protocolTemplateId = template.id,
                            availableDefinitions = definitions,
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
    /**
     * 处理should通过兼容wrapper返回结构化导出文件摘要。
     */
    fun shouldGenerateContractsCompatibilityWrapperAndReturnExportedFileSummary() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val definitions = fixture.service.listContextDefinitions(template.id)
            val firmwareProjectDir = createGeneratorWorkspace()

            try {
                val saved =
                    fixture.service.saveContextDraft(
                        genericContextRequest(
                            protocolTemplateId = template.id,
                            availableDefinitions = definitions,
                            code = "CTX_GENERATE",
                        ).copy(
                            protocolTemplateCode = template.code,
                            protocolTemplateName = template.name,
                        ).toMetadataDraft(definitions).copy(
                            exportSettings =
                                site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto(
                                    artifactKinds =
                                        setOf(
                                            site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind.METADATA_SNAPSHOT,
                                            site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind.C_SERVICE_CONTRACT,
                                            site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind.C_TRANSPORT_CONTRACT,
                                            site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind.MARKDOWN_PROTOCOL,
                                        ),
                                    kotlinClientTransports =
                                        setOf(
                                            site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind.RTU,
                                        ),
                                    cExposeTransports =
                                        setOf(
                                            site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind.TCP,
                                        ),
                                    firmwareSync =
                                        site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataFirmwareSyncDto(
                                            cOutputProjectDir = firmwareProjectDir.toString(),
                                            bridgeImplPath = "Core/Src/modbus",
                                        ),
                                ),
                        ),
                    )
                val response = fixture.service.generateContracts(saved.id!!)

                assertEquals(saved.id, response.contextId)
                assertContains(response.message, "metadata snapshot")
                assertTrue(response.generatedFiles.isNotEmpty())
                assertTrue(response.generatedFiles.any { file -> file.endsWith("/Core/Src/generated/modbus/tcp/transport/modbus_tcp_dispatch.c") })
                assertTrue(response.generatedFiles.any { file -> file.endsWith(".tcp.protocol.md") })
                assertTrue(response.generatedFiles.none { file -> file.endsWith(".kt") })
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
                deleteWorkspace(firmwareProjectDir)
            }
        }
    }

    @Test
    /**
     * 处理should兼容旧externalC输出配置并迁移为C与Markdown导出。
     */
    fun shouldGenerateExternalCAndMarkdownArtifactsWhenLegacyExternalOutputConfigured() {
        CodegenContextTestFixture().use { fixture ->
            val template = fixture.templateService.listProtocolTemplates().first { it.code == "MODBUS_RTU_CLIENT" }
            val workspaceRoot = createGeneratorWorkspace()
            val externalOutputRoot = workspaceRoot.resolve("mounted/peer-c-project/Docs/generated/modbus-metadata")
            externalOutputRoot.createDirectories()
            val saved =
                fixture.service.saveContext(
                    genericContextRequest(
                        protocolTemplateId = template.id,
                        availableDefinitions = fixture.service.listContextDefinitions(template.id),
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

                val cDispatch = externalOutputRoot.resolve("Core/Src/generated/modbus/rtu/transport/modbus_rtu_dispatch.c")
                val tcpDispatch = externalOutputRoot.resolve("Core/Src/generated/modbus/tcp/transport/modbus_tcp_dispatch.c")
                val mqttDispatch = externalOutputRoot.resolve("Core/Src/generated/modbus/mqtt/transport/modbus_mqtt_dispatch.c")
                val protocolDocDir = externalOutputRoot.resolve("Docs/generated/modbus")
                assertTrue(cDispatch.exists())
                assertTrue(tcpDispatch.exists())
                assertTrue(mqttDispatch.exists())
                assertTrue(response.generatedFiles.contains(cDispatch.toString()))
                assertTrue(response.generatedFiles.contains(tcpDispatch.toString()))
                assertTrue(response.generatedFiles.contains(mqttDispatch.toString()))
                assertTrue(response.generatedFiles.any { file -> file.startsWith(externalOutputRoot.toString()) })
                assertContains(response.message, "外部产物")
                assertTrue(response.generatedFiles.none { file -> file.endsWith(".kt") })
                assertTrue(protocolDocDir.resolve("rtu").toFile().listFiles().orEmpty().any { file -> file.name.endsWith(".rtu.protocol.md") })
                assertTrue(protocolDocDir.resolve("tcp").toFile().listFiles().orEmpty().any { file -> file.name.endsWith(".tcp.protocol.md") })
                assertTrue(protocolDocDir.resolve("mqtt").toFile().listFiles().orEmpty().any { file -> file.name.endsWith(".mqtt.protocol.md") })
            } finally {
                deleteWorkspace(workspaceRoot)
            }
        }
    }
}

/**
 * 构建更新crud请求。
 *
 * @param protocolTemplateId 协议模板 ID。
 * @param availableDefinitions 可用定义。
 * @param id ID。
 * @param code 编码。
 */
private fun buildUpdatedCrudRequest(
    protocolTemplateId: Long,
    availableDefinitions: List<site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto>,
    id: Long?,
    code: String,
): CodegenContextDetailDto =
    CodegenContextDetailDto(
        id = id,
        code = code,
        name = "Updated Context",
        description = "Updated description",
        enabled = true,
        protocolTemplateId = protocolTemplateId,
        externalCOutputRoot = "/Volumes/peer-share/board-fw-v2",
        generationSettings =
            CodegenGenerationSettingsDto(
                serverOutputRoot = "/Users/test/server/generated/jvmMain/kotlin",
                sharedOutputRoot = "/Users/test/shared/generated/commonMain/kotlin",
                gatewayOutputRoot = "/Users/test/server/generated/jvmMain/kotlin",
                apiClientOutputRoot = "/Users/test/api/build/generated/source/controller2api/commonMain/kotlin",
                apiClientPackageName = "site.addzero.kcloud.plugins.codegencontext.generated.clientv2",
                springRouteOutputRoot = "/Users/test/server/build/generated/source/spring2ktor/jvmMain/kotlin",
                cOutputRoot = "/Users/test/peer/Docs/generated/modbus/c",
                markdownOutputRoot = "/Users/test/peer/Docs/generated/modbus/markdown",
                tcpDefaults = CodegenTcpGenerationDefaultsDto(host = "10.0.0.9"),
            ),
        availableContextDefinitions = availableDefinitions,
        classes =
            listOf(
                CodegenClassDto(
                    name = "设备读服务",
                    description = "更新后的读服务。",
                    sortIndex = 0,
                    classKind = CodegenClassKind.SERVICE,
                    className = "DeviceContract",
                    methods =
                        listOf(
                            CodegenMethodDto(
                                name = "读取更新后的快照",
                                description = "更新后的读取方法。",
                                sortIndex = 0,
                                methodName = "readUpdatedSnapshot",
                                responseClassName = "UpdatedSnapshot",
                                bindings =
                                    listOf(
                                        binding(
                                            MODBUS_OPERATION_DEFINITION_CODE,
                                            "direction" to "READ",
                                            "functionCode" to "READ_HOLDING_REGISTERS",
                                            "baseAddress" to "10",
                                        ),
                                    ),
                            ),
                        ),
                ),
                CodegenClassDto(
                    name = "更新后的板卡快照",
                    description = "更新后的响应模型。",
                    sortIndex = 10,
                    classKind = CodegenClassKind.MODEL,
                    className = "UpdatedSnapshot",
                    properties =
                        listOf(
                            CodegenPropertyDto(
                                name = "Updated Uptime",
                                description = "更新后的运行时长。",
                                sortIndex = 0,
                                propertyName = "updatedUptimeMs",
                                typeName = "Int",
                                bindings =
                                    listOf(
                                        binding(
                                            MODBUS_FIELD_DEFINITION_CODE,
                                            "transportType" to "U32_BE",
                                            "registerOffset" to "0",
                                            "length" to "1",
                                        ),
                                    ),
                            ),
                            CodegenPropertyDto(
                                name = "Voltage",
                                description = "电压。",
                                sortIndex = 10,
                                propertyName = "voltageMv",
                                typeName = "Int",
                                bindings =
                                    listOf(
                                        binding(
                                            MODBUS_FIELD_DEFINITION_CODE,
                                            "transportType" to "U16",
                                            "registerOffset" to "4",
                                        ),
                                    ),
                            ),
                        ),
                ),
            ),
    )

/**
 * 处理代码生成上下文详情数据传输对象。
 *
 * @param replacement replacement。
 */
private fun CodegenContextDetailDto.replaceServiceClass(
    replacement: CodegenClassDto,
): CodegenContextDetailDto =
    copy(
        classes =
            classes.map { codegenClass ->
                if (codegenClass.classKind == CodegenClassKind.SERVICE) {
                    replacement
                } else {
                    codegenClass
                }
            },
    )

/**
 * 处理代码生成上下文详情数据传输对象。
 *
 * @param className 类名。
 * @param transform 转换函数。
 */
private fun CodegenContextDetailDto.replaceModelClass(
    className: String,
    transform: (CodegenClassDto) -> CodegenClassDto,
): CodegenContextDetailDto =
    copy(
        classes =
            classes.map { codegenClass ->
                if (codegenClass.className == className) {
                    transform(codegenClass)
                } else {
                    codegenClass
                }
            },
    )

/**
 * 处理绑定。
 *
 * @param definitionCode 定义编码。
 * @param values 值。
 */
private fun binding(
    definitionCode: String,
    vararg values: Pair<String, String>,
): CodegenContextBindingDto =
    CodegenContextBindingDto(
        definitionCode = definitionCode,
        values =
            values.map { (paramCode, value) ->
                CodegenContextBindingValueDto(
                    paramCode = paramCode,
                    value = value,
                )
            },
    )
