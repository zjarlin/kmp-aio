package com.kcloud.app

import com.kcloud.feature.ShellWindowController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.ScreenCatalog

@Single
class KCloudShellState(
    private val screenCatalog: ScreenCatalog,
) : ShellWindowController {
    private val _selectedScreenId = MutableStateFlow(screenCatalog.defaultLeafId)
    val selectedScreenId: StateFlow<String> = _selectedScreenId.asStateFlow()

    private val _windowVisible = MutableStateFlow(true)
    val windowVisible: StateFlow<Boolean> = _windowVisible.asStateFlow()

    private val _exitRequested = MutableStateFlow(false)
    val exitRequested: StateFlow<Boolean> = _exitRequested.asStateFlow()

    fun selectScreen(
        screenId: String,
    ) {
        val leaf = screenCatalog.findLeaf(screenId) ?: return
        _selectedScreenId.value = leaf.id
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
        _exitRequested.value = true
    }
}
