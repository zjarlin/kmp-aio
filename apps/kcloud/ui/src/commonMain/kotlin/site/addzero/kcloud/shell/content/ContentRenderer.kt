package site.addzero.kcloud.shell.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.core.annotation.Single
import site.addzero.generated.RouteTable
import site.addzero.kcloud.shell.ShellState
import site.addzero.kcloud.shell.navigation.RouteCatalog
import site.addzero.kcloud.shell.navigation.RouteEntry
import site.addzero.cupertino.workbench.content.WorkbenchContentSurface
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
            NavDisplay(
                backStack = shellState.backStack,
                modifier = Modifier.fillMaxSize(),
            ) { routePath ->
                when (RouteTable.allRoutes[routePath]) {
                    null -> NavEntry(routePath) {
                        EmptyShellContent(
                            routeEntry = routeCatalog.findRoute(routePath),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    else -> NavEntry(routePath) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            RouteTable.allRoutes.getValue(routePath).invoke()
                        }
                    }
                }
            }
        }
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
