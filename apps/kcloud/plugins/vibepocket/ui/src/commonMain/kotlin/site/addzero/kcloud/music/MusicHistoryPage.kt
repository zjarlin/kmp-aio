package site.addzero.kcloud.music

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.kcloud.api.ServerApiClient
import site.addzero.kcloud.api.suno.SunoTrack
import site.addzero.kcloud.model.FavoriteItem
import site.addzero.kcloud.model.FavoriteRequest
import site.addzero.kcloud.model.MusicHistoryItem
import site.addzero.kcloud.ui.StudioEmptyState
import site.addzero.kcloud.ui.StudioPill
import site.addzero.kcloud.ui.StudioSectionCard
import site.addzero.media.playlist.player.DefaultPlaylistPlayer

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

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val favorites = ServerApiClient.favoriteApi.getFavorites()
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
                    historyItems = ServerApiClient.historyApi.getHistory()
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
                    favoriteItems = ServerApiClient.favoriteApi.getFavorites()
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
                                historyItems = ServerApiClient.historyApi.getHistory()
                            } catch (error: Exception) {
                                historyError = error.message ?: "加载失败"
                            } finally {
                                historyLoading = false
                            }
                        }
                    },
                    favoriteSet = favoriteSet,
                    onFavoriteToggle = { trackId, track, taskId, newFavorite ->
                        scope.launch {
                            try {
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
                                    favoriteSet[trackId] = true
                                } else {
                                    ServerApiClient.favoriteApi.removeFavorite(trackId)
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
                                favoriteItems = ServerApiClient.favoriteApi.getFavorites()
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
                    onFavoriteToggle = { trackId, newFavorite ->
                        scope.launch {
                            try {
                                if (!newFavorite) {
                                    ServerApiClient.favoriteApi.removeFavorite(trackId)
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
                        val isFavorite = favoriteSet[trackId] == true
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
