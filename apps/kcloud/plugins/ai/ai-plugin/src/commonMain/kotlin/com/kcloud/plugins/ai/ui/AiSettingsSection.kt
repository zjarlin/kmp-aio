package com.kcloud.plugins.ai.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcloud.model.AiProviderIds
import com.kcloud.model.AppSettings
import com.kcloud.plugins.ai.spi.AiConnectionTestResult
import com.kcloud.plugins.ai.spi.AiDiagnosticsService
import com.kcloud.plugins.ai.spi.AiModelOption
import com.kcloud.plugins.settings.ui.LabeledCheckbox
import com.kcloud.plugins.settings.ui.SettingsSection
import kotlinx.coroutines.launch

@Composable
fun AiSettingsSection(
    persisted: AppSettings,
    draft: AppSettings,
    onDraftChange: (AppSettings) -> Unit,
    diagnosticsService: AiDiagnosticsService
) {
    val coroutineScope = rememberCoroutineScope()
    val providers = remember(diagnosticsService) {
        diagnosticsService.availableProviders()
    }
    val currentProvider = remember(draft.ai.selectedProviderId, providers) {
        providers.firstOrNull { descriptor -> descriptor.providerId == draft.ai.selectedProviderId }
    }
    var isTesting by remember { mutableStateOf(false) }
    var isDiscoveringModels by remember(draft.ai.selectedProviderId, draft.ai.ollama.baseUrl) {
        mutableStateOf(false)
    }
    var lastTestResult by remember(persisted.ai, draft.ai) {
        mutableStateOf<AiConnectionTestResult?>(null)
    }
    var availableModels by remember(draft.ai.selectedProviderId, draft.ai.ollama.baseUrl) {
        mutableStateOf<List<AiModelOption>>(emptyList())
    }
    var modelDiscoveryMessage by remember(draft.ai.selectedProviderId, draft.ai.ollama.baseUrl) {
        mutableStateOf<String?>(null)
    }

    SettingsSection("AI") {
        LabeledCheckbox(
            checked = draft.ai.enabled,
            label = "启用 AI 集成",
            onCheckedChange = { enabled ->
                onDraftChange(
                    draft.copy(
                        ai = draft.ai.copy(enabled = enabled)
                    )
                )
            }
        )

        Text("提供者", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            providers.forEach { provider ->
                Button(
                    onClick = {
                        onDraftChange(
                            draft.copy(
                                ai = draft.ai.copy(selectedProviderId = provider.providerId)
                            )
                        )
                    }
                ) {
                    Text(
                        if (draft.ai.selectedProviderId == provider.providerId) {
                            "${provider.displayName} ✓"
                        } else {
                            provider.displayName
                        }
                    )
                }
            }
        }

        when (draft.ai.selectedProviderId) {
            AiProviderIds.OLLAMA -> {
                OutlinedTextField(
                    value = draft.ai.ollama.baseUrl,
                    onValueChange = { value ->
                        onDraftChange(
                            draft.copy(
                                ai = draft.ai.copy(
                                    ollama = draft.ai.ollama.copy(baseUrl = value)
                                )
                            )
                        )
                    },
                    label = { Text("Ollama Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draft.ai.ollama.model,
                    onValueChange = { value ->
                        onDraftChange(
                            draft.copy(
                                ai = draft.ai.copy(
                                    ollama = draft.ai.ollama.copy(model = value)
                                )
                            )
                        )
                    },
                    label = { Text("模型名") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draft.ai.ollama.timeoutMillis.toString(),
                    onValueChange = { value ->
                        val timeoutMillis = value.toLongOrNull() ?: draft.ai.ollama.timeoutMillis
                        onDraftChange(
                            draft.copy(
                                ai = draft.ai.copy(
                                    ollama = draft.ai.ollama.copy(timeoutMillis = timeoutMillis)
                                )
                            )
                        )
                    },
                    label = { Text("超时（毫秒）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> {
                Text(
                    text = "当前 provider 的设置表单还没接入，先保留数据结构预留位。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = draft.ai.enabled && !isTesting,
                onClick = {
                    coroutineScope.launch {
                        isTesting = true
                        lastTestResult = diagnosticsService.testConnection(
                            providerId = draft.ai.selectedProviderId,
                            settings = draft.ai
                        )
                        isTesting = false
                    }
                }
            ) {
                Text(if (isTesting) "测试中…" else "测试连接")
            }

            if (currentProvider?.supportsModelDiscovery == true) {
                Button(
                    enabled = draft.ai.enabled && !isDiscoveringModels,
                    onClick = {
                        coroutineScope.launch {
                            isDiscoveringModels = true
                            val discoveredModels = diagnosticsService.discoverModels(
                                providerId = draft.ai.selectedProviderId,
                                settings = draft.ai
                            )
                            availableModels = discoveredModels
                            modelDiscoveryMessage = when {
                                discoveredModels.isEmpty() -> "当前地址下未发现可用模型。"
                                else -> "已发现 ${discoveredModels.size} 个模型，点下面按钮可直接回填。"
                            }
                            isDiscoveringModels = false
                        }
                    }
                ) {
                    Text(if (isDiscoveringModels) "拉取中…" else "拉取模型列表")
                }
            }
        }

        if (!draft.ai.enabled) {
            Text(
                text = "启用后才会使用 AI 配置。",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        modelDiscoveryMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (availableModels.isNotEmpty() && draft.ai.selectedProviderId == AiProviderIds.OLLAMA) {
            Text("可选模型", fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableModels.chunked(3).forEach { rowModels ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowModels.forEach { model ->
                            Button(
                                onClick = {
                                    onDraftChange(
                                        draft.copy(
                                            ai = draft.ai.copy(
                                                ollama = draft.ai.ollama.copy(model = model.id)
                                            )
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    if (draft.ai.ollama.model == model.id) {
                                        "${model.displayName} ✓"
                                    } else {
                                        model.displayName
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        lastTestResult?.let { result ->
            Text(
                text = result.message,
                color = if (result.success) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            result.details?.takeIf { it.isNotBlank() }?.let { details ->
                Text(
                    text = details,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
