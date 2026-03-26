package site.addzero.vibepocket.render

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.ScreenTree

@Single
class VibePocketShellState(
    private val screenTree: ScreenTree,
) {
    var selectedScreenId by mutableStateOf(screenTree.defaultLeafId)
        private set

    fun selectScreen(
        screenId: String,
    ) {
        selectedScreenId = screenTree.normalizeScreenId(screenId)
    }

    fun selectDefaultScreen() {
        selectedScreenId = screenTree.defaultLeafId
    }
}
