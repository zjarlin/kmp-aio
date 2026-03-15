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
import site.addzero.vibepocket.api.suno.SunoMusicCoverRequest
import site.addzero.vibepocket.api.suno.SunoTaskDetail

@Composable
fun MusicCoverFormDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }

    MusicActionDialog(
        title = "生成封面",
        isSubmitting = isSubmitting,
        onDismiss = onDismiss,
    ) {
        if (resultDetail == null) {
            DialogHint("为这条音轨补充封面描述、风格或标题，全部都可以留空。")
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("封面描述提示词") },
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
                            val request = SunoMusicCoverRequest(
                                taskId = taskId,
                                audioId = audioId,
                                prompt = prompt.ifBlank { null },
                                style = style.ifBlank { null },
                                title = title.ifBlank { null },
                            )
                            val detail = SunoWorkflowService.submitTask(
                                submit = { client -> client.generateMusicCover(request) },
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
                Text(if (isSubmitting) "生成中..." else "生成封面")
            }
            DialogStatusText(statusText)
            DialogErrorText(
                errorMessage = errorMessage,
                onClear = { errorMessage = null },
            )
        } else {
            val detail = resultDetail ?: return@MusicActionDialog
            DialogSuccessTitle("封面生成完成")
            DialogTrackResults(
                detail = detail,
                fallbackTaskId = taskId,
            )
            DialogCloseButton(onDismiss)
        }
    }
}
