package site.addzero.kcloud.plugins.hostconfig.service

import io.ktor.server.application.Application
import io.ktor.server.application.log
import java.sql.Connection
import kotlinx.serialization.encodeToString
import javax.sql.DataSource
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.core.network.json.json
import site.addzero.starter.AppStarter

private const val HOST_CONFIG_SQLITE_SCHEMA_RESOURCE = "/db/sqlite/host-config-local-schema.sql"

@Named("hostConfigSqliteSchemaBootstrap")
@Single
class HostConfigSqliteSchemaBootstrap(
    private val dataSource: DataSource,
) : AppStarter {
    override val order: Int = 110
    override val enable: Boolean = true

    override fun onInstall(application: Application) {
        if (!ensureHostConfigSqliteSchema(dataSource)) {
            return
        }
        application.log.info("Host-config SQLite schema is ready.")
    }
}

internal fun ensureHostConfigSqliteSchema(
    dataSource: DataSource,
): Boolean {
    dataSource.connection.use { connection ->
        if (!connection.isSqliteConnection()) {
            return false
        }
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
        }
        ensureProtocolTemplateMetadataColumn(connection)
        executeSqliteSchemaScript(
            owner = HostConfigSqliteSchemaBootstrap::class.java,
            connection = connection,
            resourcePath = HOST_CONFIG_SQLITE_SCHEMA_RESOURCE,
        )
        seedProtocolTemplateMetadata(connection)
    }
    return true
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

private fun ensureProtocolTemplateMetadataColumn(
    connection: Connection,
) {
    if (!connection.tableExists("host_config_protocol_template")) {
        return
    }
    if (connection.columnExists("host_config_protocol_template", "metadata_json")) {
        return
    }
    connection.createStatement().use { statement ->
        statement.execute("ALTER TABLE host_config_protocol_template ADD COLUMN metadata_json TEXT")
    }
}

private fun seedProtocolTemplateMetadata(
    connection: Connection,
) {
    if (!connection.tableExists("host_config_protocol_template")) {
        return
    }
    val updates = listOf(
        "MODBUS_RTU_CLIENT",
        "MODBUS_TCP_CLIENT",
        "MQTT_CLIENT",
    ).mapNotNull { code ->
        ProtocolTemplateMetadataRegistry.defaultFor(code)?.let { metadata ->
            code to metadata
        }
    }
    val sql = """
        UPDATE host_config_protocol_template
        SET metadata_json = ?
        WHERE code = ? AND (metadata_json IS NULL OR TRIM(metadata_json) = '')
    """.trimIndent()
    connection.prepareStatement(sql).use { statement ->
        updates.forEach { (code, metadata) ->
            statement.setString(1, json.encodeToString(metadata))
            statement.setString(2, code)
            statement.addBatch()
        }
        statement.executeBatch()
    }
}

private fun Connection.tableExists(
    tableName: String,
): Boolean {
    prepareStatement(
        "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = ?",
    ).use { statement ->
        statement.setString(1, tableName)
        statement.executeQuery().use { rs ->
            return rs.next() && rs.getInt(1) > 0
        }
    }
}

private fun Connection.columnExists(
    tableName: String,
    columnName: String,
): Boolean {
    prepareStatement("PRAGMA table_info($tableName)").use { statement ->
        statement.executeQuery().use { rs ->
            while (rs.next()) {
                if (rs.getString("name") == columnName) {
                    return true
                }
            }
        }
    }
    return false
}

private fun Connection.isSqliteConnection(): Boolean = metaData.url.startsWith("jdbc:sqlite:")
