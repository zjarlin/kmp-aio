package site.addzero.vibepocket.music

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.media.playlist.player.DefaultPlaylistPlayer
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.model.FavoriteRequest
import site.addzero.vibepocket.model.PersonaItem
import site.addzero.vibepocket.model.TrackAction
import site.addzero.vibepocket.ui.StudioEmptyState
import site.addzero.vibepocket.ui.StudioMetricCard
import site.addzero.vibepocket.ui.StudioSectionCard

@Composable
fun TaskProgressPanel(
    submittedJson: String?,
    submittedTaskId: String? = null,
    taskArchiveStatus: String? = null,
    taskStatus: String,
    taskDetail: SunoTaskDetail? = null,
    fallbackType: String = "generate",
    onCancelWait: (() -> Unit)? = null,
    onPersonaCreated: (PersonaItem) -> Unit = {},
) {
    val baseTaskId = submittedTaskId ?: taskDetail?.taskId
    val scope = rememberCoroutineScope()

    var extendDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var vocalRemovalDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var musicCoverDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var personaDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var replaceSectionDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var wavExportDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var boostStyleDialogTrack by remember { mutableStateOf<Pair<String, String>?>(null) }
    var refreshedTaskDetail by remember(baseTaskId) { mutableStateOf<SunoTaskDetail?>(null) }
    var refreshedArchiveStatus by remember(baseTaskId) { mutableStateOf<String?>(null) }
    var remoteLookupMessage by remember(baseTaskId) { mutableStateOf<String?>(null) }
    var isRefreshingRemoteTask by remember(baseTaskId) { mutableStateOf(false) }

    val favoriteSet = remember { mutableStateMapOf<String, Boolean>() }
    val effectiveTaskDetail = refreshedTaskDetail ?: taskDetail
    val effectiveTaskId = submittedTaskId ?: effectiveTaskDetail?.taskId
    val effectiveArchiveStatus = refreshedArchiveStatus ?: taskArchiveStatus
    val effectiveTaskStatus = refreshedTaskDetail?.displayStatus ?: taskStatus
    val tracks = effectiveTaskDetail?.response?.sunoData ?: emptyList()

    LaunchedEffect(Unit) {
        val favorites = ServerApiClient.favoriteApi.getFavorites()
        favorites.forEach { favorite ->
            favoriteSet[favorite.trackId] = true
        }
    }

    fun refreshTaskById() {
        val taskId = effectiveTaskId ?: return
        scope.launch {
            isRefreshingRemoteTask = true
            remoteLookupMessage = null
            try {
                val refreshedSnapshot = refreshSunoTaskSnapshotById(
                    taskId = taskId,
                    fallbackType = fallbackType,
                    requestJson = submittedJson,
                )
                refreshedTaskDetail = refreshedSnapshot.detail
                refreshedArchiveStatus = refreshedSnapshot.archiveStatus
                remoteLookupMessage = "已按 taskId 从 Suno 刷新：${refreshedSnapshot.detail.displayStatus}"
            } catch (error: Exception) {
                remoteLookupMessage = "按 taskId 查询失败：${SunoWorkflowService.errorMessage(error)}"
            } finally {
                isRefreshingRemoteTask = false
            }
        }
    }

    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "任务面板",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StudioMetricCard(
                    label = "音轨数",
                    value = tracks.size.toString(),
                    modifier = Modifier.width(120.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
                StudioMetricCard(
                    label = "状态",
                    value = effectiveTaskDetail?.displayStatus?.take(4) ?: effectiveTaskStatus.take(4),
                    modifier = Modifier.width(120.dp),
                    containerColor = when {
                        effectiveTaskDetail?.isSuccess == true -> MaterialTheme.colorScheme.primaryContainer
                        effectiveTaskDetail?.isFailed == true -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                )
                effectiveTaskDetail?.firstTrack?.duration?.let { firstDuration ->
                    StudioMetricCard(
                        label = "时长",
                        value = "${firstDuration.toInt()}s",
                        modifier = Modifier.width(120.dp),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    )
                }
            }

            StudioSectionCard(
                title = "任务标识",
                subtitle = "taskId 会贯穿轮询、回调恢复和后台任务建档，生成完成后再补齐结果资源。",
            ) {
                if (effectiveTaskId.isNullOrBlank()) {
                    Text(
                        text = "任务提交成功后，这里会显示 Suno 返回的 taskId。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    ) {
                        SelectionContainer {
                            Text(
                                text = effectiveTaskId,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                    effectiveArchiveStatus?.let { archiveStatus ->
                        Text(
                            text = "后台记录：$archiveStatus",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (archiveStatus.startsWith("建档失败")) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    remoteLookupMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (message.startsWith("按 taskId 查询失败")) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }
                }
            }

            StudioSectionCard(
                title = "当前状态",
                subtitle = "任务轮询会实时更新这里的文案。",
                action = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!effectiveTaskId.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = ::refreshTaskById,
                                enabled = !isRefreshingRemoteTask,
                            ) {
                                Text(if (isRefreshingRemoteTask) "查询中..." else "按 taskId 查询")
                            }
                        }
                        if (onCancelWait != null) {
                            OutlinedButton(onClick = onCancelWait) {
                                Text("取消等待")
                            }
                        }
                    }
                },
            ) {
                Text(
                    text = effectiveTaskStatus,
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

            if (effectiveTaskDetail?.isSuccess == true && tracks.isNotEmpty()) {
                DefaultPlaylistPlayer(
                    items = tracks,
                    itemKey = { track ->
                        track.playbackId(effectiveTaskId ?: "task")
                    },
                    titleOf = { track ->
                        track.displayTitle()
                    },
                    subtitleOf = { track ->
                        track.displaySubtitle(effectiveTaskId?.take(8))
                    },
                    durationMsOf = { track ->
                        track.durationMsOrNull()
                    },
                    coverUrlOf = { track ->
                        track.imageUrl
                    },
                    hasResolvableAudioOf = { track ->
                        !track.audioUrl.isNullOrBlank() || !track.streamAudioUrl.isNullOrBlank()
                    },
                    resolveAudioSource = { track ->
                        track.resolvedAudioSource()
                    },
                    itemActions = { track, _ ->
                        val trackId = track.id
                        var menuExpanded by remember(track.playbackId(effectiveTaskId ?: "task")) {
                            mutableStateOf(false)
                        }

                        if (trackId != null) {
                            val isFavorite = favoriteSet[trackId] == true
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            if (isFavorite) {
                                                ServerApiClient.favoriteApi.removeFavorite(trackId)
                                                favoriteSet.remove(trackId)
                                            } else {
                                                ServerApiClient.favoriteApi.addFavorite(
                                                    FavoriteRequest(
                                                        trackId = trackId,
                                                        taskId = effectiveTaskId ?: "",
                                                        audioUrl = track.audioUrl,
                                                        title = track.title,
                                                        tags = track.tags,
                                                        imageUrl = track.imageUrl,
                                                        duration = track.duration,
                                                    ),
                                                )
                                                favoriteSet[trackId] = true
                                            }
                                        } catch (_: Exception) {
                                            // 收藏失败不阻断主流程
                                        }
                                    }
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

                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "更多操作",
                                    )
                                }
                                TrackActionMenu(
                                    expanded = menuExpanded,
                                    onDismiss = { menuExpanded = false },
                                    onAction = { action ->
                                        menuExpanded = false
                                        val trackTaskId = effectiveTaskId ?: ""
                                        when (action) {
                                            TrackAction.EXTEND -> extendDialogTrack = trackId to trackTaskId
                                            TrackAction.VOCAL_REMOVAL -> vocalRemovalDialogTrack = trackId to trackTaskId
                                            TrackAction.GENERATE_COVER -> musicCoverDialogTrack = trackId to trackTaskId
                                            TrackAction.CREATE_PERSONA -> personaDialogTrack = trackId to trackTaskId
                                            TrackAction.REPLACE_SECTION -> replaceSectionDialogTrack = trackId to trackTaskId
                                            TrackAction.EXPORT_WAV -> wavExportDialogTrack = trackId to trackTaskId
                                            TrackAction.BOOST_STYLE -> boostStyleDialogTrack = trackId to trackTaskId
                                        }
                                    },
                                )
                            }
                        }
                    },
                )
            } else if (effectiveTaskDetail?.isFailed == true) {
                StudioEmptyState(
                    icon = "⚠",
                    title = "任务失败",
                    description = effectiveTaskDetail.errorMessage ?: effectiveTaskDetail.errorCode ?: "未知错误",
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                StudioEmptyState(
                    icon = "⏳",
                    title = "等待生成结果",
                    description = if (effectiveTaskDetail?.isProcessing == true) {
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
                        SelectionContainer {
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
            onCreated = onPersonaCreated,
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
