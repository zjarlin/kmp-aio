package site.addzero.kcloud.app.render

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.tree.AddTree
import site.addzero.component.tree.TreeViewModel
import site.addzero.component.tree.rememberTreeViewModel
import site.addzero.kcloud.app.KCloudRouteCatalog
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.kcloud.app.KCloudSidebarNode
import site.addzero.workbenchshell.spi.sidebar.WorkbenchSidebarRenderer

class KCloudSidebarRenderer(
    private val routeCatalog: KCloudRouteCatalog,
    private val shellState: KCloudShellState,
) : WorkbenchSidebarRenderer {
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
    val currentOnNodeClick = rememberUpdatedState(onNodeClick)

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
            currentOnNodeClick.value(node)
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
        modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
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
                onSearch = {
                    treeViewModel.performSearch()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = searchPlaceholder,
            )
        }

        AddTree(
            viewModel = treeViewModel,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )

        footer()
    }
}

private fun List<KCloudSidebarNode>.allBranchIds(): Set<String> {
    return buildSet {
        fun collect(nodes: List<KCloudSidebarNode>) {
            nodes.forEach { node ->
                if (node.children.isNotEmpty()) {
                    add(node.id)
                    collect(node.children)
                }
            }
        }
        collect(this@allBranchIds)
    }
}

private fun List<KCloudSidebarNode>.resolveSelectedId(
    selectedRoutePath: String?,
): String? {
    if (selectedRoutePath != null) {
        firstLeafByRoutePath(selectedRoutePath)?.let { node ->
            return node.id
        }
    }
    return firstLeafIdOrNull()
}

private fun List<KCloudSidebarNode>.firstLeafByRoutePath(
    routePath: String,
): KCloudSidebarNode? {
    return firstNotNullOfOrNull { node ->
        when {
            node.routePath == routePath -> node
            node.children.isNotEmpty() -> node.children.firstLeafByRoutePath(routePath)
            else -> null
        }
    }
}

private fun List<KCloudSidebarNode>.firstLeafIdOrNull(): String? {
    firstOrNull { node -> node.isLeaf }?.let { node ->
        return node.id
    }
    return firstNotNullOfOrNull { node ->
        node.children.firstLeafIdOrNull()
    }
}

private fun KCloudSidebarNode.firstLeafRoutePath(): String? {
    if (routePath != null) {
        return routePath
    }
    return children.firstNotNullOfOrNull { child ->
        child.firstLeafRoutePath()
    }
}
