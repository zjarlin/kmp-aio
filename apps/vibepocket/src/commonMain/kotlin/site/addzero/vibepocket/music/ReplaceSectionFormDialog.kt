package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.vibepocket.api.suno.SunoReplaceSectionRequest
import site.addzero.vibepocket.api.suno.SunoTaskDetail

@Composable
fun ReplaceSectionFormDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val playback = rememberDialogPlaybackSnapshot()

    var replaceStartText by remember { mutableStateOf("") }
    var replaceEndText by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }

    MusicActionDialog(
        title = "替换片段",
        isSubmitting = isSubmitting,
        onDismiss = onDismiss,
    ) {
        if (resultDetail == null) {
            DialogHint("设置需要替换的片段范围，再补上新的歌词或提示词。")
            OutlinedTextField(
                value = replaceStartText,
                onValueChange = { replaceStartText = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("起始秒数") },
                placeholder = { Text("replaceStart") },
                singleLine = true,
            )
            OutlinedTextField(
                value = replaceEndText,
                onValueChange = { replaceEndText = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("结束秒数") },
                placeholder = { Text("replaceEnd") },
                singleLine = true,
            )
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("新歌词 / 提示词") },
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
                            val request = SunoReplaceSectionRequest(
                                taskId = taskId,
                                audioId = audioId,
                                prompt = prompt.ifBlank { null },
                                style = style.ifBlank { null },
                                replaceStart = replaceStartText.toIntOrNull(),
                                replaceEnd = replaceEndText.toIntOrNull(),
                            )
                            val detail = SunoWorkflowService.submitTask(
                                submit = { client -> client.replaceSection(request) },
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
                Text(if (isSubmitting) "提交中..." else "提交替换")
            }
            DialogStatusText(statusText)
            DialogErrorText(
                errorMessage = errorMessage,
                onClear = { errorMessage = null },
            )
        } else {
            val detail = resultDetail ?: return@MusicActionDialog
            DialogSuccessTitle("片段替换完成")
            DialogTrackResults(
                detail = detail,
                fallbackTaskId = taskId,
                playback = playback,
            )
            DialogCloseButton(onDismiss)
        }
    }
}
