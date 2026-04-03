package site.addzero.kcloud.plugins.system.aichat.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.design.button.KCloudButton as Button
import site.addzero.kcloud.plugins.system.aichat.AiChatWorkbenchState
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_TRANSPORT_ACP
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_TRANSPORT_HTTP
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_ANTHROPIC
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_DEEPSEEK
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_GOOGLE
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OLLAMA
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENAI
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENROUTER

@Route(
    value = "AI对话",
    title = "对话会话",
    routePath = "system/ai-chat/sessions",
    icon = "SmartToy",
    order = 30.0,
    enabled = false,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统管理",
            icon = "AdminPanelSettings",
            order = 100,
        ),
    ),
)
@Composable
fun AiChatSessionsScreen() {
    val viewModel: AiChatSessionsViewModel = koinViewModel()
    AiChatPanel(state = viewModel.state)
}

@Composable
fun AiChatPanel(
    state: AiChatWorkbenchState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        state.ensureLoaded()
    }

    Row(
        modifier = modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.weight(0.9f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "会话列表",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !state.isBusy,
                        onClick = { scope.launch { state.createSession() } },
                    ) {
                        Text("新建")
                    }
                    Button(
                        enabled = !state.isBusy && state.selectedSessionId != null,
                        onClick = { scope.launch { state.deleteSelectedSession() } },
                    ) {
                        Text("删除")
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.sessions, key = { session -> session.id }) { session ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.selectedSessionId == session.id) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                                },
                            ),
                            onClick = {
                                scope.launch {
                                    state.selectSession(session.id)
                                }
                            },
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "更新时间：${session.updateTimeMillis ?: session.createTimeMillis}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1.6f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "AI 对话",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "组件现在支持把后台地址、厂商、模型 Base URL、API Key、模型名直接带进请求；HTTP transport 会真实调用 Koog provider，ACP transport 已预留网关入口。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiChatProviderConfigCard(state = state)
                if (state.statusMessage.isNotBlank()) {
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.messages, key = { message -> message.id }) { message ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = if (message.role == "assistant") "助手" else "用户",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = message.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = state.draftMessage,
                    onValueChange = { state.draftMessage = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("输入消息") },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        enabled = !state.isBusy,
                        onClick = { scope.launch { state.sendMessage() } },
                    ) {
                        Text("发送")
                    }
                }
            }
        }
    }
}

@Composable
private fun AiChatProviderConfigCard(
    state: AiChatWorkbenchState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "连接配置",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            OutlinedTextField(
                value = state.serverBaseUrl,
                onValueChange = { state.serverBaseUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("KCloud 后端 URL") },
                placeholder = { Text("http://localhost:18080/") },
                singleLine = true,
            )
            Text(
                text = "传输协议",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TransportChoice(
                    selected = state.providerTransport == AI_CHAT_TRANSPORT_HTTP,
                    label = "HTTP",
                ) {
                    state.providerTransport = AI_CHAT_TRANSPORT_HTTP
                }
                TransportChoice(
                    selected = state.providerTransport == AI_CHAT_TRANSPORT_ACP,
                    label = "ACP",
                ) {
                    state.providerTransport = AI_CHAT_TRANSPORT_ACP
                }
            }
            Text(
                text = "模型厂商",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProviderChip(
                    selected = state.providerVendor == AI_CHAT_VENDOR_OPENAI,
                    label = "OpenAI",
                ) {
                    state.providerVendor = AI_CHAT_VENDOR_OPENAI
                }
                ProviderChip(
                    selected = state.providerVendor == AI_CHAT_VENDOR_OPENROUTER,
                    label = "OpenRouter",
                ) {
                    state.providerVendor = AI_CHAT_VENDOR_OPENROUTER
                }
                ProviderChip(
                    selected = state.providerVendor == AI_CHAT_VENDOR_DEEPSEEK,
                    label = "DeepSeek",
                ) {
                    state.providerVendor = AI_CHAT_VENDOR_DEEPSEEK
                }
                ProviderChip(
                    selected = state.providerVendor == AI_CHAT_VENDOR_ANTHROPIC,
                    label = "Anthropic",
                ) {
                    state.providerVendor = AI_CHAT_VENDOR_ANTHROPIC
                }
                ProviderChip(
                    selected = state.providerVendor == AI_CHAT_VENDOR_GOOGLE,
                    label = "Google",
                ) {
                    state.providerVendor = AI_CHAT_VENDOR_GOOGLE
                }
                ProviderChip(
                    selected = state.providerVendor == AI_CHAT_VENDOR_OLLAMA,
                    label = "Ollama",
                ) {
                    state.providerVendor = AI_CHAT_VENDOR_OLLAMA
                }
            }
            OutlinedTextField(
                value = state.providerBaseUrl,
                onValueChange = { state.providerBaseUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("模型 Base URL") },
                placeholder = { Text("留空则按厂商默认地址") },
                singleLine = true,
            )
            OutlinedTextField(
                value = state.providerApiKey,
                onValueChange = { state.providerApiKey = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                placeholder = { Text("Ollama 可留空") },
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = state.providerModel,
                    onValueChange = { state.providerModel = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("模型名") },
                    placeholder = { Text("留空则按厂商默认模型") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.providerVendor,
                    onValueChange = { state.providerVendor = it },
                    modifier = Modifier.weight(0.75f),
                    label = { Text("厂商标识") },
                    singleLine = true,
                )
            }
            OutlinedTextField(
                value = state.providerSystemPrompt,
                onValueChange = { state.providerSystemPrompt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("系统提示词") },
                minLines = 2,
            )
        }
    }
}

@Composable
private fun ProviderChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun TransportChoice(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}
