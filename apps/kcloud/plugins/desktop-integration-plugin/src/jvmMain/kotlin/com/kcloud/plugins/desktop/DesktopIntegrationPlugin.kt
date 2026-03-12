package com.kcloud.plugins.desktop

import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.ShellWindowController
import com.kcloud.system.GlobalShortcutManager
import com.kcloud.system.SystemTrayManager
import org.koin.core.Koin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val desktopIntegrationPluginModule = module {
    single { SystemTrayManager() }
    single { GlobalShortcutManager() }
    singleOf(::DesktopIntegrationPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object DesktopIntegrationPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(desktopIntegrationPluginModule)
}

class DesktopIntegrationPlugin : KCloudPlugin {
    override val pluginId = "desktop-integration-plugin"
    override val order = 1

    override fun onStart(koin: Koin) {
        val shellWindowController = koin.get<ShellWindowController>()
        koin.get<SystemTrayManager>().install(
            onShowWindow = shellWindowController::showWindow,
            onExit = shellWindowController::requestExit
        )
        koin.get<GlobalShortcutManager>().register()
    }

    override fun onStop(koin: Koin) {
        koin.get<GlobalShortcutManager>().unregister()
        koin.get<SystemTrayManager>().uninstall()
    }
}
