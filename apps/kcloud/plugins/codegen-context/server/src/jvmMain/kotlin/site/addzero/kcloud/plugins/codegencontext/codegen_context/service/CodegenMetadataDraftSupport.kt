package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Path
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenClassDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenGenerationSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataDeviceFunctionDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportPlanDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataFirmwareSyncDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataIssueDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataMqttDefaultsDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataPreviewDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataResolvedFunctionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataResolvedPropertyDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataRtuDefaultsDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataTcpDefaultsDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataThingPropertyDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMethodDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMqttGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenPropertyDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenRtuGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenTcpGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenClassKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind as MetadataArtifactKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataIssueSeverity as MetadataIssueSeverity
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind
import site.addzero.kmp.exp.BusinessValidationException

private const val PROPERTY_CATALOG_CLASS_NAME = "CodegenPropertyCatalog"
private const val SERVICE_CLASS_NAME = "GeneratedDeviceContractService"
private const val METADATA_TABLE_NAME = "codegen_context_modbus_contract"
private const val METHOD_DIRECTION_PARAM = "direction"
private const val METHOD_FUNCTION_CODE_PARAM = "functionCode"
private const val METHOD_BASE_ADDRESS_PARAM = "baseAddress"
private const val FIELD_TRANSPORT_TYPE_PARAM = "transportType"
private const val FIELD_REGISTER_OFFSET_PARAM = "registerOffset"
private const val FIELD_BIT_OFFSET_PARAM = "bitOffset"
private const val FIELD_LENGTH_PARAM = "length"

internal fun CodegenMetadataDraftDto.toGenericContextDetail(
    availableDefinitions: List<CodegenContextDefinitionDto>,
): CodegenContextDetailDto {
    val propertyPool = thingProperties.associateBy(CodegenMetadataThingPropertyDraftDto::key)
    val serviceMethods =
        deviceFunctions.map { function ->
            CodegenMethodDto(
                id = function.id,
                name = function.name,
                description = function.description.cleanNullable(),
                sortIndex = function.sortIndex,
                methodName = "",
                requestClassName = null,
                responseClassName = null,
                bindings = function.bindings,
            )
        }
    val propertyCatalogClass =
        CodegenClassDto(
            name = "字段目录",
            description = "上位机维护的复用字段池。",
            sortIndex = 0,
            classKind = CodegenClassKind.MODEL,
            className = PROPERTY_CATALOG_CLASS_NAME,
            properties = thingProperties.map(CodegenMetadataThingPropertyDraftDto::toGenericProperty),
        )
    val methodModelClasses =
        deviceFunctions.mapIndexed { index, function ->
            val selectedProperties =
                function.thingPropertyKeys.mapNotNull { key ->
                    propertyPool[key]
                }
            val direction = function.bindingValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_DIRECTION_PARAM).orEmpty()
            when (direction) {
                "WRITE" ->
                    CodegenClassDto(
                        name = "${function.name.ifBlank { "未命名方法" }}请求实体",
                        description = function.description.cleanNullable(),
                        sortIndex = 100 + index * 10,
                        classKind = CodegenClassKind.MODEL,
                        className = "",
                        properties = selectedProperties.map(CodegenMetadataThingPropertyDraftDto::toGenericProperty),
                    )

                else ->
                    CodegenClassDto(
                        name = "${function.name.ifBlank { "未命名方法" }}响应实体",
                        description = function.description.cleanNullable(),
                        sortIndex = 100 + index * 10,
                        classKind = CodegenClassKind.MODEL,
                        className = "",
                        properties = selectedProperties.map(CodegenMetadataThingPropertyDraftDto::toGenericProperty),
                    )
            }
        }
    val serviceClass =
        CodegenClassDto(
            name = "设备契约服务",
            description = "由元数据草稿维护的方法集合。",
            sortIndex = 10,
            classKind = CodegenClassKind.SERVICE,
            className = SERVICE_CLASS_NAME,
            methods = serviceMethods,
        )
    return CodegenContextDetailDto(
        id = id,
        code = code,
        name = name,
        description = description.cleanNullable(),
        enabled = enabled,
        nodeId = nodeId.cleanNullable(),
        consumerTarget = consumerTarget,
        protocolTemplateId = protocolTemplateId,
        protocolTemplateCode = protocolTemplateCode,
        protocolTemplateName = protocolTemplateName,
        kotlinClientTransports = exportSettings.kotlinClientTransports.encodeTransportKinds(),
        cExposeTransports = exportSettings.cExposeTransports.encodeTransportKinds(),
        artifactKinds = exportSettings.artifactKinds.encodeArtifactKinds(),
        cOutputProjectDir = exportSettings.firmwareSync.cOutputProjectDir.cleanNullable(),
        bridgeImplPath = exportSettings.firmwareSync.bridgeImplPath.cleanNullable(),
        keilUvprojxPath = exportSettings.firmwareSync.keilUvprojxPath.cleanNullable(),
        keilTargetName = exportSettings.firmwareSync.keilTargetName.cleanNullable(),
        keilGroupName = exportSettings.firmwareSync.keilGroupName.cleanNullable(),
        mxprojectPath = exportSettings.firmwareSync.mxprojectPath.cleanNullable(),
        generationSettings = exportSettings.toGenericGenerationSettings(),
        availableContextDefinitions = availableDefinitions,
        classes = listOf(propertyCatalogClass, serviceClass) + methodModelClasses,
    )
}

