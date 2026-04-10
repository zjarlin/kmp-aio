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
        migrateLegacyDeviceModuleHierarchy(connection)
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

private fun migrateLegacyDeviceModuleHierarchy(
    connection: Connection,
) {
    if (!connection.tableExists("host_config_device") || !connection.tableExists("host_config_module_instance")) {
        return
    }
    val legacyDeviceModuleColumnExists = connection.columnExists("host_config_device", "module_id")
    val deviceProtocolColumnExists = connection.columnExists("host_config_device", "protocol_id")
    val moduleDeviceColumnExists = connection.columnExists("host_config_module_instance", "device_id")
    if (!legacyDeviceModuleColumnExists && deviceProtocolColumnExists && moduleDeviceColumnExists) {
        return
    }
    check(legacyDeviceModuleColumnExists) {
        "host_config_device 缺少 module_id，无法从旧版设备/模块层级迁移到当前 SQLite 本地结构"
    }
    val previousAutoCommit = connection.autoCommit
    connection.autoCommit = false
    try {
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = OFF")
            statement.execute("ALTER TABLE host_config_module_instance RENAME TO host_config_module_instance_legacy_hierarchy")
            statement.execute("ALTER TABLE host_config_device RENAME TO host_config_device_legacy_hierarchy")
        }
        executeSqliteSchemaScript(
            owner = HostConfigSqliteSchemaBootstrap::class.java,
            connection = connection,
            resourcePath = HOST_CONFIG_SQLITE_SCHEMA_RESOURCE,
            filter = { sql ->
                sql.startsWith("CREATE TABLE", ignoreCase = true) ||
                    sql.startsWith("CREATE INDEX", ignoreCase = true) ||
                    sql.startsWith("INSERT OR IGNORE", ignoreCase = true)
            },
        )
        connection.createStatement().use { statement ->
            statement.execute(
                """
                INSERT INTO host_config_device (
                    id,
                    protocol_id,
                    device_type_id,
                    name,
                    station_no,
                    request_interval_ms,
                    write_interval_ms,
                    byte_order2,
                    byte_order4,
                    float_order,
                    batch_analog_start,
                    batch_analog_length,
                    batch_digital_start,
                    batch_digital_length,
                    disabled,
                    sort_index,
                    created_at,
                    updated_at
                )
                WITH normalized_device AS (
                    SELECT
                        d.id,
                        mi.protocol_id,
                        d.device_type_id,
                        CASE
                            WHEN ROW_NUMBER() OVER (PARTITION BY mi.protocol_id, d.name ORDER BY d.id) = 1 THEN d.name
                            ELSE d.name || '-' || d.id
                        END AS normalized_name,
                        d.station_no,
                        d.request_interval_ms,
                        d.write_interval_ms,
                        d.byte_order2,
                        d.byte_order4,
                        d.float_order,
                        d.batch_analog_start,
                        d.batch_analog_length,
                        d.batch_digital_start,
                        d.batch_digital_length,
                        d.disabled,
                        d.sort_index,
                        d.created_at,
                        d.updated_at
                    FROM host_config_device_legacy_hierarchy d
                    INNER JOIN host_config_module_instance_legacy_hierarchy mi ON mi.id = d.module_id
                )
                SELECT
                    id,
                    protocol_id,
                    device_type_id,
                    normalized_name,
                    station_no,
                    request_interval_ms,
                    write_interval_ms,
                    byte_order2,
                    byte_order4,
                    float_order,
                    batch_analog_start,
                    batch_analog_length,
                    batch_digital_start,
                    batch_digital_length,
                    disabled,
                    sort_index,
                    created_at,
                    updated_at
                FROM normalized_device
                """.trimIndent(),
            )
            statement.execute(
                """
                CREATE TEMP TABLE tmp_host_config_legacy_modules_without_devices AS
                SELECT
                    mi.id AS legacy_module_id,
                    mi.protocol_id,
                    mi.module_template_id,
                    mi.name,
                    mi.port_name,
                    mi.baud_rate,
                    mi.data_bits,
                    mi.stop_bits,
                    mi.parity,
                    mi.response_timeout_ms,
                    mi.sort_index,
                    mi.created_at,
                    mi.updated_at,
                    mi.name || '-auto-' || mi.id AS placeholder_device_name
                FROM host_config_module_instance_legacy_hierarchy mi
                LEFT JOIN host_config_device_legacy_hierarchy d ON d.module_id = mi.id
                WHERE d.id IS NULL
                """.trimIndent(),
            )
            statement.execute(
                """
                INSERT INTO host_config_device (
                    protocol_id,
                    device_type_id,
                    name,
                    station_no,
                    request_interval_ms,
                    write_interval_ms,
                    byte_order2,
                    byte_order4,
                    float_order,
                    batch_analog_start,
                    batch_analog_length,
                    batch_digital_start,
                    batch_digital_length,
                    disabled,
                    sort_index,
                    created_at,
                    updated_at
                )
                SELECT
                    protocol_id,
                    COALESCE((SELECT MIN(id) FROM host_config_device_type), 1),
                    placeholder_device_name,
                    1,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    0,
                    sort_index,
                    created_at,
                    updated_at
                FROM tmp_host_config_legacy_modules_without_devices
                """.trimIndent(),
            )
            statement.execute(
                """
                INSERT INTO host_config_module_instance (
                    protocol_id,
                    device_id,
                    module_template_id,
                    name,
                    port_name,
                    baud_rate,
                    data_bits,
                    stop_bits,
                    parity,
                    response_timeout_ms,
                    sort_index,
                    created_at,
                    updated_at
                )
                SELECT
                    mi.protocol_id,
                    d.id,
                    mi.module_template_id,
                    mi.name,
                    mi.port_name,
                    mi.baud_rate,
                    mi.data_bits,
                    mi.stop_bits,
                    mi.parity,
                    mi.response_timeout_ms,
                    mi.sort_index,
                    mi.created_at,
                    mi.updated_at
                FROM host_config_device_legacy_hierarchy d
                INNER JOIN host_config_module_instance_legacy_hierarchy mi ON mi.id = d.module_id
                """.trimIndent(),
            )
            statement.execute(
                """
                INSERT INTO host_config_module_instance (
                    protocol_id,
                    device_id,
                    module_template_id,
                    name,
                    port_name,
                    baud_rate,
                    data_bits,
                    stop_bits,
                    parity,
                    response_timeout_ms,
                    sort_index,
                    created_at,
                    updated_at
                )
                SELECT
                    legacy.protocol_id,
                    device.id,
                    legacy.module_template_id,
                    legacy.name,
                    legacy.port_name,
                    legacy.baud_rate,
                    legacy.data_bits,
                    legacy.stop_bits,
                    legacy.parity,
                    legacy.response_timeout_ms,
                    legacy.sort_index,
                    legacy.created_at,
                    legacy.updated_at
                FROM tmp_host_config_legacy_modules_without_devices legacy
                INNER JOIN host_config_device device
                    ON device.protocol_id = legacy.protocol_id
                   AND device.name = legacy.placeholder_device_name
                """.trimIndent(),
            )
            statement.execute("DROP TABLE IF EXISTS tmp_host_config_legacy_modules_without_devices")
            statement.execute("DROP TABLE IF EXISTS host_config_device_legacy_hierarchy")
            statement.execute("DROP TABLE IF EXISTS host_config_module_instance_legacy_hierarchy")
            statement.execute("PRAGMA foreign_keys = ON")
        }
        connection.commit()
    } catch (exception: Throwable) {
        connection.rollback()
        throw exception
    } finally {
        connection.autoCommit = previousAutoCommit
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
        }
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
