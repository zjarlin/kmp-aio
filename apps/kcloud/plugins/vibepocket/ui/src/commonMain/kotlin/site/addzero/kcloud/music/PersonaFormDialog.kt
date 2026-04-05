package site.addzero.kcloud.music

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import site.addzero.cupertino.workbench.material3.OutlinedTextField
import site.addzero.cupertino.workbench.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.cupertino.workbench.button.WorkbenchButton as Button
import site.addzero.kcloud.api.ApiProvider
import site.addzero.kcloud.api.suno.SunoGeneratePersonaRequest
import site.addzero.kcloud.vibepocket.model.PersonaItem
import site.addzero.kcloud.vibepocket.model.PersonaSaveRequest

@Composable
fun PersonaFormDialog(
    audioId: String,
    taskId: String,
    onCreated: (PersonaItem) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultPersona by remember { mutableStateOf<PersonaItem?>(null) }

    MusicActionDialog(
        title = "创建 Persona",
        isSubmitting = isSubmitting,
        onDismiss = onDismiss,
    ) {
        if (resultPersona == null) {
            DialogHint("基于当前音轨创建一个可复用的声音角色，名称和描述都是必填。")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Persona 名称") },
                placeholder = { Text("必填") },
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("Persona 描述") },
                placeholder = { Text("必填") },
                singleLine = false,
                minLines = 4,
            )
            Button(
                onClick = {
                    if (isSubmitting) {
                        return@Button
                    }
                    if (name.isBlank()) {
                        errorMessage = "请输入 Persona 名称"
                        return@Button
                    }
                    if (description.isBlank()) {
                        errorMessage = "请输入 Persona 描述"
                        return@Button
                    }
                    isSubmitting = true
                    errorMessage = null
                    statusText = "正在创建..."

                    scope.launch {
                        try {
                            val request = SunoGeneratePersonaRequest(
                                taskId = taskId,
                                audioId = audioId,
                                name = name.trim(),
                                description = description.trim(),
                            )
                            statusText = "正在调用 Suno API..."
                            val personaData = SunoWorkflowService.generatePersona(request)
                            val personaId = personaData.personaId
                                ?: throw IllegalStateException("Persona 创建成功但未返回 personaId")

                            statusText = "正在保存 Persona 记录..."
                            val savedPersona = ApiProvider.personaApi.savePersona(
                                PersonaSaveRequest(
                                    personaId = personaId,
                                    name = name.trim(),
                                    description = description.trim(),
                                ),
                            )
                            resultPersona = savedPersona
                            onCreated(savedPersona)
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
                Text(if (isSubmitting) "创建中..." else "创建 Persona")
            }
            DialogStatusText(statusText)
            DialogErrorText(
                errorMessage = errorMessage,
                onClear = { errorMessage = null },
            )
        } else {
            DialogSuccessTitle("Persona 创建成功")
            DialogMonospaceValue(
                label = "Persona ID",
                value = resultPersona?.personaId.orEmpty(),
            )
            DialogInfoCard(
                title = resultPersona?.name ?: "Persona",
                body = buildString {
                    append(resultPersona?.description.orEmpty())
                    resultPersona?.createdAt
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            append("\n\n创建时间: ")
                            append(it)
                        }
                },
            )
            DialogInfoCard(
                title = "提示",
                body = "后续在音乐生成参数里就可以直接选择这个 Persona 复用声线。",
            )
            DialogCloseButton(onDismiss)
        }
    }
}
