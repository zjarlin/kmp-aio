package com.kcloud.plugins.dotfiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.dotfiles.ui.DotfilesScreen
import org.koin.core.annotation.Single

object DotfilesPluginMenus {
    const val DOTFILES = "dotfiles"
}

@Single
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
