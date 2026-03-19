package com.kcloud.features.packages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.packages.ui.PackageOrganizerScreen
import org.koin.core.annotation.Single

object PackageOrganizerFeatureMenus {
    const val PACKAGES = "package-organizer"
}

@Single(binds = [KCloudFeature::class])
class PackageOrganizerFeature : KCloudFeature {
    override val featureId = "package-organizer"
    override val order = 45
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = PackageOrganizerFeatureMenus.PACKAGES,
            title = "安装包归档",
            icon = Icons.Default.Inventory2,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 45,
            content = { PackageOrganizerScreen() }
        )
    )
}
