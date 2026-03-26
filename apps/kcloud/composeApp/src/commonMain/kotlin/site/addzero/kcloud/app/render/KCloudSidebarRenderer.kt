package site.addzero.kcloud.app.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.koin.core.annotation.Single
import site.addzero.appsidebar.AppSidebarConfig
import site.addzero.appsidebar.AppSidebarSlots
import site.addzero.appsidebar.AppSidebarStyle
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.workbenchshell.ScreenSidebar
import site.addzero.workbenchshell.ScreenTree
import site.addzero.workbenchshell.spi.sidebar.WorkbenchSidebarRenderer

@Single
class KCloudSidebarRenderer(
    private val screenTree: ScreenTree,
    private val shellState: KCloudShellState,
) : WorkbenchSidebarRenderer {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedSceneId = shellState.selectedSceneId
        val selectedNode = rememberSelectedNode(
            screenTree = screenTree,
            shellState = shellState,
        )
        val selectedSceneNode = remember(screenTree, selectedSceneId) {
            screenTree.findNode(selectedSceneId)
        }
        val scenePageCount = remember(selectedSceneNode) {
            selectedSceneNode.visibleLeafCount()
        }

        ScreenSidebar(
            title = selectedSceneNode?.name ?: "KCloud",
            items = selectedSceneNode?.children.orEmpty(),
            selectedId = selectedNode?.id,
            onLeafClick = { node ->
                shellState.selectScreen(node.id)
            },
            modifier = modifier.fillMaxSize(),
            config = AppSidebarConfig(
                style = AppSidebarStyle.FlushWorkbench,
                supportText = "场景化模块工作台",
            ),
            slots = AppSidebarSlots(
                footer = {
                    SidebarSummaryCard(
                        pageCount = scenePageCount,
                    )
                },
            ),
        )
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
