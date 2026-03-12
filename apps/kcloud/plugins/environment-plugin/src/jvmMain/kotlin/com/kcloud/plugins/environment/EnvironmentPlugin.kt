package com.kcloud.plugins.environment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.ui.screens.EnvironmentSetupScreen
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object EnvironmentPluginMenus {
    const val ENVIRONMENT_SETUP = "environment-setup"
}

private val environmentPluginModule = module {
    singleOf(::EnvironmentPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object EnvironmentPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(environmentPluginModule)
}

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
