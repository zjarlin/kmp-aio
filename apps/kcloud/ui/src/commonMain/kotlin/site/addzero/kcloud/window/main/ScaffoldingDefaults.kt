package site.addzero.kcloud.window.main

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import site.addzero.component.chat.AddChatOverlay
import site.addzero.component.chat.AddChatOverlayState
import site.addzero.kcloud.plugins.system.aichat.workbench.AiChatWorkbenchPanel
import site.addzero.kcloud.plugins.system.aichat.workbench.AiChatWorkbenchState

@Composable
internal fun DefaultOverlay(
    overlayState: AddChatOverlayState = koinInject(),
    aiChatState: AiChatWorkbenchState = koinInject(),
) {
    AddChatOverlay(
        visible = overlayState.visible,
        onDismiss = overlayState::hide,
    ) {
        AiChatWorkbenchPanel(state = aiChatState)
    }
}
