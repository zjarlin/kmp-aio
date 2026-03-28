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
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.kcloud.feature.ShellThemeMode
import site.addzero.kcloud.ui.theme.KCloudTheme
import site.addzero.workbenchshell.spi.content.WorkbenchContentRenderer
import site.addzero.workbenchshell.spi.header.WorkbenchHeaderRenderer
import site.addzero.workbenchshell.spi.sidebar.WorkbenchSidebarRenderer

@Composable
fun MainWindow(
    shellSettingsService: ShellSettingsService = koinInject(),
    sidebarRenderer: WorkbenchSidebarRenderer = koinInject(),
    headerRenderer: WorkbenchHeaderRenderer = koinInject(),
    contentRenderer: WorkbenchContentRenderer = koinInject(),
) {
    val themeMode by shellSettingsService.themeMode.collectAsState()
    val darkTheme = when (themeMode) {
        ShellThemeMode.LIGHT -> false
        ShellThemeMode.DARK -> true
        ShellThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    KCloudTheme(
        darkTheme = darkTheme,
    ) {
        AdminWorkbenchScaffold(
            modifier = Modifier.fillMaxSize(),
            pageTitle = "KCloud",
            pageSubtitle = null,
            brandLabel = "KCloud",
            welcomeLabel = "",
            contentPadding = PaddingValues(0.dp),
            detailPadding = PaddingValues(0.dp),
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
            titleContent = {
                headerRenderer.Render(
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            isDarkTheme = darkTheme,
            onThemeToggle = {
                shellSettingsService.setThemeMode(themeMode.next())
            },
            userLabel = "KC",
            onUserClick = {},
        )
    }
}

private fun ShellThemeMode.next(): ShellThemeMode {
    return when (this) {
        ShellThemeMode.LIGHT -> ShellThemeMode.DARK
        ShellThemeMode.DARK -> ShellThemeMode.SYSTEM
        ShellThemeMode.SYSTEM -> ShellThemeMode.LIGHT
    }
}
