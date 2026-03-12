package com.kcloud.plugins.file

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.ui.screens.FileManagerScreen
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object FilePluginMenus {
    const val FILE_MANAGER = "file-manager"
}

private val filePluginModule = module {
    singleOf(::FilePlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object FilePluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(filePluginModule)
}

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
