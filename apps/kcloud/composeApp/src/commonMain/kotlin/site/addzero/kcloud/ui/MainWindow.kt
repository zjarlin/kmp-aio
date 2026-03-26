package site.addzero.kcloud.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.feature.ShellThemeMode
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.kcloud.ui.theme.KCloudTheme
import org.koin.compose.koinInject
import site.addzero.workbenchshell.RenderWorkbenchScaffold

@Composable
fun MainWindow(
    shellSettingsService: ShellSettingsService = koinInject(),
) {
    val themeMode by shellSettingsService.themeMode.collectAsState()

    KCloudTheme(
        darkTheme = when (themeMode) {
            ShellThemeMode.LIGHT -> false
            ShellThemeMode.DARK -> true
            ShellThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        },
    ) {
        RenderWorkbenchScaffold(
            modifier = Modifier.fillMaxSize(),
            contentHeaderScrollable = false,
            minSidebarWidth = 248.dp,
            maxSidebarWidth = 340.dp,
        )
    }
}
