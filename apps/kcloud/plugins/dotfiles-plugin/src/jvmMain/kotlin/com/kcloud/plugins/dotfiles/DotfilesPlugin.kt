package com.kcloud.plugins.dotfiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.ui.screens.DotfilesScreen
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object DotfilesPluginMenus {
    const val DOTFILES = "dotfiles"
}

private val dotfilesPluginModule = module {
    singleOf(::DotfilesPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object DotfilesPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(dotfilesPluginModule)
}

class DotfilesPlugin : KCloudPlugin {
    override val pluginId = "dotfiles-plugin"
    override val order = 80
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = DotfilesPluginMenus.DOTFILES,
            title = "Dotfiles",
            icon = Icons.Default.Description,
            parentId = KCloudMenuGroups.SYSTEM,
            sortOrder = 80,
            content = { DotfilesScreen() }
        )
    )
}
