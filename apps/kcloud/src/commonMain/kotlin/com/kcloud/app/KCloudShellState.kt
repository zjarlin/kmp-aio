package com.kcloud.app

import com.kcloud.feature.ShellWindowController
import com.kcloud.feature.ShellTrayPanelController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.ScreenCatalog

@Single
class KCloudShellState(
    private val screenCatalog: ScreenCatalog,
) : ShellWindowController, ShellTrayPanelController {
    private val _selectedScreenId = MutableStateFlow(screenCatalog.defaultLeafId)
    val selectedScreenId: StateFlow<String> = _selectedScreenId.asStateFlow()

    private val _selectedSceneId = MutableStateFlow(
        screenCatalog.sceneRootIdFor(screenCatalog.defaultLeafId),
    )
    val selectedSceneId: StateFlow<String> = _selectedSceneId.asStateFlow()

    private val _windowVisible = MutableStateFlow(true)
    val windowVisible: StateFlow<Boolean> = _windowVisible.asStateFlow()

    private val _exitRequested = MutableStateFlow(false)
    val exitRequested: StateFlow<Boolean> = _exitRequested.asStateFlow()

    private val _trayPanelVisible = MutableStateFlow(false)
    val trayPanelVisible: StateFlow<Boolean> = _trayPanelVisible.asStateFlow()

    fun selectScreen(
        screenId: String,
    ) {
        val leaf = screenCatalog.findLeaf(screenId) ?: return
        _selectedScreenId.value = leaf.id
        _selectedSceneId.value = screenCatalog.sceneRootIdFor(leaf.id)
    }

    fun selectScene(
        sceneId: String,
    ) {
        val leafId = screenCatalog.firstVisibleLeafIdUnder(sceneId) ?: return
        _selectedSceneId.value = sceneId
        _selectedScreenId.value = leafId
    }

    override fun showWindow() {
        _windowVisible.value = true
    }

    override fun hideWindow() {
        _windowVisible.value = false
    }

    override fun toggleWindow() {
        _windowVisible.value = !_windowVisible.value
    }

    override fun requestExit() {
        _trayPanelVisible.value = false
        _exitRequested.value = true
    }

    override fun showTrayPanel() {
        _trayPanelVisible.value = true
    }

    override fun hideTrayPanel() {
        _trayPanelVisible.value = false
    }

    override fun toggleTrayPanel() {
        _trayPanelVisible.value = !_trayPanelVisible.value
    }
}

private fun ScreenCatalog.sceneRootIdFor(
    screenId: String,
): String {
    val node = findLeaf(screenId)
    return when {
        node == null -> tree.firstOrNull()?.id.orEmpty()
        node.ancestorIds.isNotEmpty() -> node.ancestorIds.first()
        else -> node.id
    }
}

private fun ScreenCatalog.firstVisibleLeafIdUnder(
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
