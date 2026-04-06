package site.addzero.kcloud.shell.sidebar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.koin.core.annotation.Single
import site.addzero.kcloud.shell.spi_impl.sys_stats.ShellState
import site.addzero.kcloud.shell.navigation.RouteCatalog
import site.addzero.kcloud.shell.navigation.SidebarNode
import site.addzero.kcloud.shell.navigation.firstLeafRoutePath
import site.addzero.kcloud.shell.navigation.rememberSelectedRoute
import site.addzero.kcloud.shell.navigation.resolveSelectedId
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

@Single(
    binds = [
        SidebarRender::class,
    ],
)
class SidebarRenderer(
    private val routeCatalog: RouteCatalog,
    private val shellState: ShellState,
) : SidebarRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedSceneId = shellState.selectedSceneId
        val selectedRoute = rememberSelectedRoute(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
        val selectedScene = remember(routeCatalog, selectedSceneId) {
            routeCatalog.findScene(selectedSceneId)
        }
        val items = selectedScene?.menuNodes.orEmpty()

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
