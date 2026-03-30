package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoResponse
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoPollRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.routes.McuDeviceInfo.kt
 * 基础路径: 
 */
interface McuDeviceInfoApi {

/**
 * getMcuDeviceInfo
 * HTTP方法: GET
 * 路径: /api/mcu/device-info
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoResponse
 */
    @GET("/api/mcu/device-info")    suspend fun getMcuDeviceInfo(): site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoResponse

/**
 * pollMcuDeviceInfo
 * HTTP方法: POST
 * 路径: /api/mcu/device-info/poll
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoPollRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoResponse
 */
    @POST("/api/mcu/device-info/poll")    suspend fun pollMcuDeviceInfo(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoPollRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoResponse

}