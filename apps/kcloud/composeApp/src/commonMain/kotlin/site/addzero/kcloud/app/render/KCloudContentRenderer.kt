package site.addzero.kcloud.app.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import site.addzero.generated.RouteTable
import site.addzero.kcloud.app.KCloudNavRoute
import site.addzero.kcloud.app.KCloudRouteCatalog
import site.addzero.kcloud.app.KCloudRouteEntry
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.workbenchshell.spi.content.WorkbenchContentRenderer

class KCloudContentRenderer(
    private val routeCatalog: KCloudRouteCatalog,
    private val shellState: KCloudShellState,
) : WorkbenchContentRenderer {
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
                    .padding(horizontal = 18.dp, vertical = 14.dp),
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
            ShellStatusBar(
                currentScene = selectedScene?.name ?: "未分组",
                currentTitle = selectedRoute?.title ?: "未选择页面",
                pageCount = selectedScene.routeCount(),
                modifier = Modifier.height(60.dp),
            )
        }
    }
}

@Composable
private fun ScreenContentEntry(
    route: KCloudNavRoute,
    routeCatalog: KCloudRouteCatalog,
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
private fun ShellStatusBar(
    currentScene: String,
    currentTitle: String,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .statusBarFrame()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$currentScene / $currentTitle",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "当前场景 $pageCount 个页面",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        )
    }
}

@Composable
private fun EmptyShellContent(
    routeEntry: KCloudRouteEntry? = null,
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
