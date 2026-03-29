package site.addzero.kcloud.app.menu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.generated.RouteKeys
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.kcloud.plugins.rbac.UserCenterWorkbenchState

data class KCloudUserMenuItem(
    val label: String,
    val routePath: String,
    val iconName: String,
)

@Single
class KCloudUserMenuState(
    private val shellState: KCloudShellState,
    private val profileState: UserCenterWorkbenchState,
) {
    val items: List<KCloudUserMenuItem> = listOf(
        KCloudUserMenuItem(
            label = "用户中心",
            routePath = RouteKeys.USER_CENTER_PROFILE_SCREEN,
            iconName = "Person",
        ),
        KCloudUserMenuItem(
            label = "AI对话",
            routePath = RouteKeys.AI_CHAT_SESSIONS_SCREEN,
            iconName = "SmartToy",
        ),
        KCloudUserMenuItem(
            label = "知识库",
            routePath = RouteKeys.KNOWLEDGE_BASE_SPACES_SCREEN,
            iconName = "MenuBook",
        ),
        KCloudUserMenuItem(
            label = "配置中心",
            routePath = RouteKeys.CONFIG_CENTER_ENTRIES_SCREEN,
            iconName = "Settings",
        ),
    )

    var expanded by mutableStateOf(false)
        private set

    suspend fun ensureLoaded() {
        profileState.ensureLoaded()
    }

    fun toggle() {
        expanded = !expanded
    }

    fun dismiss() {
        expanded = false
    }

    fun navigateTo(
        routePath: String,
    ) {
        expanded = false
        shellState.selectRoute(routePath)
    }

    val displayName: String
        get() = profileState.profile?.displayName
            ?.ifBlank { null }
            ?: profileState.displayName.ifBlank { "用户" }

    val avatarInitials: String
        get() {
            val avatarLabel = profileState.profile?.avatarLabel
                ?.ifBlank { null }
                ?: profileState.avatarLabel.ifBlank { null }
            if (avatarLabel != null) {
                return avatarLabel.toAvatarInitials()
            }
            val seed = profileState.profile?.accountKey
                ?.ifBlank { null }
                ?: displayName
            return seed.toAvatarInitials()
        }
}

private fun String.toAvatarInitials(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) {
        return "U"
    }
    val segments = trimmed.substringBefore("@")
        .split('.', '-', '_', ' ')
        .filter(String::isNotBlank)
    if (segments.isEmpty()) {
        return trimmed.take(2).uppercase()
    }
    return segments.take(2)
        .joinToString(separator = "") { part ->
            part.take(1).uppercase()
        }
        .ifBlank { "U" }
}
