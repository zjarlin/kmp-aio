package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigRequest

/**
 * 原始Controller: site.addzero.kcloud.plugins.hostconfig.routes.cloud.CloudAccessController
 * 基础路径: /api/host-config/v1
 */
interface CloudAccessApi {

/**
 * getMqttConfig
 * HTTP方法: GET
 * 路径: /api/host-config/v1/projects/{projectId}/mqtt-config
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse
 */
    @GET("/api/host-config/v1/projects/{projectId}/mqtt-config")    suspend fun getMqttConfig(
        @Path("projectId") projectId: kotlin.Long
    ): site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse

/**
 * updateMqttConfig
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/projects/{projectId}/mqtt-config
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse
 */
    @PUT("/api/host-config/v1/projects/{projectId}/mqtt-config")    suspend fun updateMqttConfig(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse

}