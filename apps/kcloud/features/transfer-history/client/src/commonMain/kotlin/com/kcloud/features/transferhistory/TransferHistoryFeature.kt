package com.kcloud.features.transferhistory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.transferhistory.ui.TransferHistoryScreen
import org.koin.core.annotation.Single

object TransferHistoryFeatureMenus {
    const val TRANSFER_HISTORY = "transfer-history"
}

@Single(binds = [KCloudFeature::class])
class TransferHistoryFeature : KCloudFeature {
    override val featureId = "transfer-history"
    override val order = 40
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = TransferHistoryFeatureMenus.TRANSFER_HISTORY,
            title = "迁移记录",
            icon = Icons.Default.Info,
            parentId = KCloudMenuGroups.SYNC,
            sortOrder = 40,
            content = { TransferHistoryScreen() }
        )
    )
}
