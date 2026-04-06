package site.addzero.kcloud.shell.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import site.addzero.kcloud.shell.spi_impl.sys_stats.ShellState

@Composable
internal fun rememberSelectedRoute(
    routeCatalog: RouteCatalog,
    shellState: ShellState,
): RouteEntry? {
    val selectedRoutePath = shellState.selectedRoutePath
    return remember(routeCatalog, selectedRoutePath) {
        routeCatalog.findRoute(selectedRoutePath)
    }
}

internal fun List<SidebarNode>.allBranchIds(): Set<String> {
    return buildSet {
        fun collect(nodes: List<SidebarNode>) {
            nodes.forEach { node ->
                if (node.children.isNotEmpty()) {
                    add(node.id)
                    collect(node.children)
                }
            }
        }
        collect(this@allBranchIds)
    }
}

internal fun List<SidebarNode>.resolveSelectedId(
    selectedRoutePath: String?,
): String? {
    if (selectedRoutePath != null) {
        firstLeafByRoutePath(selectedRoutePath)?.let { node ->
            return node.id
        }
    }
    return firstLeafIdOrNull()
}

private fun List<SidebarNode>.firstLeafByRoutePath(
    routePath: String,
): SidebarNode? {
    return firstNotNullOfOrNull { node ->
        when {
            node.routePath == routePath -> node
            node.children.isNotEmpty() -> node.children.firstLeafByRoutePath(routePath)
            else -> null
        }
    }
}

private fun List<SidebarNode>.firstLeafIdOrNull(): String? {
    firstOrNull { node -> node.isLeaf }?.let { node ->
        return node.id
    }
    return firstNotNullOfOrNull { node ->
        node.children.firstLeafIdOrNull()
    }
}

internal fun SidebarNode.firstLeafRoutePath(): String? {
    routePath?.let { route ->
        return route
    }
    return children.firstNotNullOfOrNull { child ->
        child.firstLeafRoutePath()
    }
}
