package site.addzero.kcloud.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.appsidebar.AdminWorkbenchScaffold
import site.addzero.appsidebar.adminWorkbenchActions
import site.addzero.appsidebar.adminWorkbenchConfig
import site.addzero.appsidebar.adminWorkbenchPageConfig
import site.addzero.appsidebar.adminWorkbenchSlots
import site.addzero.kcloud.ui.app.KCloudShellState
import site.addzero.kcloud.ui.app.menu.KCloudShellActions
import site.addzero.kcloud.ui.feature.ShellSettingsService
import site.addzero.kcloud.ui.feature.ShellThemeMode
import site.addzero.kcloud.ui.theme.KCloudTheme
import site.addzero.workbenchshell.spi.content.ContentRender
import site.addzero.workbenchshell.spi.header.HeaderRender
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

@Composable
fun MainWindow(
    shellSettingsService: ShellSettingsService = koinInject(),
    shellState: KCloudShellState = koinInject(),
    sidebarRenderer: SidebarRender = koinInject(),
    headerRenderer: HeaderRender = koinInject(),
    contentRenderer: ContentRender = koinInject(),
) {
    val themeMode by shellSettingsService.themeMode.collectAsState()
    val sidebarVisible = shellState.sidebarVisible
    val darkTheme = when (themeMode) {
        ShellThemeMode.LIGHT -> false
        ShellThemeMode.DARK -> true
        ShellThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    KCloudTheme(
        darkTheme = darkTheme,
    ) {
        val toggleTheme = {
            shellSettingsService.setThemeMode(
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
