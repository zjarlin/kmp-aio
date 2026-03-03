package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import site.addzero.component.glass.*
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoApiClient
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.api.suno.SunoWavRequest

@Composable
fun WavExportConfirmDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    // ── 提交状态 ──
    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── 结果 ──
    var resultDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        confirmButton = {},
        title = {
            Text(
                text = "📀 导出 WAV",
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
                // ── 确认区域（未提交或提交中时显示） ──
                if (resultDetail == null) {
                    Text(
                        text = "将该 Track 转换为 WAV 无损格式，转换完成后可下载。",
                        color = GlassTheme.TextTertiary,
                        fontSize = 13.sp,
                    )

                    // 提交按钮
                    NeonGlassButton(
                        text = if (isSubmitting) "⏳ 转换中..." else "🚀 开始转换",
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

                                    val request = SunoWavRequest(
                                        taskId = taskId,
                                        audioId = audioId,
                                    )

                                    val newTaskId = client.convertToWav(request)
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
                        text = "✅ WAV 转换完成",
                        color = GlassColors.NeonCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    // 展示 WAV 下载链接
                    val tracks = detail.response?.sunoData ?: emptyList()
                    if (tracks.isNotEmpty()) {
                        tracks.forEach { track ->
                            val wavUrl = track.audioUrl
                            GlassCard {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    track.title?.let { title ->
                                        Text(
                                            text = title,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }

                                    if (wavUrl != null) {
                                        NeonGlassButton(
                                            text = "⬇️ 下载 WAV 文件",
                                            onClick = {
                                                try {
                                                    uriHandler.openUri(wavUrl)
                                                } catch (_: Exception) {
                                                    // 忽略打开失败
                                                }
                                            },
                                            glowColor = GlassColors.NeonCyan,
                                        )
                                        Text(
                                            text = wavUrl,
                                            color = GlassColors.NeonCyan,
                                            fontSize = 11.sp,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    } else {
                                        Text(
                                            text = "⚠️ 未获取到下载链接",
                                            color = GlassColors.NeonMagenta,
                                            fontSize = 12.sp,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "⚠️ 转换完成但未返回音轨数据",
                            color = GlassColors.NeonMagenta,
                            fontSize = 12.sp,
                        )
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
