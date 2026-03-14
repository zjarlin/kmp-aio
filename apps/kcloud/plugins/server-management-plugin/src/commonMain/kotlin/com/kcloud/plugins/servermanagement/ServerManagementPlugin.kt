package com.kcloud.plugins.servermanagement

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.servermanagement.ui.ServerManagementScreen
import org.koin.core.annotation.Single

object ServerManagementPluginMenus {
    const val SERVER_MANAGEMENT = "server-management"
}

@Single
class ServerManagementPlugin : KCloudPlugin {
    override val pluginId = "server-management-plugin"
    override val order = 20
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = ServerManagementPluginMenus.SERVER_MANAGEMENT,
            title = "服务器管理",
            icon = Icons.Default.AccountBox,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 20,
            content = { ServerManagementScreen() }
        )
    )
}