internal fun CodegenContextDetailDto.toMetadataDraft(
    availableDefinitions: List<CodegenContextDefinitionDto> = this.availableContextDefinitions,
): CodegenMetadataDraftDto {
    val modelClasses = classes.filter { codegenClass -> codegenClass.classKind == CodegenClassKind.MODEL }
    val propertyPool = linkedMapOf<String, CodegenMetadataThingPropertyDraftDto>()
    val propertyKeyByName = mutableMapOf<String, String>()
    val propertyCatalog =
        modelClasses.firstOrNull { codegenClass -> codegenClass.className == PROPERTY_CATALOG_CLASS_NAME }
            ?.properties
            .orEmpty()

    fun ensureProperty(property: CodegenPropertyDto): String {
        val existingKey = property.propertyName.takeIf(String::isNotBlank)?.let(propertyKeyByName::get)
        if (existingKey != null) {
            return existingKey
        }
        val key = property.stablePropertyKey(propertyPool.size)
        val draft =
            CodegenMetadataThingPropertyDraftDto(
                key = key,
                id = property.id,
                name = property.name,
                description = property.description.cleanNullable(),
                sortIndex = property.sortIndex,
                nullable = property.nullable,
                defaultLiteral = property.defaultLiteral.cleanNullable(),
                bindings = property.bindings,
            )
        propertyPool[key] = draft
        property.propertyName.takeIf(String::isNotBlank)?.let { propertyName ->
            propertyKeyByName[propertyName] = key
        }
        return key
    }

    propertyCatalog.forEach(::ensureProperty)

    val deviceFunctions =
        classes.filter { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }
            .flatMapIndexed { index, serviceClass ->
                serviceClass.methods.mapIndexed { methodIndex, method ->
                    val selectedPropertyKeys =
                        method.boundModelProperties(modelClasses)
                            .map(::ensureProperty)
                    CodegenMetadataDeviceFunctionDraftDto(
                        key = method.stableFunctionKey(index * 100 + methodIndex),
                        id = method.id,
                        name = method.name,
                        description = method.description.cleanNullable(),
                        sortIndex = method.sortIndex,
                        thingPropertyKeys = selectedPropertyKeys.distinct(),
                        bindings = method.bindings,
                    )
                }
            }.sortedBy(CodegenMetadataDeviceFunctionDraftDto::sortIndex)

    return CodegenMetadataDraftDto(
        id = id,
        code = code,
        name = name,
        description = description.cleanNullable(),
        enabled = enabled,
        nodeId = nodeId.orEmpty(),
        consumerTarget = consumerTarget,
        protocolTemplateId = protocolTemplateId,
        protocolTemplateCode = protocolTemplateCode,
        protocolTemplateName = protocolTemplateName,
        exportSettings =
            generationSettings.toMetadataExportSettings(
                kotlinClientTransports = inferKotlinClientTransports(),
                cExposeTransports = inferCExposeTransports(),
                artifactKinds = inferArtifactKinds(),
                firmwareSync =
                    CodegenMetadataFirmwareSyncDto(
                        cOutputProjectDir = resolveFirmwareProjectDir(),
                        bridgeImplPath = bridgeImplPath.orEmpty(),
                        keilUvprojxPath = keilUvprojxPath.orEmpty(),
                        keilTargetName = keilTargetName.orEmpty(),
                        keilGroupName = keilGroupName.orEmpty(),
                        mxprojectPath = mxprojectPath.orEmpty(),
                    ),
            ),
        thingProperties = propertyPool.values.sortedBy(CodegenMetadataThingPropertyDraftDto::sortIndex),
        deviceFunctions = deviceFunctions,
    )
}

