package site.addzero.kcloud.plugins.hostconfig.gateway

import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

data class GatewayScreenState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
    val projects: List<ProjectResponse> = emptyList(),
    val selectedProjectId: Long? = null,
    val selectedTransport: TransportType = TransportType.TCP,
    val tcpConfig: ProjectModbusServerConfigResponse = defaultGatewayConfig(TransportType.TCP),
    val rtuConfig: ProjectModbusServerConfigResponse = defaultGatewayConfig(TransportType.RTU),
) {
    val selectedProject: ProjectResponse?
        get() = projects.firstOrNull { item -> item.id == selectedProjectId }

    val activeConfig: ProjectModbusServerConfigResponse
        get() = if (selectedTransport == TransportType.TCP) tcpConfig else rtuConfig
}

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
