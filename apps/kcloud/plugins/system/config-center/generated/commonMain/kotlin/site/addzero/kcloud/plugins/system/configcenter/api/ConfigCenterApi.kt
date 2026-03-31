package site.addzero.kcloud.plugins.system.configcenter.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueWriteRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.system.configcenter.routes.ConfigCenter.kt
 * 基础路径: 
 */
interface ConfigCenterApi {

/**
 * getConfigCenterValue
 * HTTP方法: GET
 * 路径: /api/system/config-center/value
 * 参数:
 *   - namespace: kotlin.String (RequestParam)
 *   - key: kotlin.String (RequestParam)
 *   - active: kotlin.String? (RequestParam)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto
 */
    @GET("/api/system/config-center/value")
    suspend fun getConfigCenterValue(
        @Query("namespace") namespace: kotlin.String,
        @Query("key") key: kotlin.String,
        @Query("active") active: kotlin.String?
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto

/**
 * putConfigCenterValue
 * HTTP方法: PUT
 * 路径: /api/system/config-center/value
 * 参数:
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueWriteRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto
 */
    @PUT("/api/system/config-center/value")
    @Headers("Content-Type: application/json")
    suspend fun putConfigCenterValue(
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueWriteRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto

}