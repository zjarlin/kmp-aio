package site.addzero.kbox.feature

import kotlinx.coroutines.flow.StateFlow

enum class KboxShellThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

interface KboxShellSettingsService {
    val themeMode: StateFlow<KboxShellThemeMode>

    fun setThemeMode(
        mode: KboxShellThemeMode,
    )
}
