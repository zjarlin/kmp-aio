package site.addzero.kcloud.window.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.cupertino.workbench.metrics.currentWorkbenchMetrics
import site.addzero.cupertino.workbench.scaffolding.RenderCupertinoWorkbenchScaffolding
import site.addzero.cupertino.workbench.theme.CupertinoWorkbenchTheme
import site.addzero.kcloud.shell.spi_impl.sys_stats.ShellState
import site.addzero.kcloud.shell.spi_impl.ScaffoldingImpl
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.resolveDarkTheme

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

    CupertinoWorkbenchTheme(
        darkTheme = darkTheme,
        content = {
            RenderCupertinoWorkbenchScaffolding(
                scaffolding = scaffolding,
                sidebarVisible = sidebarVisible,
                onSidebarToggle = shellState::toggleSidebar,
                defaultSidebarRatio = uiMetrics.sidebarRatio,
                minSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMinWidth else 0.dp,
                maxSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMaxWidth else 0.dp,
            )
            scaffolding.RenderOverlay()
        },
    )
}
