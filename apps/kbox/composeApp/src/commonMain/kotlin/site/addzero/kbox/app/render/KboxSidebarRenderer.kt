package site.addzero.kbox.app.render

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.kbox.app.KboxRouteCatalog
import site.addzero.kbox.app.KboxShellState
import site.addzero.kbox.app.KboxSidebarNode
import site.addzero.kbox.plugin.api.KboxDynamicRouteRegistry
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

class KboxSidebarRenderer(
    private val routeCatalog: KboxRouteCatalog,
    private val shellState: KboxShellState,
) : SidebarRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val routeRegistry = koinInject<KboxDynamicRouteRegistry>()
        val dynamicRoutes by routeRegistry.dynamicRoutes.collectAsState()
        val routeVersion = dynamicRoutes.size
        val selectedSceneId = shellState.selectedSceneId
        val selectedRoute = rememberSelectedRoute(routeCatalog, shellState, routeVersion)
        val selectedScene = remember(routeCatalog, selectedSceneId, routeVersion) {
            routeCatalog.findScene(selectedSceneId)
        }

        RouteSidebar(
            title = selectedScene?.name ?: "KBox",
            items = selectedScene?.menuNodes.orEmpty(),
            selectedRoutePath = selectedRoute?.routePath,
            onNodeClick = { node ->
                val routePath = node.routePath ?: node.firstLeafRoutePath() ?: return@RouteSidebar
                shellState.selectRoute(routePath)
            },
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun RouteSidebar(
    title: String,
    items: List<KboxSidebarNode>,
    selectedRoutePath: String?,
    onNodeClick: (KboxSidebarNode) -> Unit,
    modifier: Modifier = Modifier,
    searchEnabled: Boolean = true,
    searchPlaceholder: String = "搜索页面",
    header: @Composable ColumnScope.() -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    val currentOnNodeClick = rememberUpdatedState(onNodeClick)
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val visibleItems = remember(items, searchQuery) {
        items.filterByQuery(searchQuery.trim())
    }
    val flatItems = remember(visibleItems) {
        visibleItems.flatten()
    }
    val pageCount = remember(flatItems) {
        flatItems.count { item -> item.node.routePath != null }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Workspace",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = "$pageCount pages in this scene",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        header()

        if (searchEnabled) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { value -> searchQuery = value },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search") },
                placeholder = { Text(searchPlaceholder) },
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Pages",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(flatItems, key = { item -> item.node.id }) { item ->
                        SidebarNodeRow(
                            item = item,
                            selectedRoutePath = selectedRoutePath,
                            onNodeClick = { node -> currentOnNodeClick.value(node) },
                        )
                    }
                }
            }
        }

        Text(
            text = if (searchQuery.isBlank()) {
                "Compact navigation for tool-heavy workflows"
            } else {
                "Filtered $pageCount matching pages"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        footer()
    }
}

@Composable
private fun SidebarNodeRow(
    item: SidebarListItem,
    selectedRoutePath: String?,
    onNodeClick: (KboxSidebarNode) -> Unit,
) {
    val node = item.node
    val selected = node.routePath == selectedRoutePath
    val branchSelected = !selected && node.containsRoute(selectedRoutePath)
    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
        branchSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.66f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    }
    val textColor = when {
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth()
            .background(containerColor, shape = RoundedCornerShape(12.dp))
            .clickable { onNodeClick(node) }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(modifier = Modifier.width((item.depth * 14).dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = node.name,
                style = if (node.children.isEmpty()) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.labelLarge
                },
                color = textColor,
                fontWeight = if (selected || branchSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (node.routePath != null) {
                Text(
                    text = node.routePath,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class SidebarListItem(
    val node: KboxSidebarNode,
    val depth: Int,
)

private fun List<KboxSidebarNode>.filterByQuery(
    query: String,
): List<KboxSidebarNode> {
    if (query.isBlank()) {
        return this
    }
    return mapNotNull { node ->
        val filteredChildren = node.children.filterByQuery(query)
        if (node.name.contains(query, ignoreCase = true) || filteredChildren.isNotEmpty()) {
            node.copy(children = filteredChildren)
        } else {
            null
        }
    }
}

private fun List<KboxSidebarNode>.flatten(): List<SidebarListItem> {
    return buildList {
        fun collect(
            nodes: List<KboxSidebarNode>,
            depth: Int,
        ) {
            nodes.forEach { node ->
                add(SidebarListItem(node = node, depth = depth))
                if (node.children.isNotEmpty()) {
                    collect(node.children, depth + 1)
                }
            }
        }
        collect(this@flatten, 0)
    }
}
