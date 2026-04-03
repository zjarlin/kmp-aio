package site.addzero.kcloud.window.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.appsidebar.AdminWorkbenchScaffold
import site.addzero.appsidebar.adminWorkbenchConfig
import site.addzero.appsidebar.adminWorkbenchPageConfig
import site.addzero.appsidebar.adminWorkbenchSlots
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.shell.content.KCloudContentRender
import site.addzero.kcloud.shell.header.KCloudHeaderRender
import site.addzero.kcloud.shell.menu.KCloudShellActions
import site.addzero.kcloud.shell.sidebar.KCloudSidebarRender
import site.addzero.kcloud.theme.Theme
import site.addzero.kcloud.theme.ShellThemeMode
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.currentKCloudUiMetrics
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
    val uiMetrics = currentKCloudUiMetrics()
    val darkTheme = themeMode.resolveDarkTheme(
        systemDarkTheme = isSystemInDarkTheme(),
    )

    Theme(
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
                brandLabel = "OKMY DICS",
                welcomeLabel = "",
                defaultSidebarRatio = if (sidebarVisible) uiMetrics.sidebarRatio else 0f,
                minSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMinWidth else 0.dp,
                maxSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMaxWidth else 0.dp,
                contentPadding = PaddingValues(0.dp),
                detailPadding = PaddingValues(0.dp),
                isDarkTheme = darkTheme,
            ),
            slots = adminWorkbenchSlots(
                brandContent = {
                    KCloudBrandSlot()
                    headerRenderer.Render(
                        modifier = Modifier.weight(1f),
                    )
                },
                showContentHeader = false,
                userContent = {
                    KCloudShellActions(
                        darkTheme = darkTheme,
                        onThemeToggle = toggleTheme,
                    )
                },
            ),
        )
    }
}

@Composable
private fun RowScope.KCloudBrandSlot() {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "OKMY DICS",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.8.sp,
        )
    }
}
