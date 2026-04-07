package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteAction
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteActionRequest

/**
 * 原始Controller: site.addzero.kcloud.plugins.hostconfig.routes.upload.ProjectUploadController
 * 基础路径: /api/host-config/v1
 */
interface ProjectUploadApi {

/**
 * getProjectUploadStatus
 * HTTP方法: GET
 * 路径: /api/host-config/v1/projects/{projectId}/upload-project
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse
 */
    @GET("/api/host-config/v1/projects/{projectId}/upload-project")
    suspend fun getProjectUploadStatus(
        @Path("projectId") projectId: kotlin.Long
    ): site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse

/**
 * uploadProject
 * HTTP方法: POST
 * 路径: /api/host-config/v1/projects/{projectId}/upload-project
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse
 */
    @POST("/api/host-config/v1/projects/{projectId}/upload-project")
    @Headers("Content-Type: application/json")
    suspend fun uploadProject(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse

/**
 * triggerProjectUploadRemoteAction
 * HTTP方法: POST
 * 路径: /api/host-config/v1/projects/{projectId}/upload-project/actions/{action}
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - action: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteAction (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteActionRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse
 */
    @POST("/api/host-config/v1/projects/{projectId}/upload-project/actions/{action}")
    @Headers("Content-Type: application/json")
    suspend fun triggerProjectUploadRemoteAction(
        @Path("projectId") projectId: kotlin.Long,
        @Path("action") action: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteAction,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteActionRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse

/**
 * downloadProjectBackup
 * HTTP方法: GET
 * 路径: /api/host-config/v1/projects/{projectId}/upload-project/backup
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Any
 */
    @GET("/api/host-config/v1/projects/{projectId}/upload-project/backup")
    suspend fun downloadProjectBackup(
        @Path("projectId") projectId: kotlin.Long
    ): kotlin.Any

}