package com.kcloud.features.quicktransfer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.quicktransfer.ui.QuickTransferScreen
import org.koin.core.annotation.Single

@Single(binds = [KCloudFeature::class])
class QuickTransferFeature : KCloudFeature {
    override val featureId = "quick-transfer"
    override val order = 10
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = QuickTransferFeatureMenus.QUICK_TRANSFER,
            title = "快速迁移",
            icon = Icons.AutoMirrored.Filled.Send,
            parentId = KCloudMenuGroups.SYNC,
            sortOrder = 10,
            content = { QuickTransferScreen() }
        )
    )
}
