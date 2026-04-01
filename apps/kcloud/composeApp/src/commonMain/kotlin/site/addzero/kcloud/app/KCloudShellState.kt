package site.addzero.kcloud.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.generated.RouteKeys
import site.addzero.kcloud.feature.ShellTrayPanelController
import site.addzero.kcloud.feature.ShellWindowController

@Single
class KCloudShellState(
    private val routeCatalog: KCloudRouteCatalog,
) : ShellWindowController, ShellTrayPanelController {
    private val startupRoutePath = routeCatalog.findRoute(RouteKeys.MCU_CONTROL_SCREEN)?.routePath
        ?: routeCatalog.defaultRoutePath

    val backStack = mutableStateListOf(startupRoutePath)

    val selectedRoutePath: String
        get() = backStack.lastOrNull() ?: startupRoutePath

    val selectedSceneId: String
        get() = routeCatalog.sceneIdFor(selectedRoutePath)

    var windowVisible by mutableStateOf(true)
        private set

    var exitRequested by mutableStateOf(false)
        private set

    var trayPanelVisible by mutableStateOf(false)
        private set

    fun selectRoute(
        routePath: String,
    ) {
        if (routeCatalog.findRoute(routePath) == null) {
            return
        }
        navigateToRoute(routePath)
    }

    fun selectScene(
        sceneId: String,
    ) {
        val routePath = routeCatalog.firstRoutePathUnder(sceneId) ?: return
        navigateToRoute(routePath)
    }

    override fun showWindow() {
        windowVisible = true
    }

    override fun hideWindow() {
        windowVisible = false
    }

    override fun toggleWindow() {
        windowVisible = !windowVisible
    }

    override fun requestExit() {
        trayPanelVisible = false
        exitRequested = true
    }

    override fun showTrayPanel() {
        trayPanelVisible = true
    }

    override fun hideTrayPanel() {
        trayPanelVisible = false
    }

    override fun toggleTrayPanel() {
        trayPanelVisible = !trayPanelVisible
    }

    fun popNavigation() {
        if (backStack.size > 1) {
            backStack.removeLast()
        }
    }

    private fun navigateToRoute(
        routePath: String,
    ) {
        if (backStack.isEmpty()) {
            backStack += routePath
            return
        }
        if (backStack.last() == routePath) {
            return
        }

        // 工作台导航保持单选语义，切页时替换当前栈顶而不是无限累积历史。
        backStack[backStack.lastIndex] = routePath
    }
}
