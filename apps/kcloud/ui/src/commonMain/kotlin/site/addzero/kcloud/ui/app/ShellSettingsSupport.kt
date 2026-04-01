package site.addzero.kcloud.ui.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single
import site.addzero.kcloud.ui.feature.ShellSettingsService
import site.addzero.kcloud.ui.feature.ShellThemeMode

@Single(
    binds = [
        ShellSettingsService::class,
    ],
)
class DefaultShellSettingsService : ShellSettingsService {
    private val themeModeState = MutableStateFlow(ShellThemeMode.LIGHT)

    override val themeMode: StateFlow<ShellThemeMode> = themeModeState.asStateFlow()

    override fun setThemeMode(mode: ShellThemeMode) {
        themeModeState.value = mode
    }
}
