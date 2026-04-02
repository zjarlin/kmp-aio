package site.addzero.kcloud.window.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.appsidebar.AdminWorkbenchScaffold
import site.addzero.appsidebar.adminWorkbenchActions
import site.addzero.appsidebar.adminWorkbenchConfig
import site.addzero.appsidebar.adminWorkbenchPageConfig
import site.addzero.appsidebar.adminWorkbenchSlots
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.shell.content.KCloudContentRender
import site.addzero.kcloud.shell.header.KCloudHeaderRender
import site.addzero.kcloud.shell.menu.KCloudShellActions
import site.addzero.kcloud.shell.sidebar.KCloudSidebarRender
import site.addzero.kcloud.theme.KCloudTheme
import site.addzero.kcloud.theme.ShellThemeMode
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.resolveDarkTheme

@Composable
fun MainWindow(
    shellThemeState: ShellThemeState = koinInject(),
    shellState: KCloudShellState = koinInject(),
    sidebarRenderer: KCloudSidebarRender = koinInject(),
    headerRenderer: KCloudHeaderRender = koinInject(),
    contentRenderer: KCloudContentRender = koinInject(),
) {
    val themeMode = shellThemeState.themeMode
    val sidebarVisible = shellState.sidebarVisible
    val darkTheme = themeMode.resolveDarkTheme(
        systemDarkTheme = isSystemInDarkTheme(),
    )
    KCloudTheme(
        darkTheme = darkTheme,
    ) {
        val toggleTheme = {
            shellThemeState.updateThemeMode(
                if (darkTheme) {
                    ShellThemeMode.LIGHT
                } else {
                    ShellThemeMode.DARK
                },
            )
        }
        AdminWorkbenchScaffold(
            sidebar = {
                sidebarRenderer.Render(
                    modifier = Modifier.fillMaxSize(),
                )
            },
            content = {
                contentRenderer.Render(
                    modifier = Modifier.fillMaxSize(),
                )
            },
            page = adminWorkbenchPageConfig(pageTitle = "KCloud"),
            modifier = Modifier.fillMaxSize(),
            config = adminWorkbenchConfig(
                brandLabel = "KCloud",
                welcomeLabel = "",
                defaultSidebarRatio = if (sidebarVisible) 0.22f else 0f,
                minSidebarWidth = if (sidebarVisible) 248.dp else 0.dp,
                maxSidebarWidth = if (sidebarVisible) 360.dp else 0.dp,
                contentPadding = PaddingValues(0.dp),
                detailPadding = PaddingValues(0.dp),
                isDarkTheme = darkTheme,
            ),
            actions = adminWorkbenchActions(
                onThemeToggle = toggleTheme,
            ),
            slots = adminWorkbenchSlots(
                showContentHeader = true,
                titleContent = {
                    headerRenderer.Render(
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                userContent = {
                    KCloudShellActions()
                },
            ),
        )
    }
}
