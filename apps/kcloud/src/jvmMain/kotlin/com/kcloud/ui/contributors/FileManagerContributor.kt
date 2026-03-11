package com.kcloud.ui.contributors

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import com.kcloud.plugin.ui.SidebarContributor
import com.kcloud.ui.MainViewModel
import com.kcloud.ui.screens.FileManagerScreen
import org.koin.compose.koinInject
import org.koin.core.annotation.Single

@Single
class FileManagerContributor : SidebarContributor {
    override val id = "file-manager"
    override val title = "文件管理"
    override val order = 30
    override val icon = Icons.Default.Folder

    @Composable
    override fun Content() {
        val viewModel = koinInject<MainViewModel>()
        FileManagerScreen(viewModel)
    }
}
