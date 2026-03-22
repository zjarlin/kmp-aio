package com.kcloud.features.desktop

import com.kcloud.feature.DesktopLifecycleContributor
import com.kcloud.feature.ShellTrayPanelController
import com.kcloud.feature.ShellWindowController
import com.kcloud.features.desktop.system.GlobalShortcutManager
import com.kcloud.features.desktop.system.SystemTrayManager
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single
class DesktopIntegrationFeature(
    private val systemTrayManager: SystemTrayManager,
    private val globalShortcutManager: GlobalShortcutManager,
) : DesktopLifecycleContributor {
    override val order = 1

    override fun onStart(koin: Koin) {
        val shellWindowController = koin.get<ShellWindowController>()
        val shellTrayPanelController = koin.get<ShellTrayPanelController>()
        systemTrayManager.install(
            onTogglePanel = shellTrayPanelController::toggleTrayPanel,
            onShowWindow = shellWindowController::showWindow,
            onExit = shellWindowController::requestExit,
        )
        globalShortcutManager.register()
    }

    override fun onStop(koin: Koin) {
        globalShortcutManager.unregister()
        systemTrayManager.uninstall()
    }
}
