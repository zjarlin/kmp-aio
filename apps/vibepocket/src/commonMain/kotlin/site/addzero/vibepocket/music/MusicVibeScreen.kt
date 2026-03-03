package site.addzero.vibepocket.music

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import site.addzero.component.glass.GlassButton
import site.addzero.component.glass.GlassColors
import site.addzero.component.glass.NeonGlassButton
import site.addzero.ioc.annotation.Bean
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoApiClient
import site.addzero.vibepocket.api.suno.SunoGenerateRequest
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.model.MusicHistorySaveRequest
import site.addzero.vibepocket.model.MusicHistoryTrack
import site.addzero.vibepocket.model.PersonaItem


private val prettyJson = Json { // Added
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

/**
 * 音乐 Vibe 主界面
 * 分屏布局：左侧分步表单，右侧任务进度
 */
@Composable
@Bean(tags = ["screen"])
fun MusicVibeScreen() {
    val scope = rememberCoroutineScope()
    val client: SunoApiClient = koinInject()

    // ===== 表单状态 =====
    var currentStep by remember { mutableStateOf(VibeStep.LYRICS) }
    // Step 1: 歌词
    var lyrics by remember { mutableStateOf("") }
    var songName by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    // Step 2: 参数
    var title by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var mv by remember { mutableStateOf("V4_5") }
    var makeInstrumental by remember { mutableStateOf(false) }
    var vocalGender by remember { mutableStateOf("m") }
    var negativeTags by remember { mutableStateOf("") }
    var gptDescriptionPrompt by remember { mutableStateOf("") }
    // Persona
    var personas by remember { mutableStateOf<List<PersonaItem>>(emptyList()) }
    var selectedPersonaId by remember { mutableStateOf<String?>(null) }

    // ===== 任务状态 =====
    var submittedJson by remember { mutableStateOf<String?>(null) }
    var taskStatus by remember { mutableStateOf("未提交") }
    var isSubmitted by remember { mutableStateOf(false) }
    var taskDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // ===== 积分状态 =====
    var credits by remember { mutableStateOf<Int?>(null) }
    var isLoadingCredits by remember { mutableStateOf(false) }

    // ── 初始化加载 Persona 列表 & 积分 ──
    LaunchedEffect(Unit) {
        personas = try {
            ServerApiClient.getPersonas()
        } catch (_: Exception) {
            emptyList()
        }
        // 加载积分
        isLoadingCredits = true
        credits = try {
            client.getCredits()
        } catch (_: Exception) {
            null
        } finally {
            isLoadingCredits = false
        }
    }

    // ── 音乐生成成功后自动保存历史记录 ──
    LaunchedEffect(taskDetail?.taskId, taskDetail?.isSuccess) {
        val detail = taskDetail ?: return@LaunchedEffect
        if (!detail.isSuccess) return@LaunchedEffect
        val tId = detail.taskId ?: return@LaunchedEffect
        val tracks = detail.response?.sunoData ?: emptyList()
        try {
            ServerApiClient.saveHistory(
                MusicHistorySaveRequest(
                    taskId = tId,
                    type = detail.type ?: "generate",
                    status = detail.status ?: "SUCCESS",
                    tracks = tracks.map { t ->
                        MusicHistoryTrack(
                            id = t.id,
                            audioUrl = t.audioUrl,
                            title = t.title,
                            tags = t.tags,
                            imageUrl = t.imageUrl,
                            duration = t.duration,
                        )
                    },
                )
            )
        } catch (_: Exception) {
            // 保存历史失败不阻断主流程
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // ========== 左侧：分步表单 ==========
            Box(
                modifier = Modifier
                    .weight(if (isSubmitted) 0.5f else 1f)
                    .fillMaxHeight()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "🎵 Music Vibe",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentStep == VibeStep.LYRICS) "第 1 步 / 确认歌词" else "第 2 步 / Vibe 参数",
                        color = GlassColors.NeonCyan,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    CreditsBar(credits = credits, isLoading = isLoadingCredits)
                    Spacer(modifier = Modifier.height(16.dp))

                    StepIndicator(currentStep)
                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedContent(targetState = currentStep) { step ->
                        when (step) {
                            VibeStep.LYRICS -> LyricsStep(
                                lyrics = lyrics,
                                onLyricsChange = { lyrics = it },
                                songName = songName,
                                onSongNameChange = { songName = it },
                                artistName = artistName,
                                onArtistNameChange = { artistName = it }
                            )

                            VibeStep.PARAMS -> ParamsStep(
                                title = title,
                                onTitleChange = { title = it },
                                tags = tags,
                                onTagsChange = { tags = it },
                                mv = mv,
                                onMvChange = { mv = it },
                                makeInstrumental = makeInstrumental,
                                onMakeInstrumentalChange = { makeInstrumental = it },
                                vocalGender = vocalGender,
                                onVocalGenderChange = { vocalGender = it },
                                negativeTags = negativeTags,
                                onNegativeTagsChange = { negativeTags = it },
                                gptDescriptionPrompt = gptDescriptionPrompt,
                                onGptDescriptionPromptChange = { gptDescriptionPrompt = it },
                                personas = personas,
                                selectedPersonaId = selectedPersonaId,
                                onPersonaChange = { selectedPersonaId = it },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 底部按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStep == VibeStep.PARAMS) {
                            GlassButton(
                                text = "← 上一步",
                                onClick = { currentStep = VibeStep.LYRICS }
                            )
                        }

                        when (currentStep) {
                            VibeStep.LYRICS -> {
                                NeonGlassButton(
                                    text = "下一步 →",
                                    onClick = { currentStep = VibeStep.PARAMS },
                                    glowColor = GlassColors.NeonCyan,
                                    enabled = lyrics.isNotBlank()
                                )
                            }

                            VibeStep.PARAMS -> {
                                NeonGlassButton(
                                    text = if (isSubmitting) "⏳ 提交中..." else "🚀 提交 Vibe",
                                    onClick = {
                                        if (isSubmitting) return@NeonGlassButton

                                        val request = SunoGenerateRequest(
                                            prompt = lyrics,
                                            customMode = true,
                                            instrumental = makeInstrumental,
                                            model = mv,
                                            title = title.ifBlank { null },
                                            style = tags.ifBlank { null },
                                            negativeTags = negativeTags.ifBlank { null },
                                            vocalGender = vocalGender,
                                            personaId = selectedPersonaId,
                                        )
                                        val jsonStr = prettyJson.encodeToString(request)
                                        submittedJson = jsonStr
                                        isSubmitted = true
                                        isSubmitting = true
                                        taskStatus = "正在提交..."

                                        scope.launch {
                                            try {
                                                taskStatus = "正在提交任务..."
                                                val taskId = client.generateMusic(request)
                                                taskStatus = "已提交，任务 ID: $taskId\n轮询中..."

                                                val completed = client.waitForCompletion(
                                                    taskId = taskId,
                                                    onStatusUpdate = { detail ->
                                                        taskDetail = detail
                                                        taskStatus = detail?.displayStatus ?: "查询中..."
                                                    }
                                                )
                                                taskDetail = completed
                                                taskStatus = completed.displayStatus
                                            } catch (e: Exception) {
                                                taskStatus = "❌ 错误: ${e.message}"
                                            } finally {
                                                isSubmitting = false
                                                // 刷新积分
                                                credits = try {
                                                    client.getCredits()
                                                } catch (_: Exception) {
                                                    credits
                                                }
                                            }
                                        }
                                    },
                                    glowColor = GlassColors.NeonPurple,
                                    enabled = !isSubmitting
                                )
                            }
                        }
                    }
                }
            }

            // ========== 右侧：任务进度面板 ==========
            if (isSubmitted) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(top = 24.dp, end = 24.dp, bottom = 24.dp)
                ) {
                    TaskProgressPanel(
                        submittedJson = submittedJson,
                        taskStatus = taskStatus,
                        taskDetail = taskDetail,
                    )
                }
            }
        }
    }
}
