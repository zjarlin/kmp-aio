package site.addzero.kcloud.plugins.hostconfig.service

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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
import site.addzero.kcloud.plugins.hostconfig.routes.common.NotFoundException
import site.addzero.kcloud.plugins.hostconfig.routes.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.routes.project.ProjectTreeResponse

@Single
class ProjectBackupArchiveService(
    private val jdbc: HostConfigJdbc,
    private val projectService: ProjectService,
) {
    fun createBackup(projectId: Long): StoredProjectBackup {
        val project = projectService.getProject(projectId)
        val tree = projectService.getProjectTree(projectId)

        val projectProtocols = jdbc.queryRows(
            """
            SELECT pp.*
            FROM host_config_project_protocol pp
            WHERE pp.project_id = ?
            ORDER BY pp.sort_index ASC, pp.id ASC
            """.trimIndent(),
            projectId,
        )
        val protocols = jdbc.queryRows(
            """
            SELECT pi.*
            FROM host_config_protocol_instance pi
            INNER JOIN host_config_project_protocol pp ON pp.protocol_id = pi.id
            WHERE pp.project_id = ?
            ORDER BY pp.sort_index ASC, pi.id ASC
            """.trimIndent(),
            projectId,
        )
        val modules = jdbc.queryRows(
            """
            SELECT mi.*
            FROM host_config_module_instance mi
            INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
            WHERE pp.project_id = ?
            ORDER BY pp.sort_index ASC, mi.sort_index ASC, mi.id ASC
            """.trimIndent(),
            projectId,
        )
        val devices = jdbc.queryRows(
            """
            SELECT d.*
            FROM host_config_device d
            INNER JOIN host_config_module_instance mi ON mi.id = d.module_id
            INNER JOIN host_config_project_protocol pp ON pp.protocol_id = mi.protocol_id
            WHERE pp.project_id = ?
            ORDER BY pp.sort_index ASC, mi.sort_index ASC, d.sort_index ASC, d.id ASC
            """.trimIndent(),
            projectId,
        )
        val tags = jdbc.queryRows(
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
        )
        val tagValueTexts = jdbc.queryRows(
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
        )
        val mqttConfigs = jdbc.queryRows(
            "SELECT * FROM host_config_project_mqtt_config WHERE project_id = ? ORDER BY id ASC",
            projectId,
        )
        val modbusConfigs = jdbc.queryRows(
            "SELECT * FROM host_config_project_modbus_server_config WHERE project_id = ? ORDER BY transport_type ASC, id ASC",
            projectId,
        )

        val exportedAt = System.currentTimeMillis()
        val payload = linkedMapOf<String, Any?>(
            "schemaVersion" to "host-config-backup-v1",
            "exportedAt" to exportedAt,
            "exportedAtText" to formatTimestamp(exportedAt),
            "projectId" to projectId,
            "projectName" to project.name,
            "summary" to linkedMapOf(
                "projectCount" to 1,
                "protocolCount" to protocols.size,
                "moduleCount" to modules.size,
                "deviceCount" to devices.size,
                "tagCount" to tags.size,
                "tagValueTextCount" to tagValueTexts.size,
                "mqttConfigCount" to mqttConfigs.size,
                "modbusConfigCount" to modbusConfigs.size,
            ),
            "view" to linkedMapOf(
                "project" to backupJson.encodeToJsonElement(ProjectResponse.serializer(), project),
                "tree" to backupJson.encodeToJsonElement(ProjectTreeResponse.serializer(), tree),
                "mqttConfig" to mqttConfigs.firstOrNull(),
                "modbusConfigs" to modbusConfigs,
            ),
            "tables" to linkedMapOf(
                "host_config_project" to listOfNotNull(
                    jdbc.queryRows("SELECT * FROM host_config_project WHERE id = ?", projectId).firstOrNull(),
                ),
                "host_config_project_protocol" to projectProtocols,
                "host_config_protocol_instance" to protocols,
                "host_config_module_instance" to modules,
                "host_config_device" to devices,
                "host_config_tag" to tags,
                "host_config_tag_value_text" to tagValueTexts,
                "host_config_project_mqtt_config" to mqttConfigs,
                "host_config_project_modbus_server_config" to modbusConfigs,
            ),
            "dictionaries" to linkedMapOf(
                "host_config_protocol_template" to jdbc.queryRows(
                    "SELECT * FROM host_config_protocol_template ORDER BY sort_index ASC, id ASC",
                ),
                "host_config_module_template" to jdbc.queryRows(
                    "SELECT * FROM host_config_module_template ORDER BY protocol_template_id ASC, sort_index ASC, id ASC",
                ),
                "host_config_device_type" to jdbc.queryRows(
                    "SELECT * FROM host_config_device_type ORDER BY sort_index ASC, id ASC",
                ),
                "host_config_register_type" to jdbc.queryRows(
                    "SELECT * FROM host_config_register_type ORDER BY sort_index ASC, id ASC",
                ),
                "host_config_data_type" to jdbc.queryRows(
                    "SELECT * FROM host_config_data_type ORDER BY sort_index ASC, id ASC",
                ),
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
                protocolCount = protocols.size,
                moduleCount = modules.size,
                deviceCount = devices.size,
                tagCount = tags.size,
            ),
        )
    }

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

    fun loadBackupResource(projectId: Long): FileSystemResource {
        val backup = findBackup(projectId) ?: throw NotFoundException("Project backup not found")
        return FileSystemResource(backup.filePath)
    }

    private fun backupRootDir(): Path = Paths.get("build", "host-config-backups")

    private fun buildSummaryText(
        protocolCount: Int,
        moduleCount: Int,
        deviceCount: Int,
        tagCount: Int,
    ): String =
        "备份完成，已导出 $protocolCount 个协议、$moduleCount 个模块、$deviceCount 个设备、$tagCount 个点位。"

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

data class StoredProjectBackup(
    val fileName: String,
    val filePath: Path,
    val sizeBytes: Long,
    val summaryText: String,
)
