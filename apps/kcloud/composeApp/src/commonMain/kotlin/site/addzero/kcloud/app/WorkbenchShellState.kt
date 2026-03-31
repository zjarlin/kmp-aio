package site.addzero.kcloud.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import site.addzero.generated.RouteKeys
import site.addzero.kcloud.feature.ShellTrayPanelController
import site.addzero.kcloud.feature.ShellWindowController

class WorkbenchShellState(
    private val routeCatalog: WorkbenchRouteCatalog,
) : ShellWindowController, ShellTrayPanelController {
    private val startupRoutePath = routeCatalog.findRoute(RouteKeys.MCU_CONTROL_SCREEN)?.routePath
        ?: routeCatalog.defaultRoutePath

    val backStack = NavBackStack(
        WorkbenchNavRoute(
            routePath = startupRoutePath,
        ),
    )

    val selectedRoute: WorkbenchNavRoute
        get() = backStack.lastOrNull()
            ?: WorkbenchNavRoute(routePath = startupRoutePath)

    val selectedRoutePath: String
        get() = selectedRoute.routePath

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
        val route = WorkbenchNavRoute(routePath = routePath)
        if (backStack.isEmpty()) {
            backStack += route
            return
        }
        if (backStack.last() == route) {
            return
        }

        // 工作台导航保持单选语义，切页时替换当前栈顶而不是无限累积历史。
        backStack[backStack.lastIndex] = route
    }
}