internal fun CodegenMetadataDraftDto.toMetadataPreview(
    availableDefinitions: List<CodegenContextDefinitionDto>,
): CodegenMetadataPreviewDto {
    val issues = mutableListOf<CodegenMetadataIssueDto>()
    issues += validateDraftSelections()
    val genericDetail = toGenericContextDetail(availableDefinitions)
    val normalized = genericDetail.normalizeGenericDetail()
    val propertyUsage = deviceFunctions.flatMap(CodegenMetadataDeviceFunctionDraftDto::thingPropertyKeys).groupingBy { it }.eachCount()
    val resolvedProperties =
        normalized.classes
            .firstOrNull { codegenClass -> codegenClass.className == PROPERTY_CATALOG_CLASS_NAME }
            ?.properties
            .orEmpty()
            .mapIndexed { index, property ->
                val source =
                    thingProperties.getOrNull(index)
                        ?: thingProperties.firstOrNull { item -> item.id == property.id }
                CodegenMetadataResolvedPropertyDto(
                    key = source?.key ?: property.stablePropertyKey(index),
                    id = property.id,
                    name = source?.name ?: property.name,
                    resolvedPropertyName = property.propertyName.cleanNullable(),
                    resolvedTypeName = property.typeName.cleanNullable(),
                    layoutSummary = property.bindingLayoutSummary(),
                    usageCount = propertyUsage[source?.key].orZero(),
                )
            }
    val resolvedFunctions =
        normalized.classes
            .filter { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }
            .flatMap { serviceClass ->
                serviceClass.methods.mapIndexed { index, method ->
                    val source =
                        deviceFunctions.firstOrNull { item -> item.id == method.id }
                            ?: deviceFunctions.getOrNull(index)
                    CodegenMetadataResolvedFunctionDto(
                        key = source?.key ?: method.stableFunctionKey(index),
                        id = method.id,
                        name = source?.name ?: method.name,
                        resolvedMethodName = method.methodName.cleanNullable(),
                        direction = source?.bindingValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_DIRECTION_PARAM),
                        requestModelName = method.requestClassName.cleanNullable(),
                        responseModelName = method.responseClassName.cleanNullable(),
                        layoutSummary = source?.bindingLayoutSummary(),
                        thingPropertyCount = source?.thingPropertyKeys?.size ?: 0,
                    )
                }
            }

    val exportPlans = buildExportPlans(issues)

    runCatching {
        val schemas = normalized.copy(availableContextDefinitions = availableDefinitions).toModbusSpecs()
        schemas.forEach { schema ->
            validateModbusSchema(schema, Regex("[A-Za-z_][A-Za-z0-9_]*"))
        }
    }.onFailure { throwable ->
        issues += throwable.toIssue("preview")
    }

    return CodegenMetadataPreviewDto(
        resolvedProperties = resolvedProperties,
        resolvedFunctions = resolvedFunctions,
        exportPlans = exportPlans,
        issues = issues.distinctBy { issue -> issue.location + issue.message },
    )
}

