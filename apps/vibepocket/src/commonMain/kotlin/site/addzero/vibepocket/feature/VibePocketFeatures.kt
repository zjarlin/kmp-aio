package site.addzero.vibepocket.feature

import site.addzero.vibepocket.music.MusicTaskResourcePage
import site.addzero.vibepocket.music.MusicVibeScreen
import site.addzero.vibepocket.settings.SettingsPage
import site.addzero.workbenchshell.Screen

object VibePocketMenuGroups {
    const val CREATE = "root.create"
    const val SYSTEM = "root.system"
}

object VibePocketFeatureMenus {
    const val MUSIC_STUDIO = "music.studio"
    const val MUSIC_TASK_RESOURCES = "music.task-resources"
    const val SETTINGS = "system.settings"
}

private object CreateRootScreen : Screen {
    override val id = VibePocketMenuGroups.CREATE
    override val name = "创作"
}

private object SystemRootScreen : Screen {
    override val id = VibePocketMenuGroups.SYSTEM
    override val name = "系统"
}

private object MusicStudioScreen : Screen {
    override val id = VibePocketFeatureMenus.MUSIC_STUDIO
    override val pid = VibePocketMenuGroups.CREATE
    override val name = "音乐工作台"
    override val sort = 10
    override val content = {
        MusicVibeScreen()
    }
}

private object MusicTaskResourcesScreen : Screen {
    override val id = VibePocketFeatureMenus.MUSIC_TASK_RESOURCES
    override val pid = VibePocketMenuGroups.CREATE
    override val name = "生成管理"
    override val sort = 20
    override val content = {
        MusicTaskResourcePage()
    }
}

private object SettingsScreenEntry : Screen {
    override val id = VibePocketFeatureMenus.SETTINGS
    override val pid = VibePocketMenuGroups.SYSTEM
    override val name = "设置"
    override val sort = 90
    override val content = {
        SettingsPage()
    }
}

val vibePocketScreens: List<Screen> = listOf(
    CreateRootScreen,
    MusicStudioScreen,
    MusicTaskResourcesScreen,
    SystemRootScreen,
    SettingsScreenEntry,
)
