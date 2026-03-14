package site.addzero.vibepocket.music

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.model.FavoriteRequest
import site.addzero.vibepocket.model.TrackAction
import site.addzero.vibepocket.model.TrackPlayerState
import site.addzero.vibepocket.ui.StudioEmptyState
import site.addzero.vibepocket.ui.StudioMetricCard
import site.addzero.vibepocket.ui.StudioSectionCard

@Composable
fun TaskProgressPanel(
    submittedJson: String?,
    taskStatus: String,
    taskDetail: SunoTaskDetail? = null,
) {
    val tracks = taskDetail?.response?.sunoData ?: emptyList()
    val scope = rememberCoroutineScope()

    var extendDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var vocalRemovalDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var musicCoverDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var personaDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var replaceSectionDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var wavExportDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var boostStyleDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }

    val favoriteSet = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        val favorites = ServerApiClient.getFavorites()
        favorites.forEach { favorite ->
            favoriteSet[favorite.trackId] = true
        }
    }

    val currentTrackId by AudioPlayerManager.currentTrackId.collectAsState()
    val playerState by AudioPlayerManager.playerState.collectAsState()
    val progress by AudioPlayerManager.progress.collectAsState()
    val position by AudioPlayerManager.position.collectAsState()
    val duration by AudioPlayerManager.duration.collectAsState()

    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "任务面板",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StudioMetricCard(
                    label = "音轨数",
                    value = tracks.size.toString(),
                    supporting = "生成结果",
                    modifier = Modifier.width(120.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
                StudioMetricCard(
                    label = "状态",
                    value = taskDetail?.displayStatus?.take(4) ?: taskStatus.take(4),
                    supporting = "当前任务",
                    modifier = Modifier.width(120.dp),
                    containerColor = when {
                        taskDetail?.isSuccess == true -> MaterialTheme.colorScheme.primaryContainer
                        taskDetail?.isFailed == true -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                )
                taskDetail?.firstTrack?.duration?.let { firstDuration ->
                    StudioMetricCard(
                        label = "时长",
                        value = "${firstDuration.toInt()}s",
                        supporting = "首条音轨",
                        modifier = Modifier.width(120.dp),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    )
                }
            }

            StudioSectionCard(
                title = "当前状态",
                subtitle = "任务轮询会实时更新这里的文案。",
            ) {
                Text(
                    text = taskStatus,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = "生成结果",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )

            if (taskDetail?.isSuccess == true && tracks.isNotEmpty()) {
                tracks.forEach { track ->
                    val trackId = track.id
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
                                            ),
                                        )
                                        favoriteSet[trackId] = true
                                    } else {
                                        ServerApiClient.removeFavorite(trackId)
                                        favoriteSet.remove(trackId)
                                    }
                                } catch (_: Exception) {
                                    // 收藏失败不阻断主流程
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
                                currentTrackId == trackId && playerState == PlayerState.PLAYING -> AudioPlayerManager.pause()
                                currentTrackId == trackId && playerState == PlayerState.PAUSED -> AudioPlayerManager.resume()
                                else -> AudioPlayerManager.play(trackId, track.audioUrl!!)
                            }
                        },
                    )
                }
            } else if (taskDetail?.isFailed == true) {
                StudioEmptyState(
                    icon = "⚠",
                    title = "任务失败",
                    description = taskDetail.errorMessage ?: taskDetail.errorCode ?: "未知错误",
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                StudioEmptyState(
                    icon = "⏳",
                    title = "等待生成结果",
                    description = if (taskDetail?.isProcessing == true) {
                        "正在生成中，请稍候..."
                    } else {
                        "等待提交..."
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            StudioSectionCard(
                title = "请求 JSON",
                subtitle = "当前提交给 Suno 的参数快照。",
            ) {
                if (submittedJson.isNullOrBlank()) {
                    Text(
                        text = "还没有可显示的请求体。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .horizontalScroll(rememberScrollState()),
                        ) {
                            Text(
                                text = submittedJson,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }
    }

    extendDialogTrack?.let { (audioId, taskId) ->
        ExtendFormDialog(
            audioId = audioId,
            taskId = taskId,
            onDismiss = { extendDialogTrack = null },
        )
    }

    vocalRemovalDialogTrack?.let { (audioId, taskId) ->
        VocalRemovalConfirmDialog(
            audioId = audioId,
            taskId = taskId,
            onDismiss = { vocalRemovalDialogTrack = null },
        )
    }

    musicCoverDialogTrack?.let { (audioId, taskId) ->
        MusicCoverFormDialog(
            audioId = audioId,
            taskId = taskId,
            onDismiss = { musicCoverDialogTrack = null },
        )
    }

    personaDialogTrack?.let { (audioId, taskId) ->
        PersonaFormDialog(
            audioId = audioId,
            taskId = taskId,
            onDismiss = { personaDialogTrack = null },
        )
    }

    replaceSectionDialogTrack?.let { (audioId, taskId) ->
        ReplaceSectionFormDialog(
            audioId = audioId,
            taskId = taskId,
            onDismiss = { replaceSectionDialogTrack = null },
        )
    }

    wavExportDialogTrack?.let { (audioId, taskId) ->
        WavExportConfirmDialog(
            audioId = audioId,
            taskId = taskId,
            onDismiss = { wavExportDialogTrack = null },
        )
    }

    boostStyleDialogTrack?.let { (audioId, taskId) ->
        BoostStyleConfirmDialog(
            audioId = audioId,
            taskId = taskId,
            onDismiss = { boostStyleDialogTrack = null },
        )
    }
}
