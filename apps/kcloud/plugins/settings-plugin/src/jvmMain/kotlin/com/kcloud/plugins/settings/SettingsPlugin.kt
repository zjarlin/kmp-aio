package com.kcloud.plugins.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.kcloud.model.AppSettings
import com.kcloud.model.Theme
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.ShellSettingsService
import com.kcloud.plugin.ShellThemeMode
import com.kcloud.storage.SettingsStorage
import com.kcloud.ui.screens.SettingsScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object SettingsPluginMenus {
    const val SETTINGS = "settings"
}

private val settingsPluginModule = module {
    single { SettingsService() }
    single<ShellSettingsService> { get<SettingsService>() }
    singleOf(::SettingsPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object SettingsPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(settingsPluginModule)
}

class SettingsService : ShellSettingsService {
    private val _settings = MutableStateFlow(SettingsStorage.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _themeMode = MutableStateFlow(_settings.value.theme.toShellThemeMode())
    override val themeMode: StateFlow<ShellThemeMode> = _themeMode.asStateFlow()

    override fun setThemeMode(themeMode: ShellThemeMode) {
        updateSettings(_settings.value.copy(theme = themeMode.toTheme()))
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        _themeMode.value = newSettings.theme.toShellThemeMode()
        SettingsStorage.saveSettings(newSettings)
    }
}

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

private fun Theme.toShellThemeMode(): ShellThemeMode {
    return when (this) {
        Theme.LIGHT -> ShellThemeMode.LIGHT
        Theme.DARK -> ShellThemeMode.DARK
        Theme.SYSTEM -> ShellThemeMode.SYSTEM
    }
}

private fun ShellThemeMode.toTheme(): Theme {
    return when (this) {
        ShellThemeMode.LIGHT -> Theme.LIGHT
        ShellThemeMode.DARK -> Theme.DARK
        ShellThemeMode.SYSTEM -> Theme.SYSTEM
    }
}
