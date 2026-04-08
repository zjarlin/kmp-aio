package site.addzero.kcloud.plugins.hostconfig.routes.gateway

import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType
import site.addzero.kcloud.plugins.hostconfig.service.ProjectConfigService

/**
 * 宿主配置里的网关配置路由，负责 Modbus TCP/RTU 服务端参数和项目级下位机引脚配置。
 */
@Single
@RestController
@RequestMapping("/api/host-config/v1")
class GatewayConfigController(
    private val projectConfigService: ProjectConfigService,
) {
    /** 读取指定工程和传输类型对应的网关配置。 */
    @GetMapping("/projects/{projectId}/modbus-servers/{transportType}")
    fun getModbusServerConfig(
        @PathVariable projectId: Long,
        @PathVariable transportType: TransportType,
    ): ProjectModbusServerConfigResponse =
        projectConfigService.getModbusServerConfig(projectId, transportType)

    /** 保存指定传输类型的网关配置，保持路径语义稳定。 */
    @PutMapping("/projects/{projectId}/modbus-servers/{transportType}")
    fun updateModbusServerConfig(
        @PathVariable projectId: Long,
        @PathVariable transportType: TransportType,
        @RequestBody request: ProjectModbusServerConfigRequest,
    ): ProjectModbusServerConfigResponse =
        projectConfigService.updateModbusServerConfig(projectId, transportType, request)

    /** 读取项目级下位机引脚配置。 */
    @GetMapping("/projects/{projectId}/gateway-pin-config")
    fun getGatewayPinConfig(
        @PathVariable projectId: Long,
    ): ProjectGatewayPinConfigResponse =
        projectConfigService.getGatewayPinConfig(projectId)

    /** 保存项目级下位机引脚配置。 */
    @PutMapping("/projects/{projectId}/gateway-pin-config")
    fun updateGatewayPinConfig(
        @PathVariable projectId: Long,
        @RequestBody request: ProjectGatewayPinConfigRequest,
    ): ProjectGatewayPinConfigResponse =
        projectConfigService.updateGatewayPinConfig(projectId, request)
}
