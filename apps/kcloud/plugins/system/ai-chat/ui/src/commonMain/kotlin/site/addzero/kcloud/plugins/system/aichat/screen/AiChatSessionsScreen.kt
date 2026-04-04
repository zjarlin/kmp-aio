package site.addzero.kcloud.plugins.system.aichat.screen

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import site.addzero.kcloud.plugins.system.aichat.workbench.AiChatWorkbenchPanel
import site.addzero.kcloud.plugins.system.aichat.workbench.AiChatWorkbenchState

@Composable
fun AiChatSessionsScreen() {
    val state: AiChatWorkbenchState = koinInject()
    AiChatWorkbenchPanel(state = state)
}
