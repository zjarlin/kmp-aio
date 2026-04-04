package site.addzero.kcloud.plugins.system.aichat.workbench

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import site.addzero.component.chat.AddChatConnectionConfig
import site.addzero.component.chat.AddChatMessageItem
import site.addzero.component.chat.AddChatMessageRole
import site.addzero.component.chat.AddChatPanelActions
import site.addzero.component.chat.AddChatPanelState
import site.addzero.component.chat.AddChatQuickPrompt
import site.addzero.component.chat.AddChatSessionItem
import site.addzero.component.chat.AddChatTransport
import site.addzero.component.chat.AddChatVendor
class AiChatPanelPresenter(
    private val state: AiChatWorkbenchState,
    private val scope: CoroutineScope,
) {
    val panelState: AddChatPanelState
        get() = AddChatPanelState(
            title = "AI 助手",
            subtitle = "支持配置后台 URL、API Key、厂商与模型参数",
            sessions = state.sessions.map { session ->
                AddChatSessionItem(
                    id = session.id.toString(),
                    title = session.title,
                    subtitle = "更新时间：${session.updateTimeMillis ?: session.createTimeMillis}",
                )
            },
            selectedSessionId = state.selectedSessionId?.toString(),
            messages = state.messages.map { message ->
                AddChatMessageItem(
                    id = message.id.toString(),
                    role = message.role.toChatRole(),
                    content = message.content,
                    timestampLabel = (message.updateTimeMillis ?: message.createTimeMillis).toString(),
                )
            },
            quickPrompts = defaultQuickPrompts(),
            input = state.draftMessage,
            connection = AddChatConnectionConfig(
                backendUrl = state.serverBaseUrl,
                transport = state.connectionConfig().transport.toChatTransport(),
                vendor = state.connectionConfig().vendor.toChatVendor(),
                providerBaseUrl = state.connectionConfig().baseUrl,
                apiKey = state.connectionConfig().apiKey,
                model = state.connectionConfig().model,
                systemPrompt = state.connectionConfig().systemPrompt,
            ),
            showConnectionEditor = true,
            isSending = state.isBusy,
            isLoadingMessages = false,
            statusText = state.statusMessage,
        )

    val panelActions: AddChatPanelActions = AddChatPanelActions(
        onCreateSession = {
            scope.launch {
                state.createSession()
            }
        },
        onDeleteSession = {
            scope.launch {
                state.deleteSelectedSession()
            }
        },
        onSelectSession = { sessionId ->
            sessionId.toLongOrNull()?.let { id ->
                scope.launch {
                    state.selectSession(id)
                }
            }
        },
        onInputChange = state::updateDraftMessage,
        onSend = {
            scope.launch {
                state.sendMessage()
            }
        },
        onConnectionChange = { config ->
            state.updateConnection(
                backendUrl = config.backendUrl,
                transport = config.transport.wireValue,
                vendor = config.vendor.wireValue,
                providerBaseUrl = config.providerBaseUrl,
                apiKey = config.apiKey,
                model = config.model,
                systemPrompt = config.systemPrompt,
            )
        },
        onUsePrompt = { prompt ->
            state.applyQuickPrompt(prompt.content)
        },
        onToggleConnectionEditor = {},
    )

    suspend fun ensureLoaded() {
        state.ensureLoaded()
    }
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
