package site.addzero.kcloud.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.workbench.design.button.WorkbenchButton as Button
import site.addzero.kcloud.api.suno.SunoTaskDetail
import site.addzero.kcloud.api.suno.SunoWavRequest

@Composable
fun WavExportConfirmDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultDetail by remember { mutableStateOf<SunoTaskDetail?>(null) }

    MusicActionDialog(
        title = "导出 WAV",
        isSubmitting = isSubmitting,
        onDismiss = onDismiss,
    ) {
        if (resultDetail == null) {
            DialogHint("这会把当前音轨转换成 WAV 无损格式，完成后可以直接打开下载链接。")
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
                            val request = SunoWavRequest(
                                taskId = taskId,
                                audioId = audioId,
                            )
                            val detail = SunoWorkflowService.submitTask(
                                actionLabel = "提交 WAV 转换",
                                submit = { client, callbackUrl ->
                                    client.convertToWav(
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
                Text(if (isSubmitting) "转换中..." else "开始转换")
            }
            DialogStatusText(statusText)
            DialogErrorText(
                errorMessage = errorMessage,
                onClear = { errorMessage = null },
            )
        } else {
            DialogSuccessTitle("WAV 转换完成")
            val tracks = resultDetail?.response?.sunoData ?: emptyList()
            if (tracks.isEmpty()) {
                DialogHint("转换完成，但没有返回音轨数据。")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tracks.forEach { track ->
                        val wavUrl = track.audioUrl
                        if (wavUrl.isNullOrBlank()) {
                            DialogInfoCard(
                                title = track.title ?: "未命名音轨",
                                body = "未获取到下载链接",
                                accent = MaterialTheme.colorScheme.errorContainer,
                            )
                        } else {
                            DialogLinkCard(
                                title = track.title ?: "未命名音轨",
                                label = "下载 WAV 文件",
                                url = wavUrl,
                            )
                        }
                    }
                }
            }
            DialogCloseButton(onDismiss)
        }
    }
}
