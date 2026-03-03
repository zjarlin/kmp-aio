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
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoApiClient
import site.addzero.vibepocket.api.suno.SunoBoostStyleData
import site.addzero.vibepocket.api.suno.SunoBoostStyleRequest
import site.addzero.vibepocket.model.*

@Composable
fun BoostStyleConfirmDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val client: SunoApiClient = koinInject()

    // ── 提交状态 ──
    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── 结果 ──
    var resultData by remember { mutableStateOf<SunoBoostStyleData?>(null) }
    var remainingCredits by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        confirmButton = {},
        title = {
            Text(
                text = "✨ 风格提升",
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
                if (resultData == null) {
                    Text(
                        text = "将对该 Track 执行风格提升，获得更精致的音乐效果。此操作将消耗积分。",
                        color = GlassTheme.TextTertiary,
                        fontSize = 13.sp,
                    )

                    // 提交按钮
                    NeonGlassButton(
                        text = if (isSubmitting) "⏳ 处理中..." else "🚀 开始提升",
                        onClick = {
                            if (isSubmitting) return@NeonGlassButton
                            isSubmitting = true
                            errorMessage = null
                            statusText = "正在提交..."

                            scope.launch {
                                try {
                                    // Use the client injected outside the lambda
                                    val request = SunoBoostStyleRequest(
                                        taskId = taskId,
                                        audioId = audioId,
                                    )

                                    statusText = "正在执行风格提升..."
                                    val data = client.boostMusicStyle(request)
                                    resultData = data

                                    // 获取最新积分
                                    statusText = "正在刷新积分..."
                                    try {
                                        remainingCredits = client.getCredits()
                                    } catch (_: Exception) {
                                        // 积分查询失败不阻断结果展示
                                    }

                                    statusText = null
                                    // Removed client.close() as Koin should manage the client lifecycle
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
                resultData?.let { data ->
                    Text(
                        text = "✅ 风格提升完成",
                        color = GlassColors.NeonCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    GlassCard {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // 消耗积分
                            data.creditsConsumed?.let { consumed ->
                                Text(
                                    text = "🔥 消耗积分: $consumed",
                                    color = GlassColors.NeonMagenta,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            // 剩余积分（优先使用 getCredits() 的最新值，回退到响应中的值）
                            val credits = remainingCredits ?: data.creditsRemaining
                            credits?.let { remaining ->
                                Text(
                                    text = "💎 剩余积分: $remaining",
                                    color = GlassColors.NeonCyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            // 任务 ID
                            data.taskId?.let { tid ->
                                Text(
                                    text = "任务 ID: $tid",
                                    color = GlassTheme.TextTertiary,
                                    fontSize = 11.sp,
                                )
                            }
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
