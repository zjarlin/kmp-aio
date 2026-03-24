package site.addzero.kcloud.feature

import kotlinx.coroutines.flow.StateFlow
import org.koin.core.Koin

enum class ShellThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

interface ShellSettingsService {
    val themeMode: StateFlow<ShellThemeMode>

    fun setThemeMode(mode: ShellThemeMode)
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

interface ShellLocalServerService {
    val baseUrl: StateFlow<String?>
}

interface DesktopLifecycleContributor {
    val order: Int
        get() = 0

    fun onStart(koin: Koin) {}

    fun onStop(koin: Koin) {}
}

interface ServerLifecycleContributor {
    val order: Int
        get() = 0

    fun onStart() {}

    fun onStop() {}
}
