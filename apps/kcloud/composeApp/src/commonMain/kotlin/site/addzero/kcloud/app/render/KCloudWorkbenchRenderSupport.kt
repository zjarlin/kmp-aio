package site.addzero.kcloud.app.render

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.workbenchshell.ScreenNode
import site.addzero.workbenchshell.ScreenTree

@Composable
internal fun rememberSelectedNode(
    screenTree: ScreenTree,
    shellState: KCloudShellState,
): ScreenNode? {
    val selectedScreenId = shellState.selectedScreenId
    return remember(screenTree, selectedScreenId) {
        screenTree.findLeaf(selectedScreenId)
    }
}

/** 底部状态条：保留轻分隔，不再额外套一层发灰卡片。 */
internal fun Modifier.statusBarFrame(): Modifier {
    return fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(horizontal = 14.dp, vertical = 10.dp)
}

internal fun ScreenNode?.visibleLeafCount(): Int {
    return this?.children.orEmpty().sumOf { child -> child.visibleLeafCountInSubtree() }
}

internal fun ScreenNode.visibleLeafCountInSubtree(): Int {
    if (!visible) {
        return 0
    }
    if (isLeaf) {
        return 1
    }
    return children.sumOf { child -> child.visibleLeafCountInSubtree() }
}
