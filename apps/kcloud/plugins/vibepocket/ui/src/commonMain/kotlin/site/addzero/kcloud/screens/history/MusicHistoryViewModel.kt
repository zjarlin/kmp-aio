package site.addzero.kcloud.screens.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.api.ServerApiClient
import site.addzero.kcloud.api.suno.SunoTrack
import site.addzero.kcloud.vibepocket.model.FavoriteItem
import site.addzero.kcloud.vibepocket.model.FavoriteRequest
import site.addzero.kcloud.vibepocket.model.MusicHistoryItem
import site.addzero.kcloud.music.SunoWorkflowService

enum class MusicHistoryTab(
    val title: String,
    val icon: String,
) {
    ALL("全部", "📋"),
    FAVORITES("收藏", "⭐"),
}

data class MusicHistoryLoadState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class MusicHistoryScreenState(
    val selectedTab: MusicHistoryTab = MusicHistoryTab.ALL,
    val history: MusicHistoryLoadState<MusicHistoryItem> = MusicHistoryLoadState(),
    val favorites: MusicHistoryLoadState<FavoriteItem> = MusicHistoryLoadState(),
    val favoriteIds: Set<String> = emptySet(),
    val credits: Int? = null,
    val isLoadingCredits: Boolean = false,
)

@KoinViewModel
class MusicHistoryViewModel : ViewModel() {
    private val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var state by mutableStateOf(MusicHistoryScreenState())
        private set

    init {
        refreshFavoriteIds()
        refreshCredits()
        refreshSelectedTab()
    }

    fun selectTab(tab: MusicHistoryTab) {
        if (state.selectedTab == tab) {
            return
        }
        state = state.copy(selectedTab = tab)
        refreshSelectedTab()
    }

    fun refreshSelectedTab() {
        when (state.selectedTab) {
            MusicHistoryTab.ALL -> refreshHistory()
            MusicHistoryTab.FAVORITES -> refreshFavorites()
        }
    }

    fun toggleHistoryFavorite(
        trackId: String,
        track: SunoTrack,
        taskId: String,
        newFavorite: Boolean,
    ) {
        screenScope.launch {
            runCatching {
                if (newFavorite) {
                    ServerApiClient.favoriteApi.addFavorite(
                        FavoriteRequest(
                            trackId = trackId,
                            taskId = taskId,
                            audioUrl = track.audioUrl,
                            title = track.title,
                            tags = track.tags,
                            imageUrl = track.imageUrl,
                            duration = track.duration,
                        ),
                    )
                    state = state.copy(favoriteIds = state.favoriteIds + trackId)
                } else {
                    ServerApiClient.favoriteApi.removeFavorite(trackId)
                    state = state.copy(
                        favoriteIds = state.favoriteIds - trackId,
                        favorites = state.favorites.copy(
                            items = state.favorites.items.filterNot { item -> item.trackId == trackId },
                        ),
                    )
                }
            }
        }
    }

    fun removeFavorite(trackId: String) {
        screenScope.launch {
            runCatching {
                ServerApiClient.favoriteApi.removeFavorite(trackId)
                state = state.copy(
                    favoriteIds = state.favoriteIds - trackId,
                    favorites = state.favorites.copy(
                        items = state.favorites.items.filterNot { item -> item.trackId == trackId },
                    ),
                )
            }
        }
    }

    private fun refreshHistory() {
        screenScope.launch {
            state = state.copy(
                history = state.history.copy(
                    isLoading = true,
                    errorMessage = null,
                ),
            )
            try {
                val historyItems = ServerApiClient.historyApi.getHistory()
                state = state.copy(
                    history = MusicHistoryLoadState(items = historyItems),
                )
            } catch (error: Exception) {
                state = state.copy(
                    history = state.history.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "加载历史记录失败",
                    ),
                )
            }
        }
    }

    private fun refreshFavorites() {
        screenScope.launch {
            state = state.copy(
                favorites = state.favorites.copy(
                    isLoading = true,
                    errorMessage = null,
                ),
            )
            try {
                val favoriteItems = ServerApiClient.favoriteApi.getFavorites()
                state = state.copy(
                    favorites = MusicHistoryLoadState(items = favoriteItems),
                    favoriteIds = favoriteItems.mapTo(linkedSetOf()) { item -> item.trackId },
                )
            } catch (error: Exception) {
                state = state.copy(
                    favorites = state.favorites.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "加载收藏列表失败",
                    ),
                )
            }
        }
    }

    private fun refreshFavoriteIds() {
        screenScope.launch {
            runCatching {
                ServerApiClient.favoriteApi.getFavorites()
            }.onSuccess { favoriteItems ->
                state = state.copy(
                    favoriteIds = favoriteItems.mapTo(linkedSetOf()) { item -> item.trackId },
                )
            }
        }
    }

    private fun refreshCredits() {
        screenScope.launch {
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
