package site.addzero.kcloud.window.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.kcloud.shell.ShellState
import site.addzero.kcloud.theme.Theme
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.resolveDarkTheme
import site.addzero.workbench.shell.metrics.currentWorkbenchMetrics
import site.addzero.workbenchshell.RenderAdminScaffolding

@Composable
fun RenderWorkbenchWindow(
    scaffolding: ScaffoldingImpl,
    shellThemeState: ShellThemeState = koinInject(),
    shellState: ShellState = koinInject(),
) {
    val themeMode = shellThemeState.themeMode
    val darkTheme = themeMode.resolveDarkTheme(
        systemDarkTheme = isSystemInDarkTheme(),
    )
    val uiMetrics = currentWorkbenchMetrics()
    val sidebarVisible = shellState.sidebarVisible

    Theme(
        darkTheme = darkTheme,
    ) {
        RenderAdminScaffolding(
            scaffolding = scaffolding,
            sidebarVisible = sidebarVisible,
            onSidebarToggle = shellState::toggleSidebar,
            defaultSidebarRatio = uiMetrics.sidebarRatio,
            minSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMinWidth else 0.dp,
            maxSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMaxWidth else 0.dp,
        )
        scaffolding.RenderOverlay()
    }
}
