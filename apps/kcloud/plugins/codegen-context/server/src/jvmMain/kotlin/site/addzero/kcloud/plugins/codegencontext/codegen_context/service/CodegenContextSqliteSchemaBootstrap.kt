package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import io.ktor.server.application.Application
import io.ktor.server.application.log
import java.sql.Connection
import javax.sql.DataSource
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

private const val CODEGEN_CONTEXT_SQLITE_SCHEMA_RESOURCE = "/db/sqlite/codegen-context-local-schema.sql"

private val CODEGEN_CONTEXT_LEGACY_TIMESTAMP_MIGRATIONS =
    listOf(
        SqliteLegacyTableMigration(
            tableName = "codegen_context_context",
            legacyTableName = "codegen_context_context_legacy_timestamp",
            insertColumns =
                """
                id,
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
                """.trimIndent(),
            selectColumns =
                """
                id,
                code,
                name,
                description,
                enabled,
                NULL,
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
                ${sqliteEpochMillisExpression("create_time")},
                COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})
                """.trimIndent(),
        ),
        SqliteLegacyTableMigration(
            tableName = "codegen_context_class",
            legacyTableName = "codegen_context_class_legacy_timestamp",
            insertColumns =
                """
                id,
                context_id,
                name,
                description,
                sort_index,
                class_kind,
                class_name,
                package_name,
                created_at,
                updated_at
                """.trimIndent(),
            selectColumns =
                """
                id,
                context_id,
                name,
                description,
                sort_index,
                class_kind,
                class_name,
                package_name,
                ${sqliteEpochMillisExpression("create_time")},
                COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})
                """.trimIndent(),
        ),
        SqliteLegacyTableMigration(
            tableName = "codegen_context_method",
            legacyTableName = "codegen_context_method_legacy_timestamp",
            insertColumns =
                """
                id,
                owner_class_id,
                name,
                description,
                sort_index,
                method_name,
                request_class_name,
                response_class_name,
                created_at,
                updated_at
                """.trimIndent(),
            selectColumns =
                """
                id,
                owner_class_id,
                name,
                description,
                sort_index,
                method_name,
                request_class_name,
                response_class_name,
                ${sqliteEpochMillisExpression("create_time")},
                COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})
                """.trimIndent(),
        ),
        SqliteLegacyTableMigration(
            tableName = "codegen_context_property",
            legacyTableName = "codegen_context_property_legacy_timestamp",
            insertColumns =
                """
                id,
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
                """.trimIndent(),
            selectColumns =
                """
                id,
                owner_class_id,
                name,
                description,
                sort_index,
                property_name,
                type_name,
                nullable,
                default_literal,
                ${sqliteEpochMillisExpression("create_time")},
                COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})
                """.trimIndent(),
        ),
        SqliteLegacyTableMigration(
            tableName = "codegen_context_definition",
            legacyTableName = "codegen_context_definition_legacy_timestamp",
            insertColumns =
                """
                id,
                protocol_template_id,
                code,
                name,
                description,
                sort_index,
                target_kind,
                binding_target_mode,
                source_kind,
                created_at,
                updated_at
                """.trimIndent(),
            selectColumns =
                """
                id,
                protocol_template_id,
                code,
                name,
                description,
                sort_index,
                target_kind,
                binding_target_mode,
                source_kind,
                ${sqliteEpochMillisExpression("create_time")},
                COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})
                """.trimIndent(),
        ),
        SqliteLegacyTableMigration(
            tableName = "codegen_context_param_definition",
            legacyTableName = "codegen_context_param_definition_legacy_timestamp",
            insertColumns =
                """
                id,
                definition_id,
                code,
                name,
                description,
                sort_index,
                value_type,
                required,
                default_value,
                enum_options,
                placeholder,
                created_at,
                updated_at
                """.trimIndent(),
            selectColumns =
                """
                id,
                definition_id,
                code,
                name,
                description,
                sort_index,
                value_type,
                required,
                default_value,
                enum_options,
                placeholder,
                ${sqliteEpochMillisExpression("create_time")},
                COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})
                """.trimIndent(),
        ),
        SqliteLegacyTableMigration(
            tableName = "codegen_context_binding",
            legacyTableName = "codegen_context_binding_legacy_timestamp",
            insertColumns =
                """
                id,
                definition_id,
                owner_class_id,
                owner_method_id,
                owner_property_id,
                sort_index,
                created_at,
                updated_at
                """.trimIndent(),
            selectColumns =
                """
                id,
                definition_id,
                owner_class_id,
                owner_method_id,
                owner_property_id,
                sort_index,
                ${sqliteEpochMillisExpression("create_time")},
                COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})
                """.trimIndent(),
        ),
        SqliteLegacyTableMigration(
            tableName = "codegen_context_binding_value",
            legacyTableName = "codegen_context_binding_value_legacy_timestamp",
            insertColumns =
                """
                id,
                binding_id,
                param_definition_id,
                value,
                created_at,
                updated_at
                """.trimIndent(),
            selectColumns =
                """
                id,
                binding_id,
                param_definition_id,
                value,
                ${sqliteEpochMillisExpression("create_time")},
                COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})
                """.trimIndent(),
        ),
    )

