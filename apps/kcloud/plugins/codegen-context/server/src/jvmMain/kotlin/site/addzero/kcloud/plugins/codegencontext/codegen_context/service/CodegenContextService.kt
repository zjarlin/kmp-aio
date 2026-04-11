package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.sql.Connection
import java.nio.file.InvalidPathException
import java.nio.file.Path
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.core.annotation.Single
import kotlinx.serialization.json.Json
import site.addzero.kcloud.jimmer.di.insertAndReturnId
import site.addzero.kcloud.jimmer.di.query
import site.addzero.kcloud.jimmer.di.queryCount
import site.addzero.kcloud.jimmer.di.update
import site.addzero.kcloud.jimmer.di.withTransaction
import site.addzero.kcloud.plugins.codegencontext.api.context.*
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.*
import site.addzero.kmp.exp.BusinessValidationException
import site.addzero.kmp.exp.ConflictException
import site.addzero.kmp.exp.NotFoundException
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenBindingTargetMode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenClassKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenNodeKind
import site.addzero.kcloud.plugins.hostconfig.model.entity.*
import site.addzero.kcloud.plugins.hostconfig.service.Fetchers
import site.addzero.util.str.toGeneratedMethodName
import site.addzero.util.str.toGeneratedPropertyName
import site.addzero.util.str.toGeneratedTypeName

@Single
/**
 * 提供代码生成上下文的查询、保存与生成服务。
 *
 * @property sql Jimmer SQL 客户端。
 * @property contractGenerator contractgenerator。
 */
