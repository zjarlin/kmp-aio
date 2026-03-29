package site.addzero.kcloud.screens.musicstudio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import org.koin.core.annotation.Factory
import site.addzero.kcloud.music.SunoRuntimeConfig
import site.addzero.kcloud.music.SunoWorkflowService

@Factory
class MusicStudioViewModel {
    private val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var selectedTab by mutableStateOf(MusicStudioTab.COVER)
        private set

    var credits by mutableStateOf<Int?>(null)
        private set

    var isLoadingCredits by mutableStateOf(false)
        private set

    init {
        refreshCredits()
    }

    fun selectTab(
        tab: MusicStudioTab,
    ) {
        selectedTab = tab
    }

    fun refreshCredits() {
        screenScope.launch {
            val runtimeConfig = runCatching { SunoWorkflowService.loadConfig() }
                .getOrDefault(SunoRuntimeConfig())
            if (!runtimeConfig.hasToken) {
                credits = null
                isLoadingCredits = false
                return@launch
            }

            isLoadingCredits = true
            try {
                credits = SunoWorkflowService.getCreditsOrNull()
            } finally {
                isLoadingCredits = false
            }
        }
    }

    fun dispose() {
        screenScope.cancel()
    }
}
