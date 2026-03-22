package site.addzero.vibepocket.render

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import site.addzero.workbenchshell.ScreenCatalog

class VibePocketShellState(
    private val screenCatalog: ScreenCatalog,
) {
    private val _selectedScreenId = MutableStateFlow(screenCatalog.defaultLeafId)
    val selectedScreenId: StateFlow<String> = _selectedScreenId.asStateFlow()

    fun selectScreen(
        screenId: String,
    ) {
        _selectedScreenId.value = screenCatalog.normalizeScreenId(screenId)
    }

    fun selectDefaultScreen() {
        _selectedScreenId.value = screenCatalog.defaultLeafId
    }
}
