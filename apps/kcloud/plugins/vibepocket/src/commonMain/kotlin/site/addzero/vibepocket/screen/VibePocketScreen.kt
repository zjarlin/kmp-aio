package site.addzero.vibepocket.screen

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import site.addzero.annotation.Route
import site.addzero.vibepocket.music.MusicTaskResourcePage
import site.addzero.vibepocket.music.MusicVibeScreen
import site.addzero.vibepocket.screens.creativeassets.CreativeAssetsViewModel
import site.addzero.vibepocket.screens.musicstudio.MusicStudioViewModel
import site.addzero.vibepocket.screens.settings.SettingsViewModel
import site.addzero.vibepocket.settings.SettingsPage

@Route(
    value = "音乐创作",
    title = "音乐工作台",
    routePath = "vibepocket/music-studio",
    icon = "PlayArrow",
    order = 30.0,
)
@Composable
fun MusicStudioScreen() {
    val viewModel: MusicStudioViewModel = koinInject()
    MusicVibeScreen(viewModel = viewModel)
}

@Route(
    value = "音乐创作",
    title = "创作资产",
    routePath = "vibepocket/creative-assets",
    icon = "Dashboard",
    order = 40.0,
)
@Composable
fun CreativeAssetsScreen() {
    val viewModel: CreativeAssetsViewModel = koinInject()
    MusicTaskResourcePage(viewModel = viewModel)
}

@Route(
    value = "音乐创作",
    title = "设置",
    routePath = "vibepocket/settings",
    icon = "Settings",
    order = 90.0,
)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = koinInject()
    SettingsPage(viewModel = viewModel)
}
