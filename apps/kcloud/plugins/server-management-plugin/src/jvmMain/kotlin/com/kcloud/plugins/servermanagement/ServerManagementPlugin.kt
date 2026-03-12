package com.kcloud.plugins.servermanagement

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.ui.screens.ServerManagementScreen
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object ServerManagementPluginMenus {
    const val SERVER_MANAGEMENT = "server-management"
}

private val serverManagementPluginModule = module {
    singleOf(::ServerManagementPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object ServerManagementPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(serverManagementPluginModule)
}

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
