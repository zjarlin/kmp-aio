package site.addzero.system.rbac.feature

import site.addzero.system.common.exception.PermissionDeniedException
import site.addzero.system.rbac.spi.PermissionCheckSpi
import site.addzero.system.rbac.spi.PermissionSpi
import site.addzero.system.rbac.spi.RoleSpi
import site.addzero.system.rbac.spi.UserRoleSpi

/**
 * 默认权限校验服务实现
 */
open class DefaultPermissionCheckService(
    private val userRoleSpi: UserRoleSpi,
    private val roleSpi: RoleSpi,
    private val permissionSpi: PermissionSpi
) : PermissionCheckSpi {

    override fun hasPermission(userId: String, permissionCode: String): Boolean {
        val userPermissions = getUserPermissions(userId)
        return permissionCode in userPermissions
    }

    override fun hasAnyPermission(userId: String, permissionCodes: Set<String>): Boolean {
        val userPermissions = getUserPermissions(userId)
        return permissionCodes.any { it in userPermissions }
    }

    override fun hasAllPermissions(userId: String, permissionCodes: Set<String>): Boolean {
        val userPermissions = getUserPermissions(userId)
        return permissionCodes.all { it in userPermissions }
    }

    override fun getUserPermissions(userId: String): Set<String> {
        val roleIds = userRoleSpi.getRoleIds(userId)
        val permissionCodes = mutableSetOf<String>()

        roleIds.forEach { roleId ->
            val permissionIds = roleSpi.getPermissionIds(roleId)
            permissionIds.forEach { permId ->
                permissionSpi.getById(permId)?.let {
                    permissionCodes.add(it.code)
                }
            }
        }

        return permissionCodes
    }

    override fun checkPermission(userId: String, permissionCode: String) {
        if (!hasPermission(userId, permissionCode)) {
            throw PermissionDeniedException("Required permission: $permissionCode")
        }
    }

    override fun checkAnyPermission(userId: String, permissionCodes: Set<String>) {
        if (!hasAnyPermission(userId, permissionCodes)) {
            throw PermissionDeniedException("Required any of permissions: $permissionCodes")
        }
    }
}
