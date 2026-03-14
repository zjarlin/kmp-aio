package com.kcloud.plugins.file

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.file.ui.FileManagerScreen
import org.koin.core.annotation.Single

@Single
class FilePlugin : KCloudPlugin {
    override val pluginId = "file-plugin"
    override val order = 30
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = FilePluginMenus.FILE_MANAGER,
            title = "文件管理",
            icon = Icons.Default.Folder,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 30,
            content = { FileManagerScreen() }
        )
    )
}
