package site.addzero.vibepocket.music

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import site.addzero.ioc.annotation.Bean
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoGenerateRequest
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.model.MusicHistorySaveRequest
import site.addzero.vibepocket.model.MusicHistoryTrack
import site.addzero.vibepocket.model.PersonaItem
import site.addzero.vibepocket.ui.StudioPill
import site.addzero.vibepocket.ui.StudioSectionCard
import site.addzero.vibepocket.ui.SunoTokenApplyHint

private val prettyJson = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

@Composable
@Bean(tags = ["screen"])
fun MusicVibeScreen() {
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(VibeStep.LYRICS) }
    var lyrics by remember { mutableStateOf("") }
    var songName by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var mv by remember { mutableStateOf("V4_5") }
    var makeInstrumental by remember { mutableStateOf(false) }
    var vocalGender by remember { mutableStateOf("m") }
    var negativeTags by remember { mutableStateOf("") }
    var gptDescriptionPrompt by remember { mutableStateOf("") }
    var personas by remember { mutableStateOf<List<PersonaItem>>(emptyList()) }
    var selectedPersonaId by remember { mutableStateOf<String?>(null) }

    var submittedJson by remember { mutableStateOf<String?>(null) }
    var taskStatus by remember { mutableStateOf("未提交") }
    var isSubmitted by remember { mutableStateOf(false) }
    var taskDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    var credits by remember { mutableStateOf<Int?>(null) }
    var isLoadingCredits by remember { mutableStateOf(false) }
    var sunoConfig by remember { mutableStateOf(SunoRuntimeConfig()) }

    LaunchedEffect(Unit) {
        sunoConfig = try {
            SunoWorkflowService.loadConfig()
        } catch (_: Exception) {
            SunoRuntimeConfig()
        }
        personas = try {
            ServerApiClient.getPersonas()
        } catch (_: Exception) {
            emptyList()
        }

        if (!sunoConfig.hasToken) {
            credits = null
        } else {
            isLoadingCredits = true
            credits = SunoWorkflowService.getCreditsOrNull()
            isLoadingCredits = false
        }
    }

    LaunchedEffect(taskDetail?.taskId, taskDetail?.isSuccess) {
        val detail = taskDetail ?: return@LaunchedEffect
        if (!detail.isSuccess) return@LaunchedEffect
        val taskId = detail.taskId ?: return@LaunchedEffect
        val tracks = detail.response?.sunoData ?: emptyList()
        try {
            ServerApiClient.saveHistory(
                MusicHistorySaveRequest(
                    taskId = taskId,
                    type = detail.type ?: "generate",
                    status = detail.status ?: "SUCCESS",
                    tracks = tracks.map { track ->
                        MusicHistoryTrack(
                            id = track.id,
                            audioUrl = track.audioUrl,
                            title = track.title,
                            tags = track.tags,
                            imageUrl = track.imageUrl,
                            duration = track.duration,
                        )
                    },
                ),
            )
        } catch (_: Exception) {
            // 保存历史失败不阻断主流程
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (isSubmitted) 0.52f else 1f)
                .fillMaxHeight()
                .padding(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StudioPill(
                    text = if (currentStep == VibeStep.LYRICS) "Step 1 / Lyrics" else "Step 2 / Vibe",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Music Vibe",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "把歌词、风格和 persona 串成一条清晰的音乐生成流程。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                CreditsBar(
                    credits = credits,
                    isLoading = isLoadingCredits,
                )

                if (!sunoConfig.hasToken) {
                    StudioSectionCard(
                        title = "Suno 尚未配置",
                        subtitle = "搜索、试听和下载仍然可用；只有提交生成前需要先配置 Token。",
                    ) {
                        SunoTokenApplyHint(
                            intro = "还没配置 Suno API Token。没申请过的话，可以先去控制台申请。",
                        )
                    }
                }

                StepIndicator(currentStep)

                AnimatedContent(targetState = currentStep) { step ->
                    when (step) {
                        VibeStep.LYRICS -> LyricsStep(
                            lyrics = lyrics,
                            onLyricsChange = { lyrics = it },
                            songName = songName,
                            onSongNameChange = { songName = it },
                            artistName = artistName,
                            onArtistNameChange = { artistName = it },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentStep == VibeStep.PARAMS) {
                        OutlinedButton(
                            onClick = { currentStep = VibeStep.LYRICS },
                        ) {
                            Text("上一步")
                        }
                    }

                    when (currentStep) {
                        VibeStep.LYRICS -> {
                            FilledTonalButton(
                                onClick = { currentStep = VibeStep.PARAMS },
                                enabled = lyrics.isNotBlank(),
                            ) {
                                Text("下一步")
                            }
                        }

                        VibeStep.PARAMS -> {
                            Button(
                                onClick = {
                                    if (isSubmitting) return@Button

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
                                    submittedJson = prettyJson.encodeToString(request)
                                    isSubmitted = true
                                    isSubmitting = true
                                    taskStatus = "正在提交..."

                                    scope.launch {
                                        try {
                                            val latestConfig = SunoWorkflowService.loadConfig()
                                            sunoConfig = latestConfig
                                            latestConfig.requireToken()
                                            val completed = SunoWorkflowService.submitTask(
                                                submit = { client -> client.generateMusic(request) },
                                                onStatusUpdate = { status, detail ->
                                                    taskDetail = detail
                                                    taskStatus = status
                                                },
                                            )
                                            taskDetail = completed
                                            taskStatus = completed.displayStatus
                                        } catch (error: Exception) {
                                            taskStatus = "错误: ${SunoWorkflowService.errorMessage(error)}"
                                        } finally {
                                            isSubmitting = false
                                            credits = SunoWorkflowService.getCreditsOrNull() ?: credits
                                        }
                                    }
                                },
                                enabled = !isSubmitting && sunoConfig.hasToken,
                            ) {
                                Text(
                                    when {
                                        isSubmitting -> "提交中..."
                                        !sunoConfig.hasToken -> "先去设置配置 Suno API"
                                        else -> "提交 Vibe"
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isSubmitted) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight()
                    .padding(top = 24.dp, end = 24.dp, bottom = 24.dp),
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
