package com.kcloud.ui.contributors

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import com.kcloud.plugin.ui.SidebarContributor
import com.kcloud.ui.MainViewModel
import com.kcloud.ui.screens.TransferHistoryScreen
import org.koin.compose.koinInject
import org.koin.core.annotation.Single

@Single
class TransferHistoryContributor : SidebarContributor {
    override val id = "transfer-history"
    override val title = "迁移记录"
    override val order = 40
    override val icon = Icons.Default.Info

    @Composable
    override fun Content() {
        val viewModel = koinInject<MainViewModel>()
        TransferHistoryScreen(viewModel)
    }
}
