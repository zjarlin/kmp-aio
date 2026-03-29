package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.vibepocket.routes.HistoryResponse
import site.addzero.kcloud.vibepocket.routes.HistorySaveRequest

/**
 * 原始文件: site.addzero.kcloud.vibepocket.routes.History.kt
 * 基础路径: 
 */
interface HistoryApi {

/**
 * getHistory
 * HTTP方法: GET
 * 路径: /api/suno/history
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.vibepocket.routes.HistoryResponse>
 */
    @GET("/api/suno/history")    suspend fun getHistory(): kotlin.collections.List<site.addzero.kcloud.vibepocket.routes.HistoryResponse>

/**
 * saveHistory
 * HTTP方法: POST
 * 路径: /api/suno/history
 * 参数:
 *   - request: site.addzero.kcloud.vibepocket.routes.HistorySaveRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.vibepocket.routes.HistoryResponse
 */
    @POST("/api/suno/history")    suspend fun saveHistory(
        @Body request: site.addzero.kcloud.vibepocket.routes.HistorySaveRequest
    ): site.addzero.kcloud.vibepocket.routes.HistoryResponse

}