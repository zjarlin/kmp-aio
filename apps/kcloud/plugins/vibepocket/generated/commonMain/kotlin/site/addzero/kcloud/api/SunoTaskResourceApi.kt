package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.vibepocket.routes.SunoTaskResourceResponse
import site.addzero.kcloud.vibepocket.routes.SunoTaskResourceSaveRequest

/**
 * 原始文件: site.addzero.kcloud.vibepocket.routes.SunoTaskResource.kt
 * 基础路径: 
 */
interface SunoTaskResourceApi {

/**
 * list
 * HTTP方法: GET
 * 路径: /api/suno/resources
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.vibepocket.routes.SunoTaskResourceResponse>
 */
    @GET("/api/suno/resources")    suspend fun list(): kotlin.collections.List<site.addzero.kcloud.vibepocket.routes.SunoTaskResourceResponse>

/**
 * get
 * HTTP方法: GET
 * 路径: /api/suno/resources/{taskId}
 * 参数:
 *   - taskId: kotlin.String (PathVariable)
 * 返回类型: site.addzero.kcloud.vibepocket.routes.SunoTaskResourceResponse
 */
    @GET("/api/suno/resources/{taskId}")    suspend fun get(
        @Path("taskId") taskId: kotlin.String
    ): site.addzero.kcloud.vibepocket.routes.SunoTaskResourceResponse

/**
 * save
 * HTTP方法: POST
 * 路径: /api/suno/resources
 * 参数:
 *   - request: site.addzero.kcloud.vibepocket.routes.SunoTaskResourceSaveRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.vibepocket.routes.SunoTaskResourceResponse
 */
    @POST("/api/suno/resources")    suspend fun save(
        @Body request: site.addzero.kcloud.vibepocket.routes.SunoTaskResourceSaveRequest
    ): site.addzero.kcloud.vibepocket.routes.SunoTaskResourceResponse

}