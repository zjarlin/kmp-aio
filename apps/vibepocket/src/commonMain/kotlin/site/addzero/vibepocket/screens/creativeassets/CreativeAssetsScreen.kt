package site.addzero.vibepocket.screens.creativeassets

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import site.addzero.vibepocket.feature.VibePocketFeatureMenus
import site.addzero.vibepocket.music.MusicTaskResourcePage
import site.addzero.workbenchshell.Screen

object CreativeAssetsScreen : Screen {
    override val id = VibePocketFeatureMenus.CREATIVE_ASSETS
    override val name = "创作资产"
    override val sort = 20
    override val content: (@Composable () -> Unit) = {
        CreativeAssetsScreenRoute()
    }
}

@Composable
private fun CreativeAssetsScreenRoute() {
    val viewModel: CreativeAssetsViewModel = koinInject()
    MusicTaskResourcePage(viewModel = viewModel)
}
