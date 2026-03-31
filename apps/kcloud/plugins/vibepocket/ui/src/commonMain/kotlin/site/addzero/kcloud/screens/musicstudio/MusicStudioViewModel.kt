package site.addzero.kcloud.screens.musicstudio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.music.SunoRuntimeConfig
import site.addzero.kcloud.music.SunoWorkflowService

data class MusicStudioScreenState(
    val selectedTab: MusicStudioTab = MusicStudioTab.COVER,
    val credits: Int? = null,
    val isLoadingCredits: Boolean = false,
)

@KoinViewModel
class MusicStudioViewModel : ViewModel() {
    private val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var state by mutableStateOf(MusicStudioScreenState())
        private set

    init {
        refreshCredits()
    }

    fun selectTab(
        tab: MusicStudioTab,
    ) {
        state = state.copy(selectedTab = tab)
    }

    fun refreshCredits() {
        screenScope.launch {
            val runtimeConfig = runCatching { SunoWorkflowService.loadConfig() }
                .getOrDefault(SunoRuntimeConfig())
            if (!runtimeConfig.hasToken) {
                state = state.copy(
                    credits = null,
                    isLoadingCredits = false,
                )
                return@launch
            }

            state = state.copy(isLoadingCredits = true)
            try {
                state = state.copy(credits = SunoWorkflowService.getCreditsOrNull())
            } finally {
                state = state.copy(isLoadingCredits = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        screenScope.cancel()
    }
}
