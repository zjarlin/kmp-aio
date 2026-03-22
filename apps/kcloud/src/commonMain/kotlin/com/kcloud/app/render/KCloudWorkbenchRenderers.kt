package com.kcloud.app.render

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kcloud.app.KCloudShellState
import org.koin.core.annotation.Single
import site.addzero.appsidebar.AppSidebar
import site.addzero.appsidebar.AppSidebarStyle
import site.addzero.appsidebar.rememberAppSidebarState
import site.addzero.workbenchshell.ScreenCatalog
import site.addzero.workbenchshell.ScreenNode
import site.addzero.workbenchshell.toAppSidebarItems
import site.addzero.workbenchshell.spi.content.WorkbenchContentRenderer
import site.addzero.workbenchshell.spi.header.WorkbenchHeaderRenderer
import site.addzero.workbenchshell.spi.sidebar.WorkbenchSidebarRenderer

@Single
class KCloudSidebarRenderer(
    private val screenCatalog: ScreenCatalog,
    private val shellState: KCloudShellState,
) : WorkbenchSidebarRenderer {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedSceneId by shellState.selectedSceneId.collectAsState()
        val selectedNode = rememberSelectedNode(
            screenCatalog = screenCatalog,
            shellState = shellState,
        )
        val selectedSceneNode = remember(screenCatalog, selectedSceneId) {
            screenCatalog.findNode(selectedSceneId)
        }
        val sidebarItems = remember(selectedSceneNode, screenCatalog.defaultExpandedIds) {
            selectedSceneNode
                ?.children
                .orEmpty()
                .toAppSidebarItems(expandedIds = screenCatalog.defaultExpandedIds)
        }
        val sidebarState = rememberAppSidebarState(
            initialSelectedId = selectedNode?.id,
        )
        val scenePageCount = remember(selectedSceneNode) {
            selectedSceneNode.visibleLeafCount()
        }

        LaunchedEffect(selectedNode?.id, sidebarItems) {
            sidebarState.updateSelectedId(selectedNode?.id)
            sidebarState.revealSelection(
                items = sidebarItems,
                selectedId = selectedNode?.id,
            )
        }

        AppSidebar(
            title = selectedSceneNode?.name ?: "KCloud",
            supportText = "场景化模块工作台",
            items = sidebarItems,
            state = sidebarState,
            style = AppSidebarStyle.FlushWorkbench,
            modifier = modifier.sidebarFrame(),
            onItemClick = { item ->
                shellState.selectScreen(item.id)
            },
            footerSlot = {
                SidebarSummaryCard(
                    pageCount = scenePageCount,
                )
            },
        )
    }
}

@Single
class KCloudHeaderRenderer(
    private val screenCatalog: ScreenCatalog,
    private val shellState: KCloudShellState,
) : WorkbenchHeaderRenderer {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedSceneId by shellState.selectedSceneId.collectAsState()
        val sceneNodes = remember(screenCatalog) {
            screenCatalog.tree.filter { node -> node.visible }
        }
        val selectedNode = rememberSelectedNode(
            screenCatalog = screenCatalog,
            shellState = shellState,
        )
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SceneSwitcher(
                scenes = sceneNodes,
                selectedSceneId = selectedSceneId,
                onSceneSelected = shellState::selectScene,
            )
            ScreenHeader(
                breadcrumb = screenCatalog.breadcrumbNamesFor(selectedNode?.id.orEmpty()),
                title = selectedNode?.name ?: "未选择页面",
            )
        }
    }
}

@Single
class KCloudContentRenderer(
    private val screenCatalog: ScreenCatalog,
    private val shellState: KCloudShellState,
) : WorkbenchContentRenderer {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedSceneId by shellState.selectedSceneId.collectAsState()
        val selectedNode = rememberSelectedNode(
            screenCatalog = screenCatalog,
            shellState = shellState,
        )
        val selectedSceneNode = remember(screenCatalog, selectedSceneId) {
            screenCatalog.findNode(selectedSceneId)
        }

        Column(
            modifier = modifier,
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().contentFrame(),
            ) {
                val content = selectedNode?.content
                if (content == null) {
                    EmptyShellContent()
                } else {
                    content()
                }
            }
            ShellStatusBar(
                currentScene = selectedSceneNode?.name ?: "未分组",
                currentTitle = selectedNode?.name ?: "未选择页面",
                pageCount = selectedSceneNode.visibleLeafCount(),
                modifier = Modifier.height(60.dp),
            )
        }
    }
}

@Composable
private fun rememberSelectedNode(
    screenCatalog: ScreenCatalog,
    shellState: KCloudShellState,
): ScreenNode? {
    val selectedScreenId by shellState.selectedScreenId.collectAsState()
    return remember(screenCatalog, selectedScreenId) {
        screenCatalog.findLeaf(selectedScreenId)
    }
}

@Composable
private fun SidebarSummaryCard(
    pageCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "场景脚手架",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "当前场景聚合了 $pageCount 个页面",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
private fun SceneSwitcher(
    scenes: List<ScreenNode>,
    selectedSceneId: String,
    onSceneSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        scenes.forEach { scene ->
            val selected = scene.id == selectedSceneId
            Card(
                modifier = Modifier.sceneTabFrame(selected).clickable {
                    onSceneSelected(scene.id)
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                    },
                ),
            ) {
                Row(
                    modifier = Modifier.sceneTabContent(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    scene.icon?.let { icon ->
                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Text(
                        text = scene.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Text(
                        text = scene.visibleLeafCount().toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenHeader(
    breadcrumb: List<String>,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (breadcrumb.isNotEmpty()) {
            Text(
                text = breadcrumb.joinToString(" / "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ShellStatusBar(
    currentScene: String,
    currentTitle: String,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .statusBarFrame()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$currentScene / $currentTitle",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "当前场景 $pageCount 个 Screen 节点",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        )
    }
}

@Composable
private fun EmptyShellContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "暂无可用页面",
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

/** 侧栏外壳：让导航区和业务区保持稳定间距。 */
private fun Modifier.sidebarFrame(): Modifier {
    return fillMaxSize()
}

/** 内容安全区：避免业务页面直接贴到工作台边界。 */
private fun Modifier.contentFrame(): Modifier {
    return fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)
}

/** 场景标签外壳：统一选中态和未选中态的点按面积。 */
private fun Modifier.sceneTabFrame(
    selected: Boolean,
): Modifier {
    return if (selected) {
        this
    } else {
        this
    }
}

/** 底部状态条：保留轻分隔，不再额外套一层发灰卡片。 */
private fun Modifier.statusBarFrame(): Modifier {
    return fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(horizontal = 14.dp, vertical = 10.dp)
}

/** 场景标签内边距：保持图标、标题、计数的阅读节奏。 */
private fun Modifier.sceneTabContent(): Modifier {
    return padding(horizontal = 14.dp, vertical = 10.dp)
}

private fun ScreenNode?.visibleLeafCount(): Int {
    return this?.children.orEmpty().sumOf { child -> child.visibleLeafCountInSubtree() }
}

private fun ScreenNode.visibleLeafCountInSubtree(): Int {
    if (!visible) {
        return 0
    }
    if (isLeaf) {
        return 1
    }
    return children.sumOf { child -> child.visibleLeafCountInSubtree() }
}