@Named("codegenContextSqliteSchemaBootstrap")
@Single
class CodegenContextSqliteSchemaBootstrap(
    private val dataSource: DataSource,
) : AppStarter {
    override val order: Int = 120
    override val enable: Boolean = true

    override fun onInstall(application: Application) {
        if (!ensureCodegenContextSqliteSchema(dataSource)) {
            return
        }
        application.log.info("Codegen-context SQLite schema is ready.")
    }
}

internal fun ensureCodegenContextSqliteSchema(
    dataSource: DataSource,
): Boolean {
    dataSource.connection.use { connection ->
        if (!connection.isSqliteConnection()) {
            return false
        }
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
        }
        migrateLegacyCodegenContextTimestampSchema(connection)
        executeSqliteSchemaScript(
            owner = CodegenContextSqliteSchemaBootstrap::class.java,
            connection = connection,
            resourcePath = CODEGEN_CONTEXT_SQLITE_SCHEMA_RESOURCE,
        )
        ensureSqliteColumn(
            connection = connection,
            tableName = "codegen_context_context",
            columnName = "node_id",
            columnDefinition = "TEXT",
        )
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
        }
    }
    return true
}

private data class SqliteLegacyTableMigration(
    val tableName: String,
    val legacyTableName: String,
    val insertColumns: String,
    val selectColumns: String,
) {
    fun copySql(
        connection: Connection,
    ): String {
        val legacyColumns = connection.tableColumnNames(legacyTableName)
        val resolvedColumns =
            splitSqlExpressionList(insertColumns)
                .zip(splitSqlExpressionList(selectColumns))
                .mapNotNull { (insertColumn, selectExpression) ->
                    resolveLegacyColumnProjection(
                        insertColumn = insertColumn,
                        selectExpression = selectExpression,
                        legacyColumns = legacyColumns,
                    )
                }
        check(resolvedColumns.isNotEmpty()) {
            "No compatible columns found while migrating $legacyTableName -> $tableName"
        }
        return """
            INSERT INTO $tableName (
                ${resolvedColumns.joinToString(",\n                ") { it.first }}
            )
            SELECT
                ${resolvedColumns.joinToString(",\n                ") { it.second }}
            FROM $legacyTableName
        """.trimIndent()
    }
}

private fun migrateLegacyCodegenContextTimestampSchema(
    connection: Connection,
) {
    val migrations =
        CODEGEN_CONTEXT_LEGACY_TIMESTAMP_MIGRATIONS.filter { migration ->
            connection.tableHasColumn(migration.tableName, "create_time")
        }
    if (migrations.isEmpty()) {
        return
    }
    val previousAutoCommit = connection.autoCommit
    connection.autoCommit = false
    try {
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = OFF")
            migrations.forEach { migration ->
                statement.execute("ALTER TABLE ${migration.tableName} RENAME TO ${migration.legacyTableName}")
            }
        }
        executeSqliteSchemaScript(
            owner = CodegenContextSqliteSchemaBootstrap::class.java,
            connection = connection,
            resourcePath = CODEGEN_CONTEXT_SQLITE_SCHEMA_RESOURCE,
            filter = { sql -> sql.startsWith("CREATE TABLE", ignoreCase = true) },
        )
        connection.createStatement().use { statement ->
            migrations.forEach { migration ->
                statement.execute(migration.copySql(connection))
            }
            migrations.asReversed().forEach { migration ->
                statement.execute("DROP TABLE IF EXISTS ${migration.legacyTableName}")
            }
        }
        connection.commit()
    } catch (throwable: Throwable) {
        connection.rollback()
        throw throwable
    } finally {
        connection.autoCommit = previousAutoCommit
    }
}

