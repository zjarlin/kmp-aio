package site.addzero.kcloud.plugins.system.rbac.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.system.rbac.RbacRoleService
import site.addzero.kcloud.plugins.system.rbac.api.RbacDeleteResult
import site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto
import site.addzero.kcloud.plugins.system.rbac.api.RbacRoleMutationRequest

/**
 * RBAC 角色服务端路由定义，同时作为客户端 API 生成源。
 */
/**
 * 列出 RBAC 角色。
 */
@GetMapping("/api/system/rbac/roles")
fun listRbacRoles(): List<RbacRoleDto> {
    return service().listRoles()
}

/**
 * 新建 RBAC 角色。
 */
@PostMapping("/api/system/rbac/roles")
fun createRbacRole(
    @RequestBody request: RbacRoleMutationRequest,
): RbacRoleDto {
    return service().createRole(request)
}

/**
 * 更新 RBAC 角色。
 */
@PutMapping("/api/system/rbac/roles/{roleId}")
fun updateRbacRole(
    @PathVariable("roleId") roleId: Long,
    @RequestBody request: RbacRoleMutationRequest,
): RbacRoleDto {
    return service().updateRole(roleId, request)
}

/**
 * 删除 RBAC 角色。
 */
@DeleteMapping("/api/system/rbac/roles/{roleId}")
fun deleteRbacRole(
    @PathVariable("roleId") roleId: Long,
): RbacDeleteResult {
    return service().deleteRole(roleId)
}

private fun service(): RbacRoleService {
    return KoinPlatform.getKoin().get()
}
