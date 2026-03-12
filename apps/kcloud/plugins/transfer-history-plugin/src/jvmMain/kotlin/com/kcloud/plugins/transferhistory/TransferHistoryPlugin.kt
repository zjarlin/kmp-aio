package com.kcloud.plugins.transferhistory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.ui.screens.TransferHistoryScreen
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object TransferHistoryPluginMenus {
    const val TRANSFER_HISTORY = "transfer-history"
}

private val transferHistoryPluginModule = module {
    singleOf(::TransferHistoryPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object TransferHistoryPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(transferHistoryPluginModule)
}

class TransferHistoryPlugin : KCloudPlugin {
    override val pluginId = "transfer-history-plugin"
    override val order = 40
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = TransferHistoryPluginMenus.TRANSFER_HISTORY,
            title = "迁移记录",
            icon = Icons.Default.Info,
            parentId = KCloudMenuGroups.SYNC,
            sortOrder = 40,
            content = { TransferHistoryScreen() }
        )
    )
}
