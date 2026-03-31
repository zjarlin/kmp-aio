package site.addzero.kcloud.app.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import site.addzero.generated.RouteTable
import site.addzero.kcloud.app.WorkbenchNavRoute
import site.addzero.kcloud.app.WorkbenchRouteCatalog
import site.addzero.kcloud.app.WorkbenchRouteEntry
import site.addzero.kcloud.app.WorkbenchShellState
import site.addzero.workbenchshell.spi.content.ContentRender

class WorkbenchContentRender(
    private val routeCatalog: WorkbenchRouteCatalog,
    private val shellState: WorkbenchShellState,
) : ContentRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedSceneId = shellState.selectedSceneId
        val selectedScene = remember(routeCatalog, selectedSceneId) {
            routeCatalog.findScene(selectedSceneId)
        }
        val selectedRoute = rememberSelectedRoute(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )

        Column(
            modifier = modifier,
        ) {
            NavDisplay(
                backStack = shellState.backStack,
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                onBack = shellState::popNavigation,
                entryProvider = { route ->
                    NavEntry(route) { key ->
                        ScreenContentEntry(
                            route = key,
                            routeCatalog = routeCatalog,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun ScreenContentEntry(
    route: WorkbenchNavRoute,
    routeCatalog: WorkbenchRouteCatalog,
) {
    val routeEntry = remember(routeCatalog, route.routePath) {
        routeCatalog.findRoute(route.routePath)
    }
    val content = routeEntry?.routePath?.let { routePath ->
        RouteTable.allRoutes[routePath]
    }
    if (content == null) {
        EmptyShellContent(
            routeEntry = routeEntry,
        )
        return
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        content()
    }
}

@Composable
private fun EmptyShellContent(
    routeEntry: WorkbenchRouteEntry? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (routeEntry == null) "暂无可用页面" else "页面 ${routeEntry.title} 暂未挂载内容",
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