internal fun CodegenMetadataDraftDto.validateDraftOrThrow(
    availableDefinitions: List<CodegenContextDefinitionDto>,
) {
    val firstError =
        toMetadataPreview(availableDefinitions)
            .issues
            .firstOrNull { issue -> issue.severity == MetadataIssueSeverity.ERROR }
    if (firstError != null) {
        throw BusinessValidationException(firstError.message)
    }
}

private fun CodegenMetadataDraftDto.validateDraftSelections(): List<CodegenMetadataIssueDto> {
    val issues = mutableListOf<CodegenMetadataIssueDto>()
    if (protocolTemplateId <= 0L) {
        issues += CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, "protocolTemplateId", "必须先选择协议模板。")
    }
    if (code.isBlank()) {
        issues += CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, "code", "上下文编码不能为空。")
    }
    if (name.isBlank()) {
        issues += CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, "name", "上下文名称不能为空。")
    }
    if (exportSettings.artifactKinds.isEmpty()) {
        issues += CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, "artifactKinds", "至少选择一个导出目标。")
    }
    if (MetadataArtifactKind.METADATA_SNAPSHOT in exportSettings.artifactKinds &&
        exportSettings.kotlinClientTransports.isEmpty()
    ) {
        issues += CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, "kotlinClientTransports", "Kotlin 调用侧至少选择一个 transport。")
    }
    if (exportSettings.artifactKinds.any { artifact ->
            artifact in setOf(
                MetadataArtifactKind.C_SERVICE_CONTRACT,
                MetadataArtifactKind.C_TRANSPORT_CONTRACT,
                MetadataArtifactKind.MARKDOWN_PROTOCOL,
            )
        } &&
        exportSettings.cExposeTransports.isEmpty()
    ) {
        issues += CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, "cExposeTransports", "C 暴露侧至少选择一个 transport。")
    }
    if (exportSettings.requiresFirmwareProjectDir() && exportSettings.firmwareSync.cOutputProjectDir.isBlank()) {
        issues += CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, "cOutputProjectDir", "导出 C/Markdown 时必须填写 C 工程根目录。")
    }
    exportSettings.firmwareSync.cOutputProjectDir.cleanNullable()?.let { rawPath ->
        val path = runCatching { rawPath.toExpandedPath() }.getOrNull()
        when {
            path == null ->
                issues += CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, "cOutputProjectDir", "C 工程根目录不是合法路径。")
            !path.toFile().exists() ->
                issues += CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, "cOutputProjectDir", "C 工程根目录不存在。")
        }
    }
    issues += exportSettings.validateDefaultFields()
    return issues
}

private fun CodegenMetadataDraftDto.buildExportPlans(
    issues: List<CodegenMetadataIssueDto>,
): List<CodegenMetadataExportPlanDto> {
    val blockingLocations =
        issues.filter { issue -> issue.severity == MetadataIssueSeverity.ERROR }
            .map(CodegenMetadataIssueDto::location)
            .toSet()
    val plans = mutableListOf<CodegenMetadataExportPlanDto>()
    if (MetadataArtifactKind.METADATA_SNAPSHOT in exportSettings.artifactKinds) {
        exportSettings.kotlinClientTransports.forEach { transport ->
            plans +=
                CodegenMetadataExportPlanDto(
                    artifactKind = MetadataArtifactKind.METADATA_SNAPSHOT,
                    transport = transport,
                    ready = "kotlinClientTransports" !in blockingLocations && "protocolTemplateId" !in blockingLocations,
                    summary = "将把 $transport 的 metadata snapshot 写入 $METADATA_TABLE_NAME。",
                )
        }
    }
    exportSettings.cExposeTransports.forEach { transport ->
        exportSettings.artifactKinds
            .filterNot { artifact -> artifact == MetadataArtifactKind.METADATA_SNAPSHOT }
            .forEach { artifact ->
                plans +=
                    CodegenMetadataExportPlanDto(
                        artifactKind = artifact,
                        transport = transport,
                        ready = "cOutputProjectDir" !in blockingLocations && "cExposeTransports" !in blockingLocations,
                        summary =
                            when (artifact) {
                                MetadataArtifactKind.C_SERVICE_CONTRACT -> "生成 $transport 的 service contract。"
                                MetadataArtifactKind.C_TRANSPORT_CONTRACT -> "生成 $transport 的 transport dispatch。"
                                MetadataArtifactKind.MARKDOWN_PROTOCOL -> "生成 $transport 的协议文档。"
                                MetadataArtifactKind.METADATA_SNAPSHOT -> "将把 metadata snapshot 写入数据库。"
                            },
                    )
            }
    }
    return plans
}

