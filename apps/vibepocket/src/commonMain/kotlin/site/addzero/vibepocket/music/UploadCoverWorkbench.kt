package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import site.addzero.vibepocket.api.suno.SUNO_MODELS
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.api.suno.SunoUploadCoverRequest
import site.addzero.vibepocket.api.suno.VOCAL_GENDERS
import site.addzero.vibepocket.model.PersonaItem
import site.addzero.vibepocket.ui.StudioPill
import site.addzero.vibepocket.ui.StudioSectionCard
import site.addzero.vibepocket.ui.SunoTokenApplyHint

private val uploadCoverPrettyJson = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

@Composable
fun UploadCoverWorkbench(
    onPersonaCreated: (PersonaItem) -> Unit = {},
    onCreditsRefresh: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    var uploadUrl by remember { mutableStateOf("") }
    var sourceDisplayTitle by remember { mutableStateOf("") }
    var sourceDisplaySubtitle by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("V4_5ALL") }
    var selectedGender by remember { mutableStateOf("m") }
    var searchSongName by remember { mutableStateOf("") }
    var searchArtistName by remember { mutableStateOf("") }
    var personas by remember { mutableStateOf<List<PersonaItem>>(emptyList()) }
    var selectedPersonaId by remember { mutableStateOf<String?>(null) }
    var isRefreshingPersonas by remember { mutableStateOf(false) }
    var coverFormHistory by remember { mutableStateOf<List<SavedUploadCoverFormDraft>>(emptyList()) }
    var sunoConfig by remember { mutableStateOf(SunoRuntimeConfig()) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submittedJson by remember { mutableStateOf<String?>(null) }
    var submittedTaskId by remember { mutableStateOf<String?>(null) }
    var taskArchiveStatus by remember { mutableStateOf<String?>(null) }
    var taskStatus by remember { mutableStateOf("未提交") }
    var taskDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUploadingLocalFile by remember { mutableStateOf(false) }
    var localUploadStatus by remember { mutableStateOf<String?>(null) }
    var submissionJob by remember { mutableStateOf<Job?>(null) }

    fun applySavedDraft(draft: SavedUploadCoverFormDraft) {
        uploadUrl = draft.uploadUrl
        sourceDisplayTitle = draft.restoredSourceTitle()
        sourceDisplaySubtitle = draft.restoredSourceSubtitle()
        prompt = draft.prompt
        style = draft.style
        title = draft.title
        selectedModel = draft.selectedModel
        selectedGender = draft.selectedGender
        selectedPersonaId = draft.selectedPersonaId
        localUploadStatus = null
        errorMessage = null
    }

    fun cancelSubmission() {
        submissionJob?.cancel(CancellationException("用户取消等待"))
    }

    val localAudioPickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("mp3", "wav", "m4a", "aac", "flac", "ogg", "opus", "mp4"),
        title = "选择本地音频",
    ) { selectedFile ->
        if (selectedFile == null || isUploadingLocalFile) {
            return@rememberFilePickerLauncher
        }
        scope.launch {
            isUploadingLocalFile = true
            localUploadStatus = "正在上传 ${selectedFile.name}..."
            errorMessage = null
            try {
                val latestConfig = SunoWorkflowService.loadConfig()
                sunoConfig = latestConfig
                latestConfig.requireToken()
                val uploaded = SunoWorkflowService.uploadLocalAudioSource(
                    bytes = selectedFile.readBytes(),
                    fileName = selectedFile.name,
                    contentType = selectedFile.audioContentType(),
                )
                uploadUrl = uploaded.resolvedUrl
                sourceDisplayTitle = selectedFile.nameWithoutExtension.ifBlank { selectedFile.name }
                sourceDisplaySubtitle = buildLocalUploadSubtitle(uploaded.expiresAt)
                if (title.isBlank()) {
                    title = selectedFile.nameWithoutExtension.ifBlank { selectedFile.name }
                }
                localUploadStatus = "本地音频已上传，可直接提交翻唱。"
            } catch (error: Exception) {
                val message = SunoWorkflowService.errorMessage(error)
                localUploadStatus = "本地音频上传失败：$message"
                errorMessage = message
            } finally {
                isUploadingLocalFile = false
            }
        }
    }

    fun shouldRestoreDraft(): Boolean {
        return uploadUrl.isBlank() &&
            prompt.isBlank() &&
            style.isBlank() &&
            title.isBlank() &&
            selectedPersonaId == null &&
            selectedModel == "V4_5ALL" &&
            selectedGender == "m"
    }

    LaunchedEffect(Unit) {
        sunoConfig = runCatching { SunoWorkflowService.loadConfig() }
            .getOrDefault(SunoRuntimeConfig())
        coverFormHistory = runCatching { loadUploadCoverFormHistory() }
            .getOrDefault(emptyList())
        if (shouldRestoreDraft()) {
            coverFormHistory.firstOrNull()?.let(::applySavedDraft)
        }
        isRefreshingPersonas = true
        try {
            personas = loadSavedPersonas()
            if (!personas.containsPersona(selectedPersonaId)) {
                selectedPersonaId = null
            }
        } finally {
            isRefreshingPersonas = false
        }
    }

    LaunchedEffect(submittedTaskId, taskDetail?.status, submittedJson) {
        val taskId = submittedTaskId ?: return@LaunchedEffect
        val requestJson = submittedJson ?: return@LaunchedEffect
        taskArchiveStatus = "建档中..."
        taskArchiveStatus = runCatching {
            saveSunoTaskArchive(
                taskId = taskId,
                fallbackType = "upload_cover",
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
                .padding(vertical = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StudioPill(
                    text = "Upload Cover",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "翻唱工作台",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "通过搜索结果或本地音频上传带入音源，选择模型、声线和 Persona 后直接提交翻唱。上次填写的内容会自动回填。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!sunoConfig.hasToken) {
                    StudioSectionCard(
                        title = "Suno 尚未配置",
                        subtitle = "还没申请过 Token 的话，先去控制台申请；配置完成前，翻唱提交会保持禁用。",
                    ) {
                        SunoTokenApplyHint()
                    }
                }

                if (coverFormHistory.isNotEmpty()) {
                    StudioSectionCard(
                        title = "最近填写",
                        subtitle = "点一下会同时回填翻唱音源和参数；重复草稿会自动去重。",
                    ) {
                        RecentFormHistoryChips(
                            labels = coverFormHistory.map { it.chipLabel() },
                            onSelectIndex = { index ->
                                coverFormHistory.getOrNull(index)?.let(::applySavedDraft)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                MusicReferenceSearchSection(
                    songName = searchSongName,
                    onSongNameChange = { searchSongName = it },
                    artistName = searchArtistName,
                    onArtistNameChange = { searchArtistName = it },
                    title = "搜索歌曲歌词与音源",
                    subtitle = "翻唱也可以直接搜索歌曲，导入歌词并自动带入音源，不再手动输入音乐 URL。",
                    emptyHintDescription = "输入歌名后搜索，选中一首歌就能直接作为翻唱音源。",
                    sourceActionLabel = "用作翻唱音源",
                    onLyricsImported = { track, lyric ->
                        prompt = lyric.toPromptText()
                        if (title.isBlank()) {
                            title = track.name
                        }
                        searchSongName = track.name
                        if (track.artist.isNotBlank()) {
                            searchArtistName = track.artist
                        }
                    },
                    onSourceResolved = { track, asset ->
                        uploadUrl = asset.url
                        sourceDisplayTitle = track.name
                        sourceDisplaySubtitle = buildString {
                            append(track.artist.ifBlank { "未知歌手" })
                            if (track.album.isNotBlank()) {
                                append(" · ")
                                append(track.album)
                            }
                            if (track.platform.isNotBlank()) {
                                append(" · ")
                                append(track.platform)
                            }
                        }
                        if (title.isBlank()) {
                            title = track.name
                        }
                        searchSongName = track.name
                        if (track.artist.isNotBlank()) {
                            searchArtistName = track.artist
                        }
                        localUploadStatus = null
                        errorMessage = null
                    },
                )

                StudioSectionCard(
                    title = "翻唱参数",
                    subtitle = "先从上方搜索结果里选择音源；歌词、风格和标题都可以留空。",
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "也可以上传本地音频，先换成 Suno 临时文件 URL。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { localAudioPickerLauncher.launch() },
                            enabled = sunoConfig.hasToken && !isUploadingLocalFile,
                        ) {
                            Text(
                                when {
                                    isUploadingLocalFile -> "上传中..."
                                    !sunoConfig.hasToken -> "先配置 Token"
                                    else -> "选择本地音频"
                                }
                            )
                        }
                    }
                    localUploadStatus?.let { status ->
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (status.startsWith("本地音频上传失败")) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }
                    SelectedUploadSourceCard(
                        title = sourceDisplayTitle.ifBlank {
                            if (uploadUrl.isNotBlank()) {
                                title.ifBlank { "已回填音源，可直接提交" }
                            } else {
                                "尚未选择音源"
                            }
                        },
                        subtitle = sourceDisplaySubtitle.ifBlank {
                            when {
                                uploadUrl.isNotBlank() -> "音源链接已回填，可直接提交翻唱。"
                                else -> "请先通过上方搜索结果选择一首歌作为翻唱音源。"
                            }
                        },
                        urlSummary = uploadUrl.takeIf { it.isNotBlank() }?.let { rawUrl ->
                            buildString {
                                append("当前音源链接已就绪")
                                val compactUrl = rawUrl.sourceSummary()
                                if (compactUrl.isNotBlank()) {
                                    append(" · ")
                                    append(compactUrl)
                                }
                            }
                        },
                        onClear = if (uploadUrl.isNotBlank()) {
                            {
                                uploadUrl = ""
                                sourceDisplayTitle = ""
                                sourceDisplaySubtitle = ""
                                localUploadStatus = null
                            }
                        } else {
                            null
                        },
                    )
                    MusicPromptPresetSection(
                        onApplyPrompt = { prompt = it },
                        onApplyStyle = { style = it },
                    )
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 112.dp),
                        label = { Text("歌词 / 提示词") },
                        placeholder = {
                            Text("可选，不强制提供具体歌词。也可以只写风格和演唱要求。")
                        },
                        singleLine = false,
                        minLines = 4,
                    )
                    OutlinedTextField(
                        value = style,
                        onValueChange = { style = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("风格标签") },
                        placeholder = { Text("可选，例如：gospel / cinematic / male choir") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("标题") },
                        placeholder = { Text("可选") },
                        singleLine = true,
                    )
                }

                StudioSectionCard(
                    title = "模型与声音",
                    subtitle = "模型版本、声线性别和 Persona 会一起作用到这次翻唱。",
                ) {
                    Text(
                        text = "模型版本",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SUNO_MODELS.forEach { model ->
                            FilterChip(
                                selected = selectedModel == model,
                                onClick = { selectedModel = model },
                                label = { Text(model) },
                            )
                        }
                    }

                    Text(
                        text = "声线性别",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        VOCAL_GENDERS.forEach { (code, label) ->
                            FilterChip(
                                selected = selectedGender == code,
                                onClick = { selectedGender = code },
                                label = { Text(label) },
                            )
                        }
                    }

                    PersonaSelectionPanel(
                        personas = personas,
                        selectedPersonaId = selectedPersonaId,
                        onPersonaChange = { selectedPersonaId = it },
                        onRefresh = {
                            scope.launch {
                                if (isRefreshingPersonas) {
                                    return@launch
                                }
                                isRefreshingPersonas = true
                                try {
                                    personas = loadSavedPersonas()
                                    if (!personas.containsPersona(selectedPersonaId)) {
                                        selectedPersonaId = null
                                    }
                                } finally {
                                    isRefreshingPersonas = false
                                }
                            }
                        },
                        isRefreshing = isRefreshingPersonas,
                        emptyMessage = "还没有 Persona。先在生成结果里创建一个，再回来复用声线。",
                    )
                }

                StudioSectionCard(
                    title = "提交翻唱",
                    subtitle = "提交后会在右侧任务面板里持续轮询结果。",
                ) {
                    Button(
                        onClick = {
                            if (isSubmitting) {
                                return@Button
                            }
                            if (uploadUrl.isBlank()) {
                                errorMessage = "请先搜索并选择一首音源"
                                return@Button
                            }
                            isSubmitted = true
                            isSubmitting = true
                            submittedTaskId = null
                            taskArchiveStatus = null
                            errorMessage = null
                            taskDetail = null
                            taskStatus = "正在提交..."
                            val launchedJob = scope.launch {
                                try {
                                    val latestConfig = SunoWorkflowService.loadConfig()
                                    sunoConfig = latestConfig
                                    latestConfig.requireToken()
                                    val currentDraft = SavedUploadCoverFormDraft(
                                        uploadUrl = uploadUrl,
                                        sourceTitle = sourceDisplayTitle,
                                        sourceSubtitle = sourceDisplaySubtitle,
                                        prompt = prompt,
                                        style = style,
                                        title = title,
                                        selectedModel = selectedModel,
                                        selectedGender = selectedGender,
                                        selectedPersonaId = selectedPersonaId,
                                    )
                                    val draftSaved = runCatching {
                                        saveUploadCoverFormDraft(currentDraft)
                                    }.isSuccess
                                    if (draftSaved) {
                                        coverFormHistory = runCatching {
                                            loadUploadCoverFormHistory()
                                        }.getOrDefault(coverFormHistory)
                                    }
                                    val preparedUploadUrl = prepareSunoUploadSourceUrl(uploadUrl)
                                    val request = SunoUploadCoverRequest(
                                        uploadUrl = preparedUploadUrl,
                                        prompt = prompt.ifBlank { null },
                                        style = style.ifBlank { null },
                                        title = title.ifBlank { null },
                                        model = selectedModel,
                                        vocalGender = selectedGender,
                                        personaId = selectedPersonaId,
                                    )
                                    submittedJson = uploadCoverPrettyJson.encodeToString(request)
                                    val detail = SunoWorkflowService.submitTask(
                                        actionLabel = "提交翻唱",
                                        submit = { client, callbackUrl ->
                                            client.uploadCover(
                                                request.copy(callBackUrl = callbackUrl)
                                            )
                                        },
                                        onTaskAccepted = { taskId ->
                                            submittedTaskId = taskId
                                        },
                                        onStatusUpdate = { status, updatedDetail ->
                                            taskStatus = status
                                            taskDetail = updatedDetail
                                        },
                                    )
                                    taskDetail = detail
                                    taskStatus = detail.displayStatus
                                } catch (_: CancellationException) {
                                    errorMessage = null
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
                                                    fallbackType = "upload_cover",
                                                    requestJson = submittedJson,
                                                )
                                            }.getOrNull()
                                        }
                                    if (recoveredSnapshot != null) {
                                        taskDetail = recoveredSnapshot.detail
                                        taskArchiveStatus = recoveredSnapshot.archiveStatus
                                        taskStatus = recoveredTaskStatusText(recoveredSnapshot.detail)
                                        errorMessage = if (recoveredSnapshot.detail.isFailed) {
                                            recoveredSnapshot.detail.errorMessage
                                                ?: recoveredSnapshot.detail.errorCode
                                                ?: SunoWorkflowService.errorMessage(error)
                                        } else {
                                            null
                                        }
                                    } else {
                                        taskStatus = "错误: ${SunoWorkflowService.errorMessage(error)}"
                                        errorMessage = SunoWorkflowService.errorMessage(error)
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
                                else -> "提交翻唱"
                            },
                        )
                    }
                    if (isSubmitting) {
                        OutlinedButton(onClick = ::cancelSubmission) {
                            Text("取消等待")
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text(
                        text = taskStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (taskStatus.startsWith("错误")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }
        }

        if (isSubmitted) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            ) {
                TaskProgressPanel(
                    submittedJson = submittedJson,
                    submittedTaskId = submittedTaskId,
                    taskArchiveStatus = taskArchiveStatus,
                    taskStatus = taskStatus,
                    taskDetail = taskDetail,
                    fallbackType = "upload_cover",
                    onCancelWait = if (isSubmitting) ::cancelSubmission else null,
                    onPersonaCreated = onPersonaCreated,
                )
            }
        }
    }
}

@Composable
private fun SelectedUploadSourceCard(
    title: String,
    subtitle: String,
    urlSummary: String?,
    onClear: (() -> Unit)?,
) {
    StudioSectionCard(
        title = "当前翻唱音源",
        subtitle = "可以来自搜索结果，也可以来自本地音频上传，不再手动输入 URL。",
        action = {
            if (onClear != null) {
                OutlinedButton(onClick = onClear) {
                    Text("清空")
                }
            }
        },
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!urlSummary.isNullOrBlank()) {
            Text(
                text = urlSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun PlatformFile.audioContentType(): String? {
    return mimeType()?.toString()?.takeIf { it.isNotBlank() } ?: when (name.substringAfterLast('.', "").lowercase()) {
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "m4a", "mp4" -> "audio/mp4"
        "aac" -> "audio/aac"
        "flac" -> "audio/flac"
        "ogg", "oga" -> "audio/ogg"
        "opus" -> "audio/opus"
        else -> null
    }
}

private fun buildLocalUploadSubtitle(expiresAt: String?): String {
    val expiryText = expiresAt?.trim().orEmpty()
    return if (expiryText.isBlank()) {
        "本地音频已上传到 Suno 临时文件，可直接提交翻唱。"
    } else {
        "本地音频已上传到 Suno 临时文件，可直接提交翻唱。到期时间：$expiryText"
    }
}

private fun site.addzero.vibepocket.api.music.MusicLyric.toPromptText(): String {
    val plainText = lrc.lineSequence()
        .map { line ->
            line.replace(Regex("^\\s*(\\[[^]]+])+"), "").trim()
        }
        .filter { it.isNotBlank() }
        .joinToString(separator = "\n")
        .trim()
    return plainText.ifBlank { lrc.trim() }
}
