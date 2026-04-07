package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.LinkExistingProtocolRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModulePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DevicePositionUpdateRequest

/**
 * 原始Controller: site.addzero.kcloud.plugins.hostconfig.routes.project.ProjectController
 * 基础路径: /api/host-config/v1
 */
interface ProjectApi {

/**
 * listProjects
 * HTTP方法: GET
 * 路径: /api/host-config/v1/projects
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse>
 */
    @GET("/api/host-config/v1/projects")
    suspend fun listProjects(): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse>

/**
 * createProject
 * HTTP方法: POST
 * 路径: /api/host-config/v1/projects
 * 参数:
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
 */
    @POST("/api/host-config/v1/projects")
    @Headers("Content-Type: application/json")
    suspend fun createProject(
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse

/**
 * getProject
 * HTTP方法: GET
 * 路径: /api/host-config/v1/projects/{projectId}
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
 */
    @GET("/api/host-config/v1/projects/{projectId}")
    suspend fun getProject(
        @Path("projectId") projectId: kotlin.Long
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse

/**
 * updateProject
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/projects/{projectId}
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
 */
    @PUT("/api/host-config/v1/projects/{projectId}")
    @Headers("Content-Type: application/json")
    suspend fun updateProject(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse

/**
 * updateProjectPosition
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/projects/{projectId}/position
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectPositionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
 */
    @PUT("/api/host-config/v1/projects/{projectId}/position")
    @Headers("Content-Type: application/json")
    suspend fun updateProjectPosition(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectPositionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse

/**
 * deleteProject
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/projects/{projectId}
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/projects/{projectId}")
    suspend fun deleteProject(
        @Path("projectId") projectId: kotlin.Long
    ): kotlin.Unit

/**
 * getProjectTree
 * HTTP方法: GET
 * 路径: /api/host-config/v1/projects/{projectId}/tree
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
 */
    @GET("/api/host-config/v1/projects/{projectId}/tree")
    suspend fun getProjectTree(
        @Path("projectId") projectId: kotlin.Long
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse

/**
 * listProtocols
 * HTTP方法: GET
 * 路径: /api/host-config/v1/protocols
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCatalogItemResponse>
 */
    @GET("/api/host-config/v1/protocols")
    suspend fun listProtocols(): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCatalogItemResponse>

/**
 * createProtocol
 * HTTP方法: POST
 * 路径: /api/host-config/v1/projects/{projectId}/protocols
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
 */
    @POST("/api/host-config/v1/projects/{projectId}/protocols")
    @Headers("Content-Type: application/json")
    suspend fun createProtocol(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse

/**
 * linkProtocol
 * HTTP方法: POST
 * 路径: /api/host-config/v1/projects/{projectId}/protocol-links
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.LinkExistingProtocolRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
 */
    @POST("/api/host-config/v1/projects/{projectId}/protocol-links")
    @Headers("Content-Type: application/json")
    suspend fun linkProtocol(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.LinkExistingProtocolRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse

/**
 * updateProtocol
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/protocols/{protocolId}
 * 参数:
 *   - protocolId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
 */
    @PUT("/api/host-config/v1/protocols/{protocolId}")
    @Headers("Content-Type: application/json")
    suspend fun updateProtocol(
        @Path("protocolId") protocolId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse

/**
 * updateProtocolPosition
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/protocols/{protocolId}/position
 * 参数:
 *   - protocolId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolPositionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
 */
    @PUT("/api/host-config/v1/protocols/{protocolId}/position")
    @Headers("Content-Type: application/json")
    suspend fun updateProtocolPosition(
        @Path("protocolId") protocolId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolPositionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse

/**
 * deleteProtocol
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/projects/{projectId}/protocols/{protocolId}
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - protocolId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/projects/{projectId}/protocols/{protocolId}")
    suspend fun deleteProtocol(
        @Path("projectId") projectId: kotlin.Long,
        @Path("protocolId") protocolId: kotlin.Long
    ): kotlin.Unit

/**
 * createModule
 * HTTP方法: POST
 * 路径: /api/host-config/v1/protocols/{protocolId}/modules
 * 参数:
 *   - protocolId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse
 */
    @POST("/api/host-config/v1/protocols/{protocolId}/modules")
    @Headers("Content-Type: application/json")
    suspend fun createModule(
        @Path("protocolId") protocolId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse

/**
 * createProjectModule
 * HTTP方法: POST
 * 路径: /api/host-config/v1/projects/{projectId}/modules
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse
 */
    @POST("/api/host-config/v1/projects/{projectId}/modules")
    @Headers("Content-Type: application/json")
    suspend fun createProjectModule(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse

/**
 * updateModule
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/modules/{moduleId}
 * 参数:
 *   - moduleId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse
 */
    @PUT("/api/host-config/v1/modules/{moduleId}")
    @Headers("Content-Type: application/json")
    suspend fun updateModule(
        @Path("moduleId") moduleId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse

/**
 * updateModulePosition
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/modules/{moduleId}/position
 * 参数:
 *   - moduleId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.ModulePositionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse
 */
    @PUT("/api/host-config/v1/modules/{moduleId}/position")
    @Headers("Content-Type: application/json")
    suspend fun updateModulePosition(
        @Path("moduleId") moduleId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.ModulePositionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse

/**
 * deleteModule
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/modules/{moduleId}
 * 参数:
 *   - moduleId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/modules/{moduleId}")
    suspend fun deleteModule(
        @Path("moduleId") moduleId: kotlin.Long
    ): kotlin.Unit

/**
 * createDevice
 * HTTP方法: POST
 * 路径: /api/host-config/v1/modules/{moduleId}/devices
 * 参数:
 *   - moduleId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse
 */
    @POST("/api/host-config/v1/modules/{moduleId}/devices")
    @Headers("Content-Type: application/json")
    suspend fun createDevice(
        @Path("moduleId") moduleId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse

/**
 * updateDevice
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/devices/{deviceId}
 * 参数:
 *   - deviceId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse
 */
    @PUT("/api/host-config/v1/devices/{deviceId}")
    @Headers("Content-Type: application/json")
    suspend fun updateDevice(
        @Path("deviceId") deviceId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse

/**
 * updateDevicePosition
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/devices/{deviceId}/position
 * 参数:
 *   - deviceId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.project.DevicePositionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse
 */
    @PUT("/api/host-config/v1/devices/{deviceId}/position")
    @Headers("Content-Type: application/json")
    suspend fun updateDevicePosition(
        @Path("deviceId") deviceId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.project.DevicePositionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse

/**
 * deleteDevice
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/devices/{deviceId}
 * 参数:
 *   - deviceId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/devices/{deviceId}")
    suspend fun deleteDevice(
        @Path("deviceId") deviceId: kotlin.Long
    ): kotlin.Unit

}