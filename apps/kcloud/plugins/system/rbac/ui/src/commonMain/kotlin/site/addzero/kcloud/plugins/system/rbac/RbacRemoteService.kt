package site.addzero.kcloud.plugins.system.rbac

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.rbac.api.RbacApiClient
import site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto
import site.addzero.kcloud.plugins.system.rbac.api.RbacRoleMutationRequest

@Single
class RbacRemoteService {
    suspend fun listRoles(): List<RbacRoleDto> {
        return RbacApiClient.rbacApi.listRbacRoles()
    }

    suspend fun createRole(
        request: RbacRoleMutationRequest,
    ): RbacRoleDto {
        return RbacApiClient.rbacApi.createRbacRole(request)
    }

    suspend fun updateRole(
        roleId: Long,
        request: RbacRoleMutationRequest,
    ): RbacRoleDto {
        return RbacApiClient.rbacApi.updateRbacRole(roleId, request)
    }

    suspend fun deleteRole(
        roleId: Long,
    ) {
        RbacApiClient.rbacApi.deleteRbacRole(roleId)
    }
}
