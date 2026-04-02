package site.addzero.kcloud.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single

enum class ShellThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

@Single
class ShellThemeState {
    var themeMode by mutableStateOf(ShellThemeMode.LIGHT)
        private set

    fun updateThemeMode(
        mode: ShellThemeMode,
    ) {
        themeMode = mode
    }
}

fun ShellThemeMode.resolveDarkTheme(
    systemDarkTheme: Boolean,
): Boolean {
    return when (this) {
        ShellThemeMode.LIGHT -> false
        ShellThemeMode.DARK -> true
        ShellThemeMode.SYSTEM -> systemDarkTheme
    }
}
