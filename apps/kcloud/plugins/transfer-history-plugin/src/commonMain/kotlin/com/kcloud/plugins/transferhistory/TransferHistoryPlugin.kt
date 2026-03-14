package com.kcloud.plugins.transferhistory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.transferhistory.ui.TransferHistoryScreen
import org.koin.core.annotation.Single

object TransferHistoryPluginMenus {
    const val TRANSFER_HISTORY = "transfer-history"
}

@Single
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
