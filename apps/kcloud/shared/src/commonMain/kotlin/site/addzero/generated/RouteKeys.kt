package site.addzero.generated

import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

/**
 * 路由键
 * 请勿手动修改此文件
 */
object RouteKeys {
    const val MCU_CONTROL_SCREEN = "mcu/control"
    const val MCU_FLASH_SCREEN = "mcu/flash"
    const val USER_CENTER_PROFILE_SCREEN = "system/user-center/profile"
    const val MCU_ONLINE_DEV_SCREEN = "mcu/online-dev"
    const val PLUGIN_MARKET_PACKAGES_SCREEN = "system/plugin-market/packages"
    const val MCU_DEBUG_SCREEN = "mcu/debug"
    const val RBAC_USER_SCREEN = "system/rbac"
    const val AI_CHAT_SESSIONS_SCREEN = "system/ai-chat/sessions"
    const val MUSIC_STUDIO_SCREEN = "vibepocket/music-studio"
    const val KNOWLEDGE_BASE_SPACES_SCREEN = "system/knowledge-base/spaces"
    const val CREATIVE_ASSETS_SCREEN = "vibepocket/creative-assets"
    const val CONFIG_CENTER_PROJECTS_SCREEN = "system/config-center/projects"
    const val CONFIG_CENTER_SECRETS_SCREEN = "system/config-center/secrets"
    const val CONFIG_CENTER_ACCESS_SCREEN = "system/config-center/access"
    const val SETTINGS_SCREEN = "vibepocket/settings"

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "设备会话", title = "控制台", routePath = "mcu/control", icon = "PowerSettingsNew", order = 0.0, placement = RoutePlacement(scene = RouteScene(name = "设备", icon = "Build", order = 0), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuControlScreen", simpleName = "McuControlScreen"),
        Route(value = "开发工具", title = "烧录", routePath = "mcu/flash", icon = "Upload", order = 10.0, placement = RoutePlacement(scene = RouteScene(name = "设备", icon = "Build", order = 0), defaultInScene = true), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuFlashScreen", simpleName = "McuFlashScreen"),
        Route(value = "用户中心", title = "个人资料", routePath = "system/user-center/profile", icon = "Person", order = 10.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.rbac.screen.UserCenterProfileScreen", simpleName = "UserCenterProfileScreen"),
        Route(value = "开发工具", title = "在线开发", routePath = "mcu/online-dev", icon = "Build", order = 15.0, placement = RoutePlacement(scene = RouteScene(name = "设备", icon = "Build", order = 0), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuOnlineDevScreen", simpleName = "McuOnlineDevScreen"),
        Route(value = "插件市场", title = "插件源码市场", routePath = "system/plugin-market/packages", icon = "Apps", order = 15.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.pluginmarket.screen.PluginMarketPackagesScreen", simpleName = "PluginMarketPackagesScreen"),
        Route(value = "开发工具", title = "调试", routePath = "mcu/debug", icon = "BugReport", order = 20.0, placement = RoutePlacement(scene = RouteScene(name = "设备", icon = "Build", order = 0), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuDebugScreen", simpleName = "McuDebugScreen"),
        Route(value = "权限中心", title = "RBAC权限管理", routePath = "system/rbac", icon = "AdminPanelSettings", order = 20.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = true), qualifiedName = "site.addzero.kcloud.plugins.system.rbac.screen.RbacUserScreen", simpleName = "RbacUserScreen"),
        Route(value = "AI对话", title = "对话会话", routePath = "system/ai-chat/sessions", icon = "SmartToy", order = 30.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.aichat.screen.AiChatSessionsScreen", simpleName = "AiChatSessionsScreen"),
        Route(value = "创作中心", title = "音乐工作台", routePath = "vibepocket/music-studio", icon = "PlayArrow", order = 30.0, placement = RoutePlacement(scene = RouteScene(name = "音乐创作", icon = "PlayArrow", order = 200), defaultInScene = true), qualifiedName = "site.addzero.vibepocket.screen.MusicStudioScreen", simpleName = "MusicStudioScreen"),
        Route(value = "知识库", title = "知识空间", routePath = "system/knowledge-base/spaces", icon = "MenuBook", order = 40.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.knowledgebase.screen.KnowledgeBaseSpacesScreen", simpleName = "KnowledgeBaseSpacesScreen"),
        Route(value = "创作中心", title = "创作资产", routePath = "vibepocket/creative-assets", icon = "Dashboard", order = 40.0, placement = RoutePlacement(scene = RouteScene(name = "音乐创作", icon = "PlayArrow", order = 200), defaultInScene = false), qualifiedName = "site.addzero.vibepocket.screen.CreativeAssetsScreen", simpleName = "CreativeAssetsScreen"),
        Route(value = "配置中心", title = "项目与环境", routePath = "system/config-center/projects", icon = "Hub", order = 80.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.configcenter.screen.ConfigCenterProjectsScreen", simpleName = "ConfigCenterProjectsScreen"),
        Route(value = "配置中心", title = "Secret 管理", routePath = "system/config-center/secrets", icon = "Key", order = 81.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.configcenter.screen.ConfigCenterSecretsScreen", simpleName = "ConfigCenterSecretsScreen"),
        Route(value = "配置中心", title = "令牌与审计", routePath = "system/config-center/access", icon = "Shield", order = 82.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.configcenter.screen.ConfigCenterAccessScreen", simpleName = "ConfigCenterAccessScreen"),
        Route(value = "系统设置", title = "设置", routePath = "vibepocket/settings", icon = "Settings", order = 90.0, placement = RoutePlacement(scene = RouteScene(name = "音乐创作", icon = "PlayArrow", order = 200), defaultInScene = false), qualifiedName = "site.addzero.vibepocket.screen.SettingsScreen", simpleName = "SettingsScreen")
    )
}
