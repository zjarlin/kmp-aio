package site.addzero.kcloud.shell.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.button.WorkbenchIconButton
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Surface
import site.addzero.cupertino.workbench.metrics.currentWorkbenchMetrics
import site.addzero.cupertino.workbench.scaffolding.CupertinoWorkbenchSidebarMode
import site.addzero.cupertino.workbench.scaffolding.LocalCupertinoWorkbenchSidebarMode
import site.addzero.kcloud.shell.spi_impl.sys_stats.ShellState
import site.addzero.kcloud.shell.navigation.RouteCatalog
import site.addzero.kcloud.shell.navigation.SidebarNode
import site.addzero.kcloud.shell.navigation.firstLeafRoutePath
import site.addzero.kcloud.shell.navigation.rememberSelectedRoute
import site.addzero.kcloud.shell.navigation.resolveSelectedId
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

@Single
class SidebarRenderer(
    private val routeCatalog: RouteCatalog,
    private val shellState: ShellState,
) : SidebarRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val metrics = currentWorkbenchMetrics()
        val sidebarMode = LocalCupertinoWorkbenchSidebarMode.current
        val selectedSceneId = shellState.selectedSceneId
        val selectedRoute = rememberSelectedRoute(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
        val selectedScene = remember(routeCatalog, selectedSceneId) {
            routeCatalog.findScene(selectedSceneId)
        }
        val items = selectedScene?.menuNodes.orEmpty()

        if (sidebarMode == CupertinoWorkbenchSidebarMode.Collapsed) {
            CollapsedSidebarRail(
                items = items,
                selectedRoutePath = selectedRoute?.routePath,
                onNodeClick = collapsedNodeClick@{ node ->
                    val routePath = node.routePath
                        ?: node.firstLeafRoutePath()
                        ?: return@collapsedNodeClick
                    shellState.selectRoute(routePath)
                },
                modifier = modifier.fillMaxSize(),
                panelRadius = metrics.sidebarPanelRadius,
            )
        } else {
            WorkbenchTreeSidebar(
                items = items,
                selectedId = items.resolveSelectedId(selectedRoute?.routePath),
                onNodeClick = { node ->
                    val routePath = node.routePath
                        ?: node.firstLeafRoutePath()
                        ?: return@WorkbenchTreeSidebar
                    shellState.selectRoute(routePath)
                },
                modifier = modifier.fillMaxSize(),
                getId = SidebarNode::id,
                getLabel = SidebarNode::name,
                getChildren = SidebarNode::children,
                getIcon = { node -> node.icon },
            )
        }
    }
}

@Composable
private fun CollapsedSidebarRail(
    items: List<SidebarNode>,
    selectedRoutePath: String?,
    onNodeClick: (SidebarNode) -> Unit,
    modifier: Modifier = Modifier,
    panelRadius: androidx.compose.ui.unit.Dp = 20.dp,
) {
    Surface(
        modifier = modifier.padding(10.dp),
        shape = RoundedCornerShape(panelRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items.forEach { node ->
                WorkbenchIconButton(
                    onClick = {
                        onNodeClick(node)
                    },
                    tooltip = node.name,
                    variant = if (node.containsRoutePath(selectedRoutePath)) {
                        WorkbenchButtonVariant.Default
                    } else {
                        WorkbenchButtonVariant.Secondary
                    },
                ) {
                    Icon(
                        imageVector = node.icon,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

private fun SidebarNode.containsRoutePath(
    routePath: String?,
): Boolean {
    if (routePath == null) {
        return false
    }
    if (this.routePath == routePath) {
        return true
    }
    return children.any { child ->
        child.containsRoutePath(routePath)
    }
}
