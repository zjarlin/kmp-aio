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
import site.addzero.vibepocket.api.suno.SunoGeneratePersonaRequest
import site.addzero.vibepocket.model.PersonaSaveRequest

/**
 * Persona 创建表单 Dialog
 *
 * 接收 audioId 和 taskId，展示名称和描述字段（均为必填），
 * 提交后调用 SunoApiClient.generatePersona()，成功后调用 ServerApiClient.savePersona() 持久化，
 * 并显示生成的 personaId。
 */
@Composable
fun PersonaFormDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    // ── 表单字段 ──
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // ── 提交状态 ──
    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── 结果 ──
    var resultPersonaId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        confirmButton = {},
        title = {
            Text(
                text = "🎭 创建 Persona",
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
                // ── 表单区域（未完成时显示） ──
                if (resultPersonaId == null) {
                    Text(
                        text = "基于当前 Track 创建声音角色，可在后续生成中复用",
                        color = GlassTheme.TextTertiary,
                        fontSize = 12.sp,
                    )

                    // 名称（必填）
                    GlassTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Persona 名称（必填）",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 描述（必填）
                    GlassTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Persona 描述（必填）",
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 160.dp),
                    )

                    // 提交按钮
                    NeonGlassButton(
                        text = if (isSubmitting) "⏳ 创建中..." else "🚀 创建 Persona",
                        onClick = {
                            if (isSubmitting) return@NeonGlassButton
                            // 校验必填字段
                            if (name.isBlank()) {
                                errorMessage = "❌ 请输入 Persona 名称"
                                return@NeonGlassButton
                            }
                            if (description.isBlank()) {
                                errorMessage = "❌ 请输入 Persona 描述"
                                return@NeonGlassButton
                            }
                            isSubmitting = true
                            errorMessage = null
                            statusText = "正在创建..."

                            scope.launch {
                                try {
                                    val token = ServerApiClient.getConfig("suno_api_token") ?: ""
                                    val url = ServerApiClient.getConfig("suno_api_base_url")
                                        ?.ifBlank { null }
                                        ?: "https://api.sunoapi.org/api/v1"
                                    val client = SunoApiClient(apiToken = token, baseUrl = url)

                                    val request = SunoGeneratePersonaRequest(
                                        taskId = taskId,
                                        audioId = audioId,
                                        name = name.trim(),
                                        description = description.trim(),
                                    )

                                    statusText = "正在调用 Suno API..."
                                    val personaData = client.generatePersona(request)
                                    val personaId = personaData.personaId
                                        ?: throw RuntimeException("Persona 创建成功但未返回 personaId")

                                    // 保存到 Server
                                    statusText = "正在保存 Persona 记录..."
                                    ServerApiClient.savePersona(
                                        PersonaSaveRequest(
                                            personaId = personaId,
                                            name = name.trim(),
                                            description = description.trim(),
                                        )
                                    )

                                    resultPersonaId = personaId
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

                // ── 成功结果展示 ──
                resultPersonaId?.let { personaId ->
                    Text(
                        text = "✅ Persona 创建成功",
                        color = GlassColors.NeonCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Persona ID",
                                color = GlassTheme.TextTertiary,
                                fontSize = 11.sp,
                            )
                            Text(
                                text = personaId,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 可在音乐生成参数中选择此 Persona 来复用声线",
                                color = GlassColors.NeonCyan,
                                fontSize = 12.sp,
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
