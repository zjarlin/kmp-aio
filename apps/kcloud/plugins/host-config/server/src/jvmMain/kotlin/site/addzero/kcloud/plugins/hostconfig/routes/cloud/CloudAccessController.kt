package site.addzero.kcloud.plugins.hostconfig.routes.cloud

import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse
import site.addzero.kcloud.plugins.hostconfig.service.ProjectConfigService

/**
 * 宿主配置里的云接入路由，当前聚焦 MQTT 配置读写。
 */
@Single
@RestController
@RequestMapping("/api/host-config/v1")
class CloudAccessController(
    private val projectConfigService: ProjectConfigService,
) {
    /** 读取指定工程的 MQTT 配置，用于云接入页初始化。 */
    @GetMapping("/projects/{projectId}/mqtt-config")
    fun getMqttConfig(
        @PathVariable projectId: Long,
    ): ProjectMqttConfigResponse = projectConfigService.getMqttConfig(projectId)

    /** 保存 MQTT 配置，沿用工程维度的统一配置入口。 */
    @PutMapping("/projects/{projectId}/mqtt-config")
    fun updateMqttConfig(
        @PathVariable projectId: Long,
        @RequestBody request: ProjectMqttConfigRequest,
    ): ProjectMqttConfigResponse = projectConfigService.updateMqttConfig(projectId, request)
}
