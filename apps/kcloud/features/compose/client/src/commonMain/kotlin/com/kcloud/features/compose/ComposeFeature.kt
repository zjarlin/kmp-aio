package com.kcloud.features.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.compose.ui.ComposeManagerScreen
import org.koin.core.annotation.Single

object ComposeFeatureMenus {
    const val COMPOSE_MANAGER = "compose-manager"
}

@Single(binds = [KCloudFeature::class])
class ComposeFeature : KCloudFeature {
    override val featureId: String = "compose"
    override val order: Int = 25
    override val menuEntries: List<KCloudMenuEntry> = listOf(
        KCloudMenuEntry(
            id = ComposeFeatureMenus.COMPOSE_MANAGER,
            title = "Compose 管理",
            icon = Icons.Default.Dns,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 25,
            content = { ComposeManagerScreen() }
        )
    )
}
