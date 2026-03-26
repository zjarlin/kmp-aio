package site.addzero.kcloud.app.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.workbenchshell.ScreenTree
import site.addzero.workbenchshell.spi.content.WorkbenchContentRenderer

@Single
class KCloudContentRenderer(
    private val screenTree: ScreenTree,
    private val shellState: KCloudShellState,
) : WorkbenchContentRenderer {
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

        Column(
            modifier = modifier,
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
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
