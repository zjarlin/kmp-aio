package site.addzero.vibepocket.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import site.addzero.component.glass.*
import site.addzero.vibepocket.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoApiClient
import site.addzero.vibepocket.api.suno.SunoTrack

/**
 * 音乐历史/收藏页面 — 带 Tab 切换（"全部" | "收藏"）
 *
 * - 全部 Tab：从 /api/suno/history 加载历史记录
 * - 收藏 Tab：从 /api/favorites 加载收藏列表
 * - 每条记录使用 TrackCard 渲染，集成播放和收藏功能
 * - 空状态、加载状态、错误状态均有友好提示
 */
@Composable
fun MusicHistoryPage() {
    var selectedTab by remember { mutableStateOf(HistoryTab.ALL) }

    // ── 全部 Tab 状态 ──
    var historyItems by remember { mutableStateOf<List<MusicHistoryItem>>(emptyList()) }
    var historyLoading by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf<String?>(null) }

    // ── 收藏 Tab 状态 ──
    var favoriteItems by remember { mutableStateOf<List<FavoriteItem>>(emptyList()) }
    var favoriteLoading by remember { mutableStateOf(false) }
    var favoriteError by remember { mutableStateOf<String?>(null) }

    // ── 收藏集合（用于星星状态） ──
    val favoriteSet = remember { mutableStateMapOf<String, Boolean>() }

    // ── 积分状态 ──
    var credits by remember { mutableStateOf<Int?>(null) }
    var isLoadingCredits by remember { mutableStateOf(false) }

    // ── 播放状态 ──
    val currentTrackId by AudioPlayerManager.currentTrackId.collectAsState()
    val playerState by AudioPlayerManager.playerState.collectAsState()
    val progress by AudioPlayerManager.progress.collectAsState()
    val position by AudioPlayerManager.position.collectAsState()
    val duration by AudioPlayerManager.duration.collectAsState()

    val scope = rememberCoroutineScope()

    // 加载收藏集合 & 积分（初始化）
    LaunchedEffect(Unit) {
        val favorites = ServerApiClient.getFavorites()
        favorites.forEach { fav -> favoriteSet[fav.trackId] = true }

        // 加载积分
        isLoadingCredits = true
        try {
            val token = ServerApiClient.getConfig("suno_api_token") ?: ""
            val url = ServerApiClient.getConfig("suno_api_base_url")
                ?.ifBlank { null }
                ?: "https://api.sunoapi.org/api/v1"
            val client = SunoApiClient(apiToken = token, baseUrl = url)
            credits = client.getCredits()
        } catch (_: Exception) {
            credits = null
        } finally {
            isLoadingCredits = false
        }
    }

    // Tab 切换时加载数据
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            HistoryTab.ALL -> {
                historyLoading = true
                historyError = null
                try {
                    historyItems = ServerApiClient.getHistory()
                } catch (e: Exception) {
                    historyError = e.message ?: "加载历史记录失败"
                }
                historyLoading = false
            }
            HistoryTab.FAVORITES -> {
                favoriteLoading = true
                favoriteError = null
                try {
                    favoriteItems = ServerApiClient.getFavorites()
                    // 同步收藏集合
                    favoriteSet.clear()
                    favoriteItems.forEach { fav -> favoriteSet[fav.trackId] = true }
                } catch (e: Exception) {
                    favoriteError = e.message ?: "加载收藏列表失败"
                }
                favoriteLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassTheme.DarkBackground)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "🎶 音乐库",
                color = GlassTheme.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            CreditsBar(credits = credits, isLoading = isLoadingCredits)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Tab 栏 ──
            HistoryTabBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            Spacer(modifier = Modifier.height(20.dp))

            // ── 内容区 ──
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
                                try { historyItems = ServerApiClient.getHistory() }
                                catch (e: Exception) { historyError = e.message ?: "加载失败" }
                                historyLoading = false
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
                                            )
                                        )
                                        favoriteSet[trackId] = true
                                    } else {
                                        ServerApiClient.removeFavorite(trackId)
                                        favoriteSet.remove(trackId)
                                    }
                                } catch (_: Exception) { }
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
                                    favoriteItems.forEach { fav -> favoriteSet[fav.trackId] = true }
                                } catch (e: Exception) { favoriteError = e.message ?: "加载失败" }
                                favoriteLoading = false
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
                                } catch (_: Exception) { }
                            }
                        },
                    )
                }
            }
        }
    }
}


// ── Tab 枚举 ──

private enum class HistoryTab(val title: String, val icon: String) {
    ALL("全部", "📋"),
    FAVORITES("收藏", "⭐"),
}

// ── Tab 栏 ──

