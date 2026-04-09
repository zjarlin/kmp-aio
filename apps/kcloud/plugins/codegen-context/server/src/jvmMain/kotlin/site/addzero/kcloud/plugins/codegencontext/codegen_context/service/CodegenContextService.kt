package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.sql.Connection
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.nio.file.InvalidPathException
import java.nio.file.Path
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.jdbc.JdbcExecutor
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenGenerationSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextSummaryDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenFieldDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenRtuGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenSchemaDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenTcpGenerationDefaultsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.GenerateContractsResponseDto
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.*
import site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.common.BusinessValidationException
import site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.common.ConflictException
import site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.common.NotFoundException
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType
import site.addzero.kcloud.plugins.hostconfig.model.entity.*
import site.addzero.kcloud.plugins.hostconfig.service.Fetchers

@Single
class CodegenContextService(
    private val sql: KSqlClient,
    private val jdbc: JdbcExecutor,
    private val contractGenerator: CodegenContextContractGenerator,
) {
    private companion object {
        val IDENTIFIER_PATTERN = Regex("[A-Za-z_][A-Za-z0-9_]*")
        val CODE_PATTERN = Regex("[A-Za-z][A-Za-z0-9_]*")
        val PACKAGE_PATTERN = Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*")
        val SQLITE_DATE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val SUPPORTED_PROTOCOL_TEMPLATE_CODES = setOf(
            "MODBUS_RTU_CLIENT",
            "MODBUS_TCP_CLIENT",
        )
    }

    fun listContexts(): List<CodegenContextSummaryDto> {
        return sql.createQuery(CodegenContext::class) {
            orderBy(table.name.asc(), table.id.asc())
            select(table.fetch(CodegenContextFetchers.contextSummary))
        }.execute().map { context ->
            context.toSummaryDto()
        }
    }

    fun getContext(
        contextId: Long,
    ): CodegenContextDetailDto {
        return loadContext(contextId).toDetailDto()
    }

    fun saveContext(
        request: CodegenContextDetailDto,
    ): CodegenContextDetailDto {
        val normalized = request.normalized()
        validate(normalized)
        val protocolTemplate = loadProtocolTemplate(normalized.protocolTemplateId)
        ensureSupportedTemplate(protocolTemplate)
        val generationSettings = normalized.generationSettings
        val contextId =
            jdbc.withTransaction { connection ->
                val existingId = normalized.id
                val now = nowSqlValue()
                val resolvedId =
                    if (existingId == null) {
                        ensureCodeUnique(connection, normalized.code, null)
                        jdbc.insertAndReturnId(
                            connection,
                            """
                            INSERT INTO codegen_context_context (
                                code,
                                name,
                                description,
                                enabled,
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
                                create_time,
                                update_time
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                            normalized.code,
                            normalized.name,
                            normalized.description,
                            if (normalized.enabled) 1 else 0,
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
                            now,
                            now,
                        )
                    } else {
                        ensureContextExists(existingId)
                        ensureCodeUnique(connection, normalized.code, existingId)
                        jdbc.update(
                            connection,
                            """
                            UPDATE codegen_context_context
                            SET code = ?,
                                name = ?,
                                description = ?,
                                enabled = ?,
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
                                update_time = ?
                            WHERE id = ?
                            """.trimIndent(),
                            normalized.code,
                            normalized.name,
                            normalized.description,
                            if (normalized.enabled) 1 else 0,
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
                            now,
                            existingId,
                        )
                        existingId
                    }
                syncSchemas(connection, resolvedId, normalized.schemas)
                resolvedId
            }
        return getContext(contextId)
    }

    fun deleteContext(
        contextId: Long,
    ) {
        ensureContextExists(contextId)
        jdbc.withTransaction { connection ->
            jdbc.update(connection, "DELETE FROM codegen_context_context WHERE id = ?", contextId)
        }
    }

    fun generateContracts(
        contextId: Long,
    ): GenerateContractsResponseDto {
        return contractGenerator.generate(getContext(contextId))
    }

    private fun syncSchemas(
        connection: Connection,
        contextId: Long,
        schemas: List<CodegenSchemaDto>,
    ) {
        val existingSchemaIds =
            jdbc.queryIds(
                connection,
                "SELECT id FROM codegen_context_schema WHERE context_id = ?",
                contextId,
            ).toMutableSet()
        val retainedSchemaIds = mutableSetOf<Long>()
        schemas.sortedBy { it.sortIndex }.forEach { schema ->
            val existingSchemaId = schema.id
            val schemaId =
                if (existingSchemaId == null) {
                    jdbc.insertAndReturnId(
                        connection,
                        """
                        INSERT INTO codegen_context_schema (
                            context_id,
                            name,
                            description,
                            sort_index,
                            direction,
                            function_code,
                            base_address,
                            method_name,
                            model_name,
                            create_time,
                            update_time
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """.trimIndent(),
                        contextId,
                        schema.name,
                        schema.description,
                        schema.sortIndex,
                        schema.direction.name,
                        schema.functionCode.name,
                        schema.baseAddress,
                        schema.methodName,
                        schema.modelName,
                        nowSqlValue(),
                        nowSqlValue(),
                    )
                } else {
                    if (existingSchemaId !in existingSchemaIds) {
                        throw BusinessValidationException("Schema id $existingSchemaId does not belong to context $contextId.")
                    }
                    jdbc.update(
                        connection,
                        """
                        UPDATE codegen_context_schema
                        SET name = ?,
                            description = ?,
                            sort_index = ?,
                            direction = ?,
                            function_code = ?,
                            base_address = ?,
                            method_name = ?,
                            model_name = ?,
                            update_time = ?
                        WHERE id = ?
                        """.trimIndent(),
                        schema.name,
                        schema.description,
                        schema.sortIndex,
                        schema.direction.name,
                        schema.functionCode.name,
                        schema.baseAddress,
                        schema.methodName,
                        schema.modelName,
                        nowSqlValue(),
                        existingSchemaId,
                    )
                    existingSchemaId
                }
            retainedSchemaIds += schemaId
            syncFields(connection, schemaId, schema.fields)
        }
        existingSchemaIds.filterNot(retainedSchemaIds::contains).forEach { schemaId ->
            jdbc.update(connection, "DELETE FROM codegen_context_schema WHERE id = ?", schemaId)
        }
    }

    private fun syncFields(
        connection: Connection,
        schemaId: Long,
        fields: List<CodegenFieldDto>,
    ) {
        val existingFieldIds =
            jdbc.queryIds(
                connection,
                "SELECT id FROM codegen_context_field WHERE schema_id = ?",
                schemaId,
            ).toMutableSet()
        val retainedFieldIds = mutableSetOf<Long>()
        fields.sortedBy { it.sortIndex }.forEach { field ->
            val existingFieldId = field.id
            val fieldId =
                if (existingFieldId == null) {
                    jdbc.insertAndReturnId(
                        connection,
                        """
                        INSERT INTO codegen_context_field (
                            schema_id,
                            name,
                            description,
                            sort_index,
                            property_name,
                            transport_type,
                            register_offset,
                            bit_offset,
                            length,
                            translation_hint,
                            default_literal,
                            create_time,
                            update_time
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """.trimIndent(),
                        schemaId,
                        field.name,
                        field.description,
                        field.sortIndex,
                        field.propertyName,
                        field.transportType.name,
                        field.registerOffset,
                        field.bitOffset,
                        field.length,
                        field.translationHint,
                        field.defaultLiteral,
                        nowSqlValue(),
                        nowSqlValue(),
                    )
                } else {
                    if (existingFieldId !in existingFieldIds) {
                        throw BusinessValidationException("Field id $existingFieldId does not belong to schema $schemaId.")
                    }
                    jdbc.update(
                        connection,
                        """
                        UPDATE codegen_context_field
                        SET name = ?,
                            description = ?,
                            sort_index = ?,
                            property_name = ?,
                            transport_type = ?,
                            register_offset = ?,
                            bit_offset = ?,
                            length = ?,
                            translation_hint = ?,
                            default_literal = ?,
                            update_time = ?
                        WHERE id = ?
                        """.trimIndent(),
                        field.name,
                        field.description,
                        field.sortIndex,
                        field.propertyName,
                        field.transportType.name,
                        field.registerOffset,
                        field.bitOffset,
                        field.length,
                        field.translationHint,
                        field.defaultLiteral,
                        nowSqlValue(),
                        existingFieldId,
                    )
                    existingFieldId
                }
            retainedFieldIds += fieldId
        }
        existingFieldIds.filterNot(retainedFieldIds::contains).forEach { fieldId ->
            jdbc.update(connection, "DELETE FROM codegen_context_field WHERE id = ?", fieldId)
        }
    }

    private fun validate(
        request: CodegenContextDetailDto,
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
        val duplicateMethods =
            request.schemas.groupBy { it.methodName }.filterValues { schemas -> schemas.size > 1 }.keys
        if (duplicateMethods.isNotEmpty()) {
            throw BusinessValidationException("Duplicate schema methodName values: ${duplicateMethods.joinToString()}.")
        }
        request.schemas.forEach { schema ->
            validateSchema(schema)
        }
    }

    private fun validateExternalCOutputRoot(
        rawPath: String,
    ) {
        val path =
            try {
                Path.of(rawPath)
            } catch (_: InvalidPathException) {
                throw BusinessValidationException("externalCOutputRoot is not a valid filesystem path.")
            }
        if (!path.isAbsolute) {
            throw BusinessValidationException("externalCOutputRoot must be an absolute path on the current machine.")
        }
    }

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
    }

    private fun validateAbsolutePath(
        fieldName: String,
        rawPath: String,
    ) {
        val path =
            try {
                Path.of(rawPath)
            } catch (_: InvalidPathException) {
                throw BusinessValidationException("$fieldName is not a valid filesystem path.")
            }
        if (!path.isAbsolute) {
            throw BusinessValidationException("$fieldName must be an absolute path on the current machine.")
        }
    }

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

    private fun validateSchema(
        schema: CodegenSchemaDto,
    ) {
        if (schema.name.isBlank()) {
            throw BusinessValidationException("Schema name cannot be blank.")
        }
        if (!IDENTIFIER_PATTERN.matches(schema.methodName)) {
            throw BusinessValidationException("Schema methodName '${schema.methodName}' is not a valid Kotlin identifier.")
        }
        if (schema.fields.isEmpty()) {
            throw BusinessValidationException("Schema ${schema.methodName} must define at least one field.")
        }
        if (schema.direction == CodegenSchemaDirection.READ && schema.modelName.isNullOrBlank()) {
            throw BusinessValidationException("READ schema ${schema.methodName} must define modelName.")
        }
        val modelName = schema.modelName
        if (!modelName.isNullOrBlank() && !IDENTIFIER_PATTERN.matches(modelName)) {
            throw BusinessValidationException("Schema modelName '$modelName' is not a valid Kotlin identifier.")
        }
        ensureDirectionMatchesFunctionCode(schema)
        val duplicateProperties =
            schema.fields.groupBy { it.propertyName }.filterValues { fields -> fields.size > 1 }.keys
        if (duplicateProperties.isNotEmpty()) {
            throw BusinessValidationException(
                "Schema ${schema.methodName} has duplicate propertyName values: ${duplicateProperties.joinToString()}.",
            )
        }
        schema.fields.forEach { field ->
            validateField(schema, field)
        }
        validateFieldOverlaps(schema)
        when (schema.functionCode) {
            CodegenFunctionCode.WRITE_SINGLE_COIL -> {
                if (schema.fields.size != 1 || schema.fields.single().transportType != CodegenTransportType.BOOL_COIL) {
                    throw BusinessValidationException("WRITE_SINGLE_COIL requires exactly one BOOL_COIL field.")
                }
            }

            CodegenFunctionCode.WRITE_SINGLE_REGISTER -> {
                if (schema.fields.size != 1 || schema.fields.single().transportType != CodegenTransportType.U16) {
                    throw BusinessValidationException("WRITE_SINGLE_REGISTER requires exactly one U16 field.")
                }
            }

            else -> Unit
        }
    }

    private fun validateField(
        schema: CodegenSchemaDto,
        field: CodegenFieldDto,
    ) {
        if (field.name.isBlank()) {
            throw BusinessValidationException("Field name cannot be blank in schema ${schema.methodName}.")
        }
        if (!IDENTIFIER_PATTERN.matches(field.propertyName)) {
            throw BusinessValidationException("Field propertyName '${field.propertyName}' is not a valid Kotlin identifier.")
        }
        if (field.registerOffset < 0) {
            throw BusinessValidationException("Field ${field.propertyName} must use registerOffset >= 0.")
        }
        if (field.bitOffset != 0) {
            throw BusinessValidationException("Field ${field.propertyName} currently requires bitOffset = 0 in V1.")
        }
        if (field.length < 1) {
            throw BusinessValidationException("Field ${field.propertyName} must use length >= 1.")
        }
        when (field.transportType) {
            CodegenTransportType.BOOL_COIL,
            CodegenTransportType.U8,
            CodegenTransportType.U16,
            CodegenTransportType.U32_BE -> {
                if (field.length != 1) {
                    throw BusinessValidationException("Field ${field.propertyName} only supports length = 1 in V1.")
                }
            }

            CodegenTransportType.BYTE_ARRAY,
            CodegenTransportType.STRING_ASCII,
            CodegenTransportType.STRING_UTF8 -> Unit
        }
        val expectsCoil = schema.functionCode.expectsCoilSpace()
        if (expectsCoil && field.transportType != CodegenTransportType.BOOL_COIL) {
            throw BusinessValidationException(
                "Schema ${schema.methodName} uses ${schema.functionCode.name}, so field ${field.propertyName} must be BOOL_COIL.",
            )
        }
        if (!expectsCoil && field.transportType == CodegenTransportType.BOOL_COIL) {
            throw BusinessValidationException(
                "Schema ${schema.methodName} uses ${schema.functionCode.name}, so field ${field.propertyName} cannot be BOOL_COIL.",
            )
        }
    }

    private fun validateFieldOverlaps(
        schema: CodegenSchemaDto,
    ) {
        val occupied = linkedSetOf<String>()
        schema.fields.sortedBy { it.sortIndex }.forEach { field ->
            val keys =
                if (schema.functionCode.expectsCoilSpace()) {
                    listOf("c${field.registerOffset}")
                } else {
                    val width = field.transportType.registerWidth(field.length)
                    (field.registerOffset until field.registerOffset + width).map { register ->
                        "r$register"
                    }
                }
            keys.forEach { key ->
                if (!occupied.add(key)) {
                    throw BusinessValidationException(
                        "Schema ${schema.methodName} has overlapping field layout around ${field.propertyName}.",
                    )
                }
            }
        }
    }

    private fun ensureDirectionMatchesFunctionCode(
        schema: CodegenSchemaDto,
    ) {
        val allowedFunctionCodes =
            when (schema.direction) {
                CodegenSchemaDirection.READ ->
                    setOf(
                        CodegenFunctionCode.READ_COILS,
                        CodegenFunctionCode.READ_DISCRETE_INPUTS,
                        CodegenFunctionCode.READ_INPUT_REGISTERS,
                        CodegenFunctionCode.READ_HOLDING_REGISTERS,
                    )

                CodegenSchemaDirection.WRITE ->
                    setOf(
                        CodegenFunctionCode.WRITE_SINGLE_COIL,
                        CodegenFunctionCode.WRITE_MULTIPLE_COILS,
                        CodegenFunctionCode.WRITE_SINGLE_REGISTER,
                        CodegenFunctionCode.WRITE_MULTIPLE_REGISTERS,
                    )
            }
        if (schema.functionCode !in allowedFunctionCodes) {
            throw BusinessValidationException(
                "Schema ${schema.methodName} uses ${schema.functionCode.name}, which does not match ${schema.direction.name}.",
            )
        }
    }

    private fun ensureSupportedTemplate(
        protocolTemplate: ProtocolTemplate,
    ) {
        if (protocolTemplate.code !in SUPPORTED_PROTOCOL_TEMPLATE_CODES) {
            throw BusinessValidationException(
                "Protocol template ${protocolTemplate.code} is not supported by the Modbus contract generator.",
            )
        }
    }

    private fun loadContext(
        contextId: Long,
    ): CodegenContext {
        return sql.createQuery(CodegenContext::class) {
            where(table.id eq contextId)
            select(table.fetch(CodegenContextFetchers.contextDetail))
        }.execute().firstOrNull() ?: throw NotFoundException("Codegen context $contextId was not found.")
    }

    private fun loadProtocolTemplate(
        protocolTemplateId: Long,
    ): ProtocolTemplate {
        return sql.createQuery(ProtocolTemplate::class) {
            where(table.id eq protocolTemplateId)
            select(table.fetch(Fetchers.protocolTemplate))
        }.execute().firstOrNull() ?: throw NotFoundException("Protocol template $protocolTemplateId was not found.")
    }

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

    private fun ensureCodeUnique(
        connection: Connection,
        code: String,
        ignoreId: Long?,
    ) {
        val count =
            if (ignoreId == null) {
                jdbc.queryCount(
                    connection,
                    "SELECT COUNT(1) FROM codegen_context_context WHERE code = ?",
                    code,
                )
            } else {
                jdbc.queryCount(
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

    private fun CodegenContext.toDetailDto(): CodegenContextDetailDto {
        return CodegenContextDetailDto(
            id = id,
            code = code,
            name = name,
            description = description,
            enabled = enabled,
            consumerTarget = consumerTarget,
            protocolTemplateId = protocolTemplate.id,
            protocolTemplateCode = protocolTemplate.code,
            protocolTemplateName = protocolTemplate.name,
            externalCOutputRoot = externalCOutputRoot,
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
                ),
            schemas =
                schemas.sortedWith(compareBy(CodegenSchema::sortIndex, CodegenSchema::id)).map { schema ->
                    CodegenSchemaDto(
                        id = schema.id,
                        name = schema.name,
                        description = schema.description,
                        sortIndex = schema.sortIndex,
                        direction = schema.direction,
                        functionCode = schema.functionCode,
                        baseAddress = schema.baseAddress,
                        methodName = schema.methodName,
                        modelName = schema.modelName,
                        fields =
                            schema.fields.sortedWith(compareBy(CodegenField::sortIndex, CodegenField::id)).map { field ->
                                CodegenFieldDto(
                                    id = field.id,
                                    name = field.name,
                                    description = field.description,
                                    sortIndex = field.sortIndex,
                                    propertyName = field.propertyName,
                                    transportType = field.transportType,
                                    registerOffset = field.registerOffset,
                                    bitOffset = field.bitOffset,
                                    length = field.length,
                                    translationHint = field.translationHint,
                                    defaultLiteral = field.defaultLiteral,
                                )
                            },
                    )
                },
        )
    }

    private fun CodegenContextDetailDto.normalized(): CodegenContextDetailDto {
        return copy(
            code = code.trim(),
            name = name.trim(),
            description = description.cleanNullable(),
            enabled = enabled,
            consumerTarget = CodegenConsumerTarget.MCU_CONSOLE,
            externalCOutputRoot = externalCOutputRoot.cleanNullable(),
            generationSettings = generationSettings.normalized(),
            schemas =
                schemas.map { schema ->
                    schema.copy(
                        name = schema.name.trim(),
                        description = schema.description.cleanNullable(),
                        methodName = schema.methodName.trim(),
                        modelName = schema.modelName.cleanNullable(),
                        fields =
                            schema.fields.map { field ->
                                field.copy(
                                    name = field.name.trim(),
                                    description = field.description.cleanNullable(),
                                    propertyName = field.propertyName.trim(),
                                    translationHint = field.translationHint.cleanNullable(),
                                    defaultLiteral = field.defaultLiteral.cleanNullable(),
                                )
                            },
                    )
                },
        )
    }

    private fun String?.cleanNullable(): String? {
        return this?.trim()?.takeIf(String::isNotBlank)
    }

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
        )
    }

    private fun nowSqlValue(): String {
        return SQLITE_DATE_TIME_FORMATTER.format(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime())
    }
}

private fun CodegenFunctionCode.expectsCoilSpace(): Boolean =
    when (this) {
        CodegenFunctionCode.READ_COILS,
        CodegenFunctionCode.READ_DISCRETE_INPUTS,
        CodegenFunctionCode.WRITE_SINGLE_COIL,
        CodegenFunctionCode.WRITE_MULTIPLE_COILS -> true
        else -> false
    }

private fun CodegenTransportType.registerWidth(length: Int): Int =
    when (this) {
        CodegenTransportType.BOOL_COIL,
        CodegenTransportType.U8,
        CodegenTransportType.U16 -> 1
        CodegenTransportType.U32_BE -> 2
        CodegenTransportType.BYTE_ARRAY -> (length + 1) / 2
        CodegenTransportType.STRING_ASCII,
        CodegenTransportType.STRING_UTF8 -> length
    }
