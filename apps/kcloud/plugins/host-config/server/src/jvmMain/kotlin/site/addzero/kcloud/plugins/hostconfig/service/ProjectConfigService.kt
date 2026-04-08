package site.addzero.kcloud.plugins.hostconfig.service

import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.exists
import org.koin.core.annotation.Single
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteAction
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteActionRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRequest
import site.addzero.kcloud.plugins.hostconfig.routes.common.BusinessValidationException
import site.addzero.kcloud.plugins.hostconfig.routes.common.NotFoundException
import site.addzero.kcloud.plugins.hostconfig.model.entity.*
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Single
class ProjectConfigService(
    private val sql: KSqlClient,
    private val projectBackupArchiveService: ProjectBackupArchiveService,
) {
    private val uploadOperationStates = ConcurrentHashMap<Long, ProjectUploadOperationResponse>()

    companion object {
        private const val DEFAULT_FAULT_INDICATOR_PIN = "PA8"
        private const val DEFAULT_RUNNING_INDICATOR_PIN = "PA2"
    }

    fun getMqttConfig(projectId: Long): ProjectMqttConfigResponse {
        ensureProjectExists(projectId)
        val config = loadMqttConfig(projectId)
        return config?.toResponse()
            ?: ProjectMqttConfigResponse(
                id = null,
                enabled = false,
                breakpointResume = false,
                gatewayName = null,
                vendor = null,
                host = null,
                port = null,
                topic = null,
                gatewayId = null,
                authEnabled = false,
                username = null,
                passwordEncrypted = null,
                tlsEnabled = false,
                certFileRef = null,
                clientId = null,
                keepAliveSec = null,
                qos = null,
                reportPeriodSec = null,
                precision = null,
                valueChangeRatioEnabled = false,
                cloudControlDisabled = false,
            )
    }

    fun updateMqttConfig(
        projectId: Long,
        request: ProjectMqttConfigRequest,
    ): ProjectMqttConfigResponse {
        ensureProjectExists(projectId)
        val existing = loadMqttConfig(projectId)
        val now = now()
        val entity = new(ProjectMqttConfig::class).by {
            existing?.let { id = it.id }
            this.projectId = projectId
            this.enabled = request.enabled
            this.breakpointResume = request.breakpointResume
            this.gatewayName = request.gatewayName.cleanNullable()
            this.vendor = request.vendor.cleanNullable()
            this.host = request.host.cleanNullable()
            this.port = request.port
            this.topic = request.topic.cleanNullable()
            this.gatewayId = request.gatewayId.cleanNullable()
            this.authEnabled = request.authEnabled
            this.username = request.username.cleanNullable()
            this.passwordEncrypted = request.passwordEncrypted.cleanNullable()
            this.tlsEnabled = request.tlsEnabled
            this.certFileRef = request.certFileRef.cleanNullable()
            this.clientId = request.clientId.cleanNullable()
            this.keepAliveSec = request.keepAliveSec
            this.qos = request.qos
            this.reportPeriodSec = request.reportPeriodSec
            this.precision = request.precision.toDecimalOrNull()
            this.valueChangeRatioEnabled = request.valueChangeRatioEnabled
            this.cloudControlDisabled = request.cloudControlDisabled
            this.createdAt = existing?.createdAt ?: now
            this.updatedAt = now
        }
        val config = sql.saveCommand(entity) {
            setMode(if (existing == null) SaveMode.INSERT_ONLY else SaveMode.UPDATE_ONLY)
        }.execute().modifiedEntity
        return loadMqttConfig(config.project.id)?.toResponse()
            ?: throw NotFoundException("MQTT config not found")
    }

    fun getModbusServerConfig(
        projectId: Long,
        transportType: TransportType,
    ): ProjectModbusServerConfigResponse {
        ensureProjectExists(projectId)
        val config = loadModbusConfig(projectId, transportType)
        return config?.toResponse()
            ?: ProjectModbusServerConfigResponse(
                id = null,
                transportType = transportType,
                enabled = false,
                tcpPort = null,
                portName = null,
                baudRate = null,
                dataBits = null,
                stopBits = null,
                parity = null,
                stationNo = null,
            )
    }

    fun getGatewayPinConfig(projectId: Long): ProjectGatewayPinConfigResponse {
        ensureProjectExists(projectId)
        val config = loadGatewayPinConfig(projectId)
        return config?.toResponse()
            ?: ProjectGatewayPinConfigResponse(
                id = null,
                faultIndicatorPin = DEFAULT_FAULT_INDICATOR_PIN,
                runningIndicatorPin = DEFAULT_RUNNING_INDICATOR_PIN,
            )
    }

    fun updateModbusServerConfig(
        projectId: Long,
        transportType: TransportType,
        request: ProjectModbusServerConfigRequest,
    ): ProjectModbusServerConfigResponse {
        ensureProjectExists(projectId)
        val existing = loadModbusConfig(projectId, transportType)
        val now = now()
        val entity = new(ProjectModbusServerConfig::class).by {
            existing?.let { id = it.id }
            this.projectId = projectId
            this.transportType = transportType
            this.enabled = request.enabled
            this.tcpPort = request.tcpPort
            this.portName = request.portName.cleanNullable()
            this.baudRate = request.baudRate
            this.dataBits = request.dataBits
            this.stopBits = request.stopBits
            this.parity = request.parity
            this.stationNo = request.stationNo
            this.createdAt = existing?.createdAt ?: now
            this.updatedAt = now
        }
        sql.saveCommand(entity) {
            setMode(if (existing == null) SaveMode.INSERT_ONLY else SaveMode.UPDATE_ONLY)
        }.execute()
        return loadModbusConfig(projectId, transportType)?.toResponse()
            ?: throw NotFoundException("Modbus server config not found")
    }

    fun updateGatewayPinConfig(
        projectId: Long,
        request: ProjectGatewayPinConfigRequest,
    ): ProjectGatewayPinConfigResponse {
        ensureProjectExists(projectId)
        val existing = loadGatewayPinConfig(projectId)
        val now = now()
        val entity = new(ProjectGatewayPinConfig::class).by {
            existing?.let { id = it.id }
            this.projectId = projectId
            this.faultIndicatorPin = request.faultIndicatorPin.normalizePin(DEFAULT_FAULT_INDICATOR_PIN)
            this.runningIndicatorPin = request.runningIndicatorPin.normalizePin(DEFAULT_RUNNING_INDICATOR_PIN)
            this.createdAt = existing?.createdAt ?: now
            this.updatedAt = now
        }
        sql.saveCommand(entity) {
            setMode(if (existing == null) SaveMode.INSERT_ONLY else SaveMode.UPDATE_ONLY)
        }.execute()
        return loadGatewayPinConfig(projectId)?.toResponse()
            ?: throw NotFoundException("Gateway pin config not found")
    }

    fun getProjectUploadStatus(projectId: Long): ProjectUploadOperationResponse {
        ensureProjectExists(projectId)
        val idle = uploadOperationStates[projectId]
            ?: ProjectUploadOperationResponse(
                projectId = projectId,
                operation = "IDLE",
                progress = 0,
                statusText = "待开始",
                detailText = "尚未发起上传工程或远程操作",
                ipAddress = null,
                projectPath = null,
                selectedFileName = null,
                includeDriverConfig = true,
                includeFirmwareUpgrade = false,
                fastMode = false,
                backupFileName = null,
                backupDownloadUrl = null,
                backupSizeBytes = null,
                updatedAt = now(),
            )
        return attachBackupMetadata(projectId, idle)
    }

    fun uploadProject(
        projectId: Long,
        request: ProjectUploadRequest,
    ): ProjectUploadOperationResponse {
        ensureProjectExists(projectId)
        if (request.projectPath.cleanNullable() == null && request.selectedFileName.cleanNullable() == null) {
            throw BusinessValidationException("Project path or selected file name is required")
        }
        if (!request.includeDriverConfig && !request.includeFirmwareUpgrade) {
            throw BusinessValidationException("At least one upload item must be selected")
        }
        return rememberUploadOperation(
            projectId = projectId,
            operation = "UPLOAD",
            progress = 18,
            statusText = "服务端已接收上传请求",
            detailText = "当前版本已完成接口打通，设备侧上传执行器待接入",
            ipAddress = request.ipAddress.cleanNullable(),
            projectPath = request.projectPath.cleanNullable(),
            selectedFileName = request.selectedFileName.cleanNullable(),
            includeDriverConfig = request.includeDriverConfig,
            includeFirmwareUpgrade = request.includeFirmwareUpgrade,
            fastMode = request.fastMode,
        )
    }

    fun triggerProjectUploadRemoteAction(
        projectId: Long,
        action: ProjectUploadRemoteAction,
        request: ProjectUploadRemoteActionRequest,
    ): ProjectUploadOperationResponse {
        ensureProjectExists(projectId)
        if (action == ProjectUploadRemoteAction.BACKUP) {
            val backup = projectBackupArchiveService.createBackup(projectId)
            return rememberUploadOperation(
                projectId = projectId,
                operation = action.name,
                progress = 100,
                statusText = "备份配置工程完成",
                detailText = backup.summaryText,
                ipAddress = request.ipAddress.cleanNullable(),
                projectPath = null,
                selectedFileName = null,
                includeDriverConfig = true,
                includeFirmwareUpgrade = true,
                fastMode = false,
                backupFileName = backup.fileName,
                backupDownloadUrl = buildBackupDownloadUrl(projectId),
                backupSizeBytes = backup.sizeBytes,
            )
        }
        return rememberUploadOperation(
            projectId = projectId,
            operation = action.name,
            progress = 12,
            statusText = "${action.toLabel()}请求已接收",
            detailText = "当前版本仅完成接口与参数校验，设备执行器待接入",
            ipAddress = request.ipAddress.cleanNullable(),
            projectPath = null,
            selectedFileName = null,
            includeDriverConfig = false,
            includeFirmwareUpgrade = false,
            fastMode = false,
        )
    }

    fun downloadProjectBackup(projectId: Long): ResponseEntity<Resource> {
        ensureProjectExists(projectId)
        val resource = projectBackupArchiveService.loadBackupResource(projectId)
        val headers = HttpHeaders()
        headers.contentDisposition = ContentDisposition.attachment()
            .filename(resource.filename ?: "project-backup.json", StandardCharsets.UTF_8)
            .build()
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(resource.contentLength())
            .contentType(MediaType.APPLICATION_JSON)
            .body(resource)
    }

    private fun loadMqttConfig(projectId: Long): ProjectMqttConfig? =
        sql.createQuery(ProjectMqttConfig::class) {
            where(table.project.id eq projectId)
            select(table.fetch(Fetchers.mqttConfig))
        }.execute().firstOrNull()

    private fun loadModbusConfig(
        projectId: Long,
        transportType: TransportType,
    ): ProjectModbusServerConfig? =
        sql.createQuery(ProjectModbusServerConfig::class) {
            where(table.project.id eq projectId)
            where(table.transportType eq transportType)
            select(table.fetch(Fetchers.modbusConfig))
        }.execute().firstOrNull()

    private fun loadGatewayPinConfig(projectId: Long): ProjectGatewayPinConfig? =
        sql.createQuery(ProjectGatewayPinConfig::class) {
            where(table.project.id eq projectId)
            select(table)
        }.execute().firstOrNull()

    private fun ensureProjectExists(projectId: Long) {
        val exists = sql.exists(Project::class) {
            where(table.id eq projectId)
        }
        if (!exists) {
            throw NotFoundException("Project not found")
        }
    }

    private fun rememberUploadOperation(
        projectId: Long,
        operation: String,
        progress: Int,
        statusText: String,
        detailText: String?,
        ipAddress: String?,
        projectPath: String?,
        selectedFileName: String?,
        includeDriverConfig: Boolean,
        includeFirmwareUpgrade: Boolean,
        fastMode: Boolean,
        backupFileName: String? = null,
        backupDownloadUrl: String? = null,
        backupSizeBytes: Long? = null,
    ): ProjectUploadOperationResponse {
        val response = ProjectUploadOperationResponse(
            projectId = projectId,
            operation = operation,
            progress = progress,
            statusText = statusText,
            detailText = detailText,
            ipAddress = ipAddress,
            projectPath = projectPath,
            selectedFileName = selectedFileName,
            includeDriverConfig = includeDriverConfig,
            includeFirmwareUpgrade = includeFirmwareUpgrade,
            fastMode = fastMode,
            backupFileName = backupFileName,
            backupDownloadUrl = backupDownloadUrl,
            backupSizeBytes = backupSizeBytes,
            updatedAt = now(),
        )
        val enriched = attachBackupMetadata(projectId, response)
        uploadOperationStates[projectId] = enriched
        return enriched
    }

    private fun attachBackupMetadata(
        projectId: Long,
        response: ProjectUploadOperationResponse,
    ): ProjectUploadOperationResponse {
        if (response.backupFileName != null && response.backupDownloadUrl != null && response.backupSizeBytes != null) {
            return response
        }
        val backup = projectBackupArchiveService.findBackup(projectId) ?: return response
        return response.copy(
            backupFileName = backup.fileName,
            backupDownloadUrl = buildBackupDownloadUrl(projectId),
            backupSizeBytes = backup.sizeBytes,
        )
    }

    private fun buildBackupDownloadUrl(projectId: Long): String =
        "/api/host-config/v1/projects/$projectId/upload-project/backup"

    private fun ProjectMqttConfig.toResponse(): ProjectMqttConfigResponse =
        ProjectMqttConfigResponse(
            id = id,
            enabled = enabled,
            breakpointResume = breakpointResume,
            gatewayName = gatewayName,
            vendor = vendor,
            host = host,
            port = port,
            topic = topic,
            gatewayId = gatewayId,
            authEnabled = authEnabled,
            username = username,
            passwordEncrypted = passwordEncrypted,
            tlsEnabled = tlsEnabled,
            certFileRef = certFileRef,
            clientId = clientId,
            keepAliveSec = keepAliveSec,
            qos = qos,
            reportPeriodSec = reportPeriodSec,
            precision = precision.toApiDecimal(),
            valueChangeRatioEnabled = valueChangeRatioEnabled,
            cloudControlDisabled = cloudControlDisabled,
        )

    private fun ProjectModbusServerConfig.toResponse(): ProjectModbusServerConfigResponse =
        ProjectModbusServerConfigResponse(
            id = id,
            transportType = transportType,
            enabled = enabled,
            tcpPort = tcpPort,
            portName = portName,
            baudRate = baudRate,
            dataBits = dataBits,
            stopBits = stopBits,
            parity = parity,
            stationNo = stationNo,
        )

    private fun ProjectGatewayPinConfig.toResponse(): ProjectGatewayPinConfigResponse =
        ProjectGatewayPinConfigResponse(
            id = id,
            faultIndicatorPin = faultIndicatorPin,
            runningIndicatorPin = runningIndicatorPin,
        )

    private fun String?.cleanNullable(): String? =
        this?.trim()?.ifBlank { null }

    private fun String.normalizePin(
        defaultValue: String,
    ): String {
        return trim().ifBlank { defaultValue }
    }

    private fun String?.toDecimalOrNull(): BigDecimal? =
        this.cleanNullable()?.toBigDecimalOrNull()

    private fun BigDecimal?.toApiDecimal(): String? =
        this?.stripTrailingZeros()?.toPlainString()

    private fun ProjectUploadRemoteAction.toLabel(): String =
        when (this) {
            ProjectUploadRemoteAction.BACKUP -> "备份配置工程"
            ProjectUploadRemoteAction.RESTORE -> "还原配置工程"
            ProjectUploadRemoteAction.DELETE -> "删除配置工程"
            ProjectUploadRemoteAction.RESTART -> "远程重启"
        }

    private fun now(): Long = System.currentTimeMillis()
}
