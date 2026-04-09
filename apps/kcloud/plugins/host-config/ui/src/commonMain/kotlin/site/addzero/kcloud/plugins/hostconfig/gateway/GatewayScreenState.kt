package site.addzero.kcloud.plugins.hostconfig.gateway

import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

/**
 * 表示网关界面状态。
 *
 * @property loading 加载状态。
 * @property busy 繁忙状态。
 * @property errorMessage 错误消息。
 * @property noticeMessage 提示消息。
 * @property projects 项目列表。
 * @property selectedProjectId 选中项目 ID。
 * @property selectedTransport 选中的传输方式。
 * @property pinConfig 引脚配置。
 * @property tcpConfig TCP 配置。
 * @property rtuConfig RTU 配置。
 */
data class GatewayScreenState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
    val projects: List<ProjectResponse> = emptyList(),
    val selectedProjectId: Long? = null,
    val selectedTransport: TransportType = TransportType.TCP,
    val pinConfig: ProjectGatewayPinConfigResponse = defaultGatewayPinConfig(),
    val tcpConfig: ProjectModbusServerConfigResponse = defaultGatewayConfig(TransportType.TCP),
    val rtuConfig: ProjectModbusServerConfigResponse = defaultGatewayConfig(TransportType.RTU),
) {
    val selectedProject: ProjectResponse?
        get() = projects.firstOrNull { item -> item.id == selectedProjectId }

    val activeConfig: ProjectModbusServerConfigResponse
        get() = if (selectedTransport == TransportType.TCP) tcpConfig else rtuConfig
}

/**
 * 处理默认网关配置。
 *
 * @param transportType 传输类型。
 */
fun defaultGatewayConfig(
    transportType: TransportType,
): ProjectModbusServerConfigResponse {
    return ProjectModbusServerConfigResponse(
        id = null,
        transportType = transportType,
        enabled = false,
        tcpPort = if (transportType == TransportType.TCP) 502 else null,
        portName = if (transportType == TransportType.RTU) "COM1" else null,
        baudRate = if (transportType == TransportType.RTU) 9600 else null,
        dataBits = if (transportType == TransportType.RTU) 8 else null,
        stopBits = if (transportType == TransportType.RTU) 1 else null,
        parity = null,
        stationNo = if (transportType == TransportType.RTU) 1 else null,
    )
}

/**
 * 处理默认网关pin配置。
 */
fun defaultGatewayPinConfig(): ProjectGatewayPinConfigResponse {
    return ProjectGatewayPinConfigResponse(
        id = null,
        faultIndicatorPin = "PA8",
        runningIndicatorPin = "PA2",
    )
}
