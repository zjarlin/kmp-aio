package site.addzero.kcloud.plugins.hostconfig.service

import io.ktor.server.application.Application
import io.ktor.server.application.log
import java.sql.Connection
import javax.sql.DataSource
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType
import site.addzero.starter.AppStarter

/**
 * 把历史上挂在模块实例上的串口参数归并到协议实例。
 *
 * 建表和字段变更由 Flyway 负责，这里只保留 Jimmer 不擅长表达的历史数据修正。
 */
@Named("hostConfigLegacyTransportMigration")
@Single
class HostConfigLegacyTransportMigration(
    private val dataSource: DataSource,
) : AppStarter {
    override val order: Int = 210
    override val enable: Boolean = true

    /**
     * 处理oninstall。
     *
     * @param application 应用。
     */
    override fun onInstall(application: Application) {
        if (isSqliteDatasource()) {
            application.log.info("Skip host-config legacy transport migration for SQLite local datasource.")
            return
        }
        migrateLegacyModuleTransportConfig(dataSource)
    }

    private fun isSqliteDatasource(): Boolean {
        dataSource.connection.use { connection ->
            return connection.metaData.url.startsWith("jdbc:sqlite:")
        }
    }

    /**
     * 处理migrate旧版模块传输配置。
     *
     * @param dataSource 数据来源。
     */
    private fun migrateLegacyModuleTransportConfig(
        dataSource: DataSource,
    ) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val protocols = loadProtocols(connection)
                val legacyModulesByProtocol = loadLegacyModules(connection).groupBy(LegacyModuleTransportRow::protocolId)
                val conflicts = mutableListOf<String>()
                val protocolUpdates = mutableListOf<ProtocolTransportUpdate>()
                val cleanupProtocolIds = mutableSetOf<Long>()

                protocols.forEach { protocol ->
                    val legacyModules = legacyModulesByProtocol[protocol.id].orEmpty()
                    val legacyModulesWithValues = legacyModules.filter { module -> module.hasLegacyValues() }
                    if (legacyModulesWithValues.isEmpty()) {
                        return@forEach
                    }

                    if (protocol.templateCode != "MODBUS_RTU_CLIENT") {
                        conflicts += buildUnsupportedLegacyMessage(protocol, legacyModulesWithValues)
                        return@forEach
                    }

                    val normalizedConfigs = legacyModulesWithValues
                        .mapNotNull { row -> row.toTransportConfig(protocol.templateCode) }
                    if (normalizedConfigs.isEmpty()) {
                        return@forEach
                    }

                    val distinctConfigs = normalizedConfigs.distinct()
                    if (distinctConfigs.size > 1) {
                        conflicts += buildConflictMessage(protocol, legacyModulesWithValues)
                        return@forEach
                    }

                    val migratedConfig = distinctConfigs.single()
                    val currentConfig = protocol.transportConfig
                    if (currentConfig != null && currentConfig != migratedConfig) {
                        conflicts += buildProtocolMismatchMessage(
                            protocol = protocol,
                            currentConfig = currentConfig,
                            migratedConfig = migratedConfig,
                            legacyModules = legacyModulesWithValues,
                        )
                        return@forEach
                    }

                    protocolUpdates += ProtocolTransportUpdate(
                        protocolId = protocol.id,
                        transportConfig = migratedConfig,
                    )
                    cleanupProtocolIds += protocol.id
                }

                if (conflicts.isNotEmpty()) {
                    error(
                        buildString {
                            appendLine("Host config protocol transport migration is blocked by conflicts:")
                            conflicts.forEach { conflict ->
                                appendLine(conflict)
                            }
                        }.trimEnd(),
                    )
                }

                protocolUpdates.forEach { update ->
                    updateProtocolTransport(connection, update)
                }
                cleanupProtocolIds.forEach { protocolId ->
                    clearLegacyModuleTransport(connection, protocolId)
                }
                connection.commit()
            } catch (exception: Throwable) {
                connection.rollback()
                throw exception
            } finally {
                connection.autoCommit = true
            }
        }
    }

    /**
     * 加载协议。
     *
     * @param connection 数据库连接。
     */
    private fun loadProtocols(
        connection: Connection,
    ): List<ProtocolMigrationRow> {
        val sql =
            """
            SELECT
                pi.id,
                pi.name,
                pt.code AS protocol_template_code,
                pi.transport_type,
                pi.host,
                pi.tcp_port,
                pi.port_name,
                pi.baud_rate,
                pi.data_bits,
                pi.stop_bits,
                pi.parity,
                pi.response_timeout_ms
            FROM host_config_protocol_instance pi
            JOIN host_config_protocol_template pt ON pt.id = pi.protocol_template_id
            ORDER BY pi.id ASC
            """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.executeQuery().use { rs ->
                val rows = mutableListOf<ProtocolMigrationRow>()
                while (rs.next()) {
                    rows += ProtocolMigrationRow(
                        id = rs.getLong("id"),
                        name = rs.getString("name"),
                        templateCode = rs.getString("protocol_template_code"),
                        transportConfig = rs.toProtocolTransportConfig(),
                    )
                }
                return rows
            }
        }
    }

    /**
     * 加载旧版模块。
     *
     * @param connection 数据库连接。
     */
    private fun loadLegacyModules(
        connection: Connection,
    ): List<LegacyModuleTransportRow> {
        val sql =
            """
            SELECT
                protocol_id,
                id,
                name,
                port_name,
                baud_rate,
                data_bits,
                stop_bits,
                parity,
                response_timeout_ms
            FROM host_config_module_instance
            ORDER BY protocol_id ASC, id ASC
            """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.executeQuery().use { rs ->
                val rows = mutableListOf<LegacyModuleTransportRow>()
                while (rs.next()) {
                    rows += LegacyModuleTransportRow(
                        protocolId = rs.getLong("protocol_id"),
                        moduleId = rs.getLong("id"),
                        moduleName = rs.getString("name"),
                        portName = rs.getNullableString("port_name"),
                        baudRate = rs.getNullableInt("baud_rate"),
                        dataBits = rs.getNullableInt("data_bits"),
                        stopBits = rs.getNullableInt("stop_bits"),
                        parity = rs.getNullableEnum("parity"),
                        responseTimeoutMs = rs.getNullableInt("response_timeout_ms"),
                    )
                }
                return rows
            }
        }
    }

    /**
     * 更新协议传输。
     *
     * @param connection 数据库连接。
     * @param update 更新。
     */
    private fun updateProtocolTransport(
        connection: Connection,
        update: ProtocolTransportUpdate,
    ) {
        val sql =
            """
            UPDATE host_config_protocol_instance
            SET transport_type = ?,
                host = ?,
                tcp_port = ?,
                port_name = ?,
                baud_rate = ?,
                data_bits = ?,
                stop_bits = ?,
                parity = ?,
                response_timeout_ms = ?
            WHERE id = ?
            """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, update.transportConfig.transportType.name)
            statement.setObject(2, update.transportConfig.host)
            statement.setObject(3, update.transportConfig.tcpPort)
            statement.setObject(4, update.transportConfig.portName)
            statement.setObject(5, update.transportConfig.baudRate)
            statement.setObject(6, update.transportConfig.dataBits)
            statement.setObject(7, update.transportConfig.stopBits)
            statement.setObject(8, update.transportConfig.parity?.name)
            statement.setObject(9, update.transportConfig.responseTimeoutMs)
            statement.setLong(10, update.protocolId)
            statement.executeUpdate()
        }
    }

    /**
     * 处理clear旧版模块传输。
     *
     * @param connection 数据库连接。
     * @param protocolId 协议 ID。
     */
    private fun clearLegacyModuleTransport(
        connection: Connection,
        protocolId: Long,
    ) {
        val sql =
            """
            UPDATE host_config_module_instance
            SET port_name = NULL,
                baud_rate = NULL,
                data_bits = NULL,
                stop_bits = NULL,
                parity = NULL,
                response_timeout_ms = NULL
            WHERE protocol_id = ?
            """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setLong(1, protocolId)
            statement.executeUpdate()
        }
    }

    /**
     * 构建conflict消息。
     *
     * @param protocol 协议。
     * @param legacyModules 旧版模块。
     */
    private fun buildConflictMessage(
        protocol: ProtocolMigrationRow,
        legacyModules: List<LegacyModuleTransportRow>,
    ): String {
        return buildString {
            append("协议[").append(protocol.id).append(" - ").append(protocol.name).append("] 迁移冲突：")
            legacyModules.forEach { module ->
                val config = module.toTransportConfig(protocol.templateCode) ?: return@forEach
                appendLine()
                append("  模块[").append(module.moduleId).append(" - ").append(module.moduleName).append("] -> ")
                append(config.describe())
            }
        }
    }

    /**
     * 构建unsupported旧版消息。
     *
     * @param protocol 协议。
     * @param legacyModules 旧版模块。
     */
    private fun buildUnsupportedLegacyMessage(
        protocol: ProtocolMigrationRow,
        legacyModules: List<LegacyModuleTransportRow>,
    ): String {
        return buildString {
            append("协议[").append(protocol.id).append(" - ").append(protocol.name).append("] 模板[")
                .append(protocol.templateCode)
                .append("] 暂不支持从模块字段迁移通信参数：")
            legacyModules.forEach { module ->
                appendLine()
                append("  模块[").append(module.moduleId).append(" - ").append(module.moduleName).append("] -> ")
                append(module.describeLegacyValues())
            }
        }
    }

    /**
     * 构建协议mismatch消息。
     *
     * @param protocol 协议。
     * @param currentConfig 当前配置。
     * @param migratedConfig migrated配置。
     * @param legacyModules 旧版模块。
     */
    private fun buildProtocolMismatchMessage(
        protocol: ProtocolMigrationRow,
        currentConfig: ProtocolTransportConfig,
        migratedConfig: ProtocolTransportConfig,
        legacyModules: List<LegacyModuleTransportRow>,
    ): String {
        return buildString {
            append("协议[").append(protocol.id).append(" - ").append(protocol.name).append("] 当前协议配置与模块历史配置不一致：")
            appendLine()
            append("  协议当前 -> ").append(currentConfig.describe())
            appendLine()
            append("  模块历史 -> ").append(migratedConfig.describe())
            legacyModules.forEach { module ->
                val config = module.toTransportConfig(protocol.templateCode) ?: return@forEach
                appendLine()
                append("  模块[").append(module.moduleId).append(" - ").append(module.moduleName).append("] -> ")
                append(config.describe())
            }
        }
    }

    /**
     * 处理协议传输配置。
     */
    private fun ProtocolTransportConfig.describe(): String {
        return when (transportType) {
            TransportType.RTU -> {
                "RTU(portName=${portName.orEmpty()}, baudRate=${baudRate}, dataBits=${dataBits}, stopBits=${stopBits}, parity=${parity}, responseTimeoutMs=${responseTimeoutMs})"
            }

            TransportType.TCP -> {
                "TCP(host=${host.orEmpty()}, tcpPort=${tcpPort}, responseTimeoutMs=${responseTimeoutMs})"
            }
        }
    }

    /**
     * 处理旧版模块传输row。
     *
     * @param protocolTemplateCode 协议模板编码。
     */
    private fun LegacyModuleTransportRow.toTransportConfig(
        protocolTemplateCode: String,
    ): ProtocolTransportConfig? {
        return when (protocolTemplateCode) {
            "MODBUS_RTU_CLIENT" -> {
                if (
                    portName == null &&
                    baudRate == null &&
                    dataBits == null &&
                    stopBits == null &&
                    parity == null &&
                    responseTimeoutMs == null
                ) {
                    null
                } else {
                    ProtocolTransportConfig(
                        transportType = TransportType.RTU,
                        portName = portName,
                        baudRate = baudRate,
                        dataBits = dataBits,
                        stopBits = stopBits,
                        parity = parity,
                        responseTimeoutMs = responseTimeoutMs,
                    )
                }
            }

            else -> null
        }
    }

    /**
     * 处理旧版模块传输row。
     */
    private fun LegacyModuleTransportRow.hasLegacyValues(): Boolean {
        return portName != null ||
            baudRate != null ||
            dataBits != null ||
            stopBits != null ||
            parity != null ||
            responseTimeoutMs != null
    }

    /**
     * 处理旧版模块传输row。
     */
    private fun LegacyModuleTransportRow.describeLegacyValues(): String {
        return "portName=${portName.orEmpty()}, baudRate=${baudRate}, dataBits=${dataBits}, stopBits=${stopBits}, parity=${parity}, responseTimeoutMs=${responseTimeoutMs}"
    }

    /**
     * 处理java。
     */
    private fun java.sql.ResultSet.toProtocolTransportConfig(): ProtocolTransportConfig? {
        val transportType = getNullableEnum<TransportType>("transport_type") ?: return null
        return ProtocolTransportConfig(
            transportType = transportType,
            host = getNullableString("host"),
            tcpPort = getNullableInt("tcp_port"),
            portName = getNullableString("port_name"),
            baudRate = getNullableInt("baud_rate"),
            dataBits = getNullableInt("data_bits"),
            stopBits = getNullableInt("stop_bits"),
            parity = getNullableEnum("parity"),
            responseTimeoutMs = getNullableInt("response_timeout_ms"),
        )
    }

    /**
     * 处理java。
     *
     * @param columnLabel columnlabel。
     */
    private fun java.sql.ResultSet.getNullableString(
        columnLabel: String,
    ): String? {
        return getString(columnLabel)?.trim()?.ifBlank { null }
    }

    /**
     * 处理java。
     *
     * @param columnLabel columnlabel。
     */
    private fun java.sql.ResultSet.getNullableInt(
        columnLabel: String,
    ): Int? {
        val value = getInt(columnLabel)
        return if (wasNull()) {
            null
        } else {
            value
        }
    }

    private inline fun <reified T : Enum<T>> java.sql.ResultSet.getNullableEnum(
        columnLabel: String,
    ): T? {
        val value = getString(columnLabel)?.trim()?.ifBlank { null } ?: return null
        return enumValues<T>().firstOrNull { item -> item.name == value }
            ?: error("Unknown enum value '$value' in column '$columnLabel'")
    }

    /**
     * 表示协议迁移row。
     *
     * @property id 主键 ID。
     * @property name 名称。
     * @property templateCode 模板编码。
     * @property transportConfig 传输配置。
     */
    private data class ProtocolMigrationRow(
        val id: Long,
        val name: String,
        val templateCode: String,
        val transportConfig: ProtocolTransportConfig?,
    )

    /**
     * 表示旧版模块传输row。
     *
     * @property protocolId 协议 ID。
     * @property moduleId 模块 ID。
     * @property moduleName 模块名称。
     * @property portName 端口名。
     * @property baudRate 波特率。
     * @property dataBits 数据位。
     * @property stopBits 停止位。
     * @property parity 校验位。
     * @property responseTimeoutMs 响应超时时间（毫秒）。
     */
    private data class LegacyModuleTransportRow(
        val protocolId: Long,
        val moduleId: Long,
        val moduleName: String,
        val portName: String?,
        val baudRate: Int?,
        val dataBits: Int?,
        val stopBits: Int?,
        val parity: Parity?,
        val responseTimeoutMs: Int?,
    )

    /**
     * 表示协议传输更新。
     *
     * @property protocolId 协议 ID。
     * @property transportConfig 传输配置。
     */
    private data class ProtocolTransportUpdate(
        val protocolId: Long,
        val transportConfig: ProtocolTransportConfig,
    )
}
