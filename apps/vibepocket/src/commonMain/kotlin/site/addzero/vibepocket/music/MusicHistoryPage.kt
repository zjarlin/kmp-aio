package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoTrack
import site.addzero.vibepocket.model.FavoriteItem
import site.addzero.vibepocket.model.FavoriteRequest
import site.addzero.vibepocket.model.MusicHistoryItem
import site.addzero.vibepocket.model.MusicHistoryTrack
import site.addzero.vibepocket.model.TrackPlayerState
import site.addzero.vibepocket.ui.StudioEmptyState
import site.addzero.vibepocket.ui.StudioPill
import site.addzero.vibepocket.ui.StudioSectionCard

@Composable
fun MusicHistoryPage() {
    var selectedTab by remember { mutableStateOf(HistoryTab.ALL) }

    var historyItems by remember { mutableStateOf<List<MusicHistoryItem>>(emptyList()) }
    var historyLoading by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf<String?>(null) }

    var favoriteItems by remember { mutableStateOf<List<FavoriteItem>>(emptyList()) }
    var favoriteLoading by remember { mutableStateOf(false) }
    var favoriteError by remember { mutableStateOf<String?>(null) }

    val favoriteSet = remember { mutableStateMapOf<String, Boolean>() }

    var credits by remember { mutableStateOf<Int?>(null) }
    var isLoadingCredits by remember { mutableStateOf(false) }

    val currentTrackId by AudioPlayerManager.currentTrackId.collectAsState()
    val playerState by AudioPlayerManager.playerState.collectAsState()
    val progress by AudioPlayerManager.progress.collectAsState()
    val position by AudioPlayerManager.position.collectAsState()
    val duration by AudioPlayerManager.duration.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val favorites = ServerApiClient.getFavorites()
        favorites.forEach { favorite ->
            favoriteSet[favorite.trackId] = true
        }
        isLoadingCredits = true
        credits = SunoWorkflowService.getCreditsOrNull()
        isLoadingCredits = false
    }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            HistoryTab.ALL -> {
                historyLoading = true
                historyError = null
                try {
                    historyItems = ServerApiClient.getHistory()
                } catch (error: Exception) {
                    historyError = error.message ?: "加载历史记录失败"
                } finally {
                    historyLoading = false
                }
            }

            HistoryTab.FAVORITES -> {
                favoriteLoading = true
                favoriteError = null
                try {
                    favoriteItems = ServerApiClient.getFavorites()
                    favoriteSet.clear()
                    favoriteItems.forEach { favorite ->
                        favoriteSet[favorite.trackId] = true
                    }
                } catch (error: Exception) {
                    favoriteError = error.message ?: "加载收藏列表失败"
                } finally {
                    favoriteLoading = false
                }
            }
        }
    }

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
            credits = credits,
            isLoading = isLoadingCredits,
        )

        HistoryTabBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                HistoryTab.ALL -> HistoryAllContent(
                    items = historyItems,
                    isLoading = historyLoading,
                    error = historyError,
                    onRetry = {
                        scope.launch {
                            historyLoading = true
                            historyError = null
                            try {
                                historyItems = ServerApiClient.getHistory()
                            } catch (error: Exception) {
                                historyError = error.message ?: "加载失败"
                            } finally {
                                historyLoading = false
                            }
                        }
                    },
                    favoriteSet = favoriteSet,
                    currentTrackId = currentTrackId,
                    playerState = playerState,
                    progress = progress,
                    position = position,
                    duration = duration,
                    onFavoriteToggle = { trackId, track, taskId, newFavorite ->
                        scope.launch {
                            try {
                                if (newFavorite) {
                                    ServerApiClient.addFavorite(
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
                                    favoriteSet[trackId] = true
                                } else {
                                    ServerApiClient.removeFavorite(trackId)
                                    favoriteSet.remove(trackId)
                                }
                            } catch (_: Exception) {
                                // 收藏失败不阻断页面
                            }
                        }
                    },
                )

                HistoryTab.FAVORITES -> FavoritesContent(
                    items = favoriteItems,
                    isLoading = favoriteLoading,
                    error = favoriteError,
                    onRetry = {
                        scope.launch {
                            favoriteLoading = true
                            favoriteError = null
                            try {
                                favoriteItems = ServerApiClient.getFavorites()
                                favoriteSet.clear()
                                favoriteItems.forEach { favorite ->
                                    favoriteSet[favorite.trackId] = true
                                }
                            } catch (error: Exception) {
                                favoriteError = error.message ?: "加载失败"
                            } finally {
                                favoriteLoading = false
                            }
                        }
                    },
                    currentTrackId = currentTrackId,
                    playerState = playerState,
                    progress = progress,
                    position = position,
                    duration = duration,
                    onFavoriteToggle = { trackId, newFavorite ->
                        scope.launch {
                            try {
                                if (!newFavorite) {
                                    ServerApiClient.removeFavorite(trackId)
                                    favoriteSet.remove(trackId)
                                    favoriteItems = favoriteItems.filter { it.trackId != trackId }
                                }
                            } catch (_: Exception) {
                                // 收藏失败不阻断页面
                            }
                        }
                    },
                )
            }
        }
    }
}

