package com.kcloud.plugins.quicktransfer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.quicktransfer.ui.QuickTransferScreen
import org.koin.core.annotation.Single

@Single
class QuickTransferPlugin : KCloudPlugin {
    override val pluginId = "quick-transfer-plugin"
    override val order = 10
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = QuickTransferPluginMenus.QUICK_TRANSFER,
            title = "快速迁移",
            icon = Icons.AutoMirrored.Filled.Send,
            parentId = KCloudMenuGroups.SYNC,
            sortOrder = 10,
            content = { QuickTransferScreen() }
        )
    )
}
