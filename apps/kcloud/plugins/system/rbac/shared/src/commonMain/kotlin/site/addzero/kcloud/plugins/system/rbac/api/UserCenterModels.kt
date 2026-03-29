package site.addzero.kcloud.plugins.system.rbac.api

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val id: Long,
    val accountKey: String,
    val displayName: String,
    val email: String? = null,
    val avatarLabel: String = "",
    val locale: String = "zh-CN",
    val timeZone: String = "Asia/Shanghai",
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class UserProfileUpdateRequest(
    val displayName: String,
    val email: String? = null,
    val avatarLabel: String = "",
    val locale: String = "zh-CN",
    val timeZone: String = "Asia/Shanghai",
)
