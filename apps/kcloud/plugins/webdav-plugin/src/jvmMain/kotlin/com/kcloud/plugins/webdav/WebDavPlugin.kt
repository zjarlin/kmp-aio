package com.kcloud.plugins.webdav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.ui.screens.WebDavWorkspaceScreen
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object WebDavPluginMenus {
    const val WEBDAV = "webdav"
}

private val webDavPluginModule = module {
    singleOf(::WebDavPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object WebDavPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(webDavPluginModule)
}

class WebDavPlugin : KCloudPlugin {
    override val pluginId = "webdav-plugin"
    override val order = 70
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = WebDavPluginMenus.WEBDAV,
            title = "WebDAV",
            icon = Icons.Default.Cloud,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 70,
            content = { WebDavWorkspaceScreen() }
        )
    )
}
