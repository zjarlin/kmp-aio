package site.addzero.kbox.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import site.addzero.kbox.feature.KboxShellSettingsService
import site.addzero.kbox.feature.KboxShellThemeMode

class DefaultKboxShellSettingsService : KboxShellSettingsService {
    private val themeModeState = MutableStateFlow(KboxShellThemeMode.SYSTEM)

    override val themeMode: StateFlow<KboxShellThemeMode> = themeModeState.asStateFlow()

    override fun setThemeMode(
        mode: KboxShellThemeMode,
    ) {
        themeModeState.value = mode
    }
}