@Composable
private fun HistoryTabBar(selectedTab: HistoryTab, onTabSelected: (HistoryTab) -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            HistoryTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (isSelected) Modifier.background(GlassTheme.NeonCyan.copy(alpha = 0.15f))
                            else Modifier
                        )
                        .clickable(interactionSource = interactionSource, indication = null) {
                            onTabSelected(tab)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${tab.icon} ${tab.title}",
                        color = if (isSelected) GlassTheme.NeonCyan else GlassTheme.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

// ── 全部 Tab 内容 ──

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
        items.isEmpty() -> EmptyState(message = "暂无历史记录", hint = "去创作页面生成你的第一首音乐吧 🎵")
        else -> {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items.forEach { historyItem ->
                    // 历史记录头部信息
                    HistoryItemHeader(historyItem)

                    // 展开每条历史的 tracks
                    historyItem.tracks.forEach { historyTrack ->
                        val sunoTrack = historyTrack.toSunoTrack()
                        val trackId = sunoTrack.id
                        val isFavorite = trackId != null && (favoriteSet[trackId] == true)

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
                            onFavoriteToggle = { newFav ->
                                if (trackId != null) {
                                    onFavoriteToggle(trackId, sunoTrack, historyItem.taskId, newFav)
                                }
                            },
                            onAction = { /* TODO: 操作表单 Dialog */ },
                            playerState = trackPlayerState,
                            onPlayToggle = {
                                if (trackId == null || sunoTrack.audioUrl == null) return@TrackCard
                                when {
                                    currentTrackId == trackId && playerState == PlayerState.PLAYING ->
                                        AudioPlayerManager.pause()
                                    currentTrackId == trackId && playerState == PlayerState.PAUSED ->
                                        AudioPlayerManager.resume()
                                    else ->
                                        AudioPlayerManager.play(trackId, sunoTrack.audioUrl!!)
                                }
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


// ── 收藏 Tab 内容 ──

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
            hint = "点击 ⭐ 收藏喜欢的音乐",
        )
        else -> {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items.forEach { favItem ->
                    val sunoTrack = favItem.toSunoTrack()
                    val trackId = favItem.trackId

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
                        taskId = favItem.taskId,
                        isFavorite = true,
                        onFavoriteToggle = { newFav -> onFavoriteToggle(trackId, newFav) },
                        onAction = { /* TODO: 操作表单 Dialog */ },
                        playerState = trackPlayerState,
                        onPlayToggle = {
                            val audioUrl = sunoTrack.audioUrl ?: return@TrackCard
                            when {
                                currentTrackId == trackId && playerState == PlayerState.PLAYING ->
                                    AudioPlayerManager.pause()
                                currentTrackId == trackId && playerState == PlayerState.PAUSED ->
                                    AudioPlayerManager.resume()
                                else ->
                                    AudioPlayerManager.play(trackId, audioUrl)
                            }
                        },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── 历史记录头部 ──

@Composable
private fun HistoryItemHeader(item: MusicHistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = when (item.status) {
                    "SUCCESS" -> "✅"
                    "FAILED" -> "❌"
                    else -> "⏳"
                },
                fontSize = 14.sp,
            )
            Text(
                text = item.taskId.take(12) + if (item.taskId.length > 12) "…" else "",
                color = GlassTheme.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        item.createdAt?.let { time ->
            Text(
                text = time,
                color = GlassTheme.TextTertiary,
                fontSize = 11.sp,
            )
        }
    }
}

// ── 通用状态组件 ──

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = GlassTheme.NeonCyan, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("加载中...", color = GlassTheme.TextTertiary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GlassCard(modifier = Modifier.widthIn(max = 400.dp).padding(32.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("😵", fontSize = 36.sp)
                Text(
                    text = message,
                    color = GlassColors.NeonMagenta,
                    fontSize = 14.sp,
                )
                NeonGlassButton(
                    text = "🔄 重试",
                    onClick = onRetry,
                    glowColor = GlassTheme.NeonCyan,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, hint: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GlassCard(modifier = Modifier.widthIn(max = 400.dp).padding(32.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("🎵", fontSize = 48.sp)
                Text(
                    text = message,
                    color = GlassTheme.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = hint,
                    color = GlassTheme.TextTertiary,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

// ── 数据转换扩展 ──

/** MusicHistoryTrack → SunoTrack 转换 */
internal fun MusicHistoryTrack.toSunoTrack(): SunoTrack = SunoTrack(
    id = id,
    audioUrl = audioUrl,
    title = title,
    tags = tags,
    imageUrl = imageUrl,
    duration = duration,
)

/** FavoriteItem → SunoTrack 转换 */
internal fun FavoriteItem.toSunoTrack(): SunoTrack = SunoTrack(
    id = trackId,
    audioUrl = audioUrl,
    title = title,
    tags = tags,
    imageUrl = imageUrl,
    duration = duration,
)
