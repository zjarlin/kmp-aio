package com.kcloud.features.environment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.Composable
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.environment.ui.EnvironmentSetupScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

object EnvironmentFeatureMenus {
    const val ENVIRONMENT_SETUP = "environment-setup"
}

@Single
class EnvironmentFeature : Screen {
    override val id = EnvironmentFeatureMenus.ENVIRONMENT_SETUP
    override val pid = KCloudScreenRoots.SYSTEM
    override val name = "环境搭建"
    override val icon = Icons.Default.Build
    override val sort = 90
    override val content: (@Composable () -> Unit) = {
        EnvironmentSetupScreen()
    }
}
