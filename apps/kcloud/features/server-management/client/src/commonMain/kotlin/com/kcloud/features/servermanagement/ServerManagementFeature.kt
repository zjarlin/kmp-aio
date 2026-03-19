package com.kcloud.features.servermanagement

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.servermanagement.ui.ServerManagementScreen
import org.koin.core.annotation.Single

object ServerManagementFeatureMenus {
    const val SERVER_MANAGEMENT = "server-management"
}

@Single(binds = [KCloudFeature::class])
class ServerManagementFeature : KCloudFeature {
    override val featureId = "server-management"
    override val order = 20
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = ServerManagementFeatureMenus.SERVER_MANAGEMENT,
            title = "服务器管理",
            icon = Icons.Default.AccountBox,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 20,
            content = { ServerManagementScreen() }
        )
    )
}
