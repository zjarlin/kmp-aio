package site.addzero.generated

import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

/**
 * 路由键
 * 请勿手动修改此文件
 */
object RouteKeys {
    const val USER_CENTER_PROFILE_SCREEN = "system/user-center/profile"
    const val RBAC_USER_SCREEN = "system/rbac"
    const val AI_CHAT_SESSIONS_SCREEN = "system/ai-chat/sessions"
    const val MUSIC_STUDIO_SCREEN = "vibepocket/music-studio"
    const val KNOWLEDGE_BASE_SPACES_SCREEN = "system/knowledge-base/spaces"
    const val CREATIVE_ASSETS_SCREEN = "vibepocket/creative-assets"
    const val SETTINGS_SCREEN = "vibepocket/settings"

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "用户中心", title = "个人资料", routePath = "system/user-center/profile", icon = "Person", order = 10.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.rbac.screen.UserCenterProfileScreen", simpleName = "UserCenterProfileScreen"),
        Route(value = "权限中心", title = "RBAC权限管理", routePath = "system/rbac", icon = "AdminPanelSettings", order = 20.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = true), qualifiedName = "site.addzero.kcloud.plugins.system.rbac.screen.RbacUserScreen", simpleName = "RbacUserScreen"),
        Route(value = "AI对话", title = "对话会话", routePath = "system/ai-chat/sessions", icon = "SmartToy", order = 30.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.aichat.screen.AiChatSessionsScreen", simpleName = "AiChatSessionsScreen"),
        Route(value = "创作中心", title = "音乐工作台", routePath = "vibepocket/music-studio", icon = "PlayArrow", order = 30.0, placement = RoutePlacement(scene = RouteScene(name = "音乐创作", icon = "PlayArrow", order = 200), defaultInScene = true), qualifiedName = "site.addzero.vibepocket.screen.MusicStudioScreen", simpleName = "MusicStudioScreen"),
        Route(value = "知识库", title = "知识空间", routePath = "system/knowledge-base/spaces", icon = "MenuBook", order = 40.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.knowledgebase.screen.KnowledgeBaseSpacesScreen", simpleName = "KnowledgeBaseSpacesScreen"),
        Route(value = "创作中心", title = "创作资产", routePath = "vibepocket/creative-assets", icon = "Dashboard", order = 40.0, placement = RoutePlacement(scene = RouteScene(name = "音乐创作", icon = "PlayArrow", order = 200), defaultInScene = false), qualifiedName = "site.addzero.vibepocket.screen.CreativeAssetsScreen", simpleName = "CreativeAssetsScreen"),
        Route(value = "系统设置", title = "设置", routePath = "vibepocket/settings", icon = "Settings", order = 90.0, placement = RoutePlacement(scene = RouteScene(name = "音乐创作", icon = "PlayArrow", order = 200), defaultInScene = false), qualifiedName = "site.addzero.vibepocket.screen.SettingsScreen", simpleName = "SettingsScreen")
    )
}
