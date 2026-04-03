package site.addzero.kcloud.music

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import site.addzero.kcloud.design.button.KCloudButton as Button
import site.addzero.kcloud.api.suno.SunoBoostStyleData
import site.addzero.kcloud.api.suno.SunoBoostStyleRequest

@Composable
fun BoostStyleConfirmDialog(
    audioId: String,
    taskId: String,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultData by remember { mutableStateOf<SunoBoostStyleData?>(null) }
    var remainingCredits by remember { mutableStateOf<Int?>(null) }

    MusicActionDialog(
        title = "风格提升",
        isSubmitting = isSubmitting,
        onDismiss = onDismiss,
    ) {
        if (resultData == null) {
            DialogHint("风格提升会额外消耗积分，用来换取更精致的音乐结果。")
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
                            val request = SunoBoostStyleRequest(
                                taskId = taskId,
                                audioId = audioId,
                                callBackUrl = SunoWorkflowService.loadConfig().callbackUrlOrNull(),
                            )
                            statusText = "正在执行风格提升..."
                            val data = SunoWorkflowService.boostStyle(request)
                            resultData = data
                            statusText = "正在刷新积分..."
                            remainingCredits = SunoWorkflowService.getCreditsOrNull()
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
                Text(if (isSubmitting) "处理中..." else "开始提升")
            }
            DialogStatusText(statusText)
            DialogErrorText(
                errorMessage = errorMessage,
                onClear = { errorMessage = null },
            )
        } else {
            DialogSuccessTitle("风格提升完成")
            resultData?.creditsConsumed?.let { consumed ->
                DialogInfoCard(
                    title = "消耗积分",
                    body = consumed.toString(),
                    accent = MaterialTheme.colorScheme.errorContainer,
                )
            }
            (remainingCredits ?: resultData?.creditsRemaining)?.let { credits ->
                DialogInfoCard(
                    title = "剩余积分",
                    body = credits.toString(),
                    accent = MaterialTheme.colorScheme.primaryContainer,
                )
            }
            resultData?.taskId?.let { task ->
                DialogMonospaceValue(
                    label = "任务 ID",
                    value = task,
                )
            }
            DialogCloseButton(onDismiss)
        }
    }
}