private fun CodegenMetadataExportSettingsDto.requiresFirmwareProjectDir(): Boolean =
    artifactKinds.any { artifact ->
        artifact in setOf(
            MetadataArtifactKind.C_SERVICE_CONTRACT,
            MetadataArtifactKind.C_TRANSPORT_CONTRACT,
            MetadataArtifactKind.MARKDOWN_PROTOCOL,
        )
    }

private fun CodegenMetadataExportSettingsDto.validateDefaultFields(): List<CodegenMetadataIssueDto> {
    val issues = mutableListOf<CodegenMetadataIssueDto>()
    issues += validatePositiveLong(rtuDefaults.timeoutMs, "rtu.timeoutMs")
    issues += validatePositiveInt(rtuDefaults.unitId, "rtu.unitId")
    issues += validatePositiveInt(rtuDefaults.baudRate, "rtu.baudRate")
    issues += validatePositiveInt(rtuDefaults.dataBits, "rtu.dataBits")
    issues += validatePositiveInt(rtuDefaults.stopBits, "rtu.stopBits")
    issues += validateNonNegativeInt(rtuDefaults.retries, "rtu.retries")
    issues += validatePositiveLong(tcpDefaults.timeoutMs, "tcp.timeoutMs")
    issues += validatePositiveInt(tcpDefaults.port, "tcp.port")
    issues += validatePositiveInt(tcpDefaults.unitId, "tcp.unitId")
    issues += validateNonNegativeInt(tcpDefaults.retries, "tcp.retries")
    issues += validatePositiveLong(mqttDefaults.timeoutMs, "mqtt.timeoutMs")
    issues += validateNonNegativeInt(mqttDefaults.retries, "mqtt.retries")
    issues += validateRangeInt(mqttDefaults.qos, "mqtt.qos", 0..2)
    return issues
}

private fun validatePositiveInt(
    rawValue: String,
    location: String,
): List<CodegenMetadataIssueDto> =
    when {
        rawValue.isBlank() -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 不能为空。"))
        rawValue.toIntOrNull() == null -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 必须是整数。"))
        rawValue.toInt() <= 0 -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 必须大于 0。"))
        else -> emptyList()
    }

private fun validateNonNegativeInt(
    rawValue: String,
    location: String,
): List<CodegenMetadataIssueDto> =
    when {
        rawValue.isBlank() -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 不能为空。"))
        rawValue.toIntOrNull() == null -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 必须是整数。"))
        rawValue.toInt() < 0 -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 不能小于 0。"))
        else -> emptyList()
    }

private fun validatePositiveLong(
    rawValue: String,
    location: String,
): List<CodegenMetadataIssueDto> =
    when {
        rawValue.isBlank() -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 不能为空。"))
        rawValue.toLongOrNull() == null -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 必须是整数。"))
        rawValue.toLong() <= 0L -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 必须大于 0。"))
        else -> emptyList()
    }

