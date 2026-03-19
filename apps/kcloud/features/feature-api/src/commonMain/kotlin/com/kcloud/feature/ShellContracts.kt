package com.kcloud.feature

import kotlinx.coroutines.flow.StateFlow

enum class ShellThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

interface ShellSettingsService {
    val themeMode: StateFlow<ShellThemeMode>

    fun setThemeMode(themeMode: ShellThemeMode)
}

interface ShellLocalServerService {
    val baseUrl: StateFlow<String?>
}

interface ShellWindowController {
    fun showWindow()
    fun hideWindow()
    fun toggleWindow()
    fun requestExit()
}
