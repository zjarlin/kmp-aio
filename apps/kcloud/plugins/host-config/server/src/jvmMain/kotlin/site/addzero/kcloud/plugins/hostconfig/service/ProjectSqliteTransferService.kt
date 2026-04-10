package site.addzero.kcloud.plugins.hostconfig.service

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager
import javax.sql.DataSource
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectSqliteFileResponse
import site.addzero.kmp.exp.BusinessValidationException
import site.addzero.kmp.exp.NotFoundException
import site.addzero.util.db.SqlExecutor

private const val HOST_CONFIG_PROJECT_SQLITE_ROOT_PROPERTY =
    "site.addzero.kcloud.hostConfig.projectSqliteRoot"

@Single
class ProjectSqliteTransferService(
    private val jdbc: SqlExecutor,
) {
    fun exportProjectSqlite(projectId: Long): ProjectSqliteFileResponse {
        val projectRow = jdbc.queryForList(
            "SELECT * FROM host_config_project WHERE id = ?",
            projectId,
        ).firstOrNull() ?: throw NotFoundException("Project not found")
        val projectName = projectRow["name"]?.toString()?.trim().orEmpty()
            .ifBlank { "project-$projectId" }
        val targetFile = resolveExportFile(projectId, projectName)
        recreateFile(targetFile)
        val dataSource = sqliteDataSource(targetFile)
        ensureHostConfigSqliteSchema(dataSource)
        val sqliteExecutor = SqlExecutor(dataSource)
        sqliteExecutor.withTransaction { targetConnection ->
            targetConnection.createStatement().use { statement ->
                statement.execute("PRAGMA foreign_keys = OFF")
            }
            clearTargetTables(targetConnection)
            jdbc.withConnection { sourceConnection ->
                exportPlans(projectId).forEach { plan ->
                    copyQueryResult(
                        sourceConnection = sourceConnection,
                        targetConnection = targetConnection,
                        tableName = plan.tableName,
                        sql = plan.sql,
                        params = plan.params,
                    )
                }
            }
            targetConnection.createStatement().use { statement ->
                statement.execute("PRAGMA foreign_keys = ON")
            }
        }
        return readSqliteFileResponse(
            sqliteFile = targetFile,
            projectId = projectId,
            projectName = projectName,
        )
    }

    fun importProjectSqlite(sourceFilePath: String): ProjectSqliteFileResponse {
        val sourceFile = normalizeSourceFile(sourceFilePath)
        validateSqliteFile(sourceFile)
        val targetFile = resolveImportFile(sourceFile)
        if (sourceFile.canonicalFile != targetFile.canonicalFile) {
            targetFile.parentFile?.mkdirs()
            Files.copy(
                sourceFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
        return readSqliteFileResponse(sqliteFile = targetFile)
    }

    private fun clearTargetTables(
        connection: Connection,
    ) {
        connection.createStatement().use { statement ->
            exportedTableNames.reversed().forEach { tableName ->
                statement.executeUpdate("DELETE FROM $tableName")
            }
        }
    }

    private fun copyQueryResult(
        sourceConnection: Connection,
        targetConnection: Connection,
        tableName: String,
        sql: String,
        params: List<Any?>,
    ) {
        val rows = queryRows(sourceConnection, sql, params)
        if (rows.isEmpty()) {
            return
        }
        val columns = rows.first().keys.toList()
        val placeholders = columns.joinToString(", ") { "?" }
        val insertSql = buildString {
            append("INSERT INTO ")
            append(tableName)
            append(" (")
            append(columns.joinToString(", "))
            append(") VALUES (")
            append(placeholders)
            append(")")
        }
        targetConnection.prepareStatement(insertSql).use { statement ->
            rows.forEach { row ->
                columns.forEachIndexed { index, column ->
                    statement.setObject(index + 1, row[column])
                }
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private fun queryRows(
        connection: Connection,
        sql: String,
        params: List<Any?>,
    ): List<LinkedHashMap<String, Any?>> =
        connection.prepareStatement(sql).use { statement ->
            params.forEachIndexed { index, value ->
                statement.setObject(index + 1, value)
            }
            statement.executeQuery().use { resultSet ->
                buildList {
                    while (resultSet.next()) {
                        val metaData = resultSet.metaData
                        add(
                            LinkedHashMap<String, Any?>(metaData.columnCount).apply {
                                for (index in 1..metaData.columnCount) {
                                    put(metaData.getColumnLabel(index), resultSet.getObject(index))
                                }
                            },
                        )
                    }
                }
            }
        }

    private fun normalizeSourceFile(sourceFilePath: String): File {
        val normalizedPath = sourceFilePath.trim()
        if (normalizedPath.isBlank()) {
            throw BusinessValidationException("SQLite file path is required")
        }
        val sourceFile = File(normalizedPath).absoluteFile
        if (!sourceFile.exists() || !sourceFile.isFile) {
            throw BusinessValidationException("SQLite file does not exist: ${sourceFile.absolutePath}")
        }
        return sourceFile
    }

    private fun validateSqliteFile(
        sqliteFile: File,
    ) {
        val dataSource = sqliteDataSource(sqliteFile)
        val sqliteExecutor = SqlExecutor(dataSource)
        val hasProjectTable = sqliteExecutor.queryCount(
            "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'host_config_project'",
        ) > 0
        val hasProtocolTemplateTable = sqliteExecutor.queryCount(
            "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'host_config_protocol_template'",
        ) > 0
        if (!hasProjectTable || !hasProtocolTemplateTable) {
            throw BusinessValidationException("Selected file is not a valid host-config sqlite project")
        }
    }

    private fun readSqliteFileResponse(
        sqliteFile: File,
        projectId: Long? = null,
        projectName: String? = null,
    ): ProjectSqliteFileResponse {
        val dataSource = sqliteDataSource(sqliteFile)
        val sqliteExecutor = SqlExecutor(dataSource)
        val summaryText = buildSummaryText(sqliteExecutor)
        val resolvedProjectName = projectName ?: sqliteExecutor.query(
            "SELECT name FROM host_config_project ORDER BY id ASC LIMIT 1",
            mapper = { resultSet -> resultSet.getString(1) },
        ).firstOrNull()
        return ProjectSqliteFileResponse(
            projectId = projectId,
            projectName = resolvedProjectName,
            fileName = sqliteFile.name,
            filePath = sqliteFile.absolutePath,
            dataDirectory = hostConfigProjectSqliteRootDirectory().absolutePath,
            sizeBytes = sqliteFile.length(),
            summaryText = summaryText,
        )
    }

    private fun buildSummaryText(
        sqliteExecutor: SqlExecutor,
    ): String {
        val projectCount = sqliteExecutor.queryCount("SELECT COUNT(*) FROM host_config_project")
        val productCount = sqliteExecutor.queryCount("SELECT COUNT(*) FROM host_config_product_definition")
        val deviceDefinitionCount = sqliteExecutor.queryCount("SELECT COUNT(*) FROM host_config_device_definition")
        val propertyCount = sqliteExecutor.queryCount("SELECT COUNT(*) FROM host_config_property_definition")
        val featureCount = sqliteExecutor.queryCount("SELECT COUNT(*) FROM host_config_feature_definition")
        val moduleCount = sqliteExecutor.queryCount("SELECT COUNT(*) FROM host_config_module_instance")
        val deviceCount = sqliteExecutor.queryCount("SELECT COUNT(*) FROM host_config_device")
        val tagCount = sqliteExecutor.queryCount("SELECT COUNT(*) FROM host_config_tag")
        return "工程 ${projectCount} 个，物模型 ${productCount} 个，设备定义 ${deviceDefinitionCount} 个，属性 ${propertyCount} 个，功能 ${featureCount} 个，模块 ${moduleCount} 个，设备 ${deviceCount} 个，点位 ${tagCount} 个"
    }

    private fun resolveExportFile(
        projectId: Long,
        projectName: String,
    ): File {
        val baseName = sanitizeFileName(projectName).ifBlank { "project-$projectId" }
        return File(
            hostConfigProjectSqliteRootDirectory(),
            "host-config-project-$projectId-$baseName.sqlite",
        )
    }

    private fun resolveImportFile(
        sourceFile: File,
    ): File {
        val root = hostConfigProjectSqliteRootDirectory().canonicalFile
        val sourceCanonical = sourceFile.canonicalFile
        if (sourceCanonical.parentFile?.canonicalFile == root) {
            return sourceCanonical
        }
        val extension = sourceCanonical.extension.ifBlank { "sqlite" }
        val baseName = sanitizeFileName(sourceCanonical.nameWithoutExtension).ifBlank { "imported-project" }
        var candidate = File(root, "$baseName.$extension")
        var index = 1
        while (candidate.exists()) {
            candidate = File(root, "$baseName-$index.$extension")
            index += 1
        }
        return candidate
    }

    private fun recreateFile(
        targetFile: File,
    ) {
        targetFile.parentFile?.mkdirs()
        if (targetFile.exists()) {
            targetFile.delete()
        }
    }

    private fun sanitizeFileName(value: String): String {
        return value.trim()
            .replace(Regex("[\\\\/:*?\"<>|]+"), "-")
            .replace(Regex("\\s+"), "-")
            .trim('-')
    }

    private fun sqliteDataSource(
        databaseFile: File,
    ): DataSource {
        databaseFile.parentFile?.mkdirs()
        Class.forName("org.sqlite.JDBC")
        val jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
        return object : DataSource {
            override fun getConnection() = DriverManager.getConnection(jdbcUrl)

            override fun getConnection(
                username: String?,
                password: String?,
            ) = getConnection()

            override fun getLogWriter() = null

            override fun setLogWriter(out: java.io.PrintWriter?) = Unit

            override fun setLoginTimeout(seconds: Int) = Unit

            override fun getLoginTimeout() = 0

            override fun getParentLogger() = java.util.logging.Logger.getLogger("org.sqlite")

            @Suppress("UNCHECKED_CAST")
            override fun <T : Any?> unwrap(iface: Class<T>) = this as T

            override fun isWrapperFor(iface: Class<*>): Boolean = iface.isInstance(this)
        }
    }

    private data class ExportQueryPlan(
        val tableName: String,
        val sql: String,
        val params: List<Any?> = emptyList(),
    )

    private fun exportPlans(projectId: Long): List<ExportQueryPlan> {
        return listOf(
            ExportQueryPlan(
                tableName = "host_config_protocol_template",
                sql = "SELECT * FROM host_config_protocol_template ORDER BY sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_module_template",
                sql = "SELECT * FROM host_config_module_template ORDER BY protocol_template_id ASC, sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_device_type",
                sql = "SELECT * FROM host_config_device_type ORDER BY sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_register_type",
                sql = "SELECT * FROM host_config_register_type ORDER BY sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_data_type",
                sql = "SELECT * FROM host_config_data_type ORDER BY sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_label_definition",
                sql = "SELECT * FROM host_config_label_definition ORDER BY sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_product_definition",
                sql = "SELECT * FROM host_config_product_definition ORDER BY sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_device_definition",
                sql = "SELECT * FROM host_config_device_definition ORDER BY product_id ASC, sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_product_definition_label",
                sql = "SELECT * FROM host_config_product_definition_label ORDER BY product_id ASC, sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_property_definition",
                sql = "SELECT * FROM host_config_property_definition ORDER BY device_definition_id ASC, sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_feature_definition",
                sql = "SELECT * FROM host_config_feature_definition ORDER BY device_definition_id ASC, sort_index ASC, id ASC",
            ),
            ExportQueryPlan(
                tableName = "host_config_project",
                sql = "SELECT * FROM host_config_project WHERE id = ?",
                params = listOf(projectId),
            ),
            ExportQueryPlan(
                tableName = "host_config_protocol_instance",
                sql = """
                    SELECT pi.*
                    FROM host_config_protocol_instance pi
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = pi.id
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, pi.id ASC
                """.trimIndent(),
                params = listOf(projectId),
            ),
            ExportQueryPlan(
                tableName = "host_config_project_protocol",
                sql = """
                    SELECT pp.*
                    FROM host_config_project_protocol pp
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, pp.id ASC
                """.trimIndent(),
                params = listOf(projectId),
            ),
            ExportQueryPlan(
                tableName = "host_config_module_instance",
                sql = """
                    SELECT mi.*
                    FROM host_config_module_instance mi
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, mi.sort_index ASC, mi.id ASC
                """.trimIndent(),
                params = listOf(projectId),
            ),
            ExportQueryPlan(
                tableName = "host_config_device",
                sql = """
                    SELECT d.*
                    FROM host_config_device d
                    INNER JOIN host_config_module_instance mi ON mi.id = d.module_id
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, mi.sort_index ASC, d.sort_index ASC, d.id ASC
                """.trimIndent(),
                params = listOf(projectId),
            ),
            ExportQueryPlan(
                tableName = "host_config_tag",
                sql = """
                    SELECT t.*
                    FROM host_config_tag t
                    INNER JOIN host_config_device d ON d.id = t.device_id
                    INNER JOIN host_config_module_instance mi ON mi.id = d.module_id
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, mi.sort_index ASC, d.sort_index ASC, t.sort_index ASC, t.id ASC
                """.trimIndent(),
                params = listOf(projectId),
            ),
            ExportQueryPlan(
                tableName = "host_config_tag_value_text",
                sql = """
                    SELECT tvt.*
                    FROM host_config_tag_value_text tvt
                    INNER JOIN host_config_tag t ON t.id = tvt.tag_id
                    INNER JOIN host_config_device d ON d.id = t.device_id
                    INNER JOIN host_config_module_instance mi ON mi.id = d.module_id
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
                    WHERE pp.project_id = ?
                    ORDER BY tvt.tag_id ASC, tvt.sort_index ASC, tvt.id ASC
                """.trimIndent(),
                params = listOf(projectId),
            ),
            ExportQueryPlan(
                tableName = "host_config_project_mqtt_config",
                sql = "SELECT * FROM host_config_project_mqtt_config WHERE project_id = ? ORDER BY id ASC",
                params = listOf(projectId),
            ),
            ExportQueryPlan(
                tableName = "host_config_project_modbus_server_config",
                sql = "SELECT * FROM host_config_project_modbus_server_config WHERE project_id = ? ORDER BY transport_type ASC, id ASC",
                params = listOf(projectId),
            ),
            ExportQueryPlan(
                tableName = "host_config_project_gateway_pin_config",
                sql = "SELECT * FROM host_config_project_gateway_pin_config WHERE project_id = ? ORDER BY id ASC",
                params = listOf(projectId),
            ),
        )
    }
}

private val exportedTableNames: List<String> = listOf(
    "host_config_protocol_template",
    "host_config_module_template",
    "host_config_device_type",
    "host_config_register_type",
    "host_config_data_type",
    "host_config_label_definition",
    "host_config_product_definition",
    "host_config_device_definition",
    "host_config_product_definition_label",
    "host_config_property_definition",
    "host_config_feature_definition",
    "host_config_project",
    "host_config_protocol_instance",
    "host_config_project_protocol",
    "host_config_module_instance",
    "host_config_device",
    "host_config_tag",
    "host_config_tag_value_text",
    "host_config_project_mqtt_config",
    "host_config_project_modbus_server_config",
    "host_config_project_gateway_pin_config",
)

internal fun hostConfigProjectSqliteRootDirectory(): File {
    val overridePath = System.getProperty(HOST_CONFIG_PROJECT_SQLITE_ROOT_PROPERTY)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
    val root = overridePath?.let(::File) ?: File(
        File(System.getProperty("user.home"), ".kcloud/local"),
        "host-config/projects",
    )
    root.mkdirs()
    return root
}
