package site.addzero.kcloud.plugins.hostconfig.service

import java.math.BigDecimal
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.exists
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectSqliteFileResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectSqliteImportRequest
import site.addzero.kmp.exp.BusinessValidationException
import site.addzero.kmp.exp.NotFoundException
import site.addzero.kcloud.plugins.hostconfig.model.entity.*
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Single
/**
 * 提供项目配置相关服务。
 *
 * @property sql Jimmer SQL 客户端。
 */
class ProjectConfigService(
    private val sql: KSqlClient,
    private val projectSqliteTransferService: ProjectSqliteTransferService,
) {
    companion object {
        private const val DEFAULT_FAULT_INDICATOR_PIN = "PA8"
        private const val DEFAULT_RUNNING_INDICATOR_PIN = "PA2"
    }

    /**
     * 获取MQTT配置。
     *
     * @param projectId 项目 ID。
     */
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

    /**
     * 更新MQTT配置。
     *
     * @param projectId 项目 ID。
     * @param request 请求参数。
     */
    fun updateMqttConfig(
        projectId: Long,
        request: ProjectMqttConfigRequest,
    ): ProjectMqttConfigResponse {
        ensureProjectExists(projectId)
        val existing = loadMqttConfig(projectId)
        val now = now()
        val entity = ProjectMqttConfig {
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

    /**
     * 获取modbus服务端配置。
     *
     * @param projectId 项目 ID。
     * @param transportType 传输类型。
     */
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

    /**
     * 获取网关pin配置。
     *
     * @param projectId 项目 ID。
     */
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

    /**
     * 更新modbus服务端配置。
     *
     * @param projectId 项目 ID。
     * @param transportType 传输类型。
     * @param request 请求参数。
     */
    fun updateModbusServerConfig(
        projectId: Long,
        transportType: TransportType,
        request: ProjectModbusServerConfigRequest,
    ): ProjectModbusServerConfigResponse {
        ensureProjectExists(projectId)
        val existing = loadModbusConfig(projectId, transportType)
        val now = now()
        val entity = ProjectModbusServerConfig {
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

    /**
     * 更新网关pin配置。
     *
     * @param projectId 项目 ID。
     * @param request 请求参数。
     */
    fun updateGatewayPinConfig(
        projectId: Long,
        request: ProjectGatewayPinConfigRequest,
    ): ProjectGatewayPinConfigResponse {
        ensureProjectExists(projectId)
        val existing = loadGatewayPinConfig(projectId)
        val now = now()
        val entity = ProjectGatewayPinConfig {
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

    /**
     * 导出工程 sqlite 文件到本地数据目录。
     *
     * @param projectId 工程 ID。
     */
    fun exportProjectSqlite(
        projectId: Long,
    ): ProjectSqliteFileResponse {
        ensureProjectExists(projectId)
        return projectSqliteTransferService.exportProjectSqlite(projectId)
    }

    /**
     * 导入工程 sqlite 文件到本地数据目录。
     *
     * @param request 请求参数。
     */
    fun importProjectSqlite(
        request: ProjectSqliteImportRequest,
    ): ProjectSqliteFileResponse {
        return projectSqliteTransferService.importProjectSqlite(request.sourceFilePath)
    }

    /**
     * 加载MQTT配置。
     *
     * @param projectId 项目 ID。
     */
    private fun loadMqttConfig(projectId: Long): ProjectMqttConfig? =
        sql.createQuery(ProjectMqttConfig::class) {
            where(table.project.id eq projectId)
            select(table.fetch(Fetchers.mqttConfig))
        }.execute().firstOrNull()

    /**
     * 加载modbus配置。
     *
     * @param projectId 项目 ID。
     * @param transportType 传输类型。
     */
    private fun loadModbusConfig(
        projectId: Long,
        transportType: TransportType,
    ): ProjectModbusServerConfig? =
        sql.createQuery(ProjectModbusServerConfig::class) {
            where(table.project.id eq projectId)
            where(table.transportType eq transportType)
            select(table.fetch(Fetchers.modbusConfig))
        }.execute().firstOrNull()

    /**
     * 加载网关pin配置。
     *
     * @param projectId 项目 ID。
     */
    private fun loadGatewayPinConfig(projectId: Long): ProjectGatewayPinConfig? =
        sql.createQuery(ProjectGatewayPinConfig::class) {
            where(table.project.id eq projectId)
            select(table)
        }.execute().firstOrNull()

    /**
     * 确保项目存在性。
     *
     * @param projectId 项目 ID。
     */
    private fun ensureProjectExists(projectId: Long) {
        val exists = sql.exists(Project::class) {
            where(table.id eq projectId)
        }
        if (!exists) {
            throw NotFoundException("Project not found")
        }
    }

    /**
     * 处理项目MQTT配置。
     */
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

    /**
     * 处理项目modbus服务端配置。
     */
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

    /**
     * 处理项目网关pin配置。
     */
    private fun ProjectGatewayPinConfig.toResponse(): ProjectGatewayPinConfigResponse =
        ProjectGatewayPinConfigResponse(
            id = id,
            faultIndicatorPin = faultIndicatorPin,
            runningIndicatorPin = runningIndicatorPin,
        )

    /**
     * 处理string。
     */
    private fun String?.cleanNullable(): String? =
        this?.trim()?.ifBlank { null }

    /**
     * 处理string。
     *
     * @param defaultValue 默认值。
     */
    private fun String.normalizePin(
        defaultValue: String,
    ): String {
        return trim().ifBlank { defaultValue }
    }

    /**
     * 处理string。
     */
    private fun String?.toDecimalOrNull(): BigDecimal? =
        this.cleanNullable()?.toBigDecimalOrNull()

    /**
     * 处理bigdecimal。
     */
    private fun BigDecimal?.toApiDecimal(): String? =
        this?.stripTrailingZeros()?.toPlainString()

    /**
     * 获取当前时间戳。
     */
    private fun now(): Long = System.currentTimeMillis()
}
