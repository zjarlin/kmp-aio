package site.addzero.configcenter

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.Properties
import java.util.UUID

class JdbcConfigCenterValueService(
    private val settings: ConfigCenterJdbcSettings,
) : ConfigCenterValueService {
    private val dialect: ConfigCenterJdbcDialect = settings.detectDialect()

    init {
        settings.driver?.let { driver ->
            Class.forName(driver)
        }
        if (settings.autoDdl) {
            withConnection { connection ->
                ensureSchema(connection)
            }
        }
    }

    override fun listValues(
        namespace: String?,
        active: String?,
        keyword: String?,
        limit: Int,
    ): List<ConfigCenterValueDto> {
        return withConnection { connection ->
            val selectedColumns = selectColumnsSql(connection)
            val clauses = mutableListOf<String>()
            val values = mutableListOf<Any>()
            namespace?.trim()?.takeIf(String::isNotBlank)?.let { rawNamespace ->
                clauses += "namespace = ?"
                values += normalizeConfigCenterNamespace(rawNamespace)
            }
            active?.trim()?.takeIf(String::isNotBlank)?.let { rawActive ->
                clauses += "active_profile = ?"
                values += normalizeConfigCenterActive(rawActive)
            }
            keyword?.trim()?.takeIf(String::isNotBlank)?.let { rawKeyword ->
                clauses += "(config_key LIKE ? OR config_value LIKE ? OR COALESCE(description, '') LIKE ?)"
                val likeValue = "%$rawKeyword%"
                values += likeValue
                values += likeValue
                values += likeValue
            }
            val whereClause = if (clauses.isEmpty()) "" else clauses.joinToString(
                prefix = " WHERE ",
                separator = " AND ",
            )
            connection.prepareStatement(
                """
                SELECT $selectedColumns
                FROM config_center_value
                $whereClause
                ORDER BY namespace ASC, active_profile ASC, config_key ASC
                LIMIT ?
                """.trimIndent(),
            ).use { statement ->
                var parameterIndex = 1
                for (value in values) {
                    statement.setObject(parameterIndex++, value)
                }
                statement.setInt(parameterIndex, limit.coerceIn(1, 2_000))
                statement.executeQuery().use(::readValueRows)
            }
        }
    }

    override fun readValue(
        namespace: String,
        key: String,
        active: String,
    ): ConfigCenterValueDto {
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        val normalizedActive = normalizeConfigCenterActive(active)
        val normalizedKey = key.trim()
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedKey.isNotBlank()) { "key cannot be blank" }

        return withConnection { connection ->
            val selectedColumns = selectColumnsSql(connection)
            connection.prepareStatement(
                """
                SELECT $selectedColumns
                FROM config_center_value
                WHERE namespace = ?
                  AND active_profile = ?
                  AND config_key = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, normalizedNamespace)
                statement.setString(2, normalizedActive)
                statement.setString(3, normalizedKey)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        resultSet.toValueDto()
                    } else {
                        ConfigCenterValueDto(
                            namespace = normalizedNamespace,
                            active = normalizedActive,
                            key = normalizedKey,
                        )
                    }
                }
            }
        }
    }

    override fun writeValue(
        request: ConfigCenterValueWriteRequest,
    ): ConfigCenterValueDto {
        val normalizedNamespace = normalizeConfigCenterNamespace(request.namespace)
        val normalizedActive = normalizeConfigCenterActive(request.active)
        val normalizedKey = request.key.trim()
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedKey.isNotBlank()) { "key cannot be blank" }

        val now = System.currentTimeMillis()
        val createdAt = withConnection { connection ->
            lookupExistingCreateTime(
                connection = connection,
                namespace = normalizedNamespace,
                active = normalizedActive,
                key = normalizedKey,
            ) ?: now
        }

        withConnection { connection ->
            connection.prepareStatement(
                """
                INSERT INTO config_center_value (
                    id, namespace, active_profile, config_key, config_value, description, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(namespace, active_profile, config_key) DO UPDATE SET
                    config_value = excluded.config_value,
                    description = excluded.description,
                    update_time = excluded.update_time
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, UUID.randomUUID().toString())
                statement.setString(2, normalizedNamespace)
                statement.setString(3, normalizedActive)
                statement.setString(4, normalizedKey)
                statement.setString(5, request.value)
                statement.setString(6, request.description)
                statement.setLong(7, createdAt)
                statement.setLong(8, now)
                statement.executeUpdate()
            }
        }

        return readValue(
            namespace = normalizedNamespace,
            key = normalizedKey,
            active = normalizedActive,
        )
    }

    override fun deleteValue(
        namespace: String,
        key: String,
        active: String,
    ): Boolean {
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        val normalizedActive = normalizeConfigCenterActive(active)
        val normalizedKey = key.trim()
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedKey.isNotBlank()) { "key cannot be blank" }

        return withConnection { connection ->
            connection.prepareStatement(
                """
                DELETE FROM config_center_value
                WHERE namespace = ?
                  AND active_profile = ?
                  AND config_key = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, normalizedNamespace)
                statement.setString(2, normalizedActive)
                statement.setString(3, normalizedKey)
                statement.executeUpdate() > 0
            }
        }
    }

    private fun lookupExistingCreateTime(
        connection: Connection,
        namespace: String,
        active: String,
        key: String,
    ): Long? {
        connection.prepareStatement(
            """
            SELECT create_time
            FROM config_center_value
            WHERE namespace = ?
              AND active_profile = ?
              AND config_key = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, namespace)
            statement.setString(2, active)
            statement.setString(3, key)
            statement.executeQuery().use { resultSet ->
                return if (resultSet.next()) resultSet.getLong(1) else null
            }
        }
    }

    private fun ensureSchema(
        connection: Connection,
    ) {
        connection.createStatement().use { statement ->
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS config_center_value (
                    id TEXT PRIMARY KEY,
                    namespace TEXT NOT NULL,
                    active_profile TEXT NOT NULL,
                    config_key TEXT NOT NULL,
                    config_value TEXT NOT NULL,
                    description TEXT,
                    create_time BIGINT NOT NULL,
                    update_time BIGINT NOT NULL
                )
                """.trimIndent(),
            )
            statement.executeUpdate(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS idx_config_center_value_lookup
                    ON config_center_value(namespace, active_profile, config_key)
                """.trimIndent(),
            )
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS config_center_meta (
                    meta_key TEXT PRIMARY KEY,
                    meta_value TEXT NOT NULL
                )
                """.trimIndent(),
            )
            statement.executeUpdate(
                """
                INSERT INTO config_center_meta(meta_key, meta_value)
                VALUES ('table_note', '${settings.tableNote.replace("'", "''")}')
                ON CONFLICT(meta_key) DO UPDATE SET meta_value = excluded.meta_value
                """.trimIndent(),
            )
            if (dialect == ConfigCenterJdbcDialect.POSTGRES) {
                statement.executeUpdate(
                    """
                    COMMENT ON TABLE config_center_value IS '${settings.tableNote.replace("'", "''")}'
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    """
                    COMMENT ON TABLE config_center_meta IS '${settings.tableNote.replace("'", "''")}'
                    """.trimIndent(),
                )
            }
        }
        ensureCompatibleColumns(connection)
    }

    private fun <T> withConnection(
        block: (Connection) -> T,
    ): T {
        return openConnection().use(block)
    }

    private fun openConnection(): Connection {
        if (settings.username.isNullOrBlank() && settings.password.isNullOrBlank()) {
            return DriverManager.getConnection(settings.url)
        }
        val properties = Properties()
        settings.username?.let { properties["user"] = it }
        settings.password?.let { properties["password"] = it }
        return DriverManager.getConnection(settings.url, properties)
    }

    private fun ensureCompatibleColumns(
        connection: Connection,
    ) {
        val columns = existingColumns(connection)
        connection.createStatement().use { statement ->
            if ("description" !in columns) {
                statement.executeUpdate("ALTER TABLE config_center_value ADD COLUMN description TEXT")
            }
            if ("create_time" !in columns) {
                statement.executeUpdate(
                    "ALTER TABLE config_center_value ADD COLUMN create_time BIGINT NOT NULL DEFAULT 0",
                )
            }
            if ("update_time" !in columns) {
                statement.executeUpdate(
                    "ALTER TABLE config_center_value ADD COLUMN update_time BIGINT NOT NULL DEFAULT 0",
                )
            }
        }
    }

    private fun existingColumns(
        connection: Connection,
    ): Set<String> {
        val metadata = connection.metaData
        metadata.getColumns(null, null, "config_center_value", null).use { resultSet ->
            val columns = linkedSetOf<String>()
            while (resultSet.next()) {
                resultSet.getString("COLUMN_NAME")
                    ?.trim()
                    ?.lowercase()
                    ?.takeIf(String::isNotBlank)
                    ?.let(columns::add)
            }
            if (columns.isNotEmpty()) {
                return columns
            }
        }
        metadata.getColumns(null, null, "CONFIG_CENTER_VALUE", null).use { resultSet ->
            val columns = linkedSetOf<String>()
            while (resultSet.next()) {
                resultSet.getString("COLUMN_NAME")
                    ?.trim()
                    ?.lowercase()
                    ?.takeIf(String::isNotBlank)
                    ?.let(columns::add)
            }
            return columns
        }
    }

    private fun selectColumnsSql(
        connection: Connection,
    ): String {
        val columns = existingColumns(connection)
        val descriptionColumn = if ("description" in columns) "description" else "NULL AS description"
        val createTimeColumn = if ("create_time" in columns) "create_time" else "NULL AS create_time"
        val updateTimeColumn = if ("update_time" in columns) "update_time" else "NULL AS update_time"
        return listOf(
            "id",
            "namespace",
            "active_profile",
            "config_key",
            "config_value",
            descriptionColumn,
            createTimeColumn,
            updateTimeColumn,
        ).joinToString(", ")
    }
}

private fun readValueRows(
    resultSet: ResultSet,
): List<ConfigCenterValueDto> {
    val values = mutableListOf<ConfigCenterValueDto>()
    while (resultSet.next()) {
        values += resultSet.toValueDto()
    }
    return values
}

private fun ResultSet.toValueDto(): ConfigCenterValueDto {
    return ConfigCenterValueDto(
        id = getString("id").orEmpty(),
        namespace = getString("namespace").orEmpty(),
        active = getString("active_profile").orEmpty(),
        key = getString("config_key").orEmpty(),
        value = getString("config_value"),
        description = getString("description"),
        createTimeMillis = getLong("create_time").takeIf { !wasNull() },
        updateTimeMillis = getLong("update_time").takeIf { !wasNull() },
    )
}
