package site.addzero.kcloud.plugins.hostconfig.api.config

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Serializable
/**
 * 表示项目MQTT配置请求参数。
 *
 * @property enabled 是否启用。
 * @property breakpointResume breakpointresume。
 * @property gatewayName 网关名称。
 * @property vendor vendor。
 * @property host 主机地址。
 * @property port 端口。
 * @property topic 主题。
 * @property gatewayId 网关 ID。
 * @property authEnabled auth启用状态。
 * @property username 用户名。
 * @property passwordEncrypted 密码encrypted。
 * @property tlsEnabled tls启用状态。
 * @property certFileRef certfileref。
 * @property clientId 客户端 ID。
 * @property keepAliveSec keepalivesec。
 * @property qos QoS 等级。
 * @property reportPeriodSec reportperiodsec。
 * @property precision precision。
 * @property valueChangeRatioEnabled 值changeratio启用状态。
 * @property cloudControlDisabled 云接入控制disabled。
 */
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
    val precision: String? = null,
    val valueChangeRatioEnabled: Boolean = false,
    val cloudControlDisabled: Boolean = false,
)

@Serializable
/**
 * 表示项目MQTT配置响应结果。
 *
 * @property id 主键 ID。
 * @property enabled 是否启用。
 * @property breakpointResume breakpointresume。
 * @property gatewayName 网关名称。
 * @property vendor vendor。
 * @property host 主机地址。
 * @property port 端口。
 * @property topic 主题。
 * @property gatewayId 网关 ID。
 * @property authEnabled auth启用状态。
 * @property username 用户名。
 * @property passwordEncrypted 密码encrypted。
 * @property tlsEnabled tls启用状态。
 * @property certFileRef certfileref。
 * @property clientId 客户端 ID。
 * @property keepAliveSec keepalivesec。
 * @property qos QoS 等级。
 * @property reportPeriodSec reportperiodsec。
 * @property precision precision。
 * @property valueChangeRatioEnabled 值changeratio启用状态。
 * @property cloudControlDisabled 云接入控制disabled。
 */
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
    val precision: String?,
    val valueChangeRatioEnabled: Boolean,
    val cloudControlDisabled: Boolean,
)

@Serializable
/**
 * 表示项目modbus服务端配置请求参数。
 *
 * @property enabled 是否启用。
 * @property tcpPort TCP端口。
 * @property portName 端口名。
 * @property baudRate 波特率。
 * @property dataBits 数据位。
 * @property stopBits 停止位。
 * @property parity 校验位。
 * @property stationNo stationno。
 */
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
/**
 * 表示项目modbus服务端配置响应结果。
 *
 * @property id 主键 ID。
 * @property transportType 传输类型。
 * @property enabled 是否启用。
 * @property tcpPort TCP端口。
 * @property portName 端口名。
 * @property baudRate 波特率。
 * @property dataBits 数据位。
 * @property stopBits 停止位。
 * @property parity 校验位。
 * @property stationNo stationno。
 */
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
/**
 * 表示项目网关pin配置请求参数。
 *
 * @property faultIndicatorPin faultindicator引脚。
 * @property runningIndicatorPin runningindicator引脚。
 */
data class ProjectGatewayPinConfigRequest(
    val faultIndicatorPin: String = "PA8",
    val runningIndicatorPin: String = "PA2",
)

@Serializable
/**
 * 表示项目网关pin配置响应结果。
 *
 * @property id 主键 ID。
 * @property faultIndicatorPin faultindicator引脚。
 * @property runningIndicatorPin runningindicator引脚。
 */
data class ProjectGatewayPinConfigResponse(
    val id: Long?,
    val faultIndicatorPin: String,
    val runningIndicatorPin: String,
)

@Serializable
/**
 * 表示导入本地工程 sqlite 文件请求。
 *
 * @property sourceFilePath 源文件绝对路径。
 */
data class ProjectSqliteImportRequest(
    val sourceFilePath: String,
)

@Serializable
/**
 * 表示本地工程 sqlite 文件结果。
 *
 * @property projectId 工程 ID，导入场景可为空。
 * @property projectName 工程名称，导入场景可为空。
 * @property fileName 文件名。
 * @property filePath 文件绝对路径。
 * @property dataDirectory 数据目录绝对路径。
 * @property sizeBytes 文件大小。
 * @property summaryText 摘要信息。
 */
data class ProjectSqliteFileResponse(
    val projectId: Long? = null,
    val projectName: String? = null,
    val fileName: String,
    val filePath: String,
    val dataDirectory: String,
    val sizeBytes: Long,
    val summaryText: String,
)
