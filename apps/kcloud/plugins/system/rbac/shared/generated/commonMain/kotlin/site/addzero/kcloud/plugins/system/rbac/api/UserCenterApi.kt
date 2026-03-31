package site.addzero.kcloud.plugins.system.rbac.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.system.rbac.api.UserProfileDto
import site.addzero.kcloud.plugins.system.rbac.api.UserProfileUpdateRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.system.rbac.routes.UserCenter.kt
 * 基础路径: 
 */
interface UserCenterApi {

/**
 * getCurrentUserProfile
 * HTTP方法: GET
 * 路径: /api/system/user/profile
 * 返回类型: site.addzero.kcloud.plugins.system.rbac.api.UserProfileDto
 */
    @GET("/api/system/user/profile")
    suspend fun getCurrentUserProfile(): site.addzero.kcloud.plugins.system.rbac.api.UserProfileDto

/**
 * saveCurrentUserProfile
 * HTTP方法: PUT
 * 路径: /api/system/user/profile
 * 参数:
 *   - request: site.addzero.kcloud.plugins.system.rbac.api.UserProfileUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.rbac.api.UserProfileDto
 */
    @PUT("/api/system/user/profile")
    @Headers("Content-Type: application/json")
    suspend fun saveCurrentUserProfile(
        @Body request: site.addzero.kcloud.plugins.system.rbac.api.UserProfileUpdateRequest
    ): site.addzero.kcloud.plugins.system.rbac.api.UserProfileDto

}