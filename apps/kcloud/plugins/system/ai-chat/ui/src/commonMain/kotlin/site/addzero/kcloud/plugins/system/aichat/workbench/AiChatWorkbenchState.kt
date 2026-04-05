package site.addzero.kcloud.plugins.system.aichat.workbench

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_DEFAULT_SYSTEM_PROMPT
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_TRANSPORT_HTTP
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENAI
import site.addzero.kcloud.plugins.system.aichat.api.Apis
import site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageCreateRequest
import site.addzero.kcloud.plugins.system.aichat.api.AiChatProviderConfigDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionCreateRequest
import site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionDto

@Single
class AiChatWorkbenchState {
    val sessions = mutableStateListOf<AiChatSessionDto>()
    val messages = mutableStateListOf<AiChatMessageDto>()

    var selectedSessionId by mutableStateOf<Long?>(null)
        private set

    var connection by mutableStateOf(AiChatConnectionState())
        private set

    var draftMessage by mutableStateOf("")
    var statusMessage by mutableStateOf("")
        private set
    var isBusy by mutableStateOf(false)
        private set

    private var loaded = false

    suspend fun ensureLoaded() {
        if (loaded) {
            return
        }
        refreshSessions()
        loaded = true
    }

    suspend fun refreshSessions() {
        runBusy("已刷新会话") {
            val loadedSessions = Apis.aiChatApi.listAiChatSessions()
            sessions.replaceAll(loadedSessions)
            syncSelectedSession(loadedSessions)
        }
    }

    suspend fun createSession() {
        runBusy("已创建新会话") {
            val created = Apis.aiChatApi.createAiChatSession(
                AiChatSessionCreateRequest(title = "新会话"),
            )
            sessions.add(0, created)
            selectSession(created.id)
        }
    }

    suspend fun deleteSelectedSession() {
        val sessionId = selectedSessionId ?: return
        runBusy("已删除会话") {
            Apis.aiChatApi.deleteAiChatSession(sessionId)
            selectedSessionId = null
            refreshSessions()
        }
    }

    suspend fun selectSession(
        sessionId: Long,
    ) {
        selectedSessionId = sessionId
        messages.replaceAll(
            Apis.aiChatApi.listAiChatMessages(sessionId)
        )
    }

    suspend fun sendMessage() {
        val content = draftMessage.trim()
        if (content.isBlank()) {
            statusMessage = "消息不能为空"
            return
        }
        runBusy("消息已写入会话") {
            val sessionId = selectedSessionId ?: Apis.aiChatApi.createAiChatSession(
                AiChatSessionCreateRequest(title = "新会话"),
            ).also { created ->
                sessions.add(0, created)
                selectedSessionId = created.id
            }.id
            val conversation = Apis.aiChatApi.sendAiChatMessage(
                sessionId = sessionId,
                request = AiChatMessageCreateRequest(
                    content = content,
                    provider = currentProviderConfig(),
                ),
            )
            upsertSession(conversation.session)
            messages.replaceAll(conversation.messages)
            selectedSessionId = conversation.session.id
            draftMessage = ""
        }
    }

    fun updateDraftMessage(
        value: String,
    ) {
        draftMessage = value
    }

    fun applyQuickPrompt(
        prompt: String,
    ) {
        draftMessage = prompt
    }

    fun updateConnection(
        backendUrl: String,
        transport: String,
        vendor: String,
        providerBaseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
    ) {
        connection = connection.copy(
            backendUrl = backendUrl,
            transport = transport,
            vendor = vendor,
            providerBaseUrl = providerBaseUrl,
            apiKey = apiKey,
            model = model,
            systemPrompt = systemPrompt,
        )
        if (backendUrl.isNotBlank()) {
            statusMessage = "AI 助手已统一复用全局 KCloud 后端，独立 backendUrl 配置不再单独建连。"
        }
    }

    fun connectionConfig(): AiChatProviderConfigDto {
        return connection.toProviderConfig()
    }

    private fun currentProviderConfig(): AiChatProviderConfigDto {
        return connection.toProviderConfig()
    }

    private fun upsertSession(
        session: AiChatSessionDto,
    ) {
        val index = sessions.indexOfFirst { it.id == session.id }
        if (index >= 0) {
            sessions[index] = session
        } else {
            sessions.add(0, session)
        }
    }

    private suspend fun syncSelectedSession(
        loadedSessions: List<AiChatSessionDto>,
    ) {
        val nextId = selectedSessionId?.takeIf { currentId ->
            loadedSessions.any { session -> session.id == currentId }
        } ?: loadedSessions.firstOrNull()?.id
        if (nextId == null) {
            selectedSessionId = null
            messages.clear()
            return
        }
        selectSession(nextId)
    }

    private suspend fun runBusy(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        runCatching {
            block()
        }.onSuccess {
            statusMessage = successMessage
        }.onFailure { throwable ->
            statusMessage = throwable.message ?: "操作失败"
        }
        isBusy = false
    }
}

data class AiChatConnectionState(
    val backendUrl: String = "",
    val transport: String = AI_CHAT_TRANSPORT_HTTP,
    val vendor: String = AI_CHAT_VENDOR_OPENAI,
    val providerBaseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val systemPrompt: String = AI_CHAT_DEFAULT_SYSTEM_PROMPT,
)

private fun AiChatConnectionState.toProviderConfig(): AiChatProviderConfigDto {
    return AiChatProviderConfigDto(
        transport = transport,
        vendor = vendor,
        baseUrl = providerBaseUrl,
        apiKey = apiKey,
        model = model,
        systemPrompt = systemPrompt,
    )
}

private fun <T> MutableList<T>.replaceAll(
    newItems: List<T>,
) {
    clear()
    addAll(newItems)
}
