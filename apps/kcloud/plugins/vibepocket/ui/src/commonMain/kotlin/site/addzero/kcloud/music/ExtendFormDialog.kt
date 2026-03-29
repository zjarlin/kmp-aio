package site.addzero.kcloud.music

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.kcloud.api.suno.SunoExtendRequest
import site.addzero.kcloud.api.suno.SunoTaskDetail
import site.addzero.kcloud.model.PersonaItem

@Composable
fun ExtendFormDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var continueAtText by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var personas by remember { mutableStateOf<List<PersonaItem>>(emptyList()) }
    var selectedPersonaId by remember { mutableStateOf<String?>(null) }
    var isRefreshingPersonas by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }

    LaunchedEffect(Unit) {
        isRefreshingPersonas = true
        try {
            personas = loadSavedPersonas()
        } finally {
            isRefreshingPersonas = false
        }
    }

    MusicActionDialog(
        title = "扩展音乐",
        isSubmitting = isSubmitting,
        onDismiss = onDismiss,
    ) {
        if (resultDetail == null) {
            DialogHint("为这条音轨设置扩展参数，所有字段都可以留空。")
            OutlinedTextField(
                value = continueAtText,
                onValueChange = { continueAtText = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("续写位置（秒）") },
                placeholder = { Text("留空则使用默认位置") },
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
                emptyMessage = "还没有 Persona。先从已生成音轨创建一个，再回来做续写。",
            )
            Button(
                onClick = {
                    if (isSubmitting) {
                        return@Button
                    }
                    isSubmitting = true
                    errorMessage = null
                    statusText = "正在提交..."

                    scope.launch {
                        try {
                            val request = SunoExtendRequest(
                                audioId = audioId,
                                prompt = prompt.ifBlank { null },
                                style = style.ifBlank { null },
                                title = title.ifBlank { null },
                                continueAt = continueAtText.toIntOrNull(),
                                personaId = selectedPersonaId,
                            )

                            val detail = SunoWorkflowService.submitTask(
                                actionLabel = "提交扩展",
                                submit = { client, callbackUrl ->
                                    client.extendMusic(
                                        request.copy(callBackUrl = callbackUrl)
                                    )
                                },
                                onStatusUpdate = { status, _ ->
                                    statusText = status
                                },
                            )
                            resultDetail = detail
                            statusText = null
                        } catch (error: Exception) {
                            errorMessage = SunoWorkflowService.errorMessage(error)
                            statusText = null
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
            ) {
                Text(if (isSubmitting) "提交中..." else "提交扩展")
            }
            DialogStatusText(statusText)
            DialogErrorText(
                errorMessage = errorMessage,
                onClear = { errorMessage = null },
            )
        } else {
            val detail = resultDetail ?: return@MusicActionDialog
            DialogSuccessTitle("扩展完成")
            DialogTrackResults(
                detail = detail,
                fallbackTaskId = taskId,
            )
            DialogCloseButton(onDismiss)
        }
    }
}
