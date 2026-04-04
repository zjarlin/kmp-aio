package site.addzero.kcloud.shell.navigation

import org.koin.core.annotation.Single
import site.addzero.workbenchshell.spi.screen.Screen
import site.addzero.workbenchshell.spi.screen.ScreenSource
import site.addzero.workbenchshell.spi.screen.mergeScreens

@Single
class ScreenRegistry(
    private val screenSources: List<ScreenSource>,
) {
    val screens
        get() = mergeScreens(screenSources)
}
