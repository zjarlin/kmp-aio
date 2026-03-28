package site.addzero.vibepocket.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.liquidglass.LiquidGlassWorkbenchRoot
import site.addzero.vibepocket.music.MusicTaskResourcePage
import site.addzero.vibepocket.music.MusicVibeScreen
import site.addzero.vibepocket.screens.creativeassets.CreativeAssetsViewModel
import site.addzero.vibepocket.screens.musicstudio.MusicStudioViewModel
import site.addzero.vibepocket.screens.settings.SettingsViewModel
import site.addzero.vibepocket.settings.SettingsPage

@Route(
    value = "创作中心",
    title = "音乐工作台",
    routePath = "vibepocket/music-studio",
    icon = "PlayArrow",
    order = 30.0,
)
@Composable
fun MusicStudioScreen() {
    val viewModel: MusicStudioViewModel = koinViewModel()
    VibePocketSceneRoot {
        MusicVibeScreen(viewModel = viewModel)
    }
}

@Route(
    value = "创作中心",
    title = "创作资产",
    routePath = "vibepocket/creative-assets",
    icon = "Dashboard",
    order = 40.0,
)
@Composable
fun CreativeAssetsScreen() {
    val viewModel: CreativeAssetsViewModel = koinViewModel()
    VibePocketSceneRoot {
        MusicTaskResourcePage(viewModel = viewModel)
    }
}

@Route(
    value = "系统设置",
    title = "设置",
    routePath = "vibepocket/settings",
    icon = "Settings",
    order = 90.0,
)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()
    VibePocketSceneRoot {
        SettingsPage(viewModel = viewModel)
    }
}

@Composable
private fun VibePocketSceneRoot(
    content: @Composable () -> Unit,
) {
    LiquidGlassWorkbenchRoot(
        modifier = Modifier.fillMaxSize(),
    ) {
        content()
    }
}
