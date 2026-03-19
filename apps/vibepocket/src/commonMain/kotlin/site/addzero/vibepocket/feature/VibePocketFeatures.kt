package site.addzero.vibepocket.feature

import com.kcloud.feature.KCloudFeature
import com.kcloud.feature.KCloudMenuEntry
import site.addzero.vibepocket.music.MusicTaskResourcePage
import site.addzero.vibepocket.music.MusicVibeScreen
import site.addzero.vibepocket.settings.SettingsPage

object VibePocketMenuGroups {
    const val CREATE = "group.create"
    const val SYSTEM = "group.system"
}

object VibePocketFeatureMenus {
    const val MUSIC_STUDIO = "music.studio"
    const val MUSIC_TASK_RESOURCES = "music.task-resources"
    const val SETTINGS = "system.settings"
}

object MusicStudioFeature : KCloudFeature {
    override val featureId: String = "vibepocket.music-studio"
    override val order: Int = 10
    override val menuEntries: List<KCloudMenuEntry> = listOf(
        KCloudMenuEntry(
            id = VibePocketFeatureMenus.MUSIC_STUDIO,
            title = "音乐工作台",
            parentId = VibePocketMenuGroups.CREATE,
            sortOrder = 10,
            content = { MusicVibeScreen() },
        ),
        KCloudMenuEntry(
            id = VibePocketFeatureMenus.MUSIC_TASK_RESOURCES,
            title = "生成管理",
            parentId = VibePocketMenuGroups.CREATE,
            sortOrder = 20,
            content = { MusicTaskResourcePage() },
        ),
    )
}

object SettingsFeature : KCloudFeature {
    override val featureId: String = "vibepocket.settings"
    override val order: Int = 90
    override val menuEntries: List<KCloudMenuEntry> = listOf(
        KCloudMenuEntry(
            id = VibePocketFeatureMenus.SETTINGS,
            title = "设置",
            parentId = VibePocketMenuGroups.SYSTEM,
            sortOrder = 90,
            content = { SettingsPage() },
        ),
    )
}

val vibePocketFeatures: List<KCloudFeature> = listOf(
    MusicStudioFeature,
    SettingsFeature,
)
