package com.kcloud.plugins.quicktransfer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.ui.screens.QuickTransferScreen
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object QuickTransferPluginMenus {
    const val QUICK_TRANSFER = "quick-transfer"
}

private val quickTransferPluginModule = module {
    singleOf(::QuickTransferPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object QuickTransferPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(quickTransferPluginModule)
}

class QuickTransferPlugin : KCloudPlugin {
    override val pluginId = "quick-transfer-plugin"
    override val order = 10
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = QuickTransferPluginMenus.QUICK_TRANSFER,
            title = "快速迁移",
            icon = Icons.Default.Send,
            parentId = KCloudMenuGroups.SYNC,
            sortOrder = 10,
            content = { QuickTransferScreen() }
        )
    )
}
