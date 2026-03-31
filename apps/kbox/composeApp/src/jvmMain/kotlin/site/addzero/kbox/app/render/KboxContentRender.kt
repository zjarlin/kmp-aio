package site.addzero.kbox.app.render

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.koinInject
import org.koin.core.annotation.Single
import site.addzero.generated.RouteTable
import site.addzero.kbox.app.KboxNavRoute
import site.addzero.kbox.app.KboxRouteCatalog
import site.addzero.kbox.app.KboxRouteEntry
import site.addzero.kbox.app.KboxShellState
import site.addzero.kbox.plugin.api.KboxDynamicRouteRegistry
import site.addzero.workbenchshell.spi.content.ContentRender

@Single
class KboxContentRender(
    private val routeCatalog: KboxRouteCatalog,
    private val shellState: KboxShellState,
) : ContentRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val routeRegistry = koinInject<KboxDynamicRouteRegistry>()
        val dynamicRoutes by routeRegistry.dynamicRoutes.collectAsState()
        val selectedSceneId = shellState.selectedSceneId
        val selectedScene = remember(routeCatalog, selectedSceneId, dynamicRoutes) {
            routeCatalog.findScene(selectedSceneId)
        }
        val selectedRoute = rememberSelectedRoute(routeCatalog, shellState, dynamicRoutes.size)

        LaunchedEffect(dynamicRoutes.size, shellState.selectedRoutePath) {
            shellState.ensureRouteSelected()
        }

        Column(modifier = modifier) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                NavDisplay(
                    backStack = shellState.backStack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    onBack = shellState::popNavigation,
                    entryProvider = { route ->
                        NavEntry(route) { key ->
                            ScreenContentEntry(
                                route = key,
                                routeCatalog = routeCatalog,
                                routeVersion = dynamicRoutes.size,
                            )
                        }
                    },
                )
            }
            ShellStatusBar(
                currentScene = selectedScene?.name ?: "Workspace",
                currentTitle = selectedRoute?.title ?: "No page selected",
                pageCount = selectedScene?.routeCount() ?: 0,
                modifier = Modifier.height(52.dp),
            )
        }
    }
}

@Composable
private fun ScreenContentEntry(
    route: KboxNavRoute,
    routeCatalog: KboxRouteCatalog,
    routeVersion: Int,
) {
    val routeEntry = remember(routeCatalog, route.routePath, routeVersion) {
        routeCatalog.findRoute(route.routePath)
    }
    val content: (@Composable () -> Unit)? = routeEntry?.runtimeContent
        ?: routeEntry?.routePath?.let { routePath ->
            RouteTable.allRoutes[routePath]
        }
    if (content == null) {
        EmptyShellContent(routeEntry)
        return
    }
    Box(modifier = Modifier.fillMaxSize()) {
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
    val colorScheme = MaterialTheme.colorScheme
    val darkThemeEnabled = colorScheme.background.luminance() < 0.5f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarFrame()
            .background(
                if (darkThemeEnabled) {
                    colorScheme.surface.copy(alpha = 0.92f)
                } else {
                    colorScheme.surfaceVariant.copy(alpha = 0.52f)
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$currentScene / $currentTitle",
            style = MaterialTheme.typography.titleSmall,
            color = colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$pageCount pages in this scene",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
        )
    }
}

@Composable
private fun EmptyShellContent(
    routeEntry: KboxRouteEntry? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (routeEntry == null) {
                "No page available yet"
            } else {
                "Page ${routeEntry.title} is not mounted yet"
            },
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
