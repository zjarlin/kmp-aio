package com.kcloud.plugins.environment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.environment.ui.EnvironmentSetupScreen
import org.koin.core.annotation.Single

object EnvironmentPluginMenus {
    const val ENVIRONMENT_SETUP = "environment-setup"
}

@Single
class EnvironmentPlugin : KCloudPlugin {
    override val pluginId = "environment-plugin"
    override val order = 90
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = EnvironmentPluginMenus.ENVIRONMENT_SETUP,
            title = "环境搭建",
            icon = Icons.Default.Build,
            parentId = KCloudMenuGroups.SYSTEM,
            sortOrder = 90,
            visible = true,
            content = { EnvironmentSetupScreen() }
        )
    )
}
