package site.addzero.kcloud.plugins.rbac

import org.koin.core.annotation.Single
import site.addzero.kcloud.system.api.KCloudSystemApiClient
import site.addzero.kcloud.system.api.UserProfileDto
import site.addzero.kcloud.system.api.UserProfileUpdateRequest

@Single
class UserCenterRemoteService {
    suspend fun readCurrentProfile(): UserProfileDto {
        return KCloudSystemApiClient.userCenterApi.getCurrentUserProfile()
    }

    suspend fun saveCurrentProfile(
        request: UserProfileUpdateRequest,
    ): UserProfileDto {
        return KCloudSystemApiClient.userCenterApi.saveCurrentUserProfile(request)
    }
}
