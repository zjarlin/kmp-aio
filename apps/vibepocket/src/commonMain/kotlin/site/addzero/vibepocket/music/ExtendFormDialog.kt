package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
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
import site.addzero.vibepocket.api.suno.SunoExtendRequest
import site.addzero.vibepocket.api.suno.SunoTaskDetail

@Composable
fun ExtendFormDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val playback = rememberDialogPlaybackSnapshot()

    var continueAtText by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }

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
                            )

                            val detail = SunoWorkflowService.submitTask(
                                submit = { client -> client.extendMusic(request) },
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
                playback = playback,
            )
            DialogCloseButton(onDismiss)
        }
    }
}
