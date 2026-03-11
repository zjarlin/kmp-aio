package com.kcloud.ui.contributors

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.runtime.Composable
import com.kcloud.plugin.ui.SidebarContributor
import com.kcloud.ui.MainViewModel
import com.kcloud.ui.screens.ServerManagementScreen
import org.koin.compose.koinInject
import org.koin.core.annotation.Single

@Single
class ServerManagementContributor : SidebarContributor {
    override val id = "server-management"
    override val title = "服务器管理"
    override val order = 20
    override val icon = Icons.Default.AccountBox

    @Composable
    override fun Content() {
        val viewModel = koinInject<MainViewModel>()
        ServerManagementScreen(viewModel)
    }
}
