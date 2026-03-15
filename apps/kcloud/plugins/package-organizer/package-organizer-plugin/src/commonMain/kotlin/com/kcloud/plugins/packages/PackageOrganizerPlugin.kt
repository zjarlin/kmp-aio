package com.kcloud.plugins.packages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.packages.ui.PackageOrganizerScreen
import org.koin.core.annotation.Single

object PackageOrganizerPluginMenus {
    const val PACKAGES = "package-organizer"
}

@Single
class PackageOrganizerPlugin : KCloudPlugin {
    override val pluginId = "package-organizer-plugin"
    override val order = 45
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = PackageOrganizerPluginMenus.PACKAGES,
            title = "安装包归档",
            icon = Icons.Default.Inventory2,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 45,
            content = { PackageOrganizerScreen() }
        )
    )
}
