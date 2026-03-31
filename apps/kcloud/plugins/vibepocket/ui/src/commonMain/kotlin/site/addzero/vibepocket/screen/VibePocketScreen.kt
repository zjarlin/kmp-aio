package site.addzero.vibepocket.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.music.MusicHistoryScreen
import site.addzero.kcloud.music.MusicTaskResourceScreen
import site.addzero.kcloud.music.MusicVibeScreen
import site.addzero.kcloud.settings.SettingsScreen
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
fun MusicStudioRouteScreen() {
    VibePocketSceneRoot {
        MusicVibeScreen()
    }
}

@Route(
    value = "创作中心",
    title = "音乐库",
    routePath = "vibepocket/music-library",
    icon = "LibraryMusic",
    order = 35.0,
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
fun MusicLibraryRouteScreen() {
    VibePocketSceneRoot {
        MusicHistoryScreen()
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
fun CreativeAssetsRouteScreen() {
    VibePocketSceneRoot {
        MusicTaskResourceScreen()
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
fun SettingsRouteScreen() {
    VibePocketSceneRoot {
        SettingsScreen()
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
