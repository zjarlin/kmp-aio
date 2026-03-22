package com.kcloud.features.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.model.AppSettings
import com.kcloud.features.settings.ui.SettingsScreen
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

interface SettingsEditorService {
    val settings: StateFlow<AppSettings>

    fun updateSettings(newSettings: AppSettings)
}

@Single
class SettingsFeature : Screen {
    override val id = SettingsFeatureMenus.SETTINGS
    override val pid = KCloudScreenRoots.OPS
    override val name = "设置"
    override val icon = Icons.Default.Settings
    override val sort = 100
    override val content: (@Composable () -> Unit) = {
        SettingsScreen()
    }
}
