package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProfileIso

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.routes.McuSettings.kt
 * 基础路径: 
 */
interface McuSettingsApi {

/**
 * getMcuDeviceProfile
 * HTTP方法: GET
 * 路径: /api/mcu/device-profile
 * 参数:
 *   - deviceKey: kotlin.String? (RequestParam)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso
 */
    @GET("/api/mcu/device-profile")    suspend fun getMcuDeviceProfile(
        @Query("deviceKey") deviceKey: kotlin.String?
    ): site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso

/**
 * listMcuTransportProfiles
 * HTTP方法: GET
 * 路径: /api/mcu/transport-profiles
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuTransportProfilesResponse
 */
    @GET("/api/mcu/transport-profiles")    suspend fun listMcuTransportProfiles(): site.addzero.kcloud.plugins.mcuconsole.McuTransportProfilesResponse

/**
 * saveMcuDeviceProfile
 * HTTP方法: POST
 * 路径: /api/mcu/device-profile
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso
 */
    @POST("/api/mcu/device-profile")    suspend fun saveMcuDeviceProfile(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso
    ): site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso

/**
 * saveMcuTransportProfile
 * HTTP方法: POST
 * 路径: /api/mcu/transport-profile
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuTransportProfileIso (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuTransportProfileIso
 */
    @POST("/api/mcu/transport-profile")    suspend fun saveMcuTransportProfile(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuTransportProfileIso
    ): site.addzero.kcloud.plugins.mcuconsole.McuTransportProfileIso

/**
 * deleteMcuTransportProfile
 * HTTP方法: POST
 * 路径: /api/mcu/transport-profile/delete
 * 参数:
 *   - profileKey: kotlin.String (RequestParam)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuTransportProfilesResponse
 */
    @POST("/api/mcu/transport-profile/delete")    suspend fun deleteMcuTransportProfile(
        @Query("profileKey") profileKey: kotlin.String
    ): site.addzero.kcloud.plugins.mcuconsole.McuTransportProfilesResponse

}