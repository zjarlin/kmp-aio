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
) {
    val themeMode = shellThemeState.themeMode
    val darkTheme = themeMode.resolveDarkTheme(
        systemDarkTheme = isSystemInDarkTheme(),
    )
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
        KCloudWorkbenchFrame(
            scaffolding = scaffolding,
            darkTheme = darkTheme,
            onThemeToggle = toggleTheme,
        )
        scaffolding.RenderOverlay()
    }
}

@Composable
private fun KCloudWorkbenchFrame(
    scaffolding: ScaffoldingImpl,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    shellState: KCloudShellState = org.koin.compose.koinInject(),
) {
    val uiMetrics = currentKCloudUiMetrics()
    val sidebarVisible = shellState.sidebarVisible

    RenderAdminScaffolding(
        scaffolding = scaffolding,
        darkTheme = darkTheme,
        onThemeToggle = onThemeToggle,
        sidebarVisible = sidebarVisible,
        defaultSidebarRatio = uiMetrics.sidebarRatio,
        minSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMinWidth else 0.dp,
        maxSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMaxWidth else 0.dp,
    )
}
