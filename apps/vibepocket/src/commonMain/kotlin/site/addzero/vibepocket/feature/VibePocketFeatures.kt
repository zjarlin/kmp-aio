package site.addzero.vibepocket.feature

import site.addzero.vibepocket.screens.creativeassets.CreativeAssetsScreen
import site.addzero.vibepocket.screens.musicstudio.MusicStudioScreen
import site.addzero.vibepocket.screens.settings.SettingsScreen
import site.addzero.workbenchshell.Screen

object VibePocketFeatureMenus {
    const val MUSIC_STUDIO = "music.studio"
    const val CREATIVE_ASSETS = "creative.assets"
    const val SETTINGS = "system.settings"
}

val vibePocketScreens: List<Screen> = listOf(
    MusicStudioScreen,
    CreativeAssetsScreen,
    SettingsScreen,
)
