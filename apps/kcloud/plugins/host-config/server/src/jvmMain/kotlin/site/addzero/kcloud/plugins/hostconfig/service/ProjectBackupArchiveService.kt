package site.addzero.kcloud.plugins.hostconfig.service

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.koin.core.annotation.Single
import org.springframework.core.io.FileSystemResource
import site.addzero.core.network.json.AnySerializer
import site.addzero.util.db.SqlExecutor
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kmp.exp.NotFoundException

@Single
/**
 * 提供项目备份归档相关服务。
 *
 * @property jdbc 主机配置 JDBC 工具。
 * @property projectService 项目服务。
 */
class ProjectBackupArchiveService(
    private val jdbc: SqlExecutor,
    private val projectService: ProjectService,
) {
    /**
     * 创建备份。
     *
     * @param projectId 项目 ID。
     */
    fun createBackup(projectId: Long): StoredProjectBackup {
        val project = projectService.getProject(projectId)
        val tree = projectService.getProjectTree(projectId)
        val backupRows = jdbc.withTransaction { connection ->
            BackupRows(
                projectProtocols = queryForList(
                    connection,
                    """
                    SELECT pp.*
                    FROM host_config_project_protocol pp
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, pp.id ASC
                    """.trimIndent(),
                    projectId,
                ),
                protocols = queryForList(
                    connection,
                    """
                    SELECT pi.*
                    FROM host_config_protocol_instance pi
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = pi.id
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, pi.id ASC
                    """.trimIndent(),
                    projectId,
                ),
                modules = queryForList(
                    connection,
                    """
                    SELECT mi.*
                    FROM host_config_module_instance mi
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, mi.sort_index ASC, mi.id ASC
                    """.trimIndent(),
                    projectId,
                ),
                devices = queryForList(
                    connection,
                    """
                    SELECT d.*
                    FROM host_config_device d
                    INNER JOIN host_config_module_instance mi ON mi.id = d.module_id
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, mi.sort_index ASC, d.sort_index ASC, d.id ASC
                    """.trimIndent(),
                    projectId,
                ),
                tags = queryForList(
                    connection,
                    """
                    SELECT t.*
                    FROM host_config_tag t
                    INNER JOIN host_config_device d ON d.id = t.device_id
                    INNER JOIN host_config_module_instance mi ON mi.id = d.module_id
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
                    WHERE pp.project_id = ?
                    ORDER BY pp.sort_index ASC, mi.sort_index ASC, d.sort_index ASC, t.sort_index ASC, t.id ASC
                    """.trimIndent(),
                    projectId,
                ),
                tagValueTexts = queryForList(
                    connection,
                    """
                    SELECT tvt.*
                    FROM host_config_tag_value_text tvt
                    INNER JOIN host_config_tag t ON t.id = tvt.tag_id
                    INNER JOIN host_config_device d ON d.id = t.device_id
                    INNER JOIN host_config_module_instance mi ON mi.id = d.module_id
                    INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
                    WHERE pp.project_id = ?
                    ORDER BY tvt.tag_id ASC, tvt.sort_index ASC, tvt.id ASC
                    """.trimIndent(),
                    projectId,
                ),
                mqttConfigs = queryForList(
                    connection,
                    "SELECT * FROM host_config_project_mqtt_config WHERE project_id = ? ORDER BY id ASC",
                    projectId,
                ),
                modbusConfigs = queryForList(
                    connection,
                    "SELECT * FROM host_config_project_modbus_server_config WHERE project_id = ? ORDER BY transport_type ASC, id ASC",
                    projectId,
                ),
                gatewayPinConfigs = queryForList(
                    connection,
                    "SELECT * FROM host_config_project_gateway_pin_config WHERE project_id = ? ORDER BY id ASC",
                    projectId,
                ),
                projectRow = queryForList(connection, "SELECT * FROM host_config_project WHERE id = ?", projectId).firstOrNull(),
                protocolTemplates = queryForList(
                    connection,
                    "SELECT * FROM host_config_protocol_template ORDER BY sort_index ASC, id ASC",
                ),
                moduleTemplates = queryForList(
                    connection,
                    "SELECT * FROM host_config_module_template ORDER BY protocol_template_id ASC, sort_index ASC, id ASC",
                ),
                deviceTypes = queryForList(
                    connection,
                    "SELECT * FROM host_config_device_type ORDER BY sort_index ASC, id ASC",
                ),
                registerTypes = queryForList(
                    connection,
                    "SELECT * FROM host_config_register_type ORDER BY sort_index ASC, id ASC",
                ),
                dataTypes = queryForList(
                    connection,
                    "SELECT * FROM host_config_data_type ORDER BY sort_index ASC, id ASC",
                ),
            )
        }

        val exportedAt = System.currentTimeMillis()
        val payload = linkedMapOf<String, Any?>(
            "schemaVersion" to "host-config-backup-v1",
            "exportedAt" to exportedAt,
            "exportedAtText" to formatTimestamp(exportedAt),
            "projectId" to projectId,
            "projectName" to project.name,
            "summary" to linkedMapOf(
                "projectCount" to 1,
                "protocolCount" to backupRows.protocols.size,
                "moduleCount" to backupRows.modules.size,
                "deviceCount" to backupRows.devices.size,
                "tagCount" to backupRows.tags.size,
                "tagValueTextCount" to backupRows.tagValueTexts.size,
                "mqttConfigCount" to backupRows.mqttConfigs.size,
                "modbusConfigCount" to backupRows.modbusConfigs.size,
                "gatewayPinConfigCount" to backupRows.gatewayPinConfigs.size,
            ),
            "view" to linkedMapOf(
                "project" to backupJson.encodeToJsonElement(ProjectResponse.serializer(), project),
                "tree" to backupJson.encodeToJsonElement(ProjectTreeResponse.serializer(), tree),
                "mqttConfig" to backupRows.mqttConfigs.firstOrNull(),
                "modbusConfigs" to backupRows.modbusConfigs,
                "gatewayPinConfig" to backupRows.gatewayPinConfigs.firstOrNull(),
            ),
            "tables" to linkedMapOf(
                "host_config_project" to listOfNotNull(backupRows.projectRow),
                "host_config_project_protocol" to backupRows.projectProtocols,
                "host_config_protocol_instance" to backupRows.protocols,
                "host_config_module_instance" to backupRows.modules,
                "host_config_device" to backupRows.devices,
                "host_config_tag" to backupRows.tags,
                "host_config_tag_value_text" to backupRows.tagValueTexts,
                "host_config_project_mqtt_config" to backupRows.mqttConfigs,
                "host_config_project_modbus_server_config" to backupRows.modbusConfigs,
                "host_config_project_gateway_pin_config" to backupRows.gatewayPinConfigs,
            ),
            "dictionaries" to linkedMapOf(
                "host_config_protocol_template" to backupRows.protocolTemplates,
                "host_config_module_template" to backupRows.moduleTemplates,
                "host_config_device_type" to backupRows.deviceTypes,
                "host_config_register_type" to backupRows.registerTypes,
                "host_config_data_type" to backupRows.dataTypes,
            ),
        )

        val fileName = "host-config-project-$projectId-backup.json"
        val target = backupRootDir().resolve(fileName)
        Files.createDirectories(target.parent)
        Files.writeString(
            target,
            backupJson.encodeToString(AnySerializer, payload),
            StandardCharsets.UTF_8,
        )

        return StoredProjectBackup(
            fileName = fileName,
            filePath = target,
            sizeBytes = Files.size(target),
            summaryText = buildSummaryText(
                protocolCount = backupRows.protocols.size,
                moduleCount = backupRows.modules.size,
                deviceCount = backupRows.devices.size,
                tagCount = backupRows.tags.size,
            ),
        )
    }

    private fun queryForList(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): List<Map<String, Any?>> = jdbc.queryForList(connection, sql, *args)

    /**
     * 处理find备份。
     *
     * @param projectId 项目 ID。
     */
    fun findBackup(projectId: Long): StoredProjectBackup? {
        val fileName = "host-config-project-$projectId-backup.json"
        val target = backupRootDir().resolve(fileName)
        if (!Files.exists(target)) {
            return null
        }
        return StoredProjectBackup(
            fileName = fileName,
            filePath = target,
            sizeBytes = Files.size(target),
            summaryText = "已存在最近一次备份文件，可直接下载。",
        )
    }

    /**
     * 加载备份resource。
     *
     * @param projectId 项目 ID。
     */
    fun loadBackupResource(projectId: Long): FileSystemResource {
        val backup = findBackup(projectId) ?: throw NotFoundException("Project backup not found")
        return FileSystemResource(backup.filePath)
    }

    /**
     * 处理备份根目录dir。
     */
    private fun backupRootDir(): Path = Paths.get("build", "host-config-backups")

    /**
     * 构建摘要text。
     *
     * @param protocolCount 协议count。
     * @param moduleCount 模块count。
     * @param deviceCount 设备count。
     * @param tagCount 标签count。
     */
    private fun buildSummaryText(
        protocolCount: Int,
        moduleCount: Int,
        deviceCount: Int,
        tagCount: Int,
    ): String =
        "备份完成，已导出 $protocolCount 个协议、$moduleCount 个模块、$deviceCount 个设备、$tagCount 个点位。"

    /**
     * 处理formattimestamp。
     *
     * @param timestamp timestamp。
     */
    private fun formatTimestamp(timestamp: Long): String =
        DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(timestamp))

    companion object {
        private val backupJson = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
            useAlternativeNames = false
            coerceInputValues = true
            serializersModule = SerializersModule {
                contextual(Any::class, AnySerializer)
            }
        }

        private val DATE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
    }
}

/**
 * 表示stored项目备份。
 *
 * @property fileName file名称。
 * @property filePath file路径。
 * @property sizeBytes size字节。
 * @property summaryText 摘要文本。
 */
data class StoredProjectBackup(
    val fileName: String,
    val filePath: Path,
    val sizeBytes: Long,
    val summaryText: String,
)

private data class BackupRows(
    val projectProtocols: List<Map<String, Any?>>,
    val protocols: List<Map<String, Any?>>,
    val modules: List<Map<String, Any?>>,
    val devices: List<Map<String, Any?>>,
    val tags: List<Map<String, Any?>>,
    val tagValueTexts: List<Map<String, Any?>>,
    val mqttConfigs: List<Map<String, Any?>>,
    val modbusConfigs: List<Map<String, Any?>>,
    val gatewayPinConfigs: List<Map<String, Any?>>,
    val projectRow: Map<String, Any?>?,
    val protocolTemplates: List<Map<String, Any?>>,
    val moduleTemplates: List<Map<String, Any?>>,
    val deviceTypes: List<Map<String, Any?>>,
    val registerTypes: List<Map<String, Any?>>,
    val dataTypes: List<Map<String, Any?>>,
)