private fun validateRangeInt(
    rawValue: String,
    location: String,
    allowedRange: IntRange,
): List<CodegenMetadataIssueDto> {
    val intValue = rawValue.toIntOrNull()
    return when {
        rawValue.isBlank() -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 不能为空。"))
        intValue == null -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 必须是整数。"))
        intValue !in allowedRange -> listOf(CodegenMetadataIssueDto(MetadataIssueSeverity.ERROR, location, "$location 必须在 ${allowedRange.first}-${allowedRange.last} 之间。"))
        else -> emptyList()
    }
}

internal fun CodegenMetadataExportSettingsDto.toGenericGenerationSettings(): CodegenGenerationSettingsDto =
    CodegenGenerationSettingsDto(
        rtuDefaults =
            CodegenRtuGenerationDefaultsDto(
                portPath = rtuDefaults.portPath,
                unitId = rtuDefaults.unitId.toIntOrNull() ?: 0,
                baudRate = rtuDefaults.baudRate.toIntOrNull() ?: 0,
                dataBits = rtuDefaults.dataBits.toIntOrNull() ?: 0,
                stopBits = rtuDefaults.stopBits.toIntOrNull() ?: 0,
                parity = rtuDefaults.parity,
                timeoutMs = rtuDefaults.timeoutMs.toLongOrNull() ?: 0,
                retries = rtuDefaults.retries.toIntOrNull() ?: -1,
            ),
        tcpDefaults =
            CodegenTcpGenerationDefaultsDto(
                host = tcpDefaults.host,
                port = tcpDefaults.port.toIntOrNull() ?: 0,
                unitId = tcpDefaults.unitId.toIntOrNull() ?: 0,
                timeoutMs = tcpDefaults.timeoutMs.toLongOrNull() ?: 0,
                retries = tcpDefaults.retries.toIntOrNull() ?: -1,
            ),
        mqttDefaults =
            CodegenMqttGenerationDefaultsDto(
                brokerUrl = mqttDefaults.brokerUrl,
                clientId = mqttDefaults.clientId,
                requestTopic = mqttDefaults.requestTopic,
                responseTopic = mqttDefaults.responseTopic,
                qos = mqttDefaults.qos.toIntOrNull() ?: -1,
                timeoutMs = mqttDefaults.timeoutMs.toLongOrNull() ?: 0,
                retries = mqttDefaults.retries.toIntOrNull() ?: -1,
            ),
    )

private fun CodegenGenerationSettingsDto.toMetadataExportSettings(
    kotlinClientTransports: Set<CodegenMetadataTransportKind>,
    cExposeTransports: Set<CodegenMetadataTransportKind>,
    artifactKinds: Set<MetadataArtifactKind>,
    firmwareSync: CodegenMetadataFirmwareSyncDto,
): site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto =
    site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportSettingsDto(
        artifactKinds = artifactKinds.ifEmpty { setOf(MetadataArtifactKind.METADATA_SNAPSHOT) },
        kotlinClientTransports = kotlinClientTransports,
        cExposeTransports = cExposeTransports,
        firmwareSync = firmwareSync,
        rtuDefaults =
            CodegenMetadataRtuDefaultsDraftDto(
                portPath = rtuDefaults.portPath,
                unitId = rtuDefaults.unitId.toString(),
                baudRate = rtuDefaults.baudRate.toString(),
                dataBits = rtuDefaults.dataBits.toString(),
                stopBits = rtuDefaults.stopBits.toString(),
                parity = rtuDefaults.parity,
                timeoutMs = rtuDefaults.timeoutMs.toString(),
                retries = rtuDefaults.retries.toString(),
            ),
        tcpDefaults =
            CodegenMetadataTcpDefaultsDraftDto(
                host = tcpDefaults.host,
                port = tcpDefaults.port.toString(),
                unitId = tcpDefaults.unitId.toString(),
                timeoutMs = tcpDefaults.timeoutMs.toString(),
                retries = tcpDefaults.retries.toString(),
            ),
        mqttDefaults =
            CodegenMetadataMqttDefaultsDraftDto(
                brokerUrl = mqttDefaults.brokerUrl,
                clientId = mqttDefaults.clientId,
                requestTopic = mqttDefaults.requestTopic,
                responseTopic = mqttDefaults.responseTopic,
                qos = mqttDefaults.qos.toString(),
                timeoutMs = mqttDefaults.timeoutMs.toString(),
                retries = mqttDefaults.retries.toString(),
            ),
    )

