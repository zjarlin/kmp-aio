package site.addzero.kcloud.system.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.system.api.UserProfileDto
import site.addzero.kcloud.system.api.UserProfileUpdateRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.rbac.routes.UserCenter.kt
 * 基础路径: 
 */
interface UserCenterApi {

/**
 * getCurrentUserProfile
 * HTTP方法: GET
 * 路径: /api/system/user/profile
 * 返回类型: site.addzero.kcloud.system.api.UserProfileDto
 */
    @GET("/api/system/user/profile")    suspend fun getCurrentUserProfile(): site.addzero.kcloud.system.api.UserProfileDto

/**
 * saveCurrentUserProfile
 * HTTP方法: PUT
 * 路径: /api/system/user/profile
 * 参数:
 *   - request: site.addzero.kcloud.system.api.UserProfileUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.system.api.UserProfileDto
 */
    @PUT("/api/system/user/profile")    suspend fun saveCurrentUserProfile(
        @Body request: site.addzero.kcloud.system.api.UserProfileUpdateRequest
    ): site.addzero.kcloud.system.api.UserProfileDto

}