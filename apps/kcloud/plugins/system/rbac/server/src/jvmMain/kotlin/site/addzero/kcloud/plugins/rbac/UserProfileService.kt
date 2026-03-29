package site.addzero.kcloud.plugins.rbac

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.rbac.model.UserProfile
import site.addzero.kcloud.plugins.rbac.model.by
import site.addzero.kcloud.system.api.UserProfileDto
import site.addzero.kcloud.system.api.UserProfileUpdateRequest

private const val DEFAULT_ACCOUNT_KEY = "desktop-user"
private const val DEFAULT_DISPLAY_NAME = "KCloud 用户"
private const val DEFAULT_LOCALE = "zh-CN"
private const val DEFAULT_TIME_ZONE = "Asia/Shanghai"

@Single
class UserProfileService(
    private val sqlClient: KSqlClient,
) {
    fun readCurrentProfile(): UserProfileDto {
        return currentProfile().toDto()
    }

    fun saveCurrentProfile(
        request: UserProfileUpdateRequest,
    ): UserProfileDto {
        require(request.displayName.isNotBlank()) { "显示名称不能为空" }
        val existing = currentProfileEntity()
        val entity = if (existing == null) {
            new(UserProfile::class).by {
                accountKey = DEFAULT_ACCOUNT_KEY
                displayName = request.displayName.trim()
                email = request.email?.trim()?.ifBlank { null }
                avatarLabel = resolveAvatarLabel(
                    avatarLabel = request.avatarLabel,
                    displayName = request.displayName,
                )
                locale = request.locale.ifBlank { DEFAULT_LOCALE }
                timeZone = request.timeZone.ifBlank { DEFAULT_TIME_ZONE }
            }
        } else {
            new(UserProfile::class).by {
                id = existing.id
                accountKey = existing.accountKey
                displayName = request.displayName.trim()
                email = request.email?.trim()?.ifBlank { null }
                avatarLabel = resolveAvatarLabel(
                    avatarLabel = request.avatarLabel,
                    displayName = request.displayName,
                )
                locale = request.locale.ifBlank { DEFAULT_LOCALE }
                timeZone = request.timeZone.ifBlank { DEFAULT_TIME_ZONE }
                createTime = existing.createTime
            }
        }
        val saved = sqlClient.save(entity).modifiedEntity
        return saved.toDto()
    }

    private fun currentProfile(): UserProfile {
        return currentProfileEntity() ?: sqlClient.save(
            new(UserProfile::class).by {
                accountKey = DEFAULT_ACCOUNT_KEY
                displayName = DEFAULT_DISPLAY_NAME
                email = null
                avatarLabel = resolveAvatarLabel(
                    avatarLabel = "KC",
                    displayName = DEFAULT_DISPLAY_NAME,
                )
                locale = DEFAULT_LOCALE
                timeZone = DEFAULT_TIME_ZONE
            },
        ).modifiedEntity
    }

    private fun currentProfileEntity(): UserProfile? {
        return sqlClient.createQuery(UserProfile::class) {
            select(table)
        }.execute().firstOrNull { profile ->
            profile.accountKey == DEFAULT_ACCOUNT_KEY
        }
    }
}

private fun UserProfile.toDto(): UserProfileDto {
    return UserProfileDto(
        id = id,
        accountKey = accountKey,
        displayName = displayName,
        email = email,
        avatarLabel = avatarLabel,
        locale = locale,
        timeZone = timeZone,
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun resolveAvatarLabel(
    avatarLabel: String,
    displayName: String,
): String {
    val normalizedAvatar = avatarLabel.trim()
    if (normalizedAvatar.isNotBlank()) {
        return normalizedAvatar.take(2).uppercase()
    }
    val trimmedDisplayName = displayName.trim()
    if (trimmedDisplayName.isBlank()) {
        return "U"
    }
    val segments = trimmedDisplayName.split(' ', '.', '-', '_')
        .filter(String::isNotBlank)
    if (segments.isEmpty()) {
        return trimmedDisplayName.take(2).uppercase()
    }
    return segments.take(2)
        .joinToString(separator = "") { it.take(1).uppercase() }
        .ifBlank { "U" }
}