private fun executeSqliteSchemaScript(
    owner: Class<*>,
    connection: Connection,
    resourcePath: String,
    filter: (String) -> Boolean = { true },
) {
    val script =
        checkNotNull(owner.getResource(resourcePath)) {
            "Missing SQLite schema resource: $resourcePath"
        }.readText()
    connection.createStatement().use { statement ->
        script.splitToSequence(';')
            .map(String::trim)
            .filter(String::isNotBlank)
            .filter(filter)
            .forEach(statement::execute)
    }
}

private fun Connection.isSqliteConnection(): Boolean = metaData.url.startsWith("jdbc:sqlite:")

private fun Connection.tableHasColumn(
    tableName: String,
    columnName: String,
): Boolean {
    createStatement().use { statement ->
        statement.executeQuery("PRAGMA table_info($tableName)").use { resultSet ->
            while (resultSet.next()) {
                if (resultSet.getString("name").equals(columnName, ignoreCase = true)) {
                    return true
                }
            }
        }
    }
    return false
}

private fun Connection.tableColumnNames(
    tableName: String,
): Set<String> {
    val columnNames = linkedSetOf<String>()
    createStatement().use { statement ->
        statement.executeQuery("PRAGMA table_info($tableName)").use { resultSet ->
            while (resultSet.next()) {
                columnNames += resultSet.getString("name")
            }
        }
    }
    return columnNames
}

private fun ensureSqliteColumn(
    connection: Connection,
    tableName: String,
    columnName: String,
    columnDefinition: String,
) {
    if (connection.tableHasColumn(tableName, columnName)) {
        return
    }
    connection.createStatement().use { statement ->
        statement.execute("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
    }
}

private fun resolveLegacyColumnProjection(
    insertColumn: String,
    selectExpression: String,
    legacyColumns: Set<String>,
): Pair<String, String>? {
    return when (insertColumn) {
        "created_at" -> {
            when {
                legacyColumns.contains("create_time") -> insertColumn to sqliteEpochMillisExpression("create_time")
                legacyColumns.contains("created_at") -> insertColumn to "created_at"
                else -> null
            }
        }

        "updated_at" -> {
            when {
                legacyColumns.contains("update_time") && legacyColumns.contains("create_time") ->
                    insertColumn to
                        "COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})"
                legacyColumns.contains("updated_at") -> insertColumn to "updated_at"
                legacyColumns.contains("update_time") -> insertColumn to sqliteEpochMillisExpression("update_time")
                legacyColumns.contains("create_time") -> insertColumn to sqliteEpochMillisExpression("create_time")
                else -> null
            }
        }

        else -> {
            if (legacyColumns.contains(insertColumn)) {
                insertColumn to selectExpression
            } else {
                null
            }
        }
    }
}

private fun splitSqlExpressionList(
    value: String,
): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var parenthesesDepth = 0
    var inSingleQuotedString = false
    value.forEach { char ->
        when (char) {
            '\'' -> {
                inSingleQuotedString = !inSingleQuotedString
                current.append(char)
            }

            '(' -> {
                if (!inSingleQuotedString) {
                    parenthesesDepth += 1
                }
                current.append(char)
            }

            ')' -> {
                if (!inSingleQuotedString && parenthesesDepth > 0) {
                    parenthesesDepth -= 1
                }
                current.append(char)
            }

            ',' -> {
                if (!inSingleQuotedString && parenthesesDepth == 0) {
                    current.toString().trim().takeIf(String::isNotBlank)?.let(result::add)
                    current.clear()
                } else {
                    current.append(char)
                }
            }

            else -> current.append(char)
        }
    }
    current.toString().trim().takeIf(String::isNotBlank)?.let(result::add)
    return result
}

private fun sqliteEpochMillisExpression(
    columnName: String,
): String {
    return """
        CASE
            WHEN $columnName IS NULL THEN NULL
            WHEN typeof($columnName) IN ('integer', 'real') THEN CAST($columnName AS INTEGER)
            ELSE CAST(strftime('%s', $columnName) AS INTEGER) * 1000
        END
    """.trimIndent()
}
