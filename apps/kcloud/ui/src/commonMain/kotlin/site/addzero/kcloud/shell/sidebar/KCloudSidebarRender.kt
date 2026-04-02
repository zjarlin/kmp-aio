package site.addzero.kcloud.shell.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.tree.AddTree
import site.addzero.component.tree.TreeViewModel
import site.addzero.component.tree.rememberTreeViewModel
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.shell.navigation.KCloudRouteCatalog
import site.addzero.kcloud.shell.navigation.KCloudSidebarNode
import site.addzero.kcloud.shell.navigation.allBranchIds
import site.addzero.kcloud.shell.navigation.firstLeafRoutePath
import site.addzero.kcloud.shell.navigation.rememberSelectedRoute
import site.addzero.kcloud.shell.navigation.resolveSelectedId
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

@Single(
    binds = [
        SidebarRender::class,
    ],
)
class KCloudSidebarRender(
    private val routeCatalog: KCloudRouteCatalog,
    private val shellState: KCloudShellState,
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

        RouteSidebar(
            title = selectedScene?.name ?: "KCloud",
            items = selectedScene?.menuNodes.orEmpty(),
            selectedRoutePath = selectedRoute?.routePath,
            onNodeClick = { node ->
                val routePath = node.routePath
                    ?: node.firstLeafRoutePath()
                    ?: return@RouteSidebar
                shellState.selectRoute(routePath)
            },
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun RouteSidebar(
    title: String,
    items: List<KCloudSidebarNode>,
    selectedRoutePath: String?,
    onNodeClick: (KCloudSidebarNode) -> Unit,
    modifier: Modifier = Modifier,
    searchEnabled: Boolean = true,
    searchPlaceholder: String = "搜索页面",
    treeViewModel: TreeViewModel<KCloudSidebarNode> = rememberTreeViewModel(),
    header: @Composable ColumnScope.() -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    val currentOnNodeClick by rememberUpdatedState(onNodeClick)

    LaunchedEffect(treeViewModel) {
        treeViewModel.configure(
            getId = KCloudSidebarNode::id,
            getLabel = KCloudSidebarNode::name,
            getChildren = KCloudSidebarNode::children,
            getIcon = { node -> node.icon },
        )
    }

    SideEffect {
        treeViewModel.onNodeClick = { node ->
            currentOnNodeClick(node)
        }
    }

    LaunchedEffect(treeViewModel, items) {
        treeViewModel.setItems(
            newItems = items,
            initiallyExpandedIds = items.allBranchIds(),
        )
    }

    LaunchedEffect(treeViewModel, items, selectedRoutePath) {
        treeViewModel.selectNode(items.resolveSelectedId(selectedRoutePath))
    }

    LaunchedEffect(treeViewModel, searchEnabled) {
        if (!searchEnabled && treeViewModel.searchQuery.isNotBlank()) {
            treeViewModel.updateSearchQuery("")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        header()

        if (searchEnabled) {
            AddSearchBar(
                keyword = treeViewModel.searchQuery,
                onKeyWordChanged = { query ->
                    treeViewModel.updateSearchQuery(query)
                    if (query.isNotBlank()) {
                        treeViewModel.performSearch()
                    }
                },
                onSearch = treeViewModel::performSearch,
                modifier = Modifier.fillMaxWidth(),
                placeholder = searchPlaceholder,
            )
        }

        AddTree(
            viewModel = treeViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        footer()
    }
}
