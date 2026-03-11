package com.kcloud.ui.contributors

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import com.kcloud.plugin.ui.SidebarContributor
import com.kcloud.ui.MainViewModel
import com.kcloud.ui.screens.SettingsScreen
import org.koin.compose.koinInject
import org.koin.core.annotation.Single

@Single
class SettingsContributor : SidebarContributor {
    override val id = "settings"
    override val title = "设置"
    override val order = 100
    override val icon = Icons.Default.Settings

    @Composable
    override fun Content() {
        val viewModel = koinInject<MainViewModel>()
        SettingsScreen(viewModel)
    }
}
