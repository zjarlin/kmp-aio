package site.addzero.kcloud.app.render

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.tree.AddTree
import site.addzero.component.tree.TreeViewModel
import site.addzero.component.tree.rememberTreeViewModel
import site.addzero.kcloud.app.KCloudRouteCatalog
import site.addzero.kcloud.app.KCloudRouteEntry
import site.addzero.kcloud.app.KCloudShellState
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
            items = selectedScene?.routes.orEmpty(),
            selectedId = selectedRoute?.routePath,
            onRouteClick = { route ->
                shellState.selectRoute(route.routePath)
            },
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun RouteSidebar(
    title: String,
    items: List<KCloudRouteEntry>,
    selectedId: String?,
    onRouteClick: (KCloudRouteEntry) -> Unit,
    modifier: Modifier = Modifier,
    searchEnabled: Boolean = true,
    searchPlaceholder: String = "搜索页面",
    treeViewModel: TreeViewModel<KCloudRouteEntry> = rememberTreeViewModel(),
    header: @Composable ColumnScope.() -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    val currentOnRouteClick = rememberUpdatedState(onRouteClick)

    LaunchedEffect(treeViewModel) {
        treeViewModel.configure(
            getId = KCloudRouteEntry::routePath,
            getLabel = KCloudRouteEntry::title,
            getChildren = { emptyList<KCloudRouteEntry>() },
            getIcon = { route -> route.icon },
        )
    }

    SideEffect {
        treeViewModel.onNodeClick = { route ->
            currentOnRouteClick.value(route)
        }
    }

    LaunchedEffect(treeViewModel, items) {
        treeViewModel.setItems(
            newItems = items,
            initiallyExpandedIds = emptySet(),
        )
    }

    LaunchedEffect(treeViewModel, items, selectedId) {
        treeViewModel.selectNode(items.resolveSelectedId(selectedId))
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

private fun List<KCloudRouteEntry>.resolveSelectedId(
    selectedId: String?,
): String? {
    if (selectedId != null && any { route -> route.routePath == selectedId }) {
        return selectedId
    }
    return firstOrNull()?.routePath
}
