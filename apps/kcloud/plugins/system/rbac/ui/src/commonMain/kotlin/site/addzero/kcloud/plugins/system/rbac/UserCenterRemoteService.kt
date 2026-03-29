package site.addzero.kcloud.plugins.system.rbac

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.rbac.api.RbacApiClient
import site.addzero.kcloud.plugins.system.rbac.api.UserProfileDto
import site.addzero.kcloud.plugins.system.rbac.api.UserProfileUpdateRequest

@Single
class UserCenterRemoteService {
    suspend fun readCurrentProfile(): UserProfileDto {
        return RbacApiClient.userCenterApi.getCurrentUserProfile()
    }

    suspend fun saveCurrentProfile(
        request: UserProfileUpdateRequest,
    ): UserProfileDto {
        return RbacApiClient.userCenterApi.saveCurrentUserProfile(request)
    }
}
