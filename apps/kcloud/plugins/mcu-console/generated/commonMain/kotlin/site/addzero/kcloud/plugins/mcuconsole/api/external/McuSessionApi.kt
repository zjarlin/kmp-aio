package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.McuPortsResponse
import site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
import site.addzero.kcloud.plugins.mcuconsole.McuEventBatchResponse
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.McuResetRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSignalRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionLinesRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.routes.McuSession.kt
 * 基础路径: 
 */
interface McuSessionApi {

/**
 * listMcuPorts
 * HTTP方法: GET
 * 路径: /api/mcu/ports
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuPortsResponse
 */
    @GET("/api/mcu/ports")
    suspend fun listMcuPorts(): site.addzero.kcloud.plugins.mcuconsole.McuPortsResponse

/**
 * getMcuSession
 * HTTP方法: GET
 * 路径: /api/mcu/session
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
 */
    @GET("/api/mcu/session")
    suspend fun getMcuSession(): site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot

/**
 * readMcuEvents
 * HTTP方法: GET
 * 路径: /api/mcu/events
 * 参数:
 *   - afterSeq: kotlin.Long? (RequestParam)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuEventBatchResponse
 */
    @GET("/api/mcu/events")
    suspend fun readMcuEvents(
        @Query("afterSeq") afterSeq: kotlin.Long?
    ): site.addzero.kcloud.plugins.mcuconsole.McuEventBatchResponse

/**
 * openMcuSession
 * HTTP方法: POST
 * 路径: /api/mcu/session/open
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
 */
    @POST("/api/mcu/session/open")
    @Headers("Content-Type: application/json")
    suspend fun openMcuSession(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot

/**
 * closeMcuSession
 * HTTP方法: POST
 * 路径: /api/mcu/session/close
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
 */
    @POST("/api/mcu/session/close")
    suspend fun closeMcuSession(): site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot

/**
 * resetMcuSession
 * HTTP方法: POST
 * 路径: /api/mcu/session/reset
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuResetRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
 */
    @POST("/api/mcu/session/reset")
    @Headers("Content-Type: application/json")
    suspend fun resetMcuSession(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuResetRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot

/**
 * updateMcuSignals
 * HTTP方法: POST
 * 路径: /api/mcu/session/signals
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuSignalRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
 */
    @POST("/api/mcu/session/signals")
    @Headers("Content-Type: application/json")
    suspend fun updateMcuSignals(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuSignalRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot

/**
 * readMcuRecentLines
 * HTTP方法: POST
 * 路径: /api/mcu/session/lines
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuSessionLinesRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuEventBatchResponse
 */
    @POST("/api/mcu/session/lines")
    @Headers("Content-Type: application/json")
    suspend fun readMcuRecentLines(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuSessionLinesRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuEventBatchResponse

}