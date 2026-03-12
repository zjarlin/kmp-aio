package com.kcloud.plugins.packages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.ui.screens.PackageOrganizerScreen
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object PackageOrganizerPluginMenus {
    const val PACKAGES = "package-organizer"
}

private val packageOrganizerPluginModule = module {
    singleOf(::PackageOrganizerPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object PackageOrganizerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(packageOrganizerPluginModule)
}

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
