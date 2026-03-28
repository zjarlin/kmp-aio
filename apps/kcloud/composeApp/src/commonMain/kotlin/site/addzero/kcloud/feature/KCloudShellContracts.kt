package site.addzero.kcloud.feature

import kotlinx.coroutines.flow.StateFlow

enum class ShellThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

interface ShellSettingsService {
    val themeMode: StateFlow<ShellThemeMode>

    fun setThemeMode(
        mode: ShellThemeMode,
    )
}

interface ShellWindowController {
    fun showWindow()

    fun hideWindow()

    fun toggleWindow()

    fun requestExit()
}

interface ShellTrayPanelController {
    fun showTrayPanel()

    fun hideTrayPanel()

    fun toggleTrayPanel()
}
