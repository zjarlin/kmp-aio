package site.addzero.vibepocket.plugin

import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudPlugin
import site.addzero.vibepocket.music.AudioToolsPage
import site.addzero.vibepocket.music.MusicVibeScreen
import site.addzero.vibepocket.settings.SettingsPage

object VibePocketMenuGroups {
    const val CREATE = "group.create"
    const val TOOLS = "group.tools"
    const val SYSTEM = "group.system"
}

object VibePocketPluginMenus {
    const val MUSIC_STUDIO = "music.studio"
    const val AUDIO_TOOLS = "music.audio-tools"
    const val SETTINGS = "system.settings"
}

object MusicStudioPlugin : KCloudPlugin {
    override val pluginId: String = "vibepocket.music-studio"
    override val order: Int = 10
    override val menuEntries: List<KCloudMenuEntry> = listOf(
        KCloudMenuEntry(
            id = VibePocketPluginMenus.MUSIC_STUDIO,
            title = "音乐工作台",
            parentId = VibePocketMenuGroups.CREATE,
            sortOrder = 10,
            content = { MusicVibeScreen() },
        ),
    )
}

object AudioToolsPlugin : KCloudPlugin {
    override val pluginId: String = "vibepocket.audio-tools"
    override val order: Int = 20
    override val menuEntries: List<KCloudMenuEntry> = listOf(
        KCloudMenuEntry(
            id = VibePocketPluginMenus.AUDIO_TOOLS,
            title = "音频工具",
            parentId = VibePocketMenuGroups.TOOLS,
            sortOrder = 20,
            content = { AudioToolsPage() },
        ),
    )
}

object SettingsPlugin : KCloudPlugin {
    override val pluginId: String = "vibepocket.settings"
    override val order: Int = 90
    override val menuEntries: List<KCloudMenuEntry> = listOf(
        KCloudMenuEntry(
            id = VibePocketPluginMenus.SETTINGS,
            title = "设置",
            parentId = VibePocketMenuGroups.SYSTEM,
            sortOrder = 90,
            content = { SettingsPage() },
        ),
    )
}

val vibePocketPlugins: List<KCloudPlugin> = listOf(
    MusicStudioPlugin,
    AudioToolsPlugin,
    SettingsPlugin,
)
