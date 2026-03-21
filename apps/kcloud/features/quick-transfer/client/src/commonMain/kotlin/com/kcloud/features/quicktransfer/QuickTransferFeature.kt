package com.kcloud.features.quicktransfer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.quicktransfer.ui.QuickTransferScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

@Single
class QuickTransferFeature : Screen {
    override val id = QuickTransferFeatureMenus.QUICK_TRANSFER
    override val pid = KCloudScreenRoots.SYNC
    override val name = "快速迁移"
    override val icon = Icons.AutoMirrored.Filled.Send
    override val sort = 10
    override val content = {
        QuickTransferScreen()
    }
}
