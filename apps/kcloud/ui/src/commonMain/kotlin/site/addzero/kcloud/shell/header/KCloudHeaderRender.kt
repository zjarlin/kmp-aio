package site.addzero.kcloud.shell.header

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.shell.navigation.KCloudRouteCatalog
import site.addzero.kcloud.shell.navigation.KCloudRouteScene
import site.addzero.kcloud.theme.currentKCloudUiMetrics
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
        val selectedSceneId = shellState.selectedSceneId
        val uiMetrics = currentKCloudUiMetrics()
        Row(
            modifier = modifier
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            routeCatalog.scenes.forEach { scene ->
                KCloudSceneTab(
                    scene = scene,
                    selected = selectedSceneId == scene.id,
                    uiMetrics = uiMetrics,
                    onClick = {
                        shellState.selectScene(scene.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun KCloudSceneTab(
    scene: KCloudRouteScene,
    selected: Boolean,
    uiMetrics: site.addzero.kcloud.theme.KCloudUiMetrics,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) {
            colorScheme.primary
        } else {
            colorScheme.surfaceVariant.copy(alpha = 0.58f)
        },
        contentColor = if (selected) colorScheme.onPrimary else colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                colorScheme.primary
            } else {
                colorScheme.outlineVariant.copy(alpha = 0.72f)
            },
        ),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = uiMetrics.sceneTabHorizontalPadding,
                vertical = uiMetrics.sceneTabVerticalPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = scene.icon,
                contentDescription = null,
            )
            Text(
                text = scene.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}
