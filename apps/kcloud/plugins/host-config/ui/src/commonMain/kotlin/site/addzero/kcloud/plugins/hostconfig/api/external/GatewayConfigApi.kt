package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigRequest

/**
 * 原始Controller: site.addzero.kcloud.plugins.hostconfig.routes.gateway.GatewayConfigController
 * 基础路径: /api/host-config/v1
 */
interface GatewayConfigApi {

/**
 * getModbusServerConfig
 * HTTP方法: GET
 * 路径: /api/host-config/v1/projects/{projectId}/modbus-servers/{transportType}
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - transportType: site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse
 */
    @GET("/api/host-config/v1/projects/{projectId}/modbus-servers/{transportType}")
    suspend fun getModbusServerConfig(
        @Path("projectId") projectId: kotlin.Long,
        @Path("transportType") transportType: site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType
    ): site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse

/**
 * updateModbusServerConfig
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/projects/{projectId}/modbus-servers/{transportType}
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - transportType: site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse
 */
    @PUT("/api/host-config/v1/projects/{projectId}/modbus-servers/{transportType}")
    @Headers("Content-Type: application/json")
    suspend fun updateModbusServerConfig(
        @Path("projectId") projectId: kotlin.Long,
        @Path("transportType") transportType: site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse

}