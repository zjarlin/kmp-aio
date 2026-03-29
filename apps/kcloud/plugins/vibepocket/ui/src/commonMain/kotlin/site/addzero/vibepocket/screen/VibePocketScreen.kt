package site.addzero.vibepocket.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.music.MusicTaskResourcePage
import site.addzero.kcloud.music.MusicVibeScreen
import site.addzero.kcloud.screens.creativeassets.CreativeAssetsViewModel
import site.addzero.kcloud.screens.musicstudio.MusicStudioViewModel
import site.addzero.kcloud.screens.settings.SettingsViewModel
import site.addzero.kcloud.settings.SettingsPage
import site.addzero.liquidglass.LiquidGlassWorkbenchRoot

@Route(
    value = "创作中心",
    title = "音乐工作台",
    routePath = "vibepocket/music-studio",
    icon = "PlayArrow",
    order = 30.0,
    enabled = false,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "音乐创作",
            icon = "PlayArrow",
            order = 200,
        ),
        defaultInScene = true,
    ),
)
@Composable
fun MusicStudioScreen() {
    val viewModel: MusicStudioViewModel = koinInject()
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.dispose()
        }
    }
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
    enabled = false,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "音乐创作",
            icon = "PlayArrow",
            order = 200,
        ),
    ),
)
@Composable
fun CreativeAssetsScreen() {
    val viewModel: CreativeAssetsViewModel = koinInject()
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.dispose()
        }
    }
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
    enabled = false,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "音乐创作",
            icon = "PlayArrow",
            order = 200,
        ),
    ),
)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = koinInject()
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.dispose()
        }
    }
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
