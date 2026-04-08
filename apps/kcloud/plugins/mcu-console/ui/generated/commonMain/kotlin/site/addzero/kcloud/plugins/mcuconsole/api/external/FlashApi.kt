package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProbesResponse
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashResetRequest

/**
 * 原始Controller: site.addzero.kcloud.plugins.mcuconsole.routes.FlashController
 * 基础路径: /api/mcu/flash
 */
interface FlashApi {

/**
 * listProfiles
 * HTTP方法: GET
 * 路径: /api/mcu/flash/profiles
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProfilesResponse
 */
    @GET("/api/mcu/flash/profiles")
    suspend fun listProfiles(): site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProfilesResponse

/**
 * listProbes
 * HTTP方法: GET
 * 路径: /api/mcu/flash/probes
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProbesResponse
 */
    @GET("/api/mcu/flash/probes")
    suspend fun listProbes(): site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProbesResponse

/**
 * startFlash
 * HTTP方法: POST
 * 路径: /api/mcu/flash/start
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashStatusResponse
 */
    @POST("/api/mcu/flash/start")
    @Headers("Content-Type: application/json")
    suspend fun startFlash(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashRequest
    ): site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashStatusResponse

/**
 * getStatus
 * HTTP方法: GET
 * 路径: /api/mcu/flash/status
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashStatusResponse
 */
    @GET("/api/mcu/flash/status")
    suspend fun getStatus(): site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashStatusResponse

/**
 * reset
 * HTTP方法: POST
 * 路径: /api/mcu/flash/reset
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashResetRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashStatusResponse
 */
    @POST("/api/mcu/flash/reset")
    @Headers("Content-Type: application/json")
    suspend fun reset(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashResetRequest
    ): site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashStatusResponse

}