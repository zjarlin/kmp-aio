package site.addzero.kcloud.plugins.system.rbac.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto
import site.addzero.kcloud.plugins.system.rbac.api.RbacRoleMutationRequest
import site.addzero.kcloud.plugins.system.rbac.api.RbacDeleteResult

/**
 * 原始文件: site.addzero.kcloud.plugins.system.rbac.routes.Rbac.kt
 * 基础路径: 
 */
interface RbacApi {

/**
 * listRbacRoles
 * HTTP方法: GET
 * 路径: /api/system/rbac/roles
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto>
 */
    @GET("/api/system/rbac/roles")
    suspend fun listRbacRoles(): kotlin.collections.List<site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto>

/**
 * createRbacRole
 * HTTP方法: POST
 * 路径: /api/system/rbac/roles
 * 参数:
 *   - request: site.addzero.kcloud.plugins.system.rbac.api.RbacRoleMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto
 */
    @POST("/api/system/rbac/roles")
    @Headers("Content-Type: application/json")
    suspend fun createRbacRole(
        @Body request: site.addzero.kcloud.plugins.system.rbac.api.RbacRoleMutationRequest
    ): site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto

/**
 * updateRbacRole
 * HTTP方法: PUT
 * 路径: /api/system/rbac/roles/{roleId}
 * 参数:
 *   - roleId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.rbac.api.RbacRoleMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto
 */
    @PUT("/api/system/rbac/roles/{roleId}")
    @Headers("Content-Type: application/json")
    suspend fun updateRbacRole(
        @Path("roleId") roleId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.rbac.api.RbacRoleMutationRequest
    ): site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto

/**
 * deleteRbacRole
 * HTTP方法: DELETE
 * 路径: /api/system/rbac/roles/{roleId}
 * 参数:
 *   - roleId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.system.rbac.api.RbacDeleteResult
 */
    @DELETE("/api/system/rbac/roles/{roleId}")
    suspend fun deleteRbacRole(
        @Path("roleId") roleId: kotlin.Long
    ): site.addzero.kcloud.plugins.system.rbac.api.RbacDeleteResult

}