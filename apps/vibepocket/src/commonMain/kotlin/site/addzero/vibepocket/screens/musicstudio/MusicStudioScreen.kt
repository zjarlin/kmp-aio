package site.addzero.vibepocket.screens.musicstudio

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import site.addzero.vibepocket.feature.VibePocketFeatureMenus
import site.addzero.vibepocket.music.MusicVibeScreen
import site.addzero.workbenchshell.Screen

object MusicStudioScreen : Screen {
    override val id = VibePocketFeatureMenus.MUSIC_STUDIO
    override val name = "音乐工作台"
    override val sort = 10
    override val content: (@Composable () -> Unit) = {
        MusicStudioScreenRoute()
    }
}

@Composable
private fun MusicStudioScreenRoute() {
    val viewModel: MusicStudioViewModel = koinInject()
    MusicVibeScreen(viewModel = viewModel)
}
