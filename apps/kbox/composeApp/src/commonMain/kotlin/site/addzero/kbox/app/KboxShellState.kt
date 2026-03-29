package site.addzero.kbox.app

import androidx.navigation3.runtime.NavBackStack

class KboxShellState(
    private val routeCatalog: KboxRouteCatalog,
) {
    val backStack = NavBackStack(
        KboxNavRoute(routePath = routeCatalog.defaultRoutePath),
    )

    val selectedRoute: KboxNavRoute
        get() = backStack.lastOrNull() ?: KboxNavRoute(routePath = routeCatalog.defaultRoutePath)

    val selectedRoutePath: String
        get() = selectedRoute.routePath

    val selectedSceneId: String
        get() = routeCatalog.sceneIdFor(selectedRoutePath)

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

    fun popNavigation() {
        if (backStack.size > 1) {
            backStack.removeLast()
        }
    }

    fun ensureRouteSelected() {
        if (routeCatalog.findRoute(selectedRoutePath) != null) {
            return
        }
        val fallback = routeCatalog.defaultRoutePath
        if (fallback.isNotBlank()) {
            navigateToRoute(fallback)
        }
    }

    private fun navigateToRoute(
        routePath: String,
    ) {
        val route = KboxNavRoute(routePath = routePath)
        if (backStack.isEmpty()) {
            backStack += route
            return
        }
        if (backStack.last() == route) {
            return
        }
        backStack[backStack.lastIndex] = route
    }
}
