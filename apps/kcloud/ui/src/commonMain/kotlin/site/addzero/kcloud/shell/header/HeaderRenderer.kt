package site.addzero.kcloud.shell.header

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import site.addzero.cupertino.workbench.button.WorkbenchButtonSize
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.button.WorkbenchPillButton
import site.addzero.cupertino.workbench.material3.Icon
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Text
import site.addzero.kcloud.shell.spi_impl.sys_stats.ShellState
import site.addzero.kcloud.shell.navigation.RouteCatalog
import site.addzero.cupertino.workbench.metrics.currentWorkbenchMetrics
import site.addzero.workbenchshell.spi.header.HeaderRender

@Single
class HeaderRenderer(
    private val routeCatalog: RouteCatalog,
    private val shellState: ShellState,
) : HeaderRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val metrics = currentWorkbenchMetrics()
        val selectedRoutePath = shellState.selectedRoutePath
        val selectedRoute = remember(routeCatalog, selectedRoutePath) {
            routeCatalog.findRoute(selectedRoutePath)
        }
        val pageCaption = remember(routeCatalog, selectedRoutePath) {
            routeCatalog.breadcrumbNamesFor(selectedRoutePath)
                .dropLast(1)
                .joinToString(" / ")
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.widthIn(max = if (metrics.compact) 220.dp else 280.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = selectedRoute?.title ?: routeCatalog.findScene(shellState.selectedSceneId)?.name.orEmpty(),
                    style = if (metrics.compact) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                pageCaption.takeIf { value -> value.isNotBlank() }?.let { value ->
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            HeaderSceneTabs(
                items = routeCatalog.scenes,
                selectedId = shellState.selectedSceneId,
                onItemClick = { scene ->
                    shellState.selectScene(scene.id)
                },
                modifier = Modifier.weight(1f),
                itemId = { scene -> scene.id },
                itemLabel = { scene -> scene.name },
                itemIcon = { scene -> scene.icon },
            )
        }
    }
}

@Composable
private fun <T> HeaderSceneTabs(
    items: List<T>,
    selectedId: Any?,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemId: (T) -> Any,
    itemLabel: (T) -> String,
    itemIcon: (T) -> ImageVector,
) {
    val metrics = currentWorkbenchMetrics()
    val labelStyle = MaterialTheme.typography.labelLarge
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = itemId(item) == selectedId
            WorkbenchPillButton(
                onClick = { onItemClick(item) },
                variant = if (selected) {
                    WorkbenchButtonVariant.Default
                } else {
                    WorkbenchButtonVariant.Outline
                },
                size = if (metrics.compact) {
                    WorkbenchButtonSize.Sm
                } else {
                    WorkbenchButtonSize.Default
                },
            ) {
                Icon(
                    imageVector = itemIcon(item),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = itemLabel(item),
                    modifier = Modifier.align(Alignment.CenterVertically),
                    style = labelStyle,
                    lineHeight = labelStyle.fontSize,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
