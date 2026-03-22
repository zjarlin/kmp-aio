package com.kcloud.features.settings

import com.kcloud.model.AppSettings
import com.kcloud.model.Theme
import com.kcloud.feature.ShellSettingsService
import com.kcloud.feature.ShellThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

interface SettingsStorageService {
    fun loadSettings(): AppSettings

    fun saveSettings(settings: AppSettings)
}

@Single
class SettingsService(
    private val storageService: SettingsStorageService,
) : ShellSettingsService, SettingsEditorService {
    private val _settings = MutableStateFlow(storageService.loadSettings())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _themeMode = MutableStateFlow(_settings.value.theme.toShellThemeMode())
    override val themeMode: StateFlow<ShellThemeMode> = _themeMode.asStateFlow()

    override fun setThemeMode(themeMode: ShellThemeMode) {
        updateSettings(_settings.value.copy(theme = themeMode.toTheme()))
    }

    override fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        _themeMode.value = newSettings.theme.toShellThemeMode()
        storageService.saveSettings(newSettings)
    }
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
