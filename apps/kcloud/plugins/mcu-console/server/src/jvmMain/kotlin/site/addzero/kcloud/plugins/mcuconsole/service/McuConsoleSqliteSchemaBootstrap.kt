package site.addzero.kcloud.plugins.mcuconsole.service

import io.ktor.server.application.Application
import io.ktor.server.application.log
import java.sql.Connection
import javax.sql.DataSource
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

private const val MCU_CONSOLE_SQLITE_SCHEMA_RESOURCE = "/db/sqlite/mcu-console-local-schema.sql"

@Named("mcuConsoleSqliteSchemaBootstrap")
@Single
class McuConsoleSqliteSchemaBootstrap(
    private val dataSource: DataSource,
) : AppStarter {
    override val order: Int = 130
    override val enable: Boolean = true

    override fun onInstall(application: Application) {
        if (!ensureMcuConsoleSqliteSchema(dataSource)) {
            return
        }
        application.log.info("Mcu-console SQLite schema is ready.")
    }
}

internal fun ensureMcuConsoleSqliteSchema(
    dataSource: DataSource,
): Boolean {
    dataSource.connection.use { connection ->
        if (!connection.isSqliteConnection()) {
            return false
        }
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
        }
        migrateLegacyMcuDeviceProfileTimestampSchema(connection)
        executeSqliteSchemaScript(
            owner = McuConsoleSqliteSchemaBootstrap::class.java,
            connection = connection,
            resourcePath = MCU_CONSOLE_SQLITE_SCHEMA_RESOURCE,
        )
    }
    return true
}

private fun migrateLegacyMcuDeviceProfileTimestampSchema(
    connection: Connection,
) {
    if (!connection.tableHasColumn("mcu_device_profile", "create_time")) {
        return
    }
    val previousAutoCommit = connection.autoCommit
    connection.autoCommit = false
    try {
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = OFF")
            statement.execute("ALTER TABLE mcu_device_profile RENAME TO mcu_device_profile_legacy_timestamp")
        }
        executeSqliteSchemaScript(
            owner = McuConsoleSqliteSchemaBootstrap::class.java,
            connection = connection,
            resourcePath = MCU_CONSOLE_SQLITE_SCHEMA_RESOURCE,
        )
        connection.createStatement().use { statement ->
            statement.execute(
                """
                INSERT INTO mcu_device_profile (
                    id,
                    device_key,
                    manufacturer,
                    remark,
                    created_at,
                    updated_at
                )
                SELECT
                    id,
                    device_key,
                    manufacturer,
                    remark,
                    ${sqliteEpochMillisExpression("create_time")},
                    COALESCE(${sqliteEpochMillisExpression("update_time")}, ${sqliteEpochMillisExpression("create_time")})
                FROM mcu_device_profile_legacy_timestamp
                """.trimIndent(),
            )
            statement.execute("DROP TABLE IF EXISTS mcu_device_profile_legacy_timestamp")
            statement.execute("PRAGMA foreign_keys = ON")
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
) {
    val script =
        checkNotNull(owner.getResource(resourcePath)) {
            "Missing SQLite schema resource: $resourcePath"
        }.readText()
    connection.createStatement().use { statement ->
        script.splitToSequence(';')
            .map(String::trim)
            .filter(String::isNotBlank)
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
