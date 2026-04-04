package site.addzero.kcloud.shell.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.core.annotation.Single
import site.addzero.generated.RouteTable
import site.addzero.kcloud.shell.NavRoute
import site.addzero.kcloud.shell.ShellState
import site.addzero.kcloud.shell.navigation.RouteCatalog
import site.addzero.kcloud.shell.navigation.RouteEntry
import site.addzero.workbench.shell.content.WorkbenchContentSurface
import site.addzero.workbenchshell.spi.content.ContentRender

@Single
class ContentRenderer(
    private val routeCatalog: RouteCatalog,
    private val shellState: ShellState,
) : ContentRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        WorkbenchContentSurface(
            modifier = modifier.fillMaxSize(),
        ) {
            ScreenContentEntry(
                routePath = NavRoute(shellState.selectedRoutePath),
                routeCatalog = routeCatalog,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ScreenContentEntry(
    routePath: NavRoute,
    routeCatalog: RouteCatalog,
    modifier: Modifier = Modifier,
) {
    val routeEntry = remember(routeCatalog, routePath) {
        routeCatalog.findRoute(routePath.routePath)
    }
    val content = routeEntry?.routePath?.let { path ->
        RouteTable.allRoutes[path]
    }
    if (content == null) {
        EmptyShellContent(
            routeEntry = routeEntry,
            modifier = modifier,
        )
        return
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        content()
    }
}

@Composable
private fun EmptyShellContent(
    routeEntry: RouteEntry? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (routeEntry == null) "暂无可用页面" else "页面 ${routeEntry.title} 暂未挂载内容",
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
