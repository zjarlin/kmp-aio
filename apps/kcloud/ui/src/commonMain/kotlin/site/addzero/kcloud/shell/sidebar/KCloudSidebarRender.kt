package site.addzero.kcloud.shell.sidebar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import site.addzero.kcloud.theme.currentKCloudUiMetrics
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
    val uiMetrics = currentKCloudUiMetrics()

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
            .padding(uiMetrics.sidebarOuterPadding),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(uiMetrics.sidebarPanelRadius),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)),
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(uiMetrics.sidebarPanelInnerPadding),
                verticalArrangement = Arrangement.spacedBy(uiMetrics.sidebarSectionGap),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (title.isNotBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = "功能目录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    KCloudSidebarMetric(
                        label = "页面",
                        value = items.countLeafRoutes().toString(),
                        compact = uiMetrics.compact,
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
                        fieldHeight = uiMetrics.searchFieldHeight,
                        horizontalSpacing = uiMetrics.searchFieldSpacing,
                        showRefreshButton = !uiMetrics.searchFieldCompactRefreshHidden,
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(uiMetrics.sidebarTreePanelRadius),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(uiMetrics.sidebarTreePanelPadding),
                    ) {
                        AddTree(
                            viewModel = treeViewModel,
                            modifier = Modifier.fillMaxSize(),
                            metrics = uiMetrics.treeMetrics,
                        )
                    }
                }

                Text(
                    text = "左侧菜单只负责场景内导航，不再和顶部场景切换重复。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                footer()
            }
        }
    }
}

@Composable
private fun KCloudSidebarMetric(
    label: String,
    value: String,
    compact: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(if (compact) 14.dp else 16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.76f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 7.dp else 9.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private fun List<KCloudSidebarNode>.countLeafRoutes(): Int {
    return sumOf { node ->
        if (node.children.isEmpty()) {
            1
        } else {
            node.children.countLeafRoutes()
        }
    }
}
