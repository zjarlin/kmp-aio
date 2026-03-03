package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import site.addzero.component.glass.*
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoApiClient
import site.addzero.vibepocket.api.suno.SunoMusicCoverRequest
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.model.TrackPlayerState


/**
 * 封面生成参数表单 Dialog
 *
 * 接收 audioId 和 taskId，展示提示词、风格、标题等可选字段，
 * 提交后调用 SunoApiClient.generateMusicCover()，轮询任务进度并展示结果。
 * 完成后展示生成的封面图片。
 */
@Composable
fun MusicCoverFormDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    // ── 表单字段 ──
    var prompt by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    // ── 提交状态 ──
    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── 结果 ──
    var resultDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }

    // ── 播放状态 ──
    val currentTrackId by AudioPlayerManager.currentTrackId.collectAsState()
    val playerState by AudioPlayerManager.playerState.collectAsState()
    val progress by AudioPlayerManager.progress.collectAsState()
    val position by AudioPlayerManager.position.collectAsState()
    val duration by AudioPlayerManager.duration.collectAsState()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        confirmButton = {},
        title = {
            Text(
                text = "🎨 生成封面",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        containerColor = Color(0xFF1A1A2E),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ── 表单区域（未提交或提交中时显示） ──
                if (resultDetail == null) {
                    Text(
                        text = "为 Track 设置封面生成参数（均为可选）",
                        color = GlassTheme.TextTertiary,
                        fontSize = 12.sp,
                    )

                    // 提示词 / Prompt
                    GlassTextArea(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = "封面描述提示词（可选）",
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 160.dp),
                    )

                    // 风格
                    GlassTextField(
                        value = style,
                        onValueChange = { style = it },
                        placeholder = "风格标签（可选）",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 标题
                    GlassTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "标题（可选）",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 提交按钮
                    NeonGlassButton(
                        text = if (isSubmitting) "⏳ 生成中..." else "🚀 生成封面",
                        onClick = {
                            if (isSubmitting) return@NeonGlassButton
                            isSubmitting = true
                            errorMessage = null
                            statusText = "正在提交..."

                            scope.launch {
                                try {
                                    val token = ServerApiClient.getConfig("suno_api_token") ?: ""
                                    val url = ServerApiClient.getConfig("suno_api_base_url")
                                        ?.ifBlank { null }
                                        ?: "https://api.sunoapi.org/api/v1"
                                    val client = SunoApiClient(apiToken = token, baseUrl = url)

                                    val request = SunoMusicCoverRequest(
                                        taskId = taskId,
                                        audioId = audioId,
                                        prompt = prompt.ifBlank { null },
                                        style = style.ifBlank { null },
                                        title = title.ifBlank { null },
                                    )

                                    val newTaskId = client.generateMusicCover(request)
                                    statusText = "已提交，轮询中..."

                                    // 轮询等待完成
                                    val detail = client.waitForCompletion(
                                        taskId = newTaskId,
                                        maxWaitMs = 600_000L,
                                        pollIntervalMs = 30_000L,
                                        onStatusUpdate = { detail ->
                                            statusText = detail?.displayStatus ?: "轮询中..."
                                        },
                                    )
                                    resultDetail = detail
                                    statusText = null
                                } catch (e: Exception) {
                                    errorMessage = "❌ ${e.message}"
                                    statusText = null
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        glowColor = GlassColors.NeonCyan,
                        enabled = !isSubmitting,
                    )

                    // 状态文本
                    statusText?.let { status ->
                        Text(text = status, color = GlassColors.NeonCyan, fontSize = 12.sp)
                    }

                    // 错误信息
                    errorMessage?.let { error ->
                        Text(text = error, color = GlassColors.NeonMagenta, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        GlassButton(
                            text = "🔄 重试",
                            onClick = { errorMessage = null },
                        )
                    }
                }

                // ── 结果展示区域 ──
                resultDetail?.let { detail ->
                    Text(
                        text = "✅ 封面生成完成",
                        color = GlassColors.NeonCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    val tracks = detail.response?.sunoData ?: emptyList()
                    if (tracks.isNotEmpty()) {
                        tracks.forEach { track ->
                            val trackId = track.id
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
                                taskId = detail.taskId ?: taskId,
                                isFavorite = false,
                                onFavoriteToggle = {},
                                onAction = {},
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
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    GlassButton(
                        text = "关闭",
                        onClick = onDismiss,
                    )
                }
            }
        },
    )
}
