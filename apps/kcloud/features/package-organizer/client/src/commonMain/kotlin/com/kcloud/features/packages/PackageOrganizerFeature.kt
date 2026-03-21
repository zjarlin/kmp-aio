package com.kcloud.features.packages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.packages.ui.PackageOrganizerScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

object PackageOrganizerFeatureMenus {
    const val PACKAGES = "package-organizer"
}

@Single
class PackageOrganizerFeature : Screen {
    override val id = PackageOrganizerFeatureMenus.PACKAGES
    override val pid = KCloudScreenRoots.MANAGEMENT
    override val name = "安装包归档"
    override val icon = Icons.Default.Inventory2
    override val sort = 45
    override val content = {
        PackageOrganizerScreen()
    }
}
