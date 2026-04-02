package site.addzero.kcloud.ui.app.render

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.kcloud.ui.app.KCloudRouteCatalog
import site.addzero.kcloud.ui.app.KCloudRouteEntry
import site.addzero.kcloud.ui.app.KCloudShellState
import site.addzero.workbenchshell.spi.header.HeaderRender

@Single
class KCloudHeaderRender(
    private val routeCatalog: KCloudRouteCatalog,
    private val shellState: KCloudShellState,
) : HeaderRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedRoute = rememberSelectedRoute(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
        val selectedScene = remember(routeCatalog, shellState.selectedSceneId) {
            routeCatalog.findScene(shellState.selectedSceneId)
        }

        Row(
            modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            selectedScene?.routes.orEmpty().forEach { route ->
                KCloudRouteTab(
                    route = route,
                    selected = selectedRoute?.routePath == route.routePath,
                    onClick = {
                        shellState.selectRoute(route.routePath)
                    },
                )
            }
        }
    }
}

@Composable
private fun KCloudRouteTab(
    route: KCloudRouteEntry,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) colorScheme.onSurface else colorScheme.surfaceVariant.copy(alpha = 0.42f),
        contentColor = if (selected) colorScheme.surface else colorScheme.onSurface,
        tonalElevation = if (selected) 2.dp else 0.dp,
    ) {
        Text(
            text = route.title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}
