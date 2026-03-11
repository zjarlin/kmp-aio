package com.kcloud.ui.contributors

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import com.kcloud.plugin.ui.SidebarContributor
import com.kcloud.ui.MainViewModel
import com.kcloud.ui.screens.QuickTransferScreen
import org.koin.compose.koinInject
import org.koin.core.annotation.Single

@Single
class QuickTransferContributor : SidebarContributor {
    override val id = "quick-transfer"
    override val title = "快速迁移"
    override val order = 10
    override val icon = Icons.Default.Send

    @Composable
    override fun Content() {
        val viewModel = koinInject<MainViewModel>()
        QuickTransferScreen(viewModel)
    }
}
