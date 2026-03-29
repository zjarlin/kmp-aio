package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeBundlesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.routes.McuRuntime.kt
 * 基础路径: 
 */
interface McuRuntimeApi {

/**
 * listMcuRuntimeBundles
 * HTTP方法: GET
 * 路径: /api/mcu/runtime/bundles
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuRuntimeBundlesResponse
 */
    @GET("/api/mcu/runtime/bundles")    suspend fun listMcuRuntimeBundles(): site.addzero.kcloud.plugins.mcuconsole.McuRuntimeBundlesResponse

/**
 * getMcuRuntimeStatus
 * HTTP方法: GET
 * 路径: /api/mcu/runtime/status
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuRuntimeStatusResponse
 */
    @GET("/api/mcu/runtime/status")    suspend fun getMcuRuntimeStatus(): site.addzero.kcloud.plugins.mcuconsole.McuRuntimeStatusResponse

/**
 * ensureMcuRuntime
 * HTTP方法: POST
 * 路径: /api/mcu/runtime/ensure
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuRuntimeStatusResponse
 */
    @POST("/api/mcu/runtime/ensure")    suspend fun ensureMcuRuntime(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuRuntimeStatusResponse

}