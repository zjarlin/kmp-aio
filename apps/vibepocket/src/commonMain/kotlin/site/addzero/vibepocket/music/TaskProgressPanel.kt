package site.addzero.vibepocket.music

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import site.addzero.component.glass.*
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.model.FavoriteRequest
import site.addzero.vibepocket.model.TrackAction
import site.addzero.vibepocket.model.TrackPlayerState

@Composable
fun TaskProgressPanel(
    submittedJson: String?,
    taskStatus: String,
    taskDetail: SunoTaskDetail? = null,
) {
    val tracks = taskDetail?.response?.sunoData ?: emptyList()
    val scope = rememberCoroutineScope()

    // ── Dialog 状态 ──
    var extendDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) } // (audioId, taskId)
    var vocalRemovalDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var musicCoverDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var personaDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var replaceSectionDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var wavExportDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var boostStyleDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }

    // ── 收藏状态：trackId → isFavorite ──
    val favoriteSet = remember { mutableStateMapOf<String, Boolean>() }

    // 初始化时从 Server 加载收藏列表
    LaunchedEffect(Unit) {
        val favorites = ServerApiClient.getFavorites()
        favorites.forEach { fav -> favoriteSet[fav.trackId] = true }
    }

    // ── 播放状态：从 AudioPlayerManager 收集 ──
    val currentTrackId by AudioPlayerManager.currentTrackId.collectAsState()
    val playerState by AudioPlayerManager.playerState.collectAsState()
    val progress by AudioPlayerManager.progress.collectAsState()
    val position by AudioPlayerManager.position.collectAsState()
    val duration by AudioPlayerManager.duration.collectAsState()

    NeonGlassCard(
        modifier = Modifier.fillMaxSize(),
        glowColor = GlassColors.NeonMagenta
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("📊 任务面板", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassStatCard(
                    value = "${tracks.size}",
                    label = "音轨数",
                    modifier = Modifier.width(100.dp).height(80.dp),
                    glowColor = GlassColors.NeonCyan
                )
                GlassStatCard(
                    value = taskDetail?.displayStatus?.take(4) ?: taskStatus.take(4),
                    label = "状态",
                    modifier = Modifier.width(100.dp).height(80.dp),
                    glowColor = when {
                        taskDetail?.isSuccess == true -> GlassColors.NeonCyan
                        taskDetail?.isFailed == true -> GlassColors.NeonMagenta
                        else -> GlassColors.NeonPurple
                    }
                )
                val firstDuration = taskDetail?.firstTrack?.duration
                if (firstDuration != null) {
                    GlassStatCard(
                        value = "${firstDuration.toInt()}s",
                        label = "时长",
                        modifier = Modifier.width(100.dp).height(80.dp),
                        glowColor = GlassColors.NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            GlassInfoCard(title = "当前状态", content = taskStatus, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            // ===== 生成结果 =====
            Text("🎵 生成结果", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            if (taskDetail?.isSuccess == true && tracks.isNotEmpty()) {
                tracks.forEach { track ->
                    val trackId = track.id
                    val isFavorite = trackId != null && (favoriteSet[trackId] == true)

                    // 构建当前 track 的播放状态
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
                        track = track,
                        taskId = taskDetail.taskId ?: "",
                        isFavorite = isFavorite,
                        onFavoriteToggle = { newFavorite ->
                            if (trackId == null) return@TrackCard
                            scope.launch {
                                try {
                                    if (newFavorite) {
                                        ServerApiClient.addFavorite(
                                            FavoriteRequest(
                                                trackId = trackId,
                                                taskId = taskDetail.taskId ?: "",
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
                                } catch (_: Exception) {
                                    // 收藏操作失败时静默处理，不阻断主流程
                                }
                            }
                        },
                        onAction = { action ->
                            val trackAudioId = track.id ?: return@TrackCard
                            val trackTaskId = taskDetail.taskId ?: ""
                            when (action) {
                                TrackAction.EXTEND -> extendDialogTrack = trackAudioId to trackTaskId
                                TrackAction.VOCAL_REMOVAL -> vocalRemovalDialogTrack = trackAudioId to trackTaskId
                                TrackAction.GENERATE_COVER -> musicCoverDialogTrack = trackAudioId to trackTaskId
                                TrackAction.CREATE_PERSONA -> personaDialogTrack = trackAudioId to trackTaskId
                                TrackAction.REPLACE_SECTION -> replaceSectionDialogTrack = trackAudioId to trackTaskId
                                TrackAction.EXPORT_WAV -> wavExportDialogTrack = trackAudioId to trackTaskId
                                TrackAction.BOOST_STYLE -> boostStyleDialogTrack = trackAudioId to trackTaskId
                            }
                        },
                        playerState = trackPlayerState,
                        onPlayToggle = {
                            if (trackId == null || track.audioUrl == null) return@TrackCard
                            when {
                                currentTrackId == trackId && playerState == PlayerState.PLAYING ->
                                    AudioPlayerManager.pause()
                                currentTrackId == trackId && playerState == PlayerState.PAUSED ->
                                    AudioPlayerManager.resume()
                                else ->
                                    AudioPlayerManager.play(trackId, track.audioUrl!!)
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (taskDetail?.isFailed == true) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "❌ ${taskDetail.errorMessage ?: taskDetail.errorCode ?: "未知错误"}",
                            color = GlassColors.NeonMagenta,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (taskDetail?.isProcessing == true) "⏳ 正在生成中，请稍候..."
                            else "等待提交...",
                            color = GlassTheme.TextTertiary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 请求 JSON =====
            Text("📋 请求 JSON", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            submittedJson?.let { json ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = json,
                        color = GlassColors.NeonCyan.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // ── 扩展音乐 Dialog ──
    extendDialogTrack?.let { (audioId, tId) ->
        ExtendFormDialog(
            audioId = audioId,
            taskId = tId,
            onDismiss = { extendDialogTrack = null },
        )
    }

    // ── 人声分离 Dialog ──
    vocalRemovalDialogTrack?.let { (audioId, tId) ->
        VocalRemovalConfirmDialog(
            audioId = audioId,
            taskId = tId,
            onDismiss = { vocalRemovalDialogTrack = null },
        )
    }

    // ── 封面生成 Dialog ──
    musicCoverDialogTrack?.let { (audioId, tId) ->
        MusicCoverFormDialog(
            audioId = audioId,
            taskId = tId,
            onDismiss = { musicCoverDialogTrack = null },
        )
    }

    // ── 创建 Persona Dialog ──
    personaDialogTrack?.let { (audioId, tId) ->
        PersonaFormDialog(
            audioId = audioId,
            taskId = tId,
            onDismiss = { personaDialogTrack = null },
        )
    }

    // ── 片段替换 Dialog ──
    replaceSectionDialogTrack?.let { (audioId, tId) ->
        ReplaceSectionFormDialog(
            audioId = audioId,
            taskId = tId,
            onDismiss = { replaceSectionDialogTrack = null },
        )
    }

    // ── WAV 导出 Dialog ──
    wavExportDialogTrack?.let { (audioId, tId) ->
        WavExportConfirmDialog(
            audioId = audioId,
            taskId = tId,
            onDismiss = { wavExportDialogTrack = null },
        )
    }

    // ── 风格提升 Dialog ──
    boostStyleDialogTrack?.let { (audioId, tId) ->
        BoostStyleConfirmDialog(
            audioId = audioId,
            taskId = tId,
            onDismiss = { boostStyleDialogTrack = null },
        )
    }
}
