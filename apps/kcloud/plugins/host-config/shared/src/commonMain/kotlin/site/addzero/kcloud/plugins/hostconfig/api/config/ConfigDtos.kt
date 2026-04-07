package site.addzero.kcloud.plugins.hostconfig.api.config

import java.math.BigDecimal
import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Serializable
data class ProjectMqttConfigRequest(
    val enabled: Boolean = false,
    val breakpointResume: Boolean = false,
    val gatewayName: String? = null,
    val vendor: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val topic: String? = null,
    val gatewayId: String? = null,
    val authEnabled: Boolean = false,
    val username: String? = null,
    val passwordEncrypted: String? = null,
    val tlsEnabled: Boolean = false,
    val certFileRef: String? = null,
    val clientId: String? = null,
    val keepAliveSec: Int? = null,
    val qos: Int? = null,
    val reportPeriodSec: Int? = null,
    val precision: BigDecimal? = null,
    val valueChangeRatioEnabled: Boolean = false,
    val cloudControlDisabled: Boolean = false,
)

@Serializable
data class ProjectMqttConfigResponse(
    val id: Long?,
    val enabled: Boolean,
    val breakpointResume: Boolean,
    val gatewayName: String?,
    val vendor: String?,
    val host: String?,
    val port: Int?,
    val topic: String?,
    val gatewayId: String?,
    val authEnabled: Boolean,
    val username: String?,
    val passwordEncrypted: String?,
    val tlsEnabled: Boolean,
    val certFileRef: String?,
    val clientId: String?,
    val keepAliveSec: Int?,
    val qos: Int?,
    val reportPeriodSec: Int?,
    val precision: BigDecimal?,
    val valueChangeRatioEnabled: Boolean,
    val cloudControlDisabled: Boolean,
)

@Serializable
data class ProjectModbusServerConfigRequest(
    val enabled: Boolean = false,
    val tcpPort: Int? = null,
    val portName: String? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: Parity? = null,
    val stationNo: Int? = null,
)

@Serializable
data class ProjectModbusServerConfigResponse(
    val id: Long?,
    val transportType: TransportType,
    val enabled: Boolean,
    val tcpPort: Int?,
    val portName: String?,
    val baudRate: Int?,
    val dataBits: Int?,
    val stopBits: Int?,
    val parity: Parity?,
    val stationNo: Int?,
)

@Serializable
data class ProjectUploadRequest(
    val ipAddress: String,
    val includeDriverConfig: Boolean = true,
    val includeFirmwareUpgrade: Boolean = false,
    val projectPath: String? = null,
    val selectedFileName: String? = null,
    val fastMode: Boolean = false,
)

@Serializable
data class ProjectUploadRemoteActionRequest(
    val ipAddress: String,
)

@Serializable
enum class ProjectUploadRemoteAction {
    BACKUP,
    RESTORE,
    DELETE,
    RESTART,
}

@Serializable
data class ProjectUploadOperationResponse(
    val projectId: Long,
    val operation: String,
    val progress: Int,
    val statusText: String,
    val detailText: String?,
    val ipAddress: String?,
    val projectPath: String?,
    val selectedFileName: String?,
    val includeDriverConfig: Boolean,
    val includeFirmwareUpgrade: Boolean,
    val fastMode: Boolean,
    val backupFileName: String?,
    val backupDownloadUrl: String?,
    val backupSizeBytes: Long?,
    val updatedAt: Long,
)
