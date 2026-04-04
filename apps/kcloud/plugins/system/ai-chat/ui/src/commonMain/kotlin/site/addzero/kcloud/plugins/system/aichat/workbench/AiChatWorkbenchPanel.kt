package site.addzero.kcloud.plugins.system.aichat.workbench

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.chat.AddChatPanel as AddComposeChatPanel

@Composable
fun AiChatWorkbenchPanel(
    state: AiChatWorkbenchState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val presenter = remember(state, scope) {
        AiChatPanelPresenter(
            state = state,
            scope = scope,
        )
    }

    LaunchedEffect(presenter) {
        presenter.ensureLoaded()
    }

    AddComposeChatPanel(
        state = presenter.panelState,
        actions = presenter.panelActions,
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
    )
}