private fun CodegenMetadataThingPropertyDraftDto.toGenericProperty(): CodegenPropertyDto =
    CodegenPropertyDto(
        id = id,
        name = name,
        description = description.cleanNullable(),
        sortIndex = sortIndex,
        propertyName = "",
        typeName = "",
        nullable = nullable,
        defaultLiteral = defaultLiteral.cleanNullable(),
        bindings = bindings,
    )

private fun CodegenMethodDto.boundModelProperties(
    modelClasses: List<CodegenClassDto>,
): List<CodegenPropertyDto> {
    val direction = bindings.bindingValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_DIRECTION_PARAM)
    val targetClassName =
        if (direction == "WRITE") {
            requestClassName
        } else {
            responseClassName
        }
    return modelClasses.firstOrNull { model -> model.className == targetClassName }?.properties.orEmpty()
}

private fun CodegenMethodDto.stableFunctionKey(
    index: Int,
): String =
    id?.let { functionId -> "function-$functionId" } ?: methodName.cleanNullable()?.let { method -> "function-$method" } ?: "function-$index"

private fun CodegenPropertyDto.stablePropertyKey(
    index: Int,
): String =
    id?.let { propertyId -> "property-$propertyId" } ?: propertyName.cleanNullable()?.let { property -> "property-$property" } ?: "property-$index"

private fun CodegenMetadataDeviceFunctionDraftDto.bindingValue(
    definitionCode: String,
    paramCode: String,
): String? =
    bindings.bindingValue(definitionCode, paramCode)

private fun List<CodegenContextBindingDto>.bindingValue(
    definitionCode: String,
    paramCode: String,
): String? =
    firstOrNull { binding -> binding.definitionCode == definitionCode }
        ?.values
        ?.firstOrNull { value -> value.paramCode == paramCode }
        ?.value
        ?.cleanNullable()

private fun CodegenPropertyDto.bindingLayoutSummary(): String? {
    val transportType = bindings.bindingValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_TRANSPORT_TYPE_PARAM)
    val registerOffset = bindings.bindingValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_REGISTER_OFFSET_PARAM)
    val bitOffset = bindings.bindingValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_BIT_OFFSET_PARAM)
    val length = bindings.bindingValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_LENGTH_PARAM)
    return listOfNotNull(
        transportType?.let { value -> "transportType=$value" },
        registerOffset?.let { value -> "registerOffset=$value" },
        bitOffset?.let { value -> "bitOffset=$value" },
        length?.let { value -> "length=$value" },
    ).takeIf(List<String>::isNotEmpty)?.joinToString(" · ")
}

private fun CodegenMetadataDeviceFunctionDraftDto.bindingLayoutSummary(): String? {
    val direction = bindingValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_DIRECTION_PARAM)
    val functionCode = bindingValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_FUNCTION_CODE_PARAM)
    val baseAddress = bindingValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_BASE_ADDRESS_PARAM)
    return listOfNotNull(
        direction?.let { value -> "direction=$value" },
        functionCode?.let { value -> "functionCode=$value" },
        baseAddress?.let { value -> "baseAddress=$value" },
    ).takeIf(List<String>::isNotEmpty)?.joinToString(" · ")
}

internal fun Set<CodegenMetadataTransportKind>.encodeTransportKinds(): String? =
    takeIf(Set<CodegenMetadataTransportKind>::isNotEmpty)?.joinToString(",") { transport -> transport.name }

