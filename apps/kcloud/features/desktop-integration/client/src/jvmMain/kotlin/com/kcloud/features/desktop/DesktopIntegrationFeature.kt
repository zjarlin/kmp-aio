package com.kcloud.features.desktop

import com.kcloud.feature.KCloudFeature
import com.kcloud.feature.ShellWindowController
import com.kcloud.features.desktop.system.GlobalShortcutManager
import com.kcloud.features.desktop.system.SystemTrayManager
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single(binds = [KCloudFeature::class])
class DesktopIntegrationFeature(
    private val systemTrayManager: SystemTrayManager,
    private val globalShortcutManager: GlobalShortcutManager
) : KCloudFeature {
    override val featureId = "desktop-integration"
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
