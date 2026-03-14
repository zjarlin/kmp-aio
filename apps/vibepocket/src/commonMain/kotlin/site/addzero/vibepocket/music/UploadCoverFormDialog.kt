package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import site.addzero.vibepocket.api.suno.SUNO_MODELS
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.api.suno.SunoUploadCoverRequest
import site.addzero.vibepocket.api.suno.VOCAL_GENDERS

@Composable
fun UploadCoverFormDialog(
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val playback = rememberDialogPlaybackSnapshot()

    var uploadUrl by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("V4_5ALL") }
    var selectedGender by remember { mutableStateOf("m") }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }

    MusicActionDialog(
        title = "翻唱上传",
        isSubmitting = isSubmitting,
        onDismiss = onDismiss,
    ) {
        if (resultDetail == null) {
            DialogHint("输入音频 URL，再选择模型和声线性别，就可以提交翻唱任务。")
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

            Button(
                onClick = {
                    if (isSubmitting) {
                        return@Button
                    }
                    if (uploadUrl.isBlank()) {
                        errorMessage = "请输入音频 URL"
                        return@Button
                    }
                    isSubmitting = true
                    errorMessage = null
                    statusText = "正在提交..."

                    scope.launch {
                        try {
                            val request = SunoUploadCoverRequest(
                                uploadUrl = uploadUrl.trim(),
                                prompt = prompt.ifBlank { null },
                                style = style.ifBlank { null },
                                title = title.ifBlank { null },
                                model = selectedModel,
                                vocalGender = selectedGender,
                            )
                            val detail = SunoWorkflowService.submitTask(
                                submit = { client -> client.uploadCover(request) },
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
                playback = playback,
            )
            DialogCloseButton(onDismiss)
        }
    }
}
