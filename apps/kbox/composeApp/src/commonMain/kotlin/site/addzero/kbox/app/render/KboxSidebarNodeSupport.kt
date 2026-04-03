package site.addzero.kbox.app.render

import site.addzero.kbox.app.KboxSidebarNode

internal fun KboxSidebarNode.containsRoute(
    routePath: String?,
): Boolean {
    if (routePath == null) {
        return false
    }
    if (this.routePath == routePath) {
        return true
    }
    return children.any { child -> child.containsRoute(routePath) }
}

internal fun KboxSidebarNode.firstLeafRoutePath(): String? {
    routePath?.let { route ->
        return route
    }
    return children.firstNotNullOfOrNull { child ->
        child.firstLeafRoutePath()
    }
}
