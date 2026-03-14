package com.kcloud.plugins.webdav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.webdav.ui.WebDavWorkspaceScreen
import org.koin.core.annotation.Single

object WebDavPluginMenus {
    const val WEBDAV = "webdav"
}

@Single
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