class CodegenContextService(
    private val sql: KSqlClient,
    private val contractGenerator: CodegenContextContractGenerator,
) {
    private companion object {
        val IDENTIFIER_PATTERN = Regex("[A-Za-z_][A-Za-z0-9_]*")
        val CODE_PATTERN = Regex("[A-Za-z][A-Za-z0-9_]*")
        val PACKAGE_PATTERN = Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*")
        val JSON = Json { ignoreUnknownKeys = true }
        const val MODBUS_FIELD_DEFINITION_CODE = "MODBUS_FIELD"
        const val FIELD_TRANSPORT_TYPE_PARAM = "transportType"
        val SUPPORTED_PROTOCOL_TEMPLATE_CODES = setOf(
            "MODBUS_RTU_CLIENT",
            "MODBUS_TCP_CLIENT",
        )
    }

    /**
     * 列出上下文。
     */
    fun listContexts(): List<CodegenContextSummaryDto> {
        return sql.createQuery(CodegenContext::class) {
            orderBy(table.name.asc(), table.id.asc())
            select(table.fetch(CodegenContextFetchers.contextSummary))
        }.execute().map { context ->
            context.toSummaryDto()
        }
    }

    /**
     * 获取上下文。
     *
     * @param contextId 上下文 ID。
     */
    fun getContext(
        contextId: Long,
    ): CodegenContextDetailDto {
        return loadContext(contextId).toDetailDto()
    }

    /**
     * 获取元数据草稿。
     *
     * @param contextId 上下文 ID。
     */
    fun getContextDraft(
        contextId: Long,
    ): CodegenMetadataDraftDto {
        val detail = getContext(contextId)
        val availableDefinitions = loadContextDefinitions(detail.protocolTemplateId)
        return resolveNodeScopedDefaults(detail.toMetadataDraft(availableDefinitions))
    }

    /**
     * 列出上下文定义。
     *
     * @param protocolTemplateId 协议模板 ID。
     */
    fun listContextDefinitions(
        protocolTemplateId: Long,
    ): List<CodegenContextDefinitionDto> {
        ensureSupportedTemplate(loadProtocolTemplate(protocolTemplateId))
        return loadContextDefinitions(protocolTemplateId)
    }

    /**
     * 预检元数据草稿。
     *
     * @param request 草稿请求。
     */
    fun previewContextDraft(
        request: CodegenMetadataDraftDto,
    ): CodegenMetadataPreviewDto {
        val resolvedDraft = resolveNodeScopedDefaults(request)
        val availableDefinitions =
            if (resolvedDraft.protocolTemplateId > 0L) {
                ensureSupportedTemplate(loadProtocolTemplate(resolvedDraft.protocolTemplateId))
                loadContextDefinitions(resolvedDraft.protocolTemplateId)
            } else {
                emptyList()
            }
        return resolvedDraft.toMetadataPreview(availableDefinitions)
    }

    /**
     * 保存上下文。
     *
     * @param request 请求参数。
     */
    fun saveContext(
        request: CodegenContextDetailDto,
    ): CodegenContextDetailDto {
        val normalized = request.normalizeGenericDetail()
        val protocolTemplate = loadProtocolTemplate(normalized.protocolTemplateId)
        ensureSupportedTemplate(protocolTemplate)
        val availableDefinitions = loadContextDefinitions(normalized.protocolTemplateId)
        validate(normalized, availableDefinitions)
        val generationSettings = normalized.generationSettings
        val contextId =
            sql.withTransaction { connection ->
                val existingId = normalized.id
                val now = nowSqlValue()
                val resolvedId =
                    if (existingId == null) {
                        ensureCodeUnique(connection, normalized.code, null)
                        sql.insertAndReturnId(
                            connection,
                            """
                            INSERT INTO codegen_context_context (
                                code,
                                name,
                                description,
                                enabled,
                                node_id,
                                consumer_target,
                                protocol_template_id,
                                external_c_output_root,
                                server_output_root,
                                shared_output_root,
                                gateway_output_root,
                                api_client_output_root,
                                api_client_package_name,
                                spring_route_output_root,
                                c_output_root,
                                markdown_output_root,
                                kotlin_client_transports,
                                c_expose_transports,
                                artifact_kinds,
                                c_output_project_dir,
                                bridge_impl_path,
                                keil_uvprojx_path,
                                keil_target_name,
                                keil_group_name,
                                mxproject_path,
                                rtu_port_path,
                                rtu_unit_id,
                                rtu_baud_rate,
                                rtu_data_bits,
                                rtu_stop_bits,
                                rtu_parity,
                                rtu_timeout_ms,
                                rtu_retries,
                                tcp_host,
                                tcp_port,
                                tcp_unit_id,
                                tcp_timeout_ms,
                                tcp_retries,
                                mqtt_broker_url,
                                mqtt_client_id,
                                mqtt_request_topic,
                                mqtt_response_topic,
                                mqtt_qos,
                                mqtt_timeout_ms,
                                mqtt_retries,
                                created_at,
                                updated_at
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                            normalized.code,
                            normalized.name,
                            normalized.description,
                            if (normalized.enabled) 1 else 0,
                            normalized.nodeId,
                            CodegenConsumerTarget.MCU_CONSOLE.name,
                            normalized.protocolTemplateId,
                            normalized.externalCOutputRoot,
                            generationSettings.serverOutputRoot,
                            generationSettings.sharedOutputRoot,
                            generationSettings.gatewayOutputRoot,
                            generationSettings.apiClientOutputRoot,
                            generationSettings.apiClientPackageName,
                            generationSettings.springRouteOutputRoot,
                            generationSettings.cOutputRoot,
                            generationSettings.markdownOutputRoot,
                            normalized.kotlinClientTransports,
                            normalized.cExposeTransports,
                            normalized.artifactKinds,
                            normalized.cOutputProjectDir,
                            normalized.bridgeImplPath,
                            normalized.keilUvprojxPath,
                            normalized.keilTargetName,
                            normalized.keilGroupName,
                            normalized.mxprojectPath,
                            generationSettings.rtuDefaults.portPath,
                            generationSettings.rtuDefaults.unitId,
                            generationSettings.rtuDefaults.baudRate,
                            generationSettings.rtuDefaults.dataBits,
                            generationSettings.rtuDefaults.stopBits,
                            generationSettings.rtuDefaults.parity,
                            generationSettings.rtuDefaults.timeoutMs,
                            generationSettings.rtuDefaults.retries,
                            generationSettings.tcpDefaults.host,
                            generationSettings.tcpDefaults.port,
                            generationSettings.tcpDefaults.unitId,
                            generationSettings.tcpDefaults.timeoutMs,
                            generationSettings.tcpDefaults.retries,
                            generationSettings.mqttDefaults.brokerUrl,
                            generationSettings.mqttDefaults.clientId,
                            generationSettings.mqttDefaults.requestTopic,
                            generationSettings.mqttDefaults.responseTopic,
                            generationSettings.mqttDefaults.qos,
                            generationSettings.mqttDefaults.timeoutMs,
                            generationSettings.mqttDefaults.retries,
                            now,
                            now,
                        )
                    } else {
                        ensureContextExists(existingId)
                        ensureCodeUnique(connection, normalized.code, existingId)
                        sql.update(
                            connection,
                            """
                            UPDATE codegen_context_context
                            SET code = ?,
                                name = ?,
                                description = ?,
                                enabled = ?,
                                node_id = ?,
                                consumer_target = ?,
                                protocol_template_id = ?,
                                external_c_output_root = ?,
                                server_output_root = ?,
                                shared_output_root = ?,
                                gateway_output_root = ?,
                                api_client_output_root = ?,
                                api_client_package_name = ?,
                                spring_route_output_root = ?,
                                c_output_root = ?,
                                markdown_output_root = ?,
                                kotlin_client_transports = ?,
                                c_expose_transports = ?,
                                artifact_kinds = ?,
                                c_output_project_dir = ?,
                                bridge_impl_path = ?,
                                keil_uvprojx_path = ?,
                                keil_target_name = ?,
                                keil_group_name = ?,
                                mxproject_path = ?,
                                rtu_port_path = ?,
                                rtu_unit_id = ?,
                                rtu_baud_rate = ?,
                                rtu_data_bits = ?,
                                rtu_stop_bits = ?,
                                rtu_parity = ?,
                                rtu_timeout_ms = ?,
                                rtu_retries = ?,
                                tcp_host = ?,
                                tcp_port = ?,
                                tcp_unit_id = ?,
                                tcp_timeout_ms = ?,
                                tcp_retries = ?,
                                mqtt_broker_url = ?,
                                mqtt_client_id = ?,
                                mqtt_request_topic = ?,
                                mqtt_response_topic = ?,
                                mqtt_qos = ?,
                                mqtt_timeout_ms = ?,
                                mqtt_retries = ?,
                                updated_at = ?
                            WHERE id = ?
                            """.trimIndent(),
                            normalized.code,
                            normalized.name,
                            normalized.description,
                            if (normalized.enabled) 1 else 0,
                            normalized.nodeId,
                            CodegenConsumerTarget.MCU_CONSOLE.name,
                            normalized.protocolTemplateId,
                            normalized.externalCOutputRoot,
                            generationSettings.serverOutputRoot,
                            generationSettings.sharedOutputRoot,
                            generationSettings.gatewayOutputRoot,
                            generationSettings.apiClientOutputRoot,
                            generationSettings.apiClientPackageName,
                            generationSettings.springRouteOutputRoot,
                            generationSettings.cOutputRoot,
                            generationSettings.markdownOutputRoot,
                            normalized.kotlinClientTransports,
                            normalized.cExposeTransports,
                            normalized.artifactKinds,
                            normalized.cOutputProjectDir,
                            normalized.bridgeImplPath,
                            normalized.keilUvprojxPath,
                            normalized.keilTargetName,
                            normalized.keilGroupName,
                            normalized.mxprojectPath,
                            generationSettings.rtuDefaults.portPath,
                            generationSettings.rtuDefaults.unitId,
                            generationSettings.rtuDefaults.baudRate,
                            generationSettings.rtuDefaults.dataBits,
                            generationSettings.rtuDefaults.stopBits,
                            generationSettings.rtuDefaults.parity,
                            generationSettings.rtuDefaults.timeoutMs,
                            generationSettings.rtuDefaults.retries,
                            generationSettings.tcpDefaults.host,
                            generationSettings.tcpDefaults.port,
                            generationSettings.tcpDefaults.unitId,
                            generationSettings.tcpDefaults.timeoutMs,
                            generationSettings.tcpDefaults.retries,
                            generationSettings.mqttDefaults.brokerUrl,
                            generationSettings.mqttDefaults.clientId,
                            generationSettings.mqttDefaults.requestTopic,
                            generationSettings.mqttDefaults.responseTopic,
                            generationSettings.mqttDefaults.qos,
                            generationSettings.mqttDefaults.timeoutMs,
                            generationSettings.mqttDefaults.retries,
                            now,
                            existingId,
                        )
                        existingId
                    }
                replaceClasses(
                    connection = connection,
                    contextId = resolvedId,
                    protocolTemplateId = normalized.protocolTemplateId,
                    classes = normalized.classes,
                    availableDefinitions = availableDefinitions,
                )
                resolvedId
            }
        return getContext(contextId)
    }

    /**
     * 保存元数据草稿。
     *
     * @param request 草稿请求。
     */
    fun saveContextDraft(
        request: CodegenMetadataDraftDto,
    ): CodegenMetadataDraftDto {
        val resolvedDraft = resolveNodeScopedDefaults(request)
        if (resolvedDraft.protocolTemplateId <= 0L) {
            throw BusinessValidationException("必须先选择协议模板。")
        }
        ensureSupportedTemplate(loadProtocolTemplate(resolvedDraft.protocolTemplateId))
        val availableDefinitions = loadContextDefinitions(resolvedDraft.protocolTemplateId)
        val saved = saveContext(resolvedDraft.toGenericContextDetail(availableDefinitions))
        return resolveNodeScopedDefaults(saved.toMetadataDraft(loadContextDefinitions(saved.protocolTemplateId)))
    }

    /**
     * 删除上下文。
     *
     * @param contextId 上下文 ID。
     */
    fun deleteContext(
        contextId: Long,
    ) {
        ensureContextExists(contextId)
        sql.withTransaction { connection ->
            sql.update(connection, "DELETE FROM codegen_context_context WHERE id = ?", contextId)
        }
    }

    /**
     * 处理generatecontracts。
     *
     * @param contextId 上下文 ID。
     */
    fun generateContracts(
        contextId: Long,
    ): GenerateContractsResponseDto {
        val result = exportContext(contextId)
        return GenerateContractsResponseDto(
            contextId = result.contextId,
            generatedFiles =
                result.generatedArtifacts.map(CodegenGeneratedArtifactDto::path) +
                    result.projectSyncResults.mapNotNull(CodegenProjectSyncResultDto::filePath),
            message = result.message,
        )
    }

    /**
     * 导出元数据上下文。
     *
     * @param contextId 上下文 ID。
     */
    fun exportContext(
        contextId: Long,
    ): CodegenMetadataExportResultDto {
        val detail = getContext(contextId)
        val availableDefinitions = loadContextDefinitions(detail.protocolTemplateId)
        val exportDraft = detail.toMetadataDraft(availableDefinitions).toModelingWorkbenchExportDraft()
        exportDraft.validateDraftOrThrow(availableDefinitions)
        val normalized = exportDraft.toGenericContextDetail(availableDefinitions).normalizeGenericDetail()
        return contractGenerator.export(normalized, exportDraft.exportSettings)
    }

    /**
     * 替换类。
     *
     * @param connection 数据库连接。
     * @param contextId 上下文 ID。
     * @param protocolTemplateId 协议模板 ID。
     * @param classes 类。
     * @param availableDefinitions 可用定义。
     */
    private fun replaceClasses(
        connection: Connection,
        contextId: Long,
        protocolTemplateId: Long,
        classes: List<CodegenClassDto>,
        availableDefinitions: List<CodegenContextDefinitionDto>,
    ) {
        sql.update(connection, "DELETE FROM codegen_context_class WHERE context_id = ?", contextId)
        if (classes.isEmpty()) {
            return
        }
        classes.sortedBy(CodegenClassDto::sortIndex).forEach { classDto ->
            val classId =
                sql.insertAndReturnId(
                    connection,
                    """
                    INSERT INTO codegen_context_class (
                        context_id,
                        name,
                        description,
                        sort_index,
                        class_kind,
                        class_name,
                        package_name,
                        created_at,
                        updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    contextId,
                    classDto.name,
                    classDto.description,
                    classDto.sortIndex,
                    classDto.classKind.name,
                    classDto.className,
                    classDto.packageName,
                    nowSqlValue(),
                    nowSqlValue(),
                )
            insertBindings(
                connection = connection,
                protocolTemplateId = protocolTemplateId,
                availableDefinitions = availableDefinitions,
                bindings = classDto.bindings,
                ownerClassId = classId,
            )
            classDto.methods.sortedBy(CodegenMethodDto::sortIndex).forEach { methodDto ->
                val methodId =
                    sql.insertAndReturnId(
                        connection,
                        """
                        INSERT INTO codegen_context_method (
                            owner_class_id,
                            name,
                            description,
                            sort_index,
                            method_name,
                            request_class_name,
                            response_class_name,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """.trimIndent(),
                        classId,
                        methodDto.name,
                        methodDto.description,
                        methodDto.sortIndex,
                        methodDto.methodName,
                        methodDto.requestClassName,
                        methodDto.responseClassName,
                        nowSqlValue(),
                        nowSqlValue(),
                    )
                insertBindings(
                    connection = connection,
                    protocolTemplateId = protocolTemplateId,
                    availableDefinitions = availableDefinitions,
                    bindings = methodDto.bindings,
                    ownerMethodId = methodId,
                )
            }
            classDto.properties.sortedBy(CodegenPropertyDto::sortIndex).forEach { propertyDto ->
                val propertyId =
                    sql.insertAndReturnId(
                        connection,
                        """
                        INSERT INTO codegen_context_property (
                            owner_class_id,
                            name,
                            description,
                            sort_index,
                            property_name,
                            type_name,
                            nullable,
                            default_literal,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """.trimIndent(),
                        classId,
                        propertyDto.name,
                        propertyDto.description,
                        propertyDto.sortIndex,
                        propertyDto.propertyName,
                        propertyDto.typeName,
                        if (propertyDto.nullable) 1 else 0,
                        propertyDto.defaultLiteral,
                        nowSqlValue(),
                        nowSqlValue(),
                    )
                insertBindings(
                    connection = connection,
                    protocolTemplateId = protocolTemplateId,
                    availableDefinitions = availableDefinitions,
                    bindings = propertyDto.bindings,
                    ownerPropertyId = propertyId,
                )
            }
        }
    }

    /**
     * 处理insert绑定。
     *
     * @param connection 数据库连接。
     * @param protocolTemplateId 协议模板 ID。
     * @param availableDefinitions 可用定义。
     * @param bindings 绑定。
     * @param ownerClassId owner类 ID。
     * @param ownerMethodId owner方法 ID。
     * @param ownerPropertyId owner属性 ID。
     */
    private fun insertBindings(
        connection: Connection,
        protocolTemplateId: Long,
        availableDefinitions: List<CodegenContextDefinitionDto>,
        bindings: List<CodegenContextBindingDto>,
        ownerClassId: Long? = null,
        ownerMethodId: Long? = null,
        ownerPropertyId: Long? = null,
    ) {
        bindings.sortedBy(CodegenContextBindingDto::sortIndex).forEach { bindingDto ->
            val definition = resolveDefinitionForBinding(protocolTemplateId, availableDefinitions, bindingDto)
            val bindingId =
                sql.insertAndReturnId(
                    connection,
                    """
                    INSERT INTO codegen_context_binding (
                        definition_id,
                        owner_class_id,
                        owner_method_id,
                        owner_property_id,
                        sort_index,
                        created_at,
                        updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    requireNotNull(definition.id),
                    ownerClassId,
                    ownerMethodId,
                    ownerPropertyId,
                    bindingDto.sortIndex,
                    nowSqlValue(),
                    nowSqlValue(),
                )
            bindingDto.values.forEach { valueDto ->
                val paramDefinition = resolveParamDefinitionForValue(definition, valueDto)
                sql.insertAndReturnId(
                    connection,
                    """
                    INSERT INTO codegen_context_binding_value (
                        binding_id,
                        param_definition_id,
                        value,
                        created_at,
                        updated_at
                    ) VALUES (?, ?, ?, ?, ?)
                    """.trimIndent(),
                    bindingId,
                    requireNotNull(paramDefinition.id),
                    valueDto.value,
                    nowSqlValue(),
                    nowSqlValue(),
                )
            }
        }
    }

    /**
     * 处理校验。
     *
     * @param request 请求参数。
     * @param availableDefinitions 可用定义。
     */
    private fun validate(
        request: CodegenContextDetailDto,
        availableDefinitions: List<CodegenContextDefinitionDto>,
    ) {
        if (!CODE_PATTERN.matches(request.code)) {
            throw BusinessValidationException("Context code must match ${CODE_PATTERN.pattern}.")
        }
        if (request.name.isBlank()) {
            throw BusinessValidationException("Context name cannot be blank.")
        }
        if (request.consumerTarget != CodegenConsumerTarget.MCU_CONSOLE) {
            throw BusinessValidationException("Only MCU_CONSOLE is supported in V1.")
        }
        request.externalCOutputRoot?.let(::validateExternalCOutputRoot)
        validateGenerationSettings(request.generationSettings)
        validateClasses(
            protocolTemplateId = request.protocolTemplateId,
            classes = request.classes,
            availableDefinitions = availableDefinitions,
        )
    }

    /**
     * 校验外部 C 输出根目录。
     *
     * @param rawPath 原始路径。
     */
    private fun validateExternalCOutputRoot(
        rawPath: String,
    ) {
        val path =
            try {
                rawPath.toExpandedPath()
            } catch (_: InvalidPathException) {
                throw BusinessValidationException("externalCOutputRoot is not a valid filesystem path.")
            }
        if (!path.isAbsolute) {
            throw BusinessValidationException("externalCOutputRoot must be an absolute path on the current machine.")
        }
    }

    /**
     * 校验生成设置。
     *
     * @param settings 设置。
     */
    private fun validateGenerationSettings(
        settings: CodegenGenerationSettingsDto,
    ) {
        listOf(
            "serverOutputRoot" to settings.serverOutputRoot,
            "sharedOutputRoot" to settings.sharedOutputRoot,
            "gatewayOutputRoot" to settings.gatewayOutputRoot,
            "apiClientOutputRoot" to settings.apiClientOutputRoot,
            "springRouteOutputRoot" to settings.springRouteOutputRoot,
            "cOutputRoot" to settings.cOutputRoot,
            "markdownOutputRoot" to settings.markdownOutputRoot,
        ).forEach { (label, value) ->
            value?.let { validateAbsolutePath(label, it) }
        }
        settings.apiClientPackageName?.let { packageName ->
            if (!PACKAGE_PATTERN.matches(packageName)) {
                throw BusinessValidationException("apiClientPackageName '$packageName' is not a valid Kotlin package name.")
            }
        }
        if (settings.apiClientOutputRoot != null && settings.apiClientPackageName == null) {
            throw BusinessValidationException("apiClientPackageName is required when apiClientOutputRoot is configured.")
        }
        if (settings.apiClientPackageName != null && settings.apiClientOutputRoot == null) {
            throw BusinessValidationException("apiClientOutputRoot is required when apiClientPackageName is configured.")
        }
        validateRtuDefaults(settings.rtuDefaults)
        validateTcpDefaults(settings.tcpDefaults)
        validateMqttDefaults(settings.mqttDefaults)
    }

    /**
     * 校验absolute路径。
     *
     * @param fieldName field名称。
     * @param rawPath 原始路径。
     */
    private fun validateAbsolutePath(
        fieldName: String,
        rawPath: String,
    ) {
        val path =
            try {
                rawPath.toExpandedPath()
            } catch (_: InvalidPathException) {
                throw BusinessValidationException("$fieldName is not a valid filesystem path.")
            }
        if (!path.isAbsolute) {
            throw BusinessValidationException("$fieldName must be an absolute path on the current machine.")
        }
    }

    /**
     * 校验RTU默认。
     *
     * @param defaults 默认。
     */
    private fun validateRtuDefaults(
        defaults: CodegenRtuGenerationDefaultsDto,
    ) {
        if (defaults.portPath.isBlank()) {
            throw BusinessValidationException("rtuDefaults.portPath cannot be blank.")
        }
        if (defaults.unitId < 1 || defaults.unitId > 255) {
            throw BusinessValidationException("rtuDefaults.unitId must be between 1 and 255.")
        }
        if (defaults.baudRate < 1) {
            throw BusinessValidationException("rtuDefaults.baudRate must be positive.")
        }
        if (defaults.dataBits !in setOf(5, 6, 7, 8)) {
            throw BusinessValidationException("rtuDefaults.dataBits must be one of 5, 6, 7, 8.")
        }
        if (defaults.stopBits !in setOf(1, 2)) {
            throw BusinessValidationException("rtuDefaults.stopBits must be 1 or 2.")
        }
        if (defaults.parity.isBlank()) {
            throw BusinessValidationException("rtuDefaults.parity cannot be blank.")
        }
        if (defaults.timeoutMs < 1) {
            throw BusinessValidationException("rtuDefaults.timeoutMs must be positive.")
        }
        if (defaults.retries < 0) {
            throw BusinessValidationException("rtuDefaults.retries must be >= 0.")
        }
    }

    /**
     * 校验TCP默认。
     *
     * @param defaults 默认。
     */
    private fun validateTcpDefaults(
        defaults: CodegenTcpGenerationDefaultsDto,
    ) {
        if (defaults.host.isBlank()) {
            throw BusinessValidationException("tcpDefaults.host cannot be blank.")
        }
        if (defaults.port !in 1..65535) {
            throw BusinessValidationException("tcpDefaults.port must be between 1 and 65535.")
        }
        if (defaults.unitId < 1 || defaults.unitId > 255) {
            throw BusinessValidationException("tcpDefaults.unitId must be between 1 and 255.")
        }
        if (defaults.timeoutMs < 1) {
            throw BusinessValidationException("tcpDefaults.timeoutMs must be positive.")
        }
        if (defaults.retries < 0) {
            throw BusinessValidationException("tcpDefaults.retries must be >= 0.")
        }
    }

    /**
     * 校验MQTT默认。
     *
     * @param defaults 默认。
     */
    private fun validateMqttDefaults(
        defaults: CodegenMqttGenerationDefaultsDto,
    ) {
        if (defaults.brokerUrl.isBlank()) {
            throw BusinessValidationException("mqttDefaults.brokerUrl cannot be blank.")
        }
        if (defaults.clientId.isBlank()) {
            throw BusinessValidationException("mqttDefaults.clientId cannot be blank.")
        }
        if (defaults.requestTopic.isBlank()) {
            throw BusinessValidationException("mqttDefaults.requestTopic cannot be blank.")
        }
        if (defaults.responseTopic.isBlank()) {
            throw BusinessValidationException("mqttDefaults.responseTopic cannot be blank.")
        }
        if (defaults.qos !in 0..2) {
            throw BusinessValidationException("mqttDefaults.qos must be between 0 and 2.")
        }
        if (defaults.timeoutMs < 1) {
            throw BusinessValidationException("mqttDefaults.timeoutMs must be positive.")
        }
        if (defaults.retries < 0) {
            throw BusinessValidationException("mqttDefaults.retries must be >= 0.")
        }
    }

    /**
     * 校验类。
     *
     * @param protocolTemplateId 协议模板 ID。
     * @param classes 类。
     * @param availableDefinitions 可用定义。
     */
    private fun validateClasses(
        protocolTemplateId: Long,
        classes: List<CodegenClassDto>,
        availableDefinitions: List<CodegenContextDefinitionDto>,
    ) {
        val duplicateClassNames =
            classes.groupBy { codegenClass -> codegenClass.className }.filterValues { grouped -> grouped.size > 1 }.keys
        if (duplicateClassNames.isNotEmpty()) {
            throw BusinessValidationException("Duplicate className values: ${duplicateClassNames.joinToString()}.")
        }
        classes.forEach { codegenClass ->
            validateClass(protocolTemplateId, codegenClass, availableDefinitions)
        }
        if (classes.isNotEmpty()) {
            val derivedSchemas = classes.toModbusSpecs(protocolTemplateId, availableDefinitions)
            val duplicateMethods =
                derivedSchemas.groupBy(ModbusSchemaSpec::methodName).filterValues { grouped -> grouped.size > 1 }.keys
            if (duplicateMethods.isNotEmpty()) {
                throw BusinessValidationException(
                    "Duplicate generated methodName values in generic model: ${duplicateMethods.joinToString()}.",
                )
            }
            derivedSchemas.forEach { schema ->
                validateModbusSchema(schema, IDENTIFIER_PATTERN)
            }
        }
    }

    /**
     * 校验类。
     *
     * @param protocolTemplateId 协议模板 ID。
     * @param codegenClass 代码生成类。
     * @param availableDefinitions 可用定义。
     */
    private fun validateClass(
        protocolTemplateId: Long,
        codegenClass: CodegenClassDto,
        availableDefinitions: List<CodegenContextDefinitionDto>,
    ) {
        if (codegenClass.name.isBlank()) {
            throw BusinessValidationException("Class name cannot be blank.")
        }
        if (!IDENTIFIER_PATTERN.matches(codegenClass.className)) {
            throw BusinessValidationException("Class className '${codegenClass.className}' is not a valid Kotlin identifier.")
        }
        codegenClass.packageName?.let { packageName ->
            if (!PACKAGE_PATTERN.matches(packageName)) {
                throw BusinessValidationException("Class packageName '$packageName' is not a valid Kotlin package name.")
            }
        }
        validateBindings(
            protocolTemplateId = protocolTemplateId,
            expectedKind = CodegenNodeKind.CLASS,
            bindings = codegenClass.bindings,
            availableDefinitions = availableDefinitions,
        )
        val duplicateMethodNames =
            codegenClass.methods.groupBy(CodegenMethodDto::methodName).filterValues { grouped -> grouped.size > 1 }.keys
        if (duplicateMethodNames.isNotEmpty()) {
            throw BusinessValidationException(
                "Class ${codegenClass.className} has duplicate methodName values: ${duplicateMethodNames.joinToString()}.",
            )
        }
        val duplicatePropertyNames =
            codegenClass.properties.groupBy(CodegenPropertyDto::propertyName).filterValues { grouped -> grouped.size > 1 }.keys
        if (duplicatePropertyNames.isNotEmpty()) {
            throw BusinessValidationException(
                "Class ${codegenClass.className} has duplicate propertyName values: ${duplicatePropertyNames.joinToString()}.",
            )
        }
        codegenClass.methods.forEach { method ->
            validateMethod(protocolTemplateId, codegenClass, method, availableDefinitions)
        }
        codegenClass.properties.forEach { property ->
            validateProperty(protocolTemplateId, codegenClass, property, availableDefinitions)
        }
    }

    /**
     * 校验方法。
     *
     * @param protocolTemplateId 协议模板 ID。
     * @param ownerClass owner类。
     * @param method 方法。
     * @param availableDefinitions 可用定义。
     */
    private fun validateMethod(
        protocolTemplateId: Long,
        ownerClass: CodegenClassDto,
        method: CodegenMethodDto,
        availableDefinitions: List<CodegenContextDefinitionDto>,
    ) {
        if (method.name.isBlank()) {
            throw BusinessValidationException("Method name cannot be blank in class ${ownerClass.className}.")
        }
        if (!IDENTIFIER_PATTERN.matches(method.methodName)) {
            throw BusinessValidationException("Method ${method.methodName} is not a valid Kotlin identifier.")
        }
        method.requestClassName?.let { requestClassName ->
            if (!IDENTIFIER_PATTERN.matches(requestClassName)) {
                throw BusinessValidationException("Method requestClassName '$requestClassName' is not a valid Kotlin identifier.")
            }
        }
        method.responseClassName?.let { responseClassName ->
            if (!IDENTIFIER_PATTERN.matches(responseClassName)) {
                throw BusinessValidationException("Method responseClassName '$responseClassName' is not a valid Kotlin identifier.")
            }
        }
        validateBindings(
            protocolTemplateId = protocolTemplateId,
            expectedKind = CodegenNodeKind.METHOD,
            bindings = method.bindings,
            availableDefinitions = availableDefinitions,
        )
    }

    /**
     * 校验属性。
     *
     * @param protocolTemplateId 协议模板 ID。
     * @param ownerClass owner类。
     * @param property 属性。
     * @param availableDefinitions 可用定义。
     */
    private fun validateProperty(
        protocolTemplateId: Long,
        ownerClass: CodegenClassDto,
        property: CodegenPropertyDto,
        availableDefinitions: List<CodegenContextDefinitionDto>,
    ) {
        if (property.name.isBlank()) {
            throw BusinessValidationException("Property name cannot be blank in class ${ownerClass.className}.")
        }
        if (!IDENTIFIER_PATTERN.matches(property.propertyName)) {
            throw BusinessValidationException("Property ${property.propertyName} is not a valid Kotlin identifier.")
        }
        if (property.typeName.isBlank()) {
            throw BusinessValidationException("Property ${property.propertyName} must declare typeName.")
        }
        validateBindings(
            protocolTemplateId = protocolTemplateId,
            expectedKind = CodegenNodeKind.FIELD,
            bindings = property.bindings,
            availableDefinitions = availableDefinitions,
        )
    }

    /**
     * 校验绑定。
     *
     * @param protocolTemplateId 协议模板 ID。
     * @param expectedKind expected类型。
     * @param bindings 绑定。
     * @param availableDefinitions 可用定义。
     */
    private fun validateBindings(
        protocolTemplateId: Long,
        expectedKind: CodegenNodeKind,
        bindings: List<CodegenContextBindingDto>,
        availableDefinitions: List<CodegenContextDefinitionDto>,
    ) {
        val seenDefinitionCodes = mutableMapOf<String, Int>()
        bindings.forEach { binding ->
            val definition = resolveDefinitionForBinding(protocolTemplateId, availableDefinitions, binding)
            validateBindingTargetKind(definition, expectedKind)
            val currentCount = seenDefinitionCodes.getOrDefault(definition.code, 0) + 1
            seenDefinitionCodes[definition.code] = currentCount
            if (definition.bindingTargetMode == CodegenBindingTargetMode.SINGLE && currentCount > 1) {
                throw BusinessValidationException("Context definition ${definition.code} can only be bound once on $expectedKind.")
            }
            val duplicateParamCodes =
                binding.values.groupBy(CodegenContextBindingValueDto::paramCode).filterValues { grouped -> grouped.size > 1 }.keys
            if (duplicateParamCodes.isNotEmpty()) {
                throw BusinessValidationException(
                    "Binding ${definition.code} has duplicate paramCode values: ${duplicateParamCodes.joinToString()}.",
                )
            }
            definition.params.forEach { paramDefinition ->
                val bindingValue =
                    binding.values.firstOrNull { value ->
                        value.paramCode == paramDefinition.code || value.paramDefinitionId == paramDefinition.id
                    }
                validateBindingValueType(paramDefinition, bindingValue?.value ?: paramDefinition.defaultValue)
            }
        }
    }

    /**
     * 校验绑定目标类型。
     *
     * @param definition 定义。
     * @param expectedKind expected类型。
     */
    private fun validateBindingTargetKind(
        definition: CodegenContextDefinitionDto,
        expectedKind: CodegenNodeKind,
    ) {
        if (definition.targetKind != expectedKind) {
            throw BusinessValidationException(
                "Context definition ${definition.code} targets ${definition.targetKind}, cannot bind to $expectedKind.",
            )
        }
    }

    /**
     * 确保supported模板。
     *
     * @param protocolTemplate 协议模板。
     */
    private fun ensureSupportedTemplate(
        protocolTemplate: ProtocolTemplate,
    ) {
        if (protocolTemplate.code !in SUPPORTED_PROTOCOL_TEMPLATE_CODES) {
            throw BusinessValidationException(
                "Protocol template ${protocolTemplate.code} is not supported by the Modbus contract generator.",
            )
        }
    }

    /**
     * 加载上下文。
     *
     * @param contextId 上下文 ID。
     */
    private fun loadContext(
        contextId: Long,
    ): CodegenContext {
        return sql.createQuery(CodegenContext::class) {
            where(table.id eq contextId)
            select(table.fetch(CodegenContextFetchers.contextDetail))
        }.execute().firstOrNull() ?: throw NotFoundException("Codegen context $contextId was not found.")
    }

    /**
     * 加载协议模板。
     *
     * @param protocolTemplateId 协议模板 ID。
     */
    private fun loadProtocolTemplate(
        protocolTemplateId: Long,
    ): ProtocolTemplate {
        return sql.createQuery(ProtocolTemplate::class) {
            where(table.id eq protocolTemplateId)
            select(table.fetch(Fetchers.protocolTemplate))
        }.execute().firstOrNull() ?: throw NotFoundException("Protocol template $protocolTemplateId was not found.")
    }

    /**
     * 加载上下文定义。
     *
     * @param protocolTemplateId 协议模板 ID。
     */
    private fun loadContextDefinitions(
        protocolTemplateId: Long,
    ): List<CodegenContextDefinitionDto> {
        return sql.createQuery(CodegenContextDefinition::class) {
            where(table.protocolTemplate.id eq protocolTemplateId)
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(CodegenContextFetchers.definitionDetail))
        }.execute().map { definition ->
            definition.toDto()
        }
    }

    /**
     * 确保上下文存在性。
     *
     * @param contextId 上下文 ID。
     */
    private fun ensureContextExists(
        contextId: Long,
    ) {
        val exists =
            sql.createQuery(CodegenContext::class) {
                where(table.id eq contextId)
                select(table.id)
            }.execute().firstOrNull()
        if (exists == null) {
            throw NotFoundException("Codegen context $contextId was not found.")
        }
    }

    /**
     * 确保编码唯一性。
     *
     * @param connection 数据库连接。
     * @param code 编码。
     * @param ignoreId ignore ID。
     */
    private fun ensureCodeUnique(
        connection: Connection,
        code: String,
        ignoreId: Long?,
    ) {
        val count =
            if (ignoreId == null) {
                sql.queryCount(
                    connection,
                    "SELECT COUNT(1) FROM codegen_context_context WHERE code = ?",
                    code,
                )
            } else {
                sql.queryCount(
                    connection,
                    "SELECT COUNT(1) FROM codegen_context_context WHERE code = ? AND id <> ?",
                    code,
                    ignoreId,
                )
            }
        if (count > 0) {
            throw ConflictException("Context code '$code' already exists.")
        }
    }

    /**
     * 按 host-config 节点信息补齐元数据草稿默认参数。
     *
     * 这里优先把 nodeId 解析成项目/协议/设备上下文，再使用节点关联的协议配置、
     * 项目级 Modbus 配置与 MQTT 配置补齐默认值。
     * 如果调用方已经显式改过某个字段，则保留用户值，不强行覆盖。
     */
    private fun resolveNodeScopedDefaults(
        draft: CodegenMetadataDraftDto,
    ): CodegenMetadataDraftDto {
        val nodeRef = parseHostConfigNodeRef(draft.nodeId) ?: return draft
        val defaults = loadNodeScopedDefaults(nodeRef) ?: return draft
        return draft.copy(
            exportSettings =
                draft.exportSettings.copy(
                    rtuDefaults = mergeRtuDefaults(draft.exportSettings.rtuDefaults, defaults),
                    tcpDefaults = mergeTcpDefaults(draft.exportSettings.tcpDefaults, defaults),
                    mqttDefaults = mergeMqttDefaults(draft.exportSettings.mqttDefaults, defaults),
                ),
        )
    }

    /**
     * 解析 host-config 树节点 ID。
     */
    private fun parseHostConfigNodeRef(
        nodeId: String,
    ): HostConfigNodeRef? {
        val normalized = nodeId.cleanNullable() ?: return null
        val segments = normalized.split('/')
        val projectId = segments.getOrNull(1)?.toLongOrNull() ?: return null
        return when (segments.firstOrNull()) {
            "project" ->
                if (segments.size == 2) {
                    HostConfigNodeRef(projectId = projectId)
                } else {
                    null
                }

            "protocol" ->
                if (segments.size == 3) {
                    segments[2].toLongOrNull()?.let { protocolId ->
                        HostConfigNodeRef(projectId = projectId, protocolId = protocolId)
                    }
                } else {
                    null
                }

            "device" ->
                if (segments.size == 3) {
                    segments[2].toLongOrNull()?.let { deviceId ->
                        HostConfigNodeRef(projectId = projectId, deviceId = deviceId)
                    }
                } else {
                    null
                }

            "module" ->
                if (segments.size == 3) {
                    segments[2].toLongOrNull()?.let { moduleId ->
                        HostConfigNodeRef(projectId = projectId, moduleId = moduleId)
                    }
                } else {
                    null
                }

            "tag" ->
                if (segments.size == 4) {
                    val deviceId = segments[2].toLongOrNull()
                    val tagId = segments[3].toLongOrNull()
                    if (deviceId == null || tagId == null) {
                        null
                    } else {
                        HostConfigNodeRef(
                            projectId = projectId,
                            deviceId = deviceId,
                            tagId = tagId,
                        )
                    }
                } else {
                    null
                }

            else -> null
        }
    }

    /**
     * 加载节点关联的默认参数来源。
     */
    private fun loadNodeScopedDefaults(
        nodeRef: HostConfigNodeRef,
    ): HostConfigNodeDefaults? {
        val protocol = resolveProtocolForNode(nodeRef)
        val device = resolveDeviceForNode(nodeRef)
        val mqttConfig = loadProjectMqttConfig(nodeRef.projectId)
        val rtuProjectConfig = loadProjectModbusConfig(nodeRef.projectId, "RTU")
        val tcpProjectConfig = loadProjectModbusConfig(nodeRef.projectId, "TCP")
        if (protocol == null && device == null && mqttConfig == null && rtuProjectConfig == null && tcpProjectConfig == null) {
            return null
        }
        return HostConfigNodeDefaults(
            protocol = protocol,
            device = device,
            mqttConfig = mqttConfig,
            rtuProjectConfig = rtuProjectConfig,
            tcpProjectConfig = tcpProjectConfig,
        )
    }

    /**
     * 解析节点关联的协议。
     */
    private fun resolveProtocolForNode(
        nodeRef: HostConfigNodeRef,
    ): HostConfigProtocolDefaults? {
        nodeRef.protocolId?.let(::loadProtocolDefaults)?.let { protocol ->
            return protocol
        }
        nodeRef.moduleId?.let(::loadModuleDefaults)?.protocol?.let { protocol ->
            return protocol
        }
        nodeRef.deviceId?.let(::loadDeviceDefaults)?.protocol?.let { protocol ->
            return protocol
        }
        nodeRef.tagId?.let(::loadTagDefaults)?.protocol?.let { protocol ->
            return protocol
        }
        return null
    }

    /**
     * 解析节点关联的设备。
     */
    private fun resolveDeviceForNode(
        nodeRef: HostConfigNodeRef,
    ): HostConfigDeviceDefaults? {
        nodeRef.deviceId?.let(::loadDeviceDefaults)?.device?.let { device ->
            return device
        }
        nodeRef.moduleId?.let(::loadModuleDefaults)?.device?.let { device ->
            return device
        }
        nodeRef.tagId?.let(::loadTagDefaults)?.device?.let { device ->
            return device
        }
        return null
    }

    /**
     * 合并 RTU 默认参数。
     */
    private fun mergeRtuDefaults(
        current: CodegenMetadataRtuDefaultsDraftDto,
        defaults: HostConfigNodeDefaults,
    ): CodegenMetadataRtuDefaultsDraftDto {
        val protocol = defaults.protocol.takeIf { it?.transportType == "RTU" }
        val project = defaults.rtuProjectConfig
        val device = defaults.device
        val standard = CodegenMetadataRtuDefaultsDraftDto()
        return current.copy(
            portPath =
                current.portPath.takeNodeDefault(
                    nodeValue = protocol?.portName ?: project?.portName,
                    standardValue = standard.portPath,
                ),
            unitId =
                current.unitId.takeNodeDefault(
                    nodeValue = (device?.stationNo ?: project?.stationNo)?.toString(),
                    standardValue = standard.unitId,
                ),
            baudRate =
                current.baudRate.takeNodeDefault(
                    nodeValue = (protocol?.baudRate ?: project?.baudRate)?.toString(),
                    standardValue = standard.baudRate,
                ),
            dataBits =
                current.dataBits.takeNodeDefault(
                    nodeValue = (protocol?.dataBits ?: project?.dataBits)?.toString(),
                    standardValue = standard.dataBits,
                ),
            stopBits =
                current.stopBits.takeNodeDefault(
                    nodeValue = (protocol?.stopBits ?: project?.stopBits)?.toString(),
                    standardValue = standard.stopBits,
                ),
            parity =
                current.parity.takeNodeDefault(
                    nodeValue = (protocol?.parity ?: project?.parity)?.lowercase(),
                    standardValue = standard.parity,
                ),
            timeoutMs =
                current.timeoutMs.takeNodeDefault(
                    nodeValue = protocol?.responseTimeoutMs?.toString(),
                    standardValue = standard.timeoutMs,
                ),
        )
    }

    /**
     * 合并 TCP 默认参数。
     */
    private fun mergeTcpDefaults(
        current: CodegenMetadataTcpDefaultsDraftDto,
        defaults: HostConfigNodeDefaults,
    ): CodegenMetadataTcpDefaultsDraftDto {
        val protocol = defaults.protocol
        val project = defaults.tcpProjectConfig
        val device = defaults.device
        val standard = CodegenMetadataTcpDefaultsDraftDto()
        return current.copy(
            host =
                current.host.takeNodeDefault(
                    nodeValue = protocol?.host,
                    standardValue = standard.host,
                ),
            port =
                current.port.takeNodeDefault(
                    nodeValue = (protocol?.tcpPort ?: project?.tcpPort)?.toString(),
                    standardValue = standard.port,
                ),
            unitId =
                current.unitId.takeNodeDefault(
                    nodeValue = (device?.stationNo ?: project?.stationNo)?.toString(),
                    standardValue = standard.unitId,
                ),
            timeoutMs =
                current.timeoutMs.takeNodeDefault(
                    nodeValue = protocol?.responseTimeoutMs?.toString(),
                    standardValue = standard.timeoutMs,
                ),
        )
    }

    /**
     * 合并 MQTT 默认参数。
     */
    private fun mergeMqttDefaults(
        current: CodegenMetadataMqttDefaultsDraftDto,
        defaults: HostConfigNodeDefaults,
    ): CodegenMetadataMqttDefaultsDraftDto {
        val mqttConfig = defaults.mqttConfig
        val standard = CodegenMetadataMqttDefaultsDraftDto()
        val topicRoot = mqttConfig?.topic.cleanNullable()
        return current.copy(
            brokerUrl =
                current.brokerUrl.takeNodeDefault(
                    nodeValue = mqttConfig.toBrokerUrl(),
                    standardValue = standard.brokerUrl,
                ),
            clientId =
                current.clientId.takeNodeDefault(
                    nodeValue = mqttConfig?.clientId.cleanNullable() ?: mqttConfig?.gatewayId.cleanNullable(),
                    standardValue = standard.clientId,
                ),
            requestTopic =
                current.requestTopic.takeNodeDefault(
                    nodeValue = topicRoot?.let { "$it/request" },
                    standardValue = standard.requestTopic,
                ),
            responseTopic =
                current.responseTopic.takeNodeDefault(
                    nodeValue = topicRoot?.let { "$it/response" },
                    standardValue = standard.responseTopic,
                ),
            qos =
                current.qos.takeNodeDefault(
                    nodeValue = mqttConfig?.qos?.toString(),
                    standardValue = standard.qos,
                ),
        )
    }

    /**
     * 读取协议实例。
     */
    private fun loadProtocolDefaults(
        protocolId: Long?,
    ): HostConfigProtocolDefaults? {
        if (protocolId == null) {
            return null
        }
        return sql.query(
            """
            SELECT
                transport_type,
                host,
                tcp_port,
                port_name,
                baud_rate,
                data_bits,
                stop_bits,
                parity,
                response_timeout_ms
            FROM host_config_protocol_instance
            WHERE id = ?
            """.trimIndent(),
            protocolId,
        ) { resultSet ->
            HostConfigProtocolDefaults(
                transportType = resultSet.getString("transport_type")?.trim(),
                host = resultSet.getString("host")?.trim(),
                tcpPort = resultSet.getIntOrNull("tcp_port"),
                portName = resultSet.getString("port_name")?.trim(),
                baudRate = resultSet.getIntOrNull("baud_rate"),
                dataBits = resultSet.getIntOrNull("data_bits"),
                stopBits = resultSet.getIntOrNull("stop_bits"),
                parity = resultSet.getString("parity")?.trim(),
                responseTimeoutMs = resultSet.getIntOrNull("response_timeout_ms"),
            )
        }.firstOrNull()
    }

    /**
     * 读取设备实例。
     */
    private fun loadDeviceDefaults(
        deviceId: Long?,
    ): HostConfigDeviceLink? {
        if (deviceId == null) {
            return null
        }
        return sql.query(
            """
            SELECT
                d.station_no,
                p.transport_type,
                p.host,
                p.tcp_port,
                p.port_name,
                p.baud_rate,
                p.data_bits,
                p.stop_bits,
                p.parity,
                p.response_timeout_ms
            FROM host_config_device d
            JOIN host_config_protocol_instance p ON p.id = d.protocol_id
            WHERE d.id = ?
            """.trimIndent(),
            deviceId,
        ) { resultSet ->
            resultSet.toDeviceLink()
        }.firstOrNull()
    }

    /**
     * 读取模块实例。
     */
    private fun loadModuleDefaults(
        moduleId: Long?,
    ): HostConfigDeviceLink? {
        if (moduleId == null) {
            return null
        }
        return sql.query(
            """
            SELECT
                d.station_no,
                p.transport_type,
                p.host,
                p.tcp_port,
                p.port_name,
                p.baud_rate,
                p.data_bits,
                p.stop_bits,
                p.parity,
                p.response_timeout_ms
            FROM host_config_module_instance m
            JOIN host_config_device d ON d.id = m.device_id
            JOIN host_config_protocol_instance p ON p.id = m.protocol_id
            WHERE m.id = ?
            """.trimIndent(),
            moduleId,
        ) { resultSet ->
            resultSet.toDeviceLink()
        }.firstOrNull()
    }

    /**
     * 读取标签实例。
     */
    private fun loadTagDefaults(
        tagId: Long?,
    ): HostConfigDeviceLink? {
        if (tagId == null) {
            return null
        }
        return sql.query(
            """
            SELECT
                d.station_no,
                p.transport_type,
                p.host,
                p.tcp_port,
                p.port_name,
                p.baud_rate,
                p.data_bits,
                p.stop_bits,
                p.parity,
                p.response_timeout_ms
            FROM host_config_tag t
            JOIN host_config_device d ON d.id = t.device_id
            JOIN host_config_protocol_instance p ON p.id = d.protocol_id
            WHERE t.id = ?
            """.trimIndent(),
            tagId,
        ) { resultSet ->
            resultSet.toDeviceLink()
        }.firstOrNull()
    }

    /**
     * 读取项目 MQTT 配置。
     */
    private fun loadProjectMqttConfig(
        projectId: Long,
    ): HostConfigMqttDefaults? =
        sql.query(
            """
            SELECT
                host,
                port,
                topic,
                gateway_id,
                client_id,
                qos,
                tls_enabled
            FROM host_config_project_mqtt_config
            WHERE project_id = ?
            """.trimIndent(),
            projectId,
        ) { resultSet ->
            HostConfigMqttDefaults(
                host = resultSet.getString("host")?.trim(),
                port = resultSet.getIntOrNull("port"),
                topic = resultSet.getString("topic")?.trim(),
                gatewayId = resultSet.getString("gateway_id")?.trim(),
                clientId = resultSet.getString("client_id")?.trim(),
                qos = resultSet.getIntOrNull("qos"),
                tlsEnabled = resultSet.getBoolean("tls_enabled"),
            )
        }.firstOrNull()

    /**
     * 读取项目 Modbus 配置。
     */
    private fun loadProjectModbusConfig(
        projectId: Long,
        transportType: String,
    ): HostConfigModbusDefaults? =
        sql.query(
            """
            SELECT
                tcp_port,
                port_name,
                baud_rate,
                data_bits,
                stop_bits,
                parity,
                station_no
            FROM host_config_project_modbus_server_config
            WHERE project_id = ? AND transport_type = ?
            """.trimIndent(),
            projectId,
            transportType,
        ) { resultSet ->
            HostConfigModbusDefaults(
                tcpPort = resultSet.getIntOrNull("tcp_port"),
                portName = resultSet.getString("port_name")?.trim(),
                baudRate = resultSet.getIntOrNull("baud_rate"),
                dataBits = resultSet.getIntOrNull("data_bits"),
                stopBits = resultSet.getIntOrNull("stop_bits"),
                parity = resultSet.getString("parity")?.trim(),
                stationNo = resultSet.getIntOrNull("station_no"),
            )
        }.firstOrNull()

    /**
     * 处理代码生成上下文。
     */
    private fun CodegenContext.toSummaryDto(): CodegenContextSummaryDto {
        return CodegenContextSummaryDto(
            id = id,
            code = code,
            name = name,
            description = description,
            enabled = enabled,
            consumerTarget = consumerTarget,
            protocolTemplateId = protocolTemplate.id,
            protocolTemplateCode = protocolTemplate.code,
            protocolTemplateName = protocolTemplate.name,
        )
    }

    /**
     * 处理代码生成上下文。
     */
    private fun CodegenContext.toDetailDto(): CodegenContextDetailDto {
        return CodegenContextDetailDto(
            id = id,
            code = code,
            name = name,
            description = description,
            enabled = enabled,
            nodeId = nodeId,
            consumerTarget = consumerTarget,
            protocolTemplateId = protocolTemplate.id,
            protocolTemplateCode = protocolTemplate.code,
            protocolTemplateName = protocolTemplate.name,
            externalCOutputRoot = externalCOutputRoot,
            kotlinClientTransports = kotlinClientTransports,
            cExposeTransports = cExposeTransports,
            artifactKinds = artifactKinds,
            cOutputProjectDir = cOutputProjectDir,
            bridgeImplPath = bridgeImplPath,
            keilUvprojxPath = keilUvprojxPath,
            keilTargetName = keilTargetName,
            keilGroupName = keilGroupName,
            mxprojectPath = mxprojectPath,
            generationSettings =
                CodegenGenerationSettingsDto(
                    serverOutputRoot = serverOutputRoot,
                    sharedOutputRoot = sharedOutputRoot,
                    gatewayOutputRoot = gatewayOutputRoot,
                    apiClientOutputRoot = apiClientOutputRoot,
                    apiClientPackageName = apiClientPackageName,
                    springRouteOutputRoot = springRouteOutputRoot,
                    cOutputRoot = cOutputRoot ?: externalCOutputRoot?.let { "$it/c" },
                    markdownOutputRoot = markdownOutputRoot ?: externalCOutputRoot?.let { "$it/markdown" },
                    rtuDefaults =
                        CodegenRtuGenerationDefaultsDto(
                            portPath = rtuPortPath,
                            unitId = rtuUnitId,
                            baudRate = rtuBaudRate,
                            dataBits = rtuDataBits,
                            stopBits = rtuStopBits,
                            parity = rtuParity,
                            timeoutMs = rtuTimeoutMs,
                            retries = rtuRetries,
                        ),
                    tcpDefaults =
                        CodegenTcpGenerationDefaultsDto(
                            host = tcpHost,
                            port = tcpPort,
                            unitId = tcpUnitId,
                            timeoutMs = tcpTimeoutMs,
                            retries = tcpRetries,
                        ),
                    mqttDefaults =
                        CodegenMqttGenerationDefaultsDto(
                            brokerUrl = mqttBrokerUrl,
                            clientId = mqttClientId,
                            requestTopic = mqttRequestTopic,
                            responseTopic = mqttResponseTopic,
                            qos = mqttQos,
                            timeoutMs = mqttTimeoutMs,
                            retries = mqttRetries,
                        ),
                ),
            availableContextDefinitions = loadContextDefinitions(protocolTemplate.id),
            classes =
                classes.sortedWith(compareBy(CodegenClass::sortIndex, CodegenClass::id)).map { codegenClass ->
                    codegenClass.toDto()
                },
        )
    }

    /**
     * 处理代码生成上下文定义。
     */
    private fun CodegenContextDefinition.toDto(): CodegenContextDefinitionDto {
        return CodegenContextDefinitionDto(
            id = id,
            code = code,
            name = name,
            description = description,
            sortIndex = sortIndex,
            targetKind = targetKind,
            bindingTargetMode = bindingTargetMode,
            sourceKind = sourceKind,
            params =
                params.sortedWith(compareBy(CodegenContextParamDefinition::sortIndex, CodegenContextParamDefinition::id)).map { param ->
                    param.toDto()
                },
        )
    }

    /**
     * 处理代码生成上下文参数定义。
     */
    private fun CodegenContextParamDefinition.toDto(): CodegenContextParamDefinitionDto {
        return CodegenContextParamDefinitionDto(
            id = id,
            code = code,
            name = name,
            description = description,
            sortIndex = sortIndex,
            valueType = valueType,
            required = required,
            defaultValue = defaultValue,
            enumOptions = enumOptions.decodeEnumOptions(),
            placeholder = placeholder,
        )
    }

    /**
     * 处理代码生成类。
     */
    private fun CodegenClass.toDto(): CodegenClassDto {
        return CodegenClassDto(
            id = id,
            name = name,
            description = description,
            sortIndex = sortIndex,
            classKind = classKind,
            className = className,
            packageName = packageName,
            bindings =
                bindings.sortedWith(compareBy(CodegenContextBinding::sortIndex, CodegenContextBinding::id)).map { binding ->
                    binding.toDto()
                },
            methods =
                methods.sortedWith(compareBy(CodegenMethod::sortIndex, CodegenMethod::id)).map { method ->
                    method.toDto()
                },
            properties =
                properties.sortedWith(compareBy(CodegenProperty::sortIndex, CodegenProperty::id)).map { property ->
                    property.toDto()
                },
        )
    }

    /**
     * 处理代码生成方法。
     */
    private fun CodegenMethod.toDto(): CodegenMethodDto {
        return CodegenMethodDto(
            id = id,
            name = name,
            description = description,
            sortIndex = sortIndex,
            methodName = methodName,
            requestClassName = requestClassName,
            responseClassName = responseClassName,
            bindings =
                bindings.sortedWith(compareBy(CodegenContextBinding::sortIndex, CodegenContextBinding::id)).map { binding ->
                    binding.toDto()
                },
        )
    }

    /**
     * 处理代码生成属性。
     */
    private fun CodegenProperty.toDto(): CodegenPropertyDto {
        return CodegenPropertyDto(
            id = id,
            name = name,
            description = description,
            sortIndex = sortIndex,
            propertyName = propertyName,
            typeName = typeName,
            nullable = nullable,
            defaultLiteral = defaultLiteral,
            bindings =
                bindings.sortedWith(compareBy(CodegenContextBinding::sortIndex, CodegenContextBinding::id)).map { binding ->
                    binding.toDto()
                },
        )
    }

    /**
     * 处理代码生成上下文绑定。
     */
    private fun CodegenContextBinding.toDto(): CodegenContextBindingDto {
        return CodegenContextBindingDto(
            id = id,
            definitionId = definition.id,
            definitionCode = definition.code,
            sortIndex = sortIndex,
            values =
                values.sortedWith(compareBy(CodegenContextBindingValue::id)).map { value ->
                    value.toDto()
                },
        )
    }

    /**
     * 处理代码生成上下文绑定值。
     */
    private fun CodegenContextBindingValue.toDto(): CodegenContextBindingValueDto {
        return CodegenContextBindingValueDto(
            id = id,
            paramDefinitionId = paramDefinition.id,
            paramCode = paramDefinition.code,
            value = value,
        )
    }

    /**
     * 处理代码生成上下文详情数据传输对象。
     */
    private fun CodegenContextDetailDto.normalized(): CodegenContextDetailDto {
        val trimmedClasses =
            classes.map { codegenClass ->
                codegenClass.copy(
                    name = codegenClass.name.trim(),
                    description = codegenClass.description.cleanNullable(),
                    className = codegenClass.className.trim(),
                    packageName = codegenClass.packageName.cleanNullable(),
                    bindings =
                        codegenClass.bindings.map { binding ->
                            binding.normalized()
                        },
                    methods =
                        codegenClass.methods.map { method ->
                            method.copy(
                                name = method.name.trim(),
                                description = method.description.cleanNullable(),
                                methodName = method.methodName.trim(),
                                requestClassName = method.requestClassName.cleanNullable(),
                                responseClassName = method.responseClassName.cleanNullable(),
                                bindings =
                                    method.bindings.map { binding ->
                                        binding.normalized()
                                    },
                            )
                        },
                    properties =
                        codegenClass.properties.map { property ->
                            property.copy(
                                name = property.name.trim(),
                                description = property.description.cleanNullable(),
                                propertyName = property.propertyName.trim(),
                                typeName = property.typeName.trim(),
                                defaultLiteral = property.defaultLiteral.cleanNullable(),
                                bindings =
                                    property.bindings.map { binding ->
                                        binding.normalized()
                                    },
                            )
                        },
                )
            }.normalizeGeneratedIdentifiers()
        return copy(
            code = code.trim(),
            name = name.trim(),
            description = description.cleanNullable(),
            enabled = enabled,
            consumerTarget = CodegenConsumerTarget.MCU_CONSOLE,
            externalCOutputRoot = externalCOutputRoot.cleanNullable(),
            generationSettings = generationSettings.normalized(),
            availableContextDefinitions = emptyList(),
            classes = trimmedClasses,
        )
    }

    /**
     * 处理代码生成上下文绑定数据传输对象。
     */
    private fun CodegenContextBindingDto.normalized(): CodegenContextBindingDto {
        return copy(
            definitionCode = definitionCode.trim(),
            values =
                values.map { value ->
                    value.copy(
                        paramCode = value.paramCode.trim(),
                        value = value.value.cleanNullable(),
                    )
                },
        )
    }

    /**
     * 处理string。
     */
    private fun String?.cleanNullable(): String? {
        return this?.trim()?.takeIf(String::isNotBlank)
    }

    /**
     * 为类、方法与属性补齐后端派生标识符。
     */
    private fun List<CodegenClassDto>.normalizeGeneratedIdentifiers(): List<CodegenClassDto> {
        val classesWithResolvedMembers =
            map { codegenClass ->
                codegenClass.copy(
                    methods = codegenClass.methods.normalizeGeneratedMethodIdentifiers(),
                    properties = codegenClass.properties.normalizeGeneratedPropertyIdentifiers(),
                )
            }
        val requestNameQueues = mutableMapOf<String, ArrayDeque<String>>()
        val responseNameQueues = mutableMapOf<String, ArrayDeque<String>>()
        classesWithResolvedMembers.flatMap(CodegenClassDto::methods).forEach { method ->
            requestNameQueues.getOrPut(method.name) { ArrayDeque() }.addLast(requireNotNull(method.requestClassName))
            responseNameQueues.getOrPut(method.name) { ArrayDeque() }.addLast(requireNotNull(method.responseClassName))
        }
        val usedClassNames = mutableSetOf<String>()
        return classesWithResolvedMembers.map { codegenClass ->
            val explicitClassName = codegenClass.className.cleanNullable()
            val resolvedClassName =
                if (explicitClassName != null) {
                    explicitClassName
                } else {
                    val matchedMethodClassName =
                        requestNameQueues.pollGeneratedClassName(
                            methodDisplayName = codegenClass.name.removeSuffix("请求实体"),
                            enabled = codegenClass.name.endsWith("请求实体"),
                        ) ?: responseNameQueues.pollGeneratedClassName(
                            methodDisplayName = codegenClass.name.removeSuffix("响应实体"),
                            enabled = codegenClass.name.endsWith("响应实体"),
                        )
                    ensureUniqueGeneratedIdentifier(
                        base = matchedMethodClassName ?: codegenClass.name.toGeneratedTypeName(),
                        existing = usedClassNames,
                    )
                }
            usedClassNames += resolvedClassName
            codegenClass.copy(className = resolvedClassName)
        }
    }

    /**
     * 为方法补齐后端派生名称。
     */
    private fun List<CodegenMethodDto>.normalizeGeneratedMethodIdentifiers(): List<CodegenMethodDto> {
        val usedMethodNames = mutableSetOf<String>()
        return map { method ->
            val explicitMethodName = method.methodName.cleanNullable()
            val resolvedMethodName =
                explicitMethodName
                    ?: ensureUniqueGeneratedIdentifier(
                        base = method.name.toGeneratedMethodName(),
                        existing = usedMethodNames,
                    )
            usedMethodNames += resolvedMethodName
            method.copy(
                methodName = resolvedMethodName,
                requestClassName = method.requestClassName.cleanNullable() ?: resolvedMethodName.toGeneratedTypeName("GeneratedMethod") + "Request",
                responseClassName = method.responseClassName.cleanNullable() ?: resolvedMethodName.toGeneratedTypeName("GeneratedMethod") + "Response",
            )
        }
    }

    /**
     * 为属性补齐后端派生名称与类型。
     */
    private fun List<CodegenPropertyDto>.normalizeGeneratedPropertyIdentifiers(): List<CodegenPropertyDto> {
        val usedPropertyNames = mutableSetOf<String>()
        return map { property ->
            val explicitPropertyName = property.propertyName.cleanNullable()
            val resolvedPropertyName =
                explicitPropertyName
                    ?: ensureUniqueGeneratedIdentifier(
                        base = property.name.toGeneratedPropertyName(),
                        existing = usedPropertyNames,
                    )
            usedPropertyNames += resolvedPropertyName
            property.copy(
                propertyName = resolvedPropertyName,
                typeName = property.typeName.cleanNullable() ?: property.bindings.bindingValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_TRANSPORT_TYPE_PARAM).toPropertyTypeName(),
            )
        }
    }

    /**
     * 从队列中按显示名取出下一个派生类名。
     *
     * @param methodDisplayName 方法显示名。
     * @param enabled 是否启用当前匹配。
     */
    private fun MutableMap<String, ArrayDeque<String>>.pollGeneratedClassName(
        methodDisplayName: String,
        enabled: Boolean,
    ): String? {
        if (!enabled) {
            return null
        }
        val queue = get(methodDisplayName) ?: return null
        if (queue.isEmpty()) {
            remove(methodDisplayName)
            return null
        }
        val value = queue.removeFirst()
        if (queue.isEmpty()) {
            remove(methodDisplayName)
        }
        return value
    }

    /**
     * 从绑定中读取字段值。
     *
     * @param definitionCode 定义编码。
     * @param paramCode 参数编码。
     */
    private fun List<CodegenContextBindingDto>.bindingValue(
        definitionCode: String,
        paramCode: String,
    ): String? =
        firstOrNull { binding -> binding.definitionCode == definitionCode }
            ?.values
            ?.firstOrNull { value -> value.paramCode == paramCode }
            ?.value
            ?.cleanNullable()

    /**
     * 根据 transportType 推断属性类型。
     */
    private fun String?.toPropertyTypeName(): String =
        when (this) {
            "BOOL_COIL" -> "Boolean"
            "STRING_ASCII",
            "STRING_UTF8" -> "String"
            else -> "Int"
        }

    /**
     * 为自动派生名称补齐唯一后缀。
     *
     * @param base 基础名称。
     * @param existing 已存在名称。
     */
    private fun ensureUniqueGeneratedIdentifier(
        base: String,
        existing: Collection<String>,
    ): String {
        val candidateBase = base.ifBlank { "generatedName" }
        if (candidateBase !in existing) {
            return candidateBase
        }
        var index = 2
        while (true) {
            val candidate = "$candidateBase$index"
            if (candidate !in existing) {
                return candidate
            }
            index += 1
        }
    }

    /**
     * 处理代码生成设置数据传输对象。
     */
    private fun CodegenGenerationSettingsDto.normalized(): CodegenGenerationSettingsDto {
        return copy(
            serverOutputRoot = serverOutputRoot.cleanNullable(),
            sharedOutputRoot = sharedOutputRoot.cleanNullable(),
            gatewayOutputRoot = gatewayOutputRoot.cleanNullable(),
            apiClientOutputRoot = apiClientOutputRoot.cleanNullable(),
            apiClientPackageName = apiClientPackageName.cleanNullable(),
            springRouteOutputRoot = springRouteOutputRoot.cleanNullable(),
            cOutputRoot = cOutputRoot.cleanNullable(),
            markdownOutputRoot = markdownOutputRoot.cleanNullable(),
            rtuDefaults =
                rtuDefaults.copy(
                    portPath = rtuDefaults.portPath.trim(),
                    parity = rtuDefaults.parity.trim(),
                ),
            tcpDefaults =
                tcpDefaults.copy(
                    host = tcpDefaults.host.trim(),
                ),
            mqttDefaults =
                mqttDefaults.copy(
                    brokerUrl = mqttDefaults.brokerUrl.trim(),
                    clientId = mqttDefaults.clientId.trim(),
                    requestTopic = mqttDefaults.requestTopic.trim(),
                    responseTopic = mqttDefaults.responseTopic.trim(),
                ),
        )
    }

    /**
     * 处理当前时间sql值。
     */
    private fun nowSqlValue(): Long {
        return System.currentTimeMillis()
    }

    /**
     * 处理string。
     */
    private fun String?.decodeEnumOptions(): List<String> {
        val raw = this?.trim()?.takeIf(String::isNotBlank) ?: return emptyList()
        return runCatching {
            JSON.decodeFromString<List<String>>(raw)
        }.getOrDefault(
            raw.split('\n').map(String::trim).filter(String::isNotBlank),
        )
    }
}

/**
 * 表示从 host-config 树节点解析出的定位信息。
 */
private data class HostConfigNodeRef(
    val projectId: Long,
    val protocolId: Long? = null,
    val deviceId: Long? = null,
    val moduleId: Long? = null,
    val tagId: Long? = null,
)

/**
 * 表示节点关联的默认参数来源集合。
 */
private data class HostConfigNodeDefaults(
    val protocol: HostConfigProtocolDefaults? = null,
    val device: HostConfigDeviceDefaults? = null,
    val mqttConfig: HostConfigMqttDefaults? = null,
    val rtuProjectConfig: HostConfigModbusDefaults? = null,
    val tcpProjectConfig: HostConfigModbusDefaults? = null,
)

/**
 * 仅在当前值为空或仍是模板默认值时，使用节点推导值覆盖。
 */
private fun String.takeNodeDefault(
    nodeValue: String?,
    standardValue: String,
): String {
    val resolvedNodeValue = nodeValue.cleanNullable() ?: return this
    val normalizedCurrent = trim()
    if (normalizedCurrent.isBlank() || normalizedCurrent == standardValue) {
        return resolvedNodeValue
    }
    return this
}

/**
 * 把项目 MQTT 配置转换为 broker URL。
 */
private fun HostConfigMqttDefaults?.toBrokerUrl(): String? {
    val config = this ?: return null
    val host = config.host.cleanNullable() ?: return null
    val port = config.port ?: return null
    val scheme = if (config.tlsEnabled) "ssl" else "tcp"
    return "$scheme://$host:$port"
}

/**
 * 表示节点协议默认值来源。
 */
private data class HostConfigProtocolDefaults(
    val transportType: String? = null,
    val host: String? = null,
    val tcpPort: Int? = null,
    val portName: String? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: String? = null,
    val responseTimeoutMs: Int? = null,
)

/**
 * 表示节点设备默认值来源。
 */
private data class HostConfigDeviceDefaults(
    val stationNo: Int? = null,
)

/**
 * 表示节点设备与协议的联合查询结果。
 */
private data class HostConfigDeviceLink(
    val device: HostConfigDeviceDefaults,
    val protocol: HostConfigProtocolDefaults,
)

/**
 * 表示项目 MQTT 默认值来源。
 */
private data class HostConfigMqttDefaults(
    val host: String? = null,
    val port: Int? = null,
    val topic: String? = null,
    val gatewayId: String? = null,
    val clientId: String? = null,
    val qos: Int? = null,
    val tlsEnabled: Boolean = false,
)

/**
 * 表示项目 Modbus 默认值来源。
 */
private data class HostConfigModbusDefaults(
    val tcpPort: Int? = null,
    val portName: String? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: String? = null,
    val stationNo: Int? = null,
)

/**
 * 把查询结果转换成设备与协议默认值。
 */
private fun java.sql.ResultSet.toDeviceLink(): HostConfigDeviceLink {
    return HostConfigDeviceLink(
        device =
            HostConfigDeviceDefaults(
                stationNo = getIntOrNull("station_no"),
            ),
        protocol =
            HostConfigProtocolDefaults(
                transportType = getString("transport_type")?.trim(),
                host = getString("host")?.trim(),
                tcpPort = getIntOrNull("tcp_port"),
                portName = getString("port_name")?.trim(),
                baudRate = getIntOrNull("baud_rate"),
                dataBits = getIntOrNull("data_bits"),
                stopBits = getIntOrNull("stop_bits"),
                parity = getString("parity")?.trim(),
                responseTimeoutMs = getIntOrNull("response_timeout_ms"),
            ),
    )
}

/**
 * 安全读取可空整数。
 */
private fun java.sql.ResultSet.getIntOrNull(
    columnLabel: String,
): Int? {
    val value = getInt(columnLabel)
    return if (wasNull()) {
        null
    } else {
        value
    }
}
