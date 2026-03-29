package site.addzero.kbox.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.appsidebar.AdminWorkbenchScaffold
import site.addzero.kbox.feature.KboxShellSettingsService
import site.addzero.kbox.feature.KboxShellThemeMode
import site.addzero.kbox.plugin.api.KboxPluginManagerService
import site.addzero.kbox.ui.theme.KboxTheme
import site.addzero.workbenchshell.spi.content.ContentRender
import site.addzero.workbenchshell.spi.header.HeaderRender
import site.addzero.workbenchshell.spi.sidebar.SidebarRenderer

@Composable
fun MainWindow(
    shellSettingsService: KboxShellSettingsService = koinInject(),
    pluginManagerService: KboxPluginManagerService = koinInject(),
    sidebarRenderer: SidebarRenderer = koinInject(),
    headerRenderer: HeaderRender = koinInject(),
    contentRenderer: ContentRender = koinInject(),
) {
    val themeMode by shellSettingsService.themeMode.collectAsState()
    val darkTheme = when (themeMode) {
        KboxShellThemeMode.LIGHT -> false
        KboxShellThemeMode.DARK -> true
        KboxShellThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    LaunchedEffect(Unit) {
        pluginManagerService.refresh()
    }

    KboxTheme(darkTheme = darkTheme) {
        AdminWorkbenchScaffold(
            modifier = Modifier.fillMaxSize(),
            pageTitle = "KBox",
            pageSubtitle = null,
            brandLabel = "KBox",
            welcomeLabel = "",
            contentPadding = PaddingValues(0.dp),
            detailPadding = PaddingValues(0.dp),
            sidebar = {
                sidebarRenderer.Render(modifier = Modifier.fillMaxSize())
            },
            content = {
                contentRenderer.Render(modifier = Modifier.fillMaxSize())
            },
            titleContent = {
                headerRenderer.Render(modifier = Modifier.fillMaxWidth())
            },
            isDarkTheme = darkTheme,
            onThemeToggle = {
                shellSettingsService.setThemeMode(
                    if (darkTheme) {
                        KboxShellThemeMode.LIGHT
                    } else {
                        KboxShellThemeMode.DARK
                    },
                )
            },
            userContent = {},
        )
    }
}
