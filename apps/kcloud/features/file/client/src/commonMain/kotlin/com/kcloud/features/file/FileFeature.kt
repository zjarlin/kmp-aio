package com.kcloud.features.file

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.file.ui.FileManagerScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

@Single
class FileFeature : Screen {
    override val id = FileFeatureMenus.FILE_MANAGER
    override val pid = KCloudScreenRoots.MANAGEMENT
    override val name = "文件管理"
    override val icon = Icons.Default.Folder
    override val sort = 30
    override val content = {
        FileManagerScreen()
    }
}
