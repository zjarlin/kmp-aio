package site.addzero.kcloud.screens.musicstudio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.music.MusicStudioService

data class MusicStudioScreenState(
    val selectedTab: MusicStudioTab = MusicStudioTab.COVER,
    val credits: Int? = null,
    val isLoadingCredits: Boolean = false,
)

@KoinViewModel
class MusicStudioViewModel(
    private val musicStudioService: MusicStudioService,
) : ViewModel() {
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
        viewModelScope.launch {
            val runtimeConfig = musicStudioService.loadConfig()
            if (!runtimeConfig.hasToken) {
                state = state.copy(
                    credits = null,
                    isLoadingCredits = false,
                )
                return@launch
            }

            state = state.copy(isLoadingCredits = true)
            try {
                state = state.copy(credits = musicStudioService.getCreditsOrNull())
            } finally {
                state = state.copy(isLoadingCredits = false)
            }
        }
    }
}
