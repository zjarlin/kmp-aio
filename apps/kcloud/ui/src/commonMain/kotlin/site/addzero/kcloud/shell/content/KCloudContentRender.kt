package site.addzero.kcloud.shell.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import org.koin.core.annotation.Single
import site.addzero.generated.RouteTable
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.shell.navigation.KCloudRouteCatalog
import site.addzero.kcloud.shell.navigation.KCloudRouteEntry
import site.addzero.kcloud.theme.currentKCloudUiMetrics
import site.addzero.workbenchshell.spi.content.ContentRender

@Single(
    binds = [
        ContentRender::class,
    ],
)
class KCloudContentRender(
    private val routeCatalog: KCloudRouteCatalog,
    private val shellState: KCloudShellState,
) : ContentRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val uiMetrics = currentKCloudUiMetrics()
        Column(
            modifier = modifier,
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(uiMetrics.contentPanelPadding),
                shape = RoundedCornerShape(uiMetrics.contentPanelRadius),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)),
                tonalElevation = 0.dp,
            ) {
                NavDisplay(
                    backStack = shellState.backStack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(uiMetrics.contentInnerPadding),
                    onBack = shellState::popNavigation,
                ) { routePath ->
                    NavEntry(routePath) { key ->
                        ScreenContentEntry(
                            routePath = key,
                            routeCatalog = routeCatalog,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenContentEntry(
    routePath: String,
    routeCatalog: KCloudRouteCatalog,
) {
    val routeEntry = remember(routeCatalog, routePath) {
        routeCatalog.findRoute(routePath)
    }
    val content = routeEntry?.routePath?.let { path ->
        RouteTable.allRoutes[path]
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
