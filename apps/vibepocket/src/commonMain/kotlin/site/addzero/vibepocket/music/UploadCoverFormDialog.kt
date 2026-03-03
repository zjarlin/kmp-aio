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
import org.koin.compose.koinInject
import site.addzero.component.glass.*
import site.addzero.vibepocket.api.suno.SUNO_MODELS
import site.addzero.vibepocket.api.suno.SunoApiClient
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.api.suno.SunoUploadCoverRequest
import site.addzero.vibepocket.api.suno.VOCAL_GENDERS
import site.addzero.vibepocket.model.*

/**
 * 翻唱上传参数表单 Dialog
 *
 * 展示 uploadUrl（必填）、歌词/提示词、风格、标题、模型版本、声线性别等字段，
 * 提交后调用 SunoApiClient.uploadCover()，轮询任务进度并展示结果。
 */
@Composable
fun UploadCoverFormDialog(
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val client: SunoApiClient = koinInject()

    // ── 表单字段 ──
    var uploadUrl by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("V4_5ALL") }
    var selectedGender by remember { mutableStateOf("m") }

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
                text = "🎤 翻唱上传",
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
                        text = "输入音频 URL 和翻唱参数",
                        color = GlassTheme.TextTertiary,
                        fontSize = 12.sp,
                    )

                    // 音频 URL（必填）
                    GlassTextField(
                        value = uploadUrl,
                        onValueChange = { uploadUrl = it },
                        placeholder = "音频 URL（必填）",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 歌词 / Prompt
                    GlassTextArea(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = "歌词 / 提示词（可选）",
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

                    // 模型版本
                    Text(text = "模型版本", color = GlassTheme.TextSecondary, fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        SUNO_MODELS.forEach { model ->
                            GlassButton(
                                text = model,
                                onClick = { selectedModel = model },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Text(
                        text = "当前: $selectedModel",
                        color = GlassColors.NeonCyan,
                        fontSize = 11.sp,
                    )

                    // 声线性别
                    Text(text = "声线性别", color = GlassTheme.TextSecondary, fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        VOCAL_GENDERS.forEach { (code, label) ->
                            GlassButton(
                                text = label,
                                onClick = { selectedGender = code },
                            )
                        }
                    }
                    Text(
                        text = "当前: ${VOCAL_GENDERS.firstOrNull { it.first == selectedGender }?.second ?: selectedGender}",
                        color = GlassColors.NeonCyan,
                        fontSize = 11.sp,
                    )

                    // 提交按钮
                    NeonGlassButton(
                        text = if (isSubmitting) "⏳ 提交中..." else "🚀 提交翻唱",
                        onClick = {
                            if (isSubmitting) return@NeonGlassButton
                            if (uploadUrl.isBlank()) {
                                errorMessage = "❌ 请输入音频 URL"
                                return@NeonGlassButton
                            }
                            isSubmitting = true
                            errorMessage = null
                            statusText = "正在提交..."

                            scope.launch {
                                try {
                                    val request = SunoUploadCoverRequest(
                                        uploadUrl = uploadUrl.trim(),
                                        prompt = prompt.ifBlank { null },
                                        style = style.ifBlank { null },
                                        title = title.ifBlank { null },
                                        model = selectedModel,
                                        vocalGender = selectedGender,
                                    )

                                    val newTaskId = client.uploadCover(request)
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
                        text = "✅ 翻唱完成",
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
                                taskId = detail.taskId ?: "",
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
