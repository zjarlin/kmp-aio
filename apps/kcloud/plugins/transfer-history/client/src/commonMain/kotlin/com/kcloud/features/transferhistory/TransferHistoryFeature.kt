package com.kcloud.features.transferhistory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.transferhistory.ui.TransferHistoryScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

object TransferHistoryFeatureMenus {
    const val TRANSFER_HISTORY = "transfer-history"
}

@Single
class TransferHistoryFeature : Screen {
    override val id = TransferHistoryFeatureMenus.TRANSFER_HISTORY
    override val pid = KCloudScreenRoots.WORKSPACE
    override val name = "迁移记录"
    override val icon = Icons.Default.Info
    override val sort = 40
    override val content: (@Composable () -> Unit) = {
        TransferHistoryScreen()
    }
}
