package site.addzero.kcloud.app.render

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.app.KCloudRouteCatalog
import site.addzero.kcloud.app.KCloudRouteScene
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.workbenchshell.spi.header.HeaderRender

class KCloudHeaderRender(
    private val routeCatalog: KCloudRouteCatalog,
    private val shellState: KCloudShellState,
) : HeaderRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedSceneId = shellState.selectedSceneId
        val sceneNodes = remember(routeCatalog) {
            routeCatalog.scenes
        }
        val selectedRoute = rememberSelectedRoute(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
        val breadcrumb = remember(routeCatalog, shellState.selectedRoutePath) {
            routeCatalog.breadcrumbNamesFor(shellState.selectedRoutePath)
        }
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SceneSwitcher(
                scenes = sceneNodes,
                selectedSceneId = selectedSceneId,
                onSceneSelected = shellState::selectScene,
            )
            ScreenHeader(
                breadcrumb = breadcrumb,
                title = selectedRoute?.title ?: "未选择页面",
            )
        }
    }
}

@Composable
private fun SceneSwitcher(
    scenes: List<KCloudRouteScene>,
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
                modifier = Modifier.clickable {
                    onSceneSelected(scene.id)
                },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) {
                        colorScheme.primary.copy(alpha = if (darkThemeEnabled) 0.36f else 0.18f)
                    } else {
                        colorScheme.outline.copy(alpha = if (darkThemeEnabled) 0.30f else 0.16f)
                    },
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp,
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        colorScheme.primaryContainer.copy(
                            alpha = if (darkThemeEnabled) 0.72f else 1f,
                        )
                    } else {
                        colorScheme.surfaceVariant.copy(
                            alpha = if (darkThemeEnabled) 0.52f else 0.68f,
                        )
                    },
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = scene.icon,
                        contentDescription = null,
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
                        text = scene.routeCount().toString(),
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
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
}
