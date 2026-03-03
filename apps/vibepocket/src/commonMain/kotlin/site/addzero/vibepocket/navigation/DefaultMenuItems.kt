package site.addzero.vibepocket.navigation

/**
 * 默认菜单注册数据。
 *
 * 当前硬编码的默认菜单项，未来可由元编程（分析 @Route 注解）
 * 或低代码 GUI 动态替换。
 */
val defaultMenuItems = listOf(
    MenuMetadata(
        routeKey = "site.addzero.vibepocket.music.MusicVibeScreen",
        menuNameAlias = "音乐",
        icon = "🎵",
        sortOrder = 0
    ),
    MenuMetadata(
        routeKey = "site.addzero.vibepocket.screens.ImageScreen",
        menuNameAlias = "图片",
        icon = "🖼️",
        sortOrder = 1
    ),
    MenuMetadata(
        routeKey = "site.addzero.vibepocket.screens.VideoScreen",
        menuNameAlias = "视频",
        icon = "🎬",
        sortOrder = 2
    ),
    MenuMetadata(
        routeKey = "site.addzero.vibepocket.settings.SettingsPage",
        menuNameAlias = "设置",
        icon = "⚙️",
        sortOrder = 3
    )
)
