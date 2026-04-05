package site.addzero.configcenter

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import site.addzero.configcenter.db.ConfigCenterDatabase
import site.addzero.configcenter.db.Config_center_record
import site.addzero.configcenter.db.ListNamespaces
import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.logging.Logger
import javax.sql.DataSource

class JdbcConfigCenterValueService(
    private val settings: ConfigCenterJdbcSettings,
) : ConfigCenterValueService {
    private val dialect = settings.detectDialect()
    private val dataSource = ConfigCenterJdbcDataSource(settings)
    private val driver = dataSource.asJdbcDriver()
    private val database = ConfigCenterDatabase(driver)
    private val queries = database.configCenterDatabaseQueries

    init {
        if (settings.autoDdl) {
            ensureSchema()
        }
    }

    override fun listValues(
        namespace: String?,
        active: String?,
        keyword: String?,
        limit: Int,
    ): List<ConfigCenterValueDto> {
        val normalizedNamespace = namespace?.trim()?.takeIf(String::isNotBlank)?.let(::normalizeConfigCenterNamespace)
        val normalizedActive = active?.trim()?.takeIf(String::isNotBlank)?.let(::normalizeConfigCenterActive)
        val keywordLike = keyword?.trim()?.takeIf(String::isNotBlank)?.let { value -> "%$value%" }
        return queries.listValues(
            normalizedNamespace,
            normalizedActive,
            keywordLike,
            limit.coerceAtLeast(1).toLong(),
        ).executeAsList().map { row ->
            row.toDto()
        }
    }

    override fun readValue(
        namespace: String,
        path: String,
        active: String,
    ): ConfigCenterValueDto {
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        val normalizedPath = normalizeConfigCenterPath(path)
        val normalizedActive = normalizeConfigCenterActive(active)
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedPath.isNotBlank()) { "path cannot be blank" }

        return queries.readValue(
            normalizedNamespace,
            normalizedActive,
            normalizedPath,
        ).executeAsOneOrNull()?.toDto()
            ?: ConfigCenterValueDto(
                namespace = normalizedNamespace,
                active = normalizedActive,
                path = normalizedPath,
            )
    }

    override fun writeValue(
        request: ConfigCenterValueWriteRequest,
    ): ConfigCenterValueDto {
        val normalizedNamespace = normalizeConfigCenterNamespace(request.namespace)
        val normalizedActive = normalizeConfigCenterActive(request.active)
        val normalizedPath = normalizeConfigCenterPath(request.path)
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedPath.isNotBlank()) { "path cannot be blank" }

        val now = System.currentTimeMillis()
        val existing = queries.readValue(
            normalizedNamespace,
            normalizedActive,
            normalizedPath,
        ).executeAsOneOrNull()
        database.transaction {
            queries.upsertValue(
                namespace = normalizedNamespace,
                active_env = normalizedActive,
                config_path = normalizedPath,
                config_value = request.value,
                create_time = existing?.create_time ?: now,
                update_time = now,
            )
        }
        return readValue(
            namespace = normalizedNamespace,
            path = normalizedPath,
            active = normalizedActive,
        )
    }

    override fun deleteValue(
        namespace: String,
        path: String,
        active: String,
    ): Boolean {
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        val normalizedPath = normalizeConfigCenterPath(path)
        val normalizedActive = normalizeConfigCenterActive(active)
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedPath.isNotBlank()) { "path cannot be blank" }

        val existing = queries.readValue(
            normalizedNamespace,
            normalizedActive,
            normalizedPath,
        ).executeAsOneOrNull() ?: return false
        database.transaction {
            queries.deleteValue(
                namespace = existing.namespace,
                active_env = existing.active_env,
                config_path = existing.config_path,
            )
        }
        return true
    }

    fun listNamespaces(): List<ConfigCenterNamespaceDto> {
        return queries.listNamespaces()
            .executeAsList()
            .map(ListNamespaces::toDto)
    }

    fun deleteNamespace(
        namespace: String,
    ): Boolean {
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        if (queries.countNamespaceEntries(normalizedNamespace).executeAsOne() == 0L) {
            return false
        }
        database.transaction {
            queries.deleteNamespace(normalizedNamespace)
        }
        return true
    }

    private fun ensureSchema() {
        dataSource.connection.use { connection ->
            if (!tableExists(connection, CONFIG_CENTER_RECORD_TABLE)) {
                ConfigCenterDatabase.Schema.create(driver)
            }
            migrateLegacyValueTableIfNeeded(connection)
        }
    }

    private fun migrateLegacyValueTableIfNeeded(
        connection: Connection,
    ) {
        if (!tableExists(connection, LEGACY_CONFIG_CENTER_VALUE_TABLE)) {
            return
        }
        if (queries.countRecords().executeAsOne() > 0L) {
            return
        }
        val columns = existingColumns(connection, LEGACY_CONFIG_CENTER_VALUE_TABLE)
        val createTimeSql = if ("create_time" in columns) "create_time" else "0 AS create_time"
        val updateTimeSql = if ("update_time" in columns) "update_time" else "0 AS update_time"
        connection.prepareStatement(
            """
            SELECT namespace, active_profile, config_key, config_value, $createTimeSql, $updateTimeSql
            FROM $LEGACY_CONFIG_CENTER_VALUE_TABLE
            ORDER BY namespace ASC, active_profile ASC, config_key ASC
            """.trimIndent(),
        ).use { statement ->
            statement.executeQuery().use { resultSet ->
                database.transaction {
                    while (resultSet.next()) {
                        val normalizedNamespace = normalizeConfigCenterNamespace(resultSet.getString(1).orEmpty())
                        val normalizedActive = normalizeConfigCenterActive(resultSet.getString(2).orEmpty())
                        val normalizedPath = normalizeConfigCenterPath(resultSet.getString(3).orEmpty())
                        if (normalizedNamespace.isBlank() || normalizedPath.isBlank()) {
                            continue
                        }
                        val value = resultSet.getString(4) ?: ""
                        val now = System.currentTimeMillis()
                        val createTime = resultSet.readOptionalLong(5)?.takeIf { it > 0L } ?: now
                        val updateTime = resultSet.readOptionalLong(6)?.takeIf { it > 0L } ?: createTime
                        queries.upsertValue(
                            namespace = normalizedNamespace,
                            active_env = normalizedActive,
                            config_path = normalizedPath,
                            config_value = value,
                            create_time = createTime,
                            update_time = updateTime,
                        )
                    }
                }
            }
        }
    }

    private fun tableExists(
        connection: Connection,
        tableName: String,
    ): Boolean {
        val metadata = connection.metaData
        fun lookup(candidate: String): Boolean {
            metadata.getTables(null, null, candidate, null).use { resultSet ->
                return resultSet.next()
            }
        }
        return lookup(tableName) || lookup(tableName.uppercase())
    }

    private fun existingColumns(
        connection: Connection,
        tableName: String,
    ): Set<String> {
        val metadata = connection.metaData
        fun lookup(candidate: String): Set<String> {
            metadata.getColumns(null, null, candidate, null).use { resultSet ->
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
        return lookup(tableName).ifEmpty { lookup(tableName.uppercase()) }
    }

    private fun Config_center_record.toDto(): ConfigCenterValueDto {
        return ConfigCenterValueDto(
            namespace = namespace,
            active = active_env,
            path = config_path,
            value = config_value,
            createTimeMillis = create_time,
            updateTimeMillis = update_time,
        )
    }
}

private fun ListNamespaces.toDto(): ConfigCenterNamespaceDto {
    return ConfigCenterNamespaceDto(
        namespace = namespace,
        entryCount = entry_count.toInt(),
    )
}

private class ConfigCenterJdbcDataSource(
    private val settings: ConfigCenterJdbcSettings,
) : DataSource {
    init {
        settings.driver?.takeIf(String::isNotBlank)?.let { driverName ->
            Class.forName(driverName)
        }
    }

    override fun getConnection(): Connection {
        return if (settings.username.isNullOrBlank()) {
            DriverManager.getConnection(settings.url)
        } else {
            DriverManager.getConnection(settings.url, settings.username, settings.password)
        }
    }

    override fun getConnection(
        username: String?,
        password: String?,
    ): Connection {
        return DriverManager.getConnection(settings.url, username, password)
    }

    override fun getLogWriter(): PrintWriter? = null

    override fun setLogWriter(
        out: PrintWriter?,
    ) = Unit

    override fun setLoginTimeout(
        seconds: Int,
    ) {
        DriverManager.setLoginTimeout(seconds)
    }

    override fun getLoginTimeout(): Int {
        return DriverManager.getLoginTimeout()
    }

    override fun getParentLogger(): Logger {
        return Logger.getLogger("site.addzero.configcenter")
    }

    override fun <T : Any> unwrap(
        iface: Class<T>?,
    ): T {
        throw UnsupportedOperationException("unwrap is not supported")
    }

    override fun isWrapperFor(
        iface: Class<*>?,
    ): Boolean = false
}

private fun ResultSet.readOptionalLong(
    columnIndex: Int,
): Long? {
    val value = getLong(columnIndex)
    return value.takeIf { !wasNull() }
}

private const val CONFIG_CENTER_RECORD_TABLE = "config_center_record"
private const val LEGACY_CONFIG_CENTER_VALUE_TABLE = "config_center_value"
