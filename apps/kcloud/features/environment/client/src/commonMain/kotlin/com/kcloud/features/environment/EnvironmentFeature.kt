package com.kcloud.features.environment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.environment.ui.EnvironmentSetupScreen
import org.koin.core.annotation.Single

object EnvironmentFeatureMenus {
    const val ENVIRONMENT_SETUP = "environment-setup"
}

@Single(binds = [KCloudFeature::class])
class EnvironmentFeature : KCloudFeature {
    override val featureId = "environment"
    override val order = 90
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = EnvironmentFeatureMenus.ENVIRONMENT_SETUP,
            title = "环境搭建",
            icon = Icons.Default.Build,
            parentId = KCloudMenuGroups.SYSTEM,
            sortOrder = 90,
            visible = true,
            content = { EnvironmentSetupScreen() }
        )
    )
}
