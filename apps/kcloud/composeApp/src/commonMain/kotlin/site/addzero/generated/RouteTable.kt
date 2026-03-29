package site.addzero.generated

import androidx.compose.runtime.Composable

/**
 * 路由表
 * 请勿手动修改此文件
 */
object RouteTable {
    /**
     * 所有路由映射
     */
    val allRoutes: Map<String, @Composable () -> Unit> = mapOf(
        RouteKeys.USER_CENTER_PROFILE_SCREEN to @Composable { site.addzero.kcloud.plugins.system.rbac.screen.UserCenterProfileScreen() },
        RouteKeys.RBAC_USER_SCREEN to @Composable { site.addzero.kcloud.plugins.system.rbac.screen.RbacUserScreen() },
        RouteKeys.AI_CHAT_SESSIONS_SCREEN to @Composable { site.addzero.kcloud.plugins.system.aichat.screen.AiChatSessionsScreen() },
        RouteKeys.MUSIC_STUDIO_SCREEN to @Composable { site.addzero.vibepocket.screen.MusicStudioScreen() },
        RouteKeys.KNOWLEDGE_BASE_SPACES_SCREEN to @Composable { site.addzero.kcloud.plugins.system.knowledgebase.screen.KnowledgeBaseSpacesScreen() },
        RouteKeys.CREATIVE_ASSETS_SCREEN to @Composable { site.addzero.vibepocket.screen.CreativeAssetsScreen() },
        RouteKeys.SETTINGS_SCREEN to @Composable { site.addzero.vibepocket.screen.SettingsScreen() }
    )

    /**
     * 根据路由键获取对应的Composable函数
     */
    operator fun get(routeKey: String): @Composable () -> Unit {
        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: $routeKey")
    }
}