private enum class HistoryTab(
    val title: String,
    val icon: String,
) {
    ALL("全部", "📋"),
    FAVORITES("收藏", "⭐"),
}

@Composable
private fun HistoryTabBar(
    selectedTab: HistoryTab,
    onTabSelected: (HistoryTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HistoryTab.entries.forEach { tab ->
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
    favoriteSet: Map<String, Boolean>,
    currentTrackId: String?,
    playerState: PlayerState,
    progress: Float,
    position: Long,
    duration: Long,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items.forEach { historyItem ->
                    HistoryItemHeader(historyItem)
                    historyItem.tracks.forEach { historyTrack ->
                        val sunoTrack = historyTrack.toSunoTrack()
                        val trackId = sunoTrack.id
                        val isFavorite = trackId != null && favoriteSet[trackId] == true
                        val trackPlayerState = if (trackId != null && currentTrackId == trackId) {
                            TrackPlayerState(
                                isPlaying = playerState == PlayerState.PLAYING,
                                progress = progress,
                                currentTime = AudioPlayerManager.formatTime(position),
                                totalTime = AudioPlayerManager.formatTime(duration),
                            )
                        } else {
                            TrackPlayerState()
                        }

                        TrackCard(
                            track = sunoTrack,
                            taskId = historyItem.taskId,
                            isFavorite = isFavorite,
                            onFavoriteToggle = { newFavorite ->
                                if (trackId != null) {
                                    onFavoriteToggle(trackId, sunoTrack, historyItem.taskId, newFavorite)
                                }
                            },
                            onAction = {},
                            playerState = trackPlayerState,
                            onPlayToggle = {
                                if (trackId == null || sunoTrack.audioUrl == null) {
                                    return@TrackCard
                                }
                                when {
                                    currentTrackId == trackId && playerState == PlayerState.PLAYING -> AudioPlayerManager.pause()
                                    currentTrackId == trackId && playerState == PlayerState.PAUSED -> AudioPlayerManager.resume()
                                    else -> AudioPlayerManager.play(trackId, sunoTrack.audioUrl!!)
                                }
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    items: List<FavoriteItem>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    currentTrackId: String?,
    playerState: PlayerState,
    progress: Float,
    position: Long,
    duration: Long,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items.forEach { favoriteItem ->
                    val sunoTrack = favoriteItem.toSunoTrack()
                    val trackId = favoriteItem.trackId
                    val trackPlayerState = if (currentTrackId == trackId) {
                        TrackPlayerState(
                            isPlaying = playerState == PlayerState.PLAYING,
                            progress = progress,
                            currentTime = AudioPlayerManager.formatTime(position),
                            totalTime = AudioPlayerManager.formatTime(duration),
                        )
                    } else {
                        TrackPlayerState()
                    }

                    TrackCard(
                        track = sunoTrack,
                        taskId = favoriteItem.taskId,
                        isFavorite = true,
                        onFavoriteToggle = { newFavorite ->
                            onFavoriteToggle(trackId, newFavorite)
                        },
                        onAction = {},
                        playerState = trackPlayerState,
                        onPlayToggle = {
                            val audioUrl = sunoTrack.audioUrl ?: return@TrackCard
                            when {
                                currentTrackId == trackId && playerState == PlayerState.PLAYING -> AudioPlayerManager.pause()
                                currentTrackId == trackId && playerState == PlayerState.PAUSED -> AudioPlayerManager.resume()
                                else -> AudioPlayerManager.play(trackId, audioUrl)
                            }
                        },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HistoryItemHeader(item: MusicHistoryItem) {
    StudioSectionCard(
        modifier = Modifier.fillMaxWidth(),
        title = item.taskId.take(12) + if (item.taskId.length > 12) "…" else "",
        subtitle = item.createdAt ?: "未知时间",
        action = {
            val pillText = when (item.status) {
                "SUCCESS" -> "成功"
                "FAILED" -> "失败"
                else -> "处理中"
            }
            StudioPill(text = pillText)
        },
    ) {}
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

internal fun MusicHistoryTrack.toSunoTrack(): SunoTrack = SunoTrack(
    id = id,
    audioUrl = audioUrl,
    title = title,
    tags = tags,
    imageUrl = imageUrl,
    duration = duration,
)

internal fun FavoriteItem.toSunoTrack(): SunoTrack = SunoTrack(
    id = trackId,
    audioUrl = audioUrl,
    title = title,
    tags = tags,
    imageUrl = imageUrl,
    duration = duration,
)
