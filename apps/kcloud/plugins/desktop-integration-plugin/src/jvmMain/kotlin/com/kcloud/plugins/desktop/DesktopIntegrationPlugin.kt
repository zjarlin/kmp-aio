package com.kcloud.plugins.desktop

import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.ShellWindowController
import com.kcloud.plugins.desktop.system.GlobalShortcutManager
import com.kcloud.plugins.desktop.system.SystemTrayManager
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single
class DesktopIntegrationPlugin(
    private val systemTrayManager: SystemTrayManager,
    private val globalShortcutManager: GlobalShortcutManager
) : KCloudPlugin {
    override val pluginId = "desktop-integration-plugin"
    override val order = 1

    override fun onStart(koin: Koin) {
        val shellWindowController = koin.get<ShellWindowController>()
        systemTrayManager.install(
            onShowWindow = shellWindowController::showWindow,
            onExit = shellWindowController::requestExit
        )
        globalShortcutManager.register()
    }

    override fun onStop(koin: Koin) {
        globalShortcutManager.unregister()
        systemTrayManager.uninstall()
    }
}
