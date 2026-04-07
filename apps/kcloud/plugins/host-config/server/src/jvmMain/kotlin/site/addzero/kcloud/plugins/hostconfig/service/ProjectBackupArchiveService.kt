package site.addzero.kcloud.plugins.hostconfig.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class ProjectBackupArchiveService(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
    private val projectService: ProjectService,
    private val tagService: TagService,
) {

    fun createBackup(projectId: Long): StoredProjectBackup {
        val project = projectService.getProject(projectId)
        val tree = projectService.getProjectTree(projectId)
        val projectProtocols = queryRows(
            """
            SELECT pp.*
            FROM project_protocol pp
            WHERE pp.project_id = ?
            ORDER BY pp.sort_index ASC, pp.id ASC
            """.trimIndent(),
            projectId,
        )
        val protocols = queryRows(
            """
            SELECT pi.*
            FROM protocol_instance pi
            INNER JOIN project_protocol pp ON pp.protocol_id = pi.id
            WHERE pp.project_id = ?
            ORDER BY pp.sort_index ASC, pi.id ASC
            """.trimIndent(),
            projectId,
        )
        val modules = queryRows(
            """
            SELECT mi.*
            FROM module_instance mi
            INNER JOIN project_protocol pp ON pp.protocol_id = mi.protocol_id
            WHERE pp.project_id = ?
            ORDER BY pp.sort_index ASC, mi.sort_index ASC, mi.id ASC
            """.trimIndent(),
            projectId,
        )
        val devices = queryRows(
            """
            SELECT d.*
            FROM device d
            INNER JOIN module_instance mi ON mi.id = d.module_id
            INNER JOIN project_protocol pp ON pp.protocol_id = mi.protocol_id
            WHERE pp.project_id = ?
            ORDER BY pp.sort_index ASC, mi.sort_index ASC, d.sort_index ASC, d.id ASC
            """.trimIndent(),
            projectId,
        )
        val tags = queryRows(
            """
            SELECT t.*
            FROM tag t
            INNER JOIN device d ON d.id = t.device_id
            INNER JOIN module_instance mi ON mi.id = d.module_id
            INNER JOIN project_protocol pp ON pp.protocol_id = mi.protocol_id
            WHERE pp.project_id = ?
            ORDER BY pp.sort_index ASC, mi.sort_index ASC, d.sort_index ASC, t.sort_index ASC, t.id ASC
            """.trimIndent(),
            projectId,
        )
        val tagValueTexts = queryRows(
            """
            SELECT tvt.*
            FROM tag_value_text tvt
            INNER JOIN tag t ON t.id = tvt.tag_id
            INNER JOIN device d ON d.id = t.device_id
            INNER JOIN module_instance mi ON mi.id = d.module_id
            INNER JOIN project_protocol pp ON pp.protocol_id = mi.protocol_id
            WHERE pp.project_id = ?
            ORDER BY tvt.tag_id ASC, tvt.sort_index ASC, tvt.id ASC
            """.trimIndent(),
            projectId,
        )
        val mqttConfigs = queryRows(
            "SELECT * FROM project_mqtt_config WHERE project_id = ? ORDER BY id ASC",
            projectId,
        )
        val modbusConfigs = queryRows(
            "SELECT * FROM project_modbus_server_config WHERE project_id = ? ORDER BY transport_type ASC, id ASC",
            projectId,
        )

        val exportedAt = System.currentTimeMillis()
        val payload = linkedMapOf<String, Any?>(
            "schemaVersion" to "okmy-dics-backup-v1",
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
                "project" to project,
                "tree" to tree,
                "projectMqttConfig" to mqttConfigs.firstOrNull(),
                "projectModbusServerConfigs" to modbusConfigs,
                "protocols" to tree.protocols.map { protocol ->
                    linkedMapOf(
                        "id" to protocol.id,
                        "name" to protocol.name,
                        "sortIndex" to protocol.sortIndex,
                        "pollingIntervalMs" to protocol.pollingIntervalMs,
                        "protocolTemplateId" to protocol.protocolTemplateId,
                        "protocolTemplateCode" to protocol.protocolTemplateCode,
                        "protocolTemplateName" to protocol.protocolTemplateName,
                        "modules" to protocol.modules.map { module ->
                            linkedMapOf(
                                "id" to module.id,
                                "name" to module.name,
                                "sortIndex" to module.sortIndex,
                                "protocolId" to module.protocolId,
                                "portName" to module.portName,
                                "baudRate" to module.baudRate,
                                "dataBits" to module.dataBits,
                                "stopBits" to module.stopBits,
                                "parity" to module.parity,
                                "responseTimeoutMs" to module.responseTimeoutMs,
                                "moduleTemplateId" to module.moduleTemplateId,
                                "moduleTemplateCode" to module.moduleTemplateCode,
                                "moduleTemplateName" to module.moduleTemplateName,
                                "devices" to module.devices.map { device ->
                                    linkedMapOf(
                                        "id" to device.id,
                                        "name" to device.name,
                                        "sortIndex" to device.sortIndex,
                                        "stationNo" to device.stationNo,
                                        "requestIntervalMs" to device.requestIntervalMs,
                                        "writeIntervalMs" to device.writeIntervalMs,
                                        "byteOrder2" to device.byteOrder2,
                                        "byteOrder4" to device.byteOrder4,
                                        "floatOrder" to device.floatOrder,
                                        "batchAnalogStart" to device.batchAnalogStart,
                                        "batchAnalogLength" to device.batchAnalogLength,
                                        "batchDigitalStart" to device.batchDigitalStart,
                                        "batchDigitalLength" to device.batchDigitalLength,
                                        "disabled" to device.disabled,
                                        "deviceTypeId" to device.deviceTypeId,
                                        "deviceTypeCode" to device.deviceTypeCode,
                                        "deviceTypeName" to device.deviceTypeName,
                                        "tags" to tagService.listTags(device.id, 0, Int.MAX_VALUE).d,
                                    )
                                },
                            )
                        },
                    )
                },
            ),
            "tables" to linkedMapOf(
                "project" to listOf(querySingleRow("SELECT * FROM project WHERE id = ?", projectId)),
                "project_protocol" to projectProtocols,
                "protocol_instance" to protocols,
                "module_instance" to modules,
                "device" to devices,
                "tag" to tags,
                "tag_value_text" to tagValueTexts,
                "project_mqtt_config" to mqttConfigs,
                "project_modbus_server_config" to modbusConfigs,
            ),
            "dictionaries" to linkedMapOf(
                "protocol_template" to queryRows("SELECT * FROM protocol_template ORDER BY sort_index ASC, id ASC"),
                "module_template" to queryRows("SELECT * FROM module_template ORDER BY protocol_template_id ASC, sort_index ASC, id ASC"),
                "device_type" to queryRows("SELECT * FROM device_type ORDER BY sort_index ASC, id ASC"),
                "register_type" to queryRows("SELECT * FROM register_type ORDER BY sort_index ASC, id ASC"),
                "data_type" to queryRows("SELECT * FROM data_type ORDER BY sort_index ASC, id ASC"),
            ),
        )

        val fileName = "okmy-dics-project-$projectId-backup.json"
        val target = backupRootDir().resolve(fileName)
        Files.createDirectories(target.parent)
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), payload)

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
        val fileName = "okmy-dics-project-$projectId-backup.json"
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
        val backup = findBackup(projectId) ?: throw site.addzero.kcloud.plugins.hostconfig.api.common.NotFoundException("Project backup not found")
        return FileSystemResource(backup.filePath)
    }

    private fun backupRootDir(): Path = Paths.get("build", "project-backups")

    private fun querySingleRow(sql: String, vararg args: Any): Map<String, Any?> =
        queryRows(sql, *args).firstOrNull() ?: emptyMap<String, Any?>()

    private fun queryRows(sql: String, vararg args: Any): List<Map<String, Any?>> =
        jdbcTemplate.queryForList(sql, *args).map { LinkedHashMap(it) }

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
