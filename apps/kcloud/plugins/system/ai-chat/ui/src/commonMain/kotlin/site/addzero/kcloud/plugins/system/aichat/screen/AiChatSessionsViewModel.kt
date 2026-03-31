package site.addzero.kcloud.plugins.system.aichat.screen

import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.system.aichat.AiChatWorkbenchState

@KoinViewModel
class AiChatSessionsViewModel(
    val state: AiChatWorkbenchState,
) : ViewModel()
