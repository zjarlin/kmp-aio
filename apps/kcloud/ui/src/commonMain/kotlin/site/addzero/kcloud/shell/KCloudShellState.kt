package site.addzero.kcloud.shell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.shell.navigation.KCloudRouteCatalog

@Single
class KCloudShellState(
    private val routeCatalog: KCloudRouteCatalog,
) {
    private val startupRoutePath = routeCatalog.defaultRoutePath
        .ifBlank { routeCatalog.routeEntries.firstOrNull()?.routePath.orEmpty() }

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

    var sidebarVisible by mutableStateOf(true)
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

    fun showWindow() {
        windowVisible = true
    }

    fun hideWindow() {
        windowVisible = false
    }

    fun toggleWindow() {
        windowVisible = !windowVisible
    }

    fun requestExit() {
        trayPanelVisible = false
        exitRequested = true
    }

    fun showTrayPanel() {
        trayPanelVisible = true
    }

    fun hideTrayPanel() {
        trayPanelVisible = false
    }

    fun toggleTrayPanel() {
        trayPanelVisible = !trayPanelVisible
    }

    fun showSidebar() {
        sidebarVisible = true
    }

    fun hideSidebar() {
        sidebarVisible = false
    }

    fun toggleSidebar() {
        sidebarVisible = !sidebarVisible
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
