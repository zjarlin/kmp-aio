package com.kcloud.features.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.kcloud.model.AppSettings
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.settings.ui.SettingsScreen
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

interface SettingsEditorService {
    val settings: StateFlow<AppSettings>

    fun updateSettings(newSettings: AppSettings)
}

@Single(binds = [KCloudFeature::class])
class SettingsFeature : KCloudFeature {
    override val featureId = "settings"
    override val order = 100
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = SettingsFeatureMenus.SETTINGS,
            title = "设置",
            icon = Icons.Default.Settings,
            parentId = KCloudMenuGroups.SYSTEM,
            sortOrder = 100,
            content = { SettingsScreen() }
        )
    )
}
