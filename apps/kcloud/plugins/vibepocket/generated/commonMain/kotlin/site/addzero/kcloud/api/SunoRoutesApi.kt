package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.vibepocket.dto.TaskListResult
import site.addzero.kcloud.vibepocket.dto.TaskResult
import site.addzero.kcloud.vibepocket.dto.GenerateRequest
import kotlinx.serialization.json.JsonElement

/**
 * 原始文件: site.addzero.kcloud.vibepocket.routes.SunoRoutes.kt
 * 基础路径: 
 */
interface SunoRoutesApi {

/**
 * listTasks
 * HTTP方法: GET
 * 路径: /api/suno/tasks
 * 返回类型: site.addzero.kcloud.vibepocket.dto.TaskListResult
 */
    @GET("/api/suno/tasks")
    suspend fun listTasks(): site.addzero.kcloud.vibepocket.dto.TaskListResult

/**
 * readTask
 * HTTP方法: GET
 * 路径: /api/suno/tasks/{taskId}
 * 参数:
 *   - taskId: kotlin.String (PathVariable)
 * 返回类型: site.addzero.kcloud.vibepocket.dto.TaskResult
 */
    @GET("/api/suno/tasks/{taskId}")
    suspend fun readTask(
        @Path("taskId") taskId: kotlin.String
    ): site.addzero.kcloud.vibepocket.dto.TaskResult

/**
 * generateMusic
 * HTTP方法: POST
 * 路径: /api/suno/generate
 * 参数:
 *   - request: site.addzero.kcloud.vibepocket.dto.GenerateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.vibepocket.dto.TaskResult
 */
    @POST("/api/suno/generate")
    @Headers("Content-Type: application/json")
    suspend fun generateMusic(
        @Body request: site.addzero.kcloud.vibepocket.dto.GenerateRequest
    ): site.addzero.kcloud.vibepocket.dto.TaskResult

/**
 * handleSunoCallback
 * HTTP方法: POST
 * 路径: /api/suno/callback/{kind}
 * 参数:
 *   - kind: kotlin.String (PathVariable)
 *   - requestId: kotlin.String? (RequestParam)
 *   - payload: kotlinx.serialization.json.JsonElement (RequestBody)
 * 返回类型: kotlin.String
 */
    @POST("/api/suno/callback/{kind}")
    @Headers("Content-Type: application/json")
    suspend fun handleSunoCallback(
        @Path("kind") kind: kotlin.String,
        @Query("requestId") requestId: kotlin.String?,
        @Body payload: kotlinx.serialization.json.JsonElement
    ): kotlin.String

}