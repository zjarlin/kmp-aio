package site.addzero.workbenchshell

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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.tree.AddTree
import site.addzero.component.tree.TreeViewModel
import site.addzero.component.tree.rememberTreeViewModel

@Composable
fun ScreenSidebar(
    title: String,
    items: List<ScreenNode>,
    selectedId: String?,
    onLeafClick: (ScreenNode) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    searchEnabled: Boolean = true,
    searchPlaceholder: String = "搜索菜单",
    treeViewModel: TreeViewModel<ScreenNode> = rememberTreeViewModel(),
    header: @Composable ColumnScope.() -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    val currentOnLeafClick = rememberUpdatedState(onLeafClick)

    LaunchedEffect(treeViewModel) {
        treeViewModel.configure(
            getId = ScreenNode::id,
            getLabel = ScreenNode::name,
            getChildren = ScreenNode::children,
            getIcon = { node -> node.icon },
        )
    }

    SideEffect {
        treeViewModel.onNodeClick = { node ->
            currentOnLeafClick.value(node)
        }
    }

    LaunchedEffect(treeViewModel, items) {
        treeViewModel.setItems(
            newItems = items,
            initiallyExpandedIds = items.allBranchIds(),
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
        if (title.isNotBlank() || !subtitle.isNullOrBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                subtitle?.takeIf(String::isNotBlank)?.let { supportText ->
                    Text(
                        text = supportText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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

private fun List<ScreenNode>.allBranchIds(): Set<String> {
    return buildSet {
        fun collect(nodes: List<ScreenNode>) {
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

private fun List<ScreenNode>.resolveSelectedId(
    selectedId: String?,
): String? {
    if (selectedId != null && containsNode(selectedId)) {
        return selectedId
    }
    return firstLeafIdOrNull()
}

private fun List<ScreenNode>.containsNode(
    nodeId: String,
): Boolean {
    return any { node ->
        node.id == nodeId || node.children.containsNode(nodeId)
    }
}

private fun List<ScreenNode>.firstLeafIdOrNull(): String? {
    firstOrNull { node -> node.isLeaf }?.let { node ->
        return node.id
    }
    return firstNotNullOfOrNull { node ->
        node.children.firstLeafIdOrNull()
    }
}
