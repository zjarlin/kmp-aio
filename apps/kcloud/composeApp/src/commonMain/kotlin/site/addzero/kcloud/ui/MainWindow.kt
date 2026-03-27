package site.addzero.kcloud.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.ui.theme.KCloudTheme
import org.koin.compose.koinInject
import site.addzero.kcloud.viewmodel.ThemeViewModel
import site.addzero.workbenchshell.RenderWorkbenchScaffold

@Composable
fun MainWindow(
    themeViewModel: ThemeViewModel,
) {
//    val themeMode by shellSettingsService.themeMode.collectAsState()
    KCloudTheme {
        RenderWorkbenchScaffold(
            modifier = Modifier.fillMaxSize(),
            contentHeaderScrollable = false,
            minSidebarWidth = 248.dp,
            maxSidebarWidth = 340.dp,
        )
    }
}
