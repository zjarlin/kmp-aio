package site.addzero.kcloud.plugins.system.aichat.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import site.addzero.component.chat.AddChatConnectionConfig
import site.addzero.component.chat.AddChatMessageItem
import site.addzero.component.chat.AddChatMessageRole
import site.addzero.component.chat.AddChatPanelActions
import site.addzero.component.chat.AddChatPanelState
import site.addzero.component.chat.AddChatQuickPrompt
import site.addzero.component.chat.AddChatSessionItem
import site.addzero.component.chat.AddChatTransport
import site.addzero.component.chat.AddChatVendor
import site.addzero.component.chat.AddChatPanel as AddComposeChatPanel
import site.addzero.kcloud.plugins.system.aichat.AiChatWorkbenchState

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

    AddComposeChatPanel(
        state = state.toPanelState(),
        actions = AddChatPanelActions(
            onCreateSession = { scope.launch { state.createSession() } },
            onDeleteSession = { scope.launch { state.deleteSelectedSession() } },
            onSelectSession = { sessionId ->
                sessionId.toLongOrNull()?.let { id ->
                    scope.launch { state.selectSession(id) }
                }
            },
            onInputChange = { state.draftMessage = it },
            onSend = { scope.launch { state.sendMessage() } },
            onConnectionChange = { config -> state.applyConnectionConfig(config) },
            onUsePrompt = { prompt -> state.draftMessage = prompt.content },
            onToggleConnectionEditor = {},
        ),
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
    )
}

private fun AiChatWorkbenchState.toPanelState(): AddChatPanelState {
    return AddChatPanelState(
        title = "AI 助手",
        subtitle = "支持配置后台 URL、API Key、厂商与模型参数",
        sessions = sessions.map { session ->
            AddChatSessionItem(
                id = session.id.toString(),
                title = session.title,
                subtitle = "更新时间：${session.updateTimeMillis ?: session.createTimeMillis}",
            )
        },
        selectedSessionId = selectedSessionId?.toString(),
        messages = messages.map { message ->
            AddChatMessageItem(
                id = message.id.toString(),
                role = message.role.toChatRole(),
                content = message.content,
                timestampLabel = (message.updateTimeMillis ?: message.createTimeMillis).toString(),
            )
        },
        quickPrompts = defaultQuickPrompts(),
        input = draftMessage,
        connection = AddChatConnectionConfig(
            backendUrl = serverBaseUrl,
            transport = providerTransport.toChatTransport(),
            vendor = providerVendor.toChatVendor(),
            providerBaseUrl = providerBaseUrl,
            apiKey = providerApiKey,
            model = providerModel,
            systemPrompt = providerSystemPrompt,
        ),
        showConnectionEditor = true,
        isSending = isBusy,
        isLoadingMessages = false,
        statusText = statusMessage,
    )
}

private fun AiChatWorkbenchState.applyConnectionConfig(
    config: AddChatConnectionConfig,
) {
    serverBaseUrl = config.backendUrl
    providerTransport = config.transport.wireValue
    providerVendor = config.vendor.wireValue
    providerBaseUrl = config.providerBaseUrl
    providerApiKey = config.apiKey
    providerModel = config.model
    providerSystemPrompt = config.systemPrompt
}

private fun String.toChatRole(): AddChatMessageRole {
    return when (lowercase()) {
        "assistant" -> AddChatMessageRole.Assistant
        "system" -> AddChatMessageRole.System
        "tool" -> AddChatMessageRole.Tool
        else -> AddChatMessageRole.User
    }
}

private fun String.toChatTransport(): AddChatTransport {
    return AddChatTransport.entries.firstOrNull { it.wireValue == lowercase() } ?: AddChatTransport.Http
}

private fun String.toChatVendor(): AddChatVendor {
    return AddChatVendor.entries.firstOrNull { it.wireValue == lowercase() } ?: AddChatVendor.OpenAI
}

private fun defaultQuickPrompts(): List<AddChatQuickPrompt> {
    return listOf(
        AddChatQuickPrompt(
            id = "summary",
            title = "总结当前页面",
            content = "请总结当前页面或当前上下文的关键信息，并给出下一步建议。",
        ),
        AddChatQuickPrompt(
            id = "review",
            title = "代码评审",
            content = "请按严重程度列出当前改动里的问题、风险和缺少的测试。",
        ),
        AddChatQuickPrompt(
            id = "sql",
            title = "生成 SQL",
            content = "请根据当前需求直接给出可执行的 SQL，并说明会影响哪些表。",
        ),
    )
}
