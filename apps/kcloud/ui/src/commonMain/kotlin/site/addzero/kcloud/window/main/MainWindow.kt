package site.addzero.kcloud.window.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.koin.compose.koinInject
import site.addzero.cupertino.workbench.metrics.LocalWorkbenchMetrics
import site.addzero.cupertino.workbench.metrics.WorkbenchPresets
import site.addzero.cupertino.workbench.scaffolding.RenderCupertinoWorkbenchScaffolding
import site.addzero.cupertino.workbench.theme.CupertinoWorkbenchTheme
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.resolveDarkTheme
import site.addzero.kcloud.shell.spi_impl.sys_stats.ShellState
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Composable
fun RenderWorkbenchWindow(
    scaffolding: ScaffoldingSpi,
    shellThemeState: ShellThemeState = koinInject(),
    shellState: ShellState = koinInject(),
) {
    val themeMode = shellThemeState.themeMode
    val darkTheme = themeMode.resolveDarkTheme(
        systemDarkTheme = isSystemInDarkTheme(),
    )
    val workbenchMetrics = WorkbenchPresets.DesktopCompact

    CupertinoWorkbenchTheme(
        darkTheme = darkTheme,
        content = {
            CompositionLocalProvider(
                LocalWorkbenchMetrics provides workbenchMetrics,
            ) {
                RenderCupertinoWorkbenchScaffolding(
                    scaffolding = scaffolding,
                    sidebarMode = shellState.sidebarMode,
                    onSidebarToggle = shellState::toggleSidebar,
                    defaultSidebarRatio = workbenchMetrics.sidebarRatio,
                    minSidebarWidth = workbenchMetrics.sidebarMinWidth,
                    maxSidebarWidth = workbenchMetrics.sidebarMaxWidth,
                )
                scaffolding.RenderOverlay()
            }
        },
    )
}
