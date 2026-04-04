package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStopRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.routes.McuScript.kt
 * 基础路径: 
 */
interface McuScriptApi {

/**
 * getMcuScriptStatus
 * HTTP方法: GET
 * 路径: /api/mcu/script/status
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
 */
    @GET("/api/mcu/script/status")
    suspend fun getMcuScriptStatus(): site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse

/**
 * executeMcuScript
 * HTTP方法: POST
 * 路径: /api/mcu/script/execute
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
 */
    @POST("/api/mcu/script/execute")
    @Headers("Content-Type: application/json")
    suspend fun executeMcuScript(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse

/**
 * stopMcuScript
 * HTTP方法: POST
 * 路径: /api/mcu/script/stop
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuScriptStopRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
 */
    @POST("/api/mcu/script/stop")
    @Headers("Content-Type: application/json")
    suspend fun stopMcuScript(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuScriptStopRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse

}