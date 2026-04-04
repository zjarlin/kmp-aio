package site.addzero.kcloud.window.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.theme.Theme
import site.addzero.kcloud.theme.ShellThemeMode
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.currentKCloudUiMetrics
import site.addzero.kcloud.theme.resolveDarkTheme
import site.addzero.workbenchshell.RenderAdminScaffolding

@Composable
fun RenderKCloudWindow(
    scaffolding: ScaffoldingImpl,
    shellThemeState: ShellThemeState = org.koin.compose.koinInject(),
    shellState: KCloudShellState = org.koin.compose.koinInject(),
) {
    val themeMode = shellThemeState.themeMode
    val darkTheme = themeMode.resolveDarkTheme(
        systemDarkTheme = isSystemInDarkTheme(),
    )
    val uiMetrics = currentKCloudUiMetrics()
    val sidebarVisible = shellState.sidebarVisible
    val toggleTheme = remember(shellThemeState, darkTheme) {
        {
            shellThemeState.updateThemeMode(
                if (darkTheme) {
                    ShellThemeMode.LIGHT
                } else {
                    ShellThemeMode.DARK
                },
            )
        }
    }

    Theme(
        darkTheme = darkTheme,
    ) {
        RenderAdminScaffolding(
            scaffolding = scaffolding,
            darkTheme = darkTheme,
            onThemeToggle = toggleTheme,
            sidebarVisible = sidebarVisible,
            defaultSidebarRatio = uiMetrics.sidebarRatio,
            minSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMinWidth else 0.dp,
            maxSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMaxWidth else 0.dp,
        )
        scaffolding.RenderOverlay()
    }
}
