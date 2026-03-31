package site.addzero.kbox.app.render

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.core.annotation.Single
import site.addzero.kbox.app.KboxRouteCatalog
import site.addzero.kbox.app.KboxRouteScene
import site.addzero.kbox.app.KboxShellState
import site.addzero.kbox.plugin.api.KboxDynamicRouteRegistry
import site.addzero.workbenchshell.spi.header.HeaderRender
@Single
class KboxHeaderRender(
    private val routeCatalog: KboxRouteCatalog,
    private val shellState: KboxShellState,
) : HeaderRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val routeRegistry = koinInject<KboxDynamicRouteRegistry>()
        val dynamicRoutes by routeRegistry.dynamicRoutes.collectAsState()
        val selectedSceneId = shellState.selectedSceneId
        val scenes = remember(routeCatalog, dynamicRoutes) {
            routeCatalog.scenes
        }
        val selectedRoute = rememberSelectedRoute(routeCatalog, shellState, dynamicRoutes.size)
        val breadcrumb = remember(routeCatalog, shellState.selectedRoutePath, dynamicRoutes) {
            routeCatalog.breadcrumbNamesFor(shellState.selectedRoutePath)
        }
        val selectedScene = remember(scenes, selectedSceneId) {
            scenes.firstOrNull { scene -> scene.id == selectedSceneId }
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SceneSwitcher(
                scenes = scenes,
                selectedSceneId = selectedSceneId,
                onSceneSelected = shellState::selectScene,
            )
            ScreenHeader(
                breadcrumb = breadcrumb,
                title = selectedRoute?.title ?: "No page selected",
                currentScene = selectedScene?.name.orEmpty(),
                pageCount = selectedScene?.routeCount ?: 0,
            )
        }
    }
}

@Composable
private fun SceneSwitcher(
    scenes: List<KboxRouteScene>,
    selectedSceneId: String,
    onSceneSelected: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val darkThemeEnabled = colorScheme.background.luminance() < 0.5f

    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        scenes.forEach { scene ->
            val selected = scene.id == selectedSceneId
            Card(
                modifier = Modifier
                    .clickable { onSceneSelected(scene.id) }
                    .widthIn(min = 108.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) {
                        colorScheme.primary.copy(alpha = if (darkThemeEnabled) 0.44f else 0.24f)
                    } else {
                        colorScheme.outline.copy(alpha = if (darkThemeEnabled) 0.35f else 0.24f)
                    },
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        colorScheme.primaryContainer.copy(alpha = if (darkThemeEnabled) 0.82f else 1f)
                    } else {
                        colorScheme.surface.copy(alpha = if (darkThemeEnabled) 0.92f else 0.88f)
                    },
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = scene.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Text(
                        text = scene.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (selected) {
                            colorScheme.onPrimaryContainer
                        } else {
                            colorScheme.onSurface
                        },
                    )
                    Text(
                        text = scene.routeCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) {
                            colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                        } else {
                            colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenHeader(
    breadcrumb: List<String>,
    title: String,
    currentScene: String,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (breadcrumb.isNotEmpty()) {
                    Text(
                        text = breadcrumb.joinToString(" / "),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = currentScene.ifBlank { "Workspace" },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "$pageCount pages available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
