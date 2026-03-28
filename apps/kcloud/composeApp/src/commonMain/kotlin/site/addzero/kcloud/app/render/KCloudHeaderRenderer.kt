package site.addzero.kcloud.app.render

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.app.KCloudRouteCatalog
import site.addzero.kcloud.app.KCloudRouteScene
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.workbenchshell.spi.header.WorkbenchHeaderRenderer

class KCloudHeaderRenderer(
    private val routeCatalog: KCloudRouteCatalog,
    private val shellState: KCloudShellState,
) : WorkbenchHeaderRenderer {
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
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
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
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Text(
                        text = scene.routeCount().toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
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
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
