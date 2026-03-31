package site.addzero.kcloud.music

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.kcloud.api.suno.SUNO_MODELS
import site.addzero.kcloud.api.suno.SunoTaskDetail
import site.addzero.kcloud.api.suno.SunoUploadCoverRequest
import site.addzero.kcloud.api.suno.VOCAL_GENDERS
import site.addzero.kcloud.vibepocket.model.PersonaItem

private fun uploadCoverDebug(message: String) {
    println("[UploadCoverFormDialog] $message")
}

@Composable
fun UploadCoverFormDialog(
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var uploadUrl by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("V4_5ALL") }
    var selectedGender by remember { mutableStateOf("m") }
    var personas by remember { mutableStateOf<List<PersonaItem>>(emptyList()) }
    var selectedPersonaId by remember { mutableStateOf<String?>(null) }
    var isRefreshingPersonas by remember { mutableStateOf(false) }
    var coverFormHistory by remember { mutableStateOf<List<SavedUploadCoverFormDraft>>(emptyList()) }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }

    fun applySavedDraft(draft: SavedUploadCoverFormDraft) {
        uploadUrl = draft.uploadUrl
        prompt = draft.prompt
        style = draft.style
        title = draft.title
        selectedModel = draft.selectedModel
        selectedGender = draft.selectedGender
        selectedPersonaId = draft.selectedPersonaId
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
        coverFormHistory = runCatching { loadUploadCoverFormHistory() }.getOrDefault(emptyList())
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

    MusicActionDialog(
        title = "翻唱上传",
        isSubmitting = isSubmitting,
        onDismiss = onDismiss,
    ) {
        if (resultDetail == null) {
            DialogHint("输入音频 URL，再选择模型和声线性别，就可以提交翻唱任务。")
            if (coverFormHistory.isNotEmpty()) {
                DialogHint("最近填写")
                RecentFormHistoryChips(
                    labels = coverFormHistory.map { it.chipLabel() },
                    onSelectIndex = { index ->
                        coverFormHistory.getOrNull(index)?.let(::applySavedDraft)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            OutlinedTextField(
                value = uploadUrl,
                onValueChange = { uploadUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("音频 URL") },
                placeholder = { Text("必填") },
                singleLine = true,
            )
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("歌词 / 提示词") },
                placeholder = { Text("可选") },
                singleLine = false,
                minLines = 4,
            )
            OutlinedTextField(
                value = style,
                onValueChange = { style = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("风格标签") },
                placeholder = { Text("可选") },
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

            DialogHint("模型版本")
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

            DialogHint("声线性别")
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

            DialogHint("Persona 声音角色")
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
                emptyMessage = "还没有 Persona。先从生成结果里创建一个，再回来做翻唱复用声线。",
            )

            Button(
                onClick = {
                    uploadCoverDebug(
                        "submit clicked urlBlank=${uploadUrl.isBlank()} model=$selectedModel gender=$selectedGender personaId=$selectedPersonaId"
                    )
                    if (isSubmitting) {
                        uploadCoverDebug("ignored duplicated click while submitting")
                        return@Button
                    }
                    if (uploadUrl.isBlank()) {
                        uploadCoverDebug("validation failed: uploadUrl is blank")
                        errorMessage = "请输入音频 URL"
                        return@Button
                    }
                    isSubmitting = true
                    errorMessage = null
                    statusText = "正在提交..."

                    scope.launch {
                        try {
                            val currentDraft = SavedUploadCoverFormDraft(
                                uploadUrl = uploadUrl,
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
                            uploadCoverDebug("request prepared originalUrl=${uploadUrl.trim()} resolvedUrl=$preparedUploadUrl")
                            uploadCoverDebug("request payload (callback managed by workflow): $request")
                            val detail = SunoWorkflowService.submitTask(
                                actionLabel = "提交翻唱",
                                submit = { client, callbackUrl ->
                                    client.uploadCover(
                                        request.copy(callBackUrl = callbackUrl)
                                    )
                                },
                                onStatusUpdate = { status, _ ->
                                    uploadCoverDebug("status update: $status")
                                    statusText = status
                                },
                            )
                            resultDetail = detail
                            uploadCoverDebug(
                                "submit completed taskId=${detail.taskId} status=${detail.status} error=${detail.errorMessage ?: detail.errorCode}"
                            )
                            statusText = null
                        } catch (error: Exception) {
                            uploadCoverDebug("submit failed: ${error::class.simpleName}: ${error.message}")
                            errorMessage = SunoWorkflowService.errorMessage(error)
                            statusText = null
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
            ) {
                Text(if (isSubmitting) "提交中..." else "提交翻唱")
            }
            DialogStatusText(statusText)
            DialogErrorText(
                errorMessage = errorMessage,
                onClear = { errorMessage = null },
            )
        } else {
            val detail = resultDetail ?: return@MusicActionDialog
            DialogSuccessTitle("翻唱完成")
            DialogTrackResults(
                detail = detail,
                fallbackTaskId = "",
            )
            DialogCloseButton(onDismiss)
        }
    }
}