internal fun String?.decodeTransportKinds(): Set<CodegenMetadataTransportKind> =
    this?.split(',')
        ?.map(String::trim)
        ?.mapNotNull { token ->
            CodegenMetadataTransportKind.entries.firstOrNull { transport -> transport.name.equals(token, ignoreCase = true) }
        }?.toSet()
        .orEmpty()

internal fun Set<MetadataArtifactKind>.encodeArtifactKinds(): String? =
    takeIf(Set<MetadataArtifactKind>::isNotEmpty)?.joinToString(",") { artifact -> artifact.name }

internal fun String?.decodeArtifactKinds(): Set<MetadataArtifactKind> =
    this?.split(',')
        ?.map(String::trim)
        ?.mapNotNull { token ->
            MetadataArtifactKind.entries.firstOrNull { artifact -> artifact.name.equals(token, ignoreCase = true) }
        }?.toSet()
        .orEmpty()

private fun Throwable.toIssue(
    location: String,
): CodegenMetadataIssueDto =
    CodegenMetadataIssueDto(
        severity = MetadataIssueSeverity.ERROR,
        location = location,
        message = message ?: "预检失败。",
    )

private fun Int?.orZero(): Int = this ?: 0

private fun CodegenContextDetailDto.inferKotlinClientTransports(): Set<CodegenMetadataTransportKind> =
    kotlinClientTransports.decodeTransportKinds().ifEmpty { setOf(defaultMetadataTransport()) }

private fun CodegenContextDetailDto.inferCExposeTransports(): Set<CodegenMetadataTransportKind> =
    cExposeTransports.decodeTransportKinds().ifEmpty {
        when {
            hasLegacyExternalOutputs() -> CodegenMetadataTransportKind.entries.toSet()
            else -> emptySet()
        }
    }

private fun CodegenContextDetailDto.inferArtifactKinds(): Set<MetadataArtifactKind> =
    artifactKinds.decodeArtifactKinds().ifEmpty {
        buildSet {
            add(MetadataArtifactKind.METADATA_SNAPSHOT)
            if (hasLegacyExternalOutputs()) {
                add(MetadataArtifactKind.C_SERVICE_CONTRACT)
                add(MetadataArtifactKind.C_TRANSPORT_CONTRACT)
                add(MetadataArtifactKind.MARKDOWN_PROTOCOL)
            }
        }
    }

private fun CodegenContextDetailDto.resolveFirmwareProjectDir(): String =
    cOutputProjectDir.cleanNullable()
        ?: externalCOutputRoot.cleanNullable()
        ?: generationSettings.cOutputRoot.parentDirectoryString()
        ?: generationSettings.markdownOutputRoot.parentDirectoryString()
        ?: ""

private fun CodegenContextDetailDto.hasLegacyExternalOutputs(): Boolean =
    externalCOutputRoot.cleanNullable() != null ||
        generationSettings.cOutputRoot.cleanNullable() != null ||
        generationSettings.markdownOutputRoot.cleanNullable() != null

private fun CodegenContextDetailDto.defaultMetadataTransport(): CodegenMetadataTransportKind =
    when (protocolTemplateCode) {
        "MODBUS_TCP_CLIENT" -> CodegenMetadataTransportKind.TCP
        else -> CodegenMetadataTransportKind.RTU
    }

internal fun CodegenMetadataDraftDto.toModelingWorkbenchExportDraft(): CodegenMetadataDraftDto =
    copy(
        exportSettings =
            exportSettings.copy(
                artifactKinds =
                    setOf(
                        MetadataArtifactKind.C_SERVICE_CONTRACT,
                        MetadataArtifactKind.C_TRANSPORT_CONTRACT,
                        MetadataArtifactKind.MARKDOWN_PROTOCOL,
                    ),
                kotlinClientTransports = emptySet(),
                cExposeTransports = CodegenMetadataTransportKind.entries.toSet(),
            ),
    )

private fun String?.parentDirectoryString(): String? {
    val candidate = this.cleanNullable() ?: return null
    return runCatching { candidate.toExpandedPath().parent?.toString() }.getOrNull()
}
