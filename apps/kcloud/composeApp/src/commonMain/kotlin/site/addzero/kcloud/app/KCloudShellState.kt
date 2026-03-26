package site.addzero.kcloud.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.feature.ShellWindowController
import site.addzero.kcloud.feature.ShellTrayPanelController
import site.addzero.workbenchshell.ScreenTree

@Single
class KCloudShellState(
    private val screenTree: ScreenTree,
) : ShellWindowController, ShellTrayPanelController {
    var selectedScreenId by mutableStateOf(screenTree.defaultLeafId)
        private set

    var selectedSceneId by mutableStateOf(
        screenTree.sceneRootIdFor(screenTree.defaultLeafId),
    )
        private set

    var windowVisible by mutableStateOf(true)
        private set

    var exitRequested by mutableStateOf(false)
        private set

    var trayPanelVisible by mutableStateOf(false)
        private set

    fun selectScreen(
        screenId: String,
    ) {
        val leaf = screenTree.findLeaf(screenId) ?: return
        selectedScreenId = leaf.id
        selectedSceneId = screenTree.sceneRootIdFor(leaf.id)
    }

    fun selectScene(
        sceneId: String,
    ) {
        val leafId = screenTree.firstVisibleLeafIdUnder(sceneId) ?: return
        selectedSceneId = sceneId
        selectedScreenId = leafId
    }

    override fun showWindow() {
        windowVisible = true
    }

    override fun hideWindow() {
        windowVisible = false
    }

    override fun toggleWindow() {
        windowVisible = !windowVisible
    }

    override fun requestExit() {
        trayPanelVisible = false
        exitRequested = true
    }

    override fun showTrayPanel() {
        trayPanelVisible = true
    }

    override fun hideTrayPanel() {
        trayPanelVisible = false
    }

    override fun toggleTrayPanel() {
        trayPanelVisible = !trayPanelVisible
    }
}

private fun ScreenTree.sceneRootIdFor(
    screenId: String,
): String {
    val node = findLeaf(screenId)
    return when {
        node == null -> roots.firstOrNull()?.id.orEmpty()
        node.ancestorIds.isNotEmpty() -> node.ancestorIds.first()
        else -> node.id
    }
}

private fun ScreenTree.firstVisibleLeafIdUnder(
    sceneId: String,
): String? {
    return findNode(sceneId)?.firstVisibleLeafId()
}

private fun site.addzero.workbenchshell.ScreenNode.firstVisibleLeafId(): String? {
    if (!visible) {
        return null
    }
    if (isLeaf) {
        return id
    }
    return children.firstNotNullOfOrNull { child -> child.firstVisibleLeafId() }
}
