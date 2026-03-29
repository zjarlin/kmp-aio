package site.addzero.kcloud.music

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import site.addzero.kcloud.api.suno.SunoGenerateRequest
import site.addzero.kcloud.api.suno.SunoTaskDetail
import site.addzero.kcloud.model.PersonaItem
import site.addzero.kcloud.screens.musicstudio.MusicStudioTab
import site.addzero.kcloud.screens.musicstudio.MusicStudioViewModel
import site.addzero.kcloud.ui.StudioPill
import site.addzero.kcloud.ui.StudioSectionCard
import site.addzero.kcloud.ui.SunoTokenApplyHint
import site.addzero.liquidglass.LiquidGlassTabs

private val prettyJson = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

@Composable
fun MusicVibeScreen(
    viewModel: MusicStudioViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StudioPill(
            text = "Music Studio",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "音乐工作台",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            CreditsBar(
                credits = viewModel.credits,
                isLoading = viewModel.isLoadingCredits,
            )
        }
        Text(
            text = "生成音乐和翻唱统一放在一个界面里切 tab，不再拆成两个侧边栏菜单。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LiquidGlassTabs(
            items = MusicStudioTab.entries.toList(),
            selectedItem = viewModel.selectedTab,
            onSelectedItemChange = viewModel::selectTab,
            modifier = Modifier.fillMaxWidth(),
        ) { tab, selected ->
            Text(
                text = tab.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
        Text(
            text = viewModel.selectedTab.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (viewModel.selectedTab) {
                MusicStudioTab.COVER -> UploadCoverWorkbench(
                    onCreditsRefresh = viewModel::refreshCredits,
                )
                MusicStudioTab.GENERATE -> GenerateMusicWorkbench(
                    onCreditsRefresh = viewModel::refreshCredits,
                )
            }
        }
    }
}

@Composable
private fun GenerateMusicWorkbench(
    onCreditsRefresh: () -> Unit,
) {
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
    var isRefreshingPersonas by remember { mutableStateOf(false) }
    var vibeFormHistory by remember { mutableStateOf<List<SavedVibeFormDraft>>(emptyList()) }

    var submittedJson by remember { mutableStateOf<String?>(null) }
    var submittedTaskId by remember { mutableStateOf<String?>(null) }
    var taskArchiveStatus by remember { mutableStateOf<String?>(null) }
    var taskStatus by remember { mutableStateOf("未提交") }
    var isSubmitted by remember { mutableStateOf(false) }
    var taskDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionJob by remember { mutableStateOf<Job?>(null) }

    var sunoConfig by remember { mutableStateOf(SunoRuntimeConfig()) }

    fun acceptPersonas(loaded: List<PersonaItem>) {
        personas = loaded
        if (!loaded.containsPersona(selectedPersonaId)) {
            selectedPersonaId = null
        }
    }

    fun applyCreatedPersona(persona: PersonaItem) {
        personas = personas.upsertPersona(persona)
        selectedPersonaId = persona.personaId
        currentStep = VibeStep.PARAMS
    }

    fun applySavedDraft(draft: SavedVibeFormDraft) {
        lyrics = draft.lyrics
        songName = draft.songName
        artistName = draft.artistName
        title = draft.title
        tags = draft.tags
        mv = draft.mv
        makeInstrumental = draft.makeInstrumental
        vocalGender = draft.vocalGender
        negativeTags = draft.negativeTags
        gptDescriptionPrompt = draft.gptDescriptionPrompt
        selectedPersonaId = draft.selectedPersonaId
        if (!personas.containsPersona(selectedPersonaId)) {
            selectedPersonaId = null
        }
    }

    fun shouldRestoreDraft(): Boolean {
        return lyrics.isBlank() &&
            songName.isBlank() &&
            artistName.isBlank() &&
            title.isBlank() &&
            tags.isBlank() &&
            negativeTags.isBlank() &&
            gptDescriptionPrompt.isBlank() &&
            !makeInstrumental &&
            selectedPersonaId == null &&
            mv == "V4_5" &&
            vocalGender == "m"
    }

    fun cancelSubmission() {
        submissionJob?.cancel(CancellationException("用户取消等待"))
    }

    LaunchedEffect(Unit) {
        sunoConfig = try {
            SunoWorkflowService.loadConfig()
        } catch (_: Exception) {
            SunoRuntimeConfig()
        }
        isRefreshingPersonas = true
        acceptPersonas(
            try {
                loadSavedPersonas()
            } catch (_: Exception) {
                emptyList()
            },
        )
        isRefreshingPersonas = false

        vibeFormHistory = runCatching { loadVibeFormHistory() }.getOrDefault(emptyList())
        if (shouldRestoreDraft()) {
            vibeFormHistory.firstOrNull()?.let(::applySavedDraft)
        }

    }

    LaunchedEffect(submittedTaskId, taskDetail?.status, submittedJson) {
        val taskId = submittedTaskId ?: return@LaunchedEffect
        val requestJson = submittedJson ?: return@LaunchedEffect
        taskArchiveStatus = "建档中..."
        taskArchiveStatus = runCatching {
            saveSunoTaskArchive(
                taskId = taskId,
                fallbackType = "generate",
                requestJson = requestJson,
                detail = taskDetail,
            ).archiveStatusText()
        }.getOrElse { error ->
            error.archiveFailureText()
        }
    }

    LaunchedEffect(taskDetail?.taskId, taskDetail?.isSuccess) {
        persistSunoHistoryIfSuccess(taskDetail)
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (isSubmitted) 0.52f else 1f)
                .fillMaxHeight()
                .padding(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StudioPill(
                    text = if (currentStep == VibeStep.LYRICS) "Step 1 / Lyrics" else "Step 2 / Vibe",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "生成音乐",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "把歌词、风格和 persona 串成一条清晰的音乐生成流程。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!sunoConfig.hasToken) {
                    StudioSectionCard(
                        title = "Suno 尚未配置",
                        subtitle = "搜索、试听和下载仍然可用；提交生成前至少要先配置 Token。",
                    ) {
                        SunoTokenApplyHint(
                            intro = "还没配置 Suno API Token。没申请过的话，可以先去控制台申请。",
                        )
                    }
                }

                if (sunoConfig.hasToken && !sunoConfig.hasCallbackUrl) {
                    StudioSectionCard(
                        title = "Callback URL 可选但推荐",
                        subtitle = "不填也能正常提交并轮询；只有在 Suno 提交响应被中断时，才会少一层自动恢复能力。",
                    ) {
                        Text(
                            text = "如果你想在网络抖动或 EOF 场景下自动找回 taskId，可以在设置页补一个公网 HTTPS Callback URL。临时调试可以先用 Cloudflare Tunnel。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (vibeFormHistory.isNotEmpty()) {
                    StudioSectionCard(
                        title = "最近填写",
                        subtitle = "点一下直接回填，上次的 Vibe 参数不用重新打。",
                    ) {
                        RecentFormHistoryChips(
                            labels = vibeFormHistory.map { it.chipLabel() },
                            onSelectIndex = { index ->
                                vibeFormHistory.getOrNull(index)?.let(::applySavedDraft)
                            },
                            modifier = Modifier.fillMaxWidth(),
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
                            onRefreshPersonas = {
                                scope.launch {
                                    if (isRefreshingPersonas) {
                                        return@launch
                                    }
                                    isRefreshingPersonas = true
                                    try {
                                        acceptPersonas(loadSavedPersonas())
                                    } finally {
                                        isRefreshingPersonas = false
                                    }
                                }
                            },
                            isRefreshingPersonas = isRefreshingPersonas,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                    isSubmitted = true
                                    isSubmitting = true
                                    submittedTaskId = null
                                    taskArchiveStatus = null
                                    taskStatus = "正在提交..."
                                    val launchedJob = scope.launch {
                                        try {
                                            val latestConfig = SunoWorkflowService.loadConfig()
                                            sunoConfig = latestConfig
                                            latestConfig.requireToken()
                                            val currentDraft = SavedVibeFormDraft(
                                                lyrics = lyrics,
                                                songName = songName,
                                                artistName = artistName,
                                                title = title,
                                                tags = tags,
                                                mv = mv,
                                                makeInstrumental = makeInstrumental,
                                                vocalGender = vocalGender,
                                                negativeTags = negativeTags,
                                                gptDescriptionPrompt = gptDescriptionPrompt,
                                                selectedPersonaId = selectedPersonaId,
                                            )
                                            val draftSaved = runCatching {
                                                saveVibeFormDraft(currentDraft)
                                            }.isSuccess
                                            if (draftSaved) {
                                                vibeFormHistory = runCatching {
                                                    loadVibeFormHistory()
                                                }.getOrDefault(vibeFormHistory)
                                            }
                                            val mergedStyle = buildSunoStyleText(
                                                tags = tags,
                                                descriptionPrompt = gptDescriptionPrompt,
                                            )
                                            val request = SunoGenerateRequest(
                                                prompt = lyrics,
                                                customMode = true,
                                                instrumental = makeInstrumental,
                                                model = mv,
                                                title = title.ifBlank { null },
                                                style = mergedStyle.ifBlank { null },
                                                negativeTags = negativeTags.ifBlank { null },
                                                vocalGender = vocalGender,
                                                personaId = selectedPersonaId,
                                            )
                                            submittedJson = prettyJson.encodeToString(request)
                                            val completed = SunoWorkflowService.submitTask(
                                                actionLabel = "提交音乐生成",
                                                submit = { client, callbackUrl ->
                                                    client.generateMusic(
                                                        request.copy(callBackUrl = callbackUrl)
                                                    )
                                                },
                                                onTaskAccepted = { taskId ->
                                                    submittedTaskId = taskId
                                                },
                                                onStatusUpdate = { status, detail ->
                                                    taskDetail = detail
                                                    taskStatus = status
                                                },
                                            )
                                            taskDetail = completed
                                            taskStatus = completed.displayStatus
                                        } catch (_: CancellationException) {
                                            taskStatus = if (submittedTaskId.isNullOrBlank()) {
                                                isSubmitted = false
                                                "已取消提交"
                                            } else {
                                                "已取消等待，Suno 侧任务可能仍在继续"
                                            }
                                        } catch (error: Exception) {
                                            val recoveredTaskId = submittedTaskId
                                            val recoveredSnapshot = recoveredTaskId
                                                ?.takeIf { it.isNotBlank() }
                                                ?.let { taskId ->
                                                    runCatching {
                                                        refreshSunoTaskSnapshotById(
                                                            taskId = taskId,
                                                            fallbackType = "generate",
                                                            requestJson = submittedJson,
                                                        )
                                                    }.getOrNull()
                                                }
                                            if (recoveredSnapshot != null) {
                                                taskDetail = recoveredSnapshot.detail
                                                taskArchiveStatus = recoveredSnapshot.archiveStatus
                                                taskStatus = recoveredTaskStatusText(recoveredSnapshot.detail)
                                            } else {
                                                taskStatus = "错误: ${SunoWorkflowService.errorMessage(error)}"
                                            }
                                        } finally {
                                            val currentJob = currentCoroutineContext()[Job]
                                            isSubmitting = false
                                            if (submissionJob === currentJob) {
                                                submissionJob = null
                                            }
                                            onCreditsRefresh()
                                        }
                                    }
                                    submissionJob = launchedJob
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
                            if (isSubmitting) {
                                OutlinedButton(onClick = ::cancelSubmission) {
                                    Text("取消等待")
                                }
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
                    .padding(top = 12.dp, end = 12.dp, bottom = 12.dp),
            ) {
                TaskProgressPanel(
                    submittedJson = submittedJson,
                    submittedTaskId = submittedTaskId,
                    taskArchiveStatus = taskArchiveStatus,
                    taskStatus = taskStatus,
                    taskDetail = taskDetail,
                    fallbackType = "generate",
                    onCancelWait = if (isSubmitting) ::cancelSubmission else null,
                    onPersonaCreated = { persona ->
                        applyCreatedPersona(persona)
                    },
                )
            }
        }
    }
}
