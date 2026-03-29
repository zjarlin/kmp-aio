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
        RouteKeys.MCU_CONTROL_SCREEN to @Composable { site.addzero.kcloud.plugins.mcuconsole.screen.McuControlScreen() },
        RouteKeys.MCU_FLASH_SCREEN to @Composable { site.addzero.kcloud.plugins.mcuconsole.screen.McuFlashScreen() },
        RouteKeys.USER_CENTER_PROFILE_SCREEN to @Composable { site.addzero.kcloud.plugins.rbac.screen.UserCenterProfileScreen() },
        RouteKeys.MCU_ONLINE_DEV_SCREEN to @Composable { site.addzero.kcloud.plugins.mcuconsole.screen.McuOnlineDevScreen() },
        RouteKeys.PLUGIN_MARKET_PACKAGES_SCREEN to @Composable { site.addzero.kcloud.plugins.system.pluginmarket.screen.PluginMarketPackagesScreen() },
        RouteKeys.MCU_DEBUG_SCREEN to @Composable { site.addzero.kcloud.plugins.mcuconsole.screen.McuDebugScreen() },
        RouteKeys.RBAC_USER_SCREEN to @Composable { site.addzero.kcloud.plugins.rbac.screen.RbacUserScreen() },
        RouteKeys.AI_CHAT_SESSIONS_SCREEN to @Composable { site.addzero.kcloud.plugins.system.aichat.screen.AiChatSessionsScreen() },
        RouteKeys.MUSIC_STUDIO_SCREEN to @Composable { site.addzero.vibepocket.screen.MusicStudioScreen() },
        RouteKeys.KNOWLEDGE_BASE_SPACES_SCREEN to @Composable { site.addzero.kcloud.plugins.system.knowledgebase.screen.KnowledgeBaseSpacesScreen() },
        RouteKeys.CREATIVE_ASSETS_SCREEN to @Composable { site.addzero.vibepocket.screen.CreativeAssetsScreen() },
        RouteKeys.CONFIG_CENTER_ENTRIES_SCREEN to @Composable { site.addzero.kcloud.plugins.system.configcenter.screen.ConfigCenterEntriesScreen() },
        RouteKeys.CONFIG_CENTER_TARGETS_SCREEN to @Composable { site.addzero.kcloud.plugins.system.configcenter.screen.ConfigCenterTargetsScreen() },
        RouteKeys.CONFIG_CENTER_PREVIEW_SCREEN to @Composable { site.addzero.kcloud.plugins.system.configcenter.screen.ConfigCenterPreviewScreen() },
        RouteKeys.SETTINGS_SCREEN to @Composable { site.addzero.vibepocket.screen.SettingsScreen() }
    )

    /**
     * 根据路由键获取对应的Composable函数
     */
    operator fun get(routeKey: String): @Composable () -> Unit {
        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: $routeKey")
    }
}
