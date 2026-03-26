package site.addzero.vibepocket.screens.settings

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import org.koin.compose.koinInject
import site.addzero.vibepocket.feature.VibePocketFeatureMenus
import site.addzero.vibepocket.settings.SettingsPage
import site.addzero.workbenchshell.Screen

@Single(binds = [Screen::class])
class SettingsScreen : Screen {
    override val id = VibePocketFeatureMenus.SETTINGS
    override val name = "设置"
    override val sort = 90
    override val content: (@Composable () -> Unit) = {
        SettingsScreenRoute()
    }
}

@Composable
private fun SettingsScreenRoute() {
    val viewModel: SettingsViewModel = koinInject()
    SettingsPage(viewModel = viewModel)
}
