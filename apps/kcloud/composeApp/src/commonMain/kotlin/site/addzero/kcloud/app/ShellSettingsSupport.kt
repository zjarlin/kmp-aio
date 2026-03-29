package site.addzero.kcloud.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.kcloud.feature.ShellThemeMode

@Single
class DefaultShellSettingsService : ShellSettingsService {
    private val themeModeState = MutableStateFlow(ShellThemeMode.SYSTEM)

    override val themeMode: StateFlow<ShellThemeMode> = themeModeState.asStateFlow()

    override fun setThemeMode(mode: ShellThemeMode) {
        themeModeState.value = mode
    }
}
