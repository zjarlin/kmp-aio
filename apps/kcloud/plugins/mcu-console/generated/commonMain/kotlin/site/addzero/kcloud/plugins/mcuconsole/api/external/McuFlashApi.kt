package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.routes.McuFlash.kt
 * 基础路径: 
 */
interface McuFlashApi {

/**
 * listMcuFlashProfiles
 * HTTP方法: GET
 * 路径: /api/mcu/flash/profiles
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
 */
    @GET("/api/mcu/flash/profiles")    suspend fun listMcuFlashProfiles(): site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse

/**
 * getMcuFlashStatus
 * HTTP方法: GET
 * 路径: /api/mcu/flash/status
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
 */
    @GET("/api/mcu/flash/status")    suspend fun getMcuFlashStatus(): site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse

/**
 * startMcuFlash
 * HTTP方法: POST
 * 路径: /api/mcu/flash/start
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
 */
    @POST("/api/mcu/flash/start")    suspend fun startMcuFlash(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse

}