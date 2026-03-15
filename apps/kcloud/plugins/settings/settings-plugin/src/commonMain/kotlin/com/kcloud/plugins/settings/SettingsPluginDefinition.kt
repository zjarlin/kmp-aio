package com.kcloud.plugins.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.kcloud.model.AppSettings
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.settings.ui.SettingsScreen
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

interface SettingsEditorService {
    val settings: StateFlow<AppSettings>

    fun updateSettings(newSettings: AppSettings)
}

@Single
class SettingsPlugin : KCloudPlugin {
    override val pluginId = "settings-plugin"
    override val order = 100
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = SettingsPluginMenus.SETTINGS,
            title = "设置",
            icon = Icons.Default.Settings,
            parentId = KCloudMenuGroups.SYSTEM,
            sortOrder = 100,
            content = { SettingsScreen() }
        )
    )
}
