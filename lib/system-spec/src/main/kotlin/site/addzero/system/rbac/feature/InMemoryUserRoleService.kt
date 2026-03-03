package site.addzero.system.rbac.feature

import site.addzero.system.common.exception.ResourceNotFoundException
import site.addzero.system.rbac.dto.UserRoleDTO
import site.addzero.system.rbac.spi.RoleSpi
import site.addzero.system.rbac.spi.UserRoleSpi
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于内存的用户角色服务默认实现
 */
open class InMemoryUserRoleService(
    private val roleSpi: RoleSpi
) : UserRoleSpi {

    // userId -> set of roleIds
    protected val userRoleStore = ConcurrentHashMap<String, MutableSet<String>>()

    override fun assignRoles(userId: String, roleIds: Set<String>) {
        // 验证所有角色存在
        roleIds.forEach { roleId ->
            roleSpi.getById(roleId)
                ?: throw ResourceNotFoundException("Role", roleId)
        }

        userRoleStore.getOrPut(userId) { mutableSetOf() }.addAll(roleIds)
    }

    override fun getRoleIds(userId: String): Set<String> =
        userRoleStore[userId]?.toSet() ?: emptySet()

    override fun getRoles(userId: String): List<UserRoleDTO> {
        val roleIds = userRoleStore[userId] ?: return emptyList()

        return roleIds.mapNotNull { roleId ->
            roleSpi.getById(roleId)?.let { role ->
                UserRoleDTO(
                    userId = userId,
                    roleId = role.id,
                    roleCode = role.code,
                    roleName = role.name,
                    assignedAt = Instant.now(),
                    assignedBy = null
                )
            }
        }
    }

    override fun removeRole(userId: String, roleId: String) {
        userRoleStore[userId]?.remove(roleId)
    }

    override fun clearRoles(userId: String) {
        userRoleStore.remove(userId)
    }

    override fun hasRole(userId: String, roleCode: String): Boolean {
        val roleIds = userRoleStore[userId] ?: return false
        return roleIds.any { roleId ->
            roleSpi.getById(roleId)?.code == roleCode
        }
    }

    override fun hasAnyRole(userId: String, roleCodes: Set<String>): Boolean {
        val roleIds = userRoleStore[userId] ?: return false
        val userRoleCodes = roleIds.mapNotNull { roleSpi.getById(it)?.code }
        return userRoleCodes.any { it in roleCodes }
    }
}
