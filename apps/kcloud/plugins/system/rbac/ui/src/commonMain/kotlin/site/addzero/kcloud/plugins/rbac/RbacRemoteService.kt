package site.addzero.kcloud.plugins.rbac

import org.koin.core.annotation.Single
import site.addzero.kcloud.system.api.KCloudSystemApiClient
import site.addzero.kcloud.system.api.RbacRoleDto
import site.addzero.kcloud.system.api.RbacRoleMutationRequest

@Single
class RbacRemoteService {
    suspend fun listRoles(): List<RbacRoleDto> {
        return KCloudSystemApiClient.rbacApi.listRbacRoles()
    }

    suspend fun createRole(
        request: RbacRoleMutationRequest,
    ): RbacRoleDto {
        return KCloudSystemApiClient.rbacApi.createRbacRole(request)
    }

    suspend fun updateRole(
        roleId: Long,
        request: RbacRoleMutationRequest,
    ): RbacRoleDto {
        return KCloudSystemApiClient.rbacApi.updateRbacRole(roleId, request)
    }

    suspend fun deleteRole(
        roleId: Long,
    ) {
        KCloudSystemApiClient.rbacApi.deleteRbacRole(roleId)
    }
}
