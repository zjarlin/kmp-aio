package site.addzero.kcloud.music

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.kcloud.api.suno.SunoTrack
import site.addzero.kcloud.vibepocket.model.FavoriteItem
import site.addzero.kcloud.vibepocket.model.MusicHistoryItem
import site.addzero.kcloud.screens.history.MusicHistoryTab
import site.addzero.kcloud.screens.history.MusicHistoryViewModel
import site.addzero.kcloud.ui.StudioEmptyState
import site.addzero.kcloud.ui.StudioSectionCard
import site.addzero.media.playlist.player.DefaultPlaylistPlayer

@Composable
fun MusicHistoryScreen() {
    val viewModel: MusicHistoryViewModel = koinViewModel()
    val state = viewModel.state
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "音乐库",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "查看生成历史和收藏内容，试听会复用同一个全局播放器。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CreditsBar(
            credits = state.credits,
            isLoading = state.isLoadingCredits,
        )

        HistoryTabBar(
            selectedTab = state.selectedTab,
            onTabSelected = viewModel::selectTab,
        )

        Box(modifier = Modifier.weight(1f)) {
            when (state.selectedTab) {
                MusicHistoryTab.ALL -> HistoryAllContent(
                    items = state.history.items,
                    isLoading = state.history.isLoading,
                    error = state.history.errorMessage,
                    onRetry = viewModel::refreshSelectedTab,
                    favoriteSet = state.favoriteIds,
                    onFavoriteToggle = viewModel::toggleHistoryFavorite,
                )

                MusicHistoryTab.FAVORITES -> FavoritesContent(
                    items = state.favorites.items,
                    isLoading = state.favorites.isLoading,
                    error = state.favorites.errorMessage,
                    onRetry = viewModel::refreshSelectedTab,
                    onFavoriteToggle = { trackId, newFavorite ->
                        if (!newFavorite) {
                            viewModel.removeFavorite(trackId)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryTabBar(
    selectedTab: MusicHistoryTab,
    onTabSelected: (MusicHistoryTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MusicHistoryTab.entries.forEach { tab ->
            if (tab == selectedTab) {
                FilledTonalButton(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("${tab.icon} ${tab.title}")
                }
            } else {
                OutlinedButton(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("${tab.icon} ${tab.title}")
                }
            }
        }
    }
}

@Composable
private fun HistoryAllContent(
    items: List<MusicHistoryItem>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    favoriteSet: Set<String>,
    onFavoriteToggle: (trackId: String, track: SunoTrack, taskId: String, newFavorite: Boolean) -> Unit,
) {
    when {
        isLoading -> LoadingState()
        error != null -> ErrorState(message = error, onRetry = onRetry)
        items.isEmpty() -> EmptyState(
            message = "暂无历史记录",
            hint = "去创作页面生成你的第一首音乐吧。",
        )

        else -> {
            val playlistItems = remember(items) {
                items.flatMap { historyItem -> historyItem.toPlaylistEntries() }
            }
            DefaultPlaylistPlayer(
                items = playlistItems,
                modifier = Modifier.fillMaxSize(),
                itemKey = { entry ->
                    entry.track.playbackId(entry.taskId)
                },
                titleOf = { entry ->
                    entry.track.displayTitle()
                },
                subtitleOf = { entry ->
                    entry.track.displaySubtitle(entry.taskId.take(8), entry.createdAt, entry.status)
                },
                durationMsOf = { entry ->
                    entry.track.durationMsOrNull()
                },
                coverUrlOf = { entry ->
                    entry.track.imageUrl
                },
                hasResolvableAudioOf = { entry ->
                    !entry.track.audioUrl.isNullOrBlank() || !entry.track.streamAudioUrl.isNullOrBlank()
                },
                resolveAudioSource = { entry ->
                    entry.track.resolvedAudioSource()
                },
                itemActions = { entry, _ ->
                    val trackId = entry.track.id
                    if (trackId != null) {
                        val isFavorite = trackId in favoriteSet
                        IconButton(
                            onClick = {
                                onFavoriteToggle(trackId, entry.track, entry.taskId, !isFavorite)
                            },
                        ) {
                            Icon(
                                imageVector = if (isFavorite) {
                                    Icons.Filled.Star
                                } else {
                                    Icons.Filled.StarBorder
                                },
                                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun FavoritesContent(
    items: List<FavoriteItem>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onFavoriteToggle: (trackId: String, newFavorite: Boolean) -> Unit,
) {
    when {
        isLoading -> LoadingState()
        error != null -> ErrorState(message = error, onRetry = onRetry)
        items.isEmpty() -> EmptyState(
            message = "暂无收藏",
            hint = "点击星标收藏喜欢的音乐。",
        )

        else -> {
            val playlistItems = remember(items) {
                items.map { favoriteItem -> favoriteItem.toPlaylistEntry() }
            }
            DefaultPlaylistPlayer(
                items = playlistItems,
                modifier = Modifier.fillMaxSize(),
                itemKey = { entry ->
                    entry.track.playbackId(entry.item.taskId)
                },
                titleOf = { entry ->
                    entry.track.displayTitle()
                },
                subtitleOf = { entry ->
                    entry.track.displaySubtitle(entry.item.taskId.take(8), entry.item.createdAt)
                },
                durationMsOf = { entry ->
                    entry.track.durationMsOrNull()
                },
                coverUrlOf = { entry ->
                    entry.track.imageUrl
                },
                hasResolvableAudioOf = { entry ->
                    !entry.track.audioUrl.isNullOrBlank() || !entry.track.streamAudioUrl.isNullOrBlank()
                },
                resolveAudioSource = { entry ->
                    entry.track.resolvedAudioSource()
                },
                itemActions = { entry, _ ->
                    IconButton(
                        onClick = {
                            onFavoriteToggle(entry.item.trackId, false)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "取消收藏",
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        StudioSectionCard(
            title = "加载失败",
            subtitle = message,
        ) {
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    hint: String,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        StudioEmptyState(
            icon = "🎵",
            title = message,
            description = hint,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
