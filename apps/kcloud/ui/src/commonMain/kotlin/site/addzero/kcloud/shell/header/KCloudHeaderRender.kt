package site.addzero.kcloud.shell.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.kcloud.design.button.KCloudButtonSize as ShadcnButtonSize
import site.addzero.kcloud.design.button.KCloudButtonVariant as ShadcnButtonVariant
import site.addzero.kcloud.design.button.KCloudPillButton
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
            modifier = modifier,
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
    KCloudPillButton(
        onClick = onClick,
        variant = if (selected) ShadcnButtonVariant.Default else ShadcnButtonVariant.Outline,
        size = if (uiMetrics.compact) ShadcnButtonSize.Sm else ShadcnButtonSize.Default,
    ) {
        Icon(
            imageVector = scene.icon,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = scene.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}
