package site.addzero.kcloud.plugins.system.aichat.workbench

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_DEFAULT_SYSTEM_PROMPT
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_TRANSPORT_HTTP
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENAI
import site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatProviderConfigDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionDto

@Single
class AiChatWorkbenchState(
    private val remoteService: AiChatRemoteService,
) {
    val sessions = mutableStateListOf<AiChatSessionDto>()
    val messages = mutableStateListOf<AiChatMessageDto>()

    var selectedSessionId by mutableStateOf<Long?>(null)
        private set

    var serverBaseUrl by mutableStateOf("")
    var providerTransport by mutableStateOf(AI_CHAT_TRANSPORT_HTTP)
    var providerVendor by mutableStateOf(AI_CHAT_VENDOR_OPENAI)
    var providerBaseUrl by mutableStateOf("")
    var providerApiKey by mutableStateOf("")
    var providerModel by mutableStateOf("")
    var providerSystemPrompt by mutableStateOf(AI_CHAT_DEFAULT_SYSTEM_PROMPT)

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
            val loadedSessions = remoteService.listSessions(serverBaseUrl = serverBaseUrl)
            sessions.replaceAll(loadedSessions)
            syncSelectedSession(loadedSessions)
        }
    }

    suspend fun createSession() {
        runBusy("已创建新会话") {
            val created = remoteService.createSession(
                title = "新会话",
                serverBaseUrl = serverBaseUrl,
            )
            sessions.add(0, created)
            selectSession(created.id)
        }
    }

    suspend fun deleteSelectedSession() {
        val sessionId = selectedSessionId ?: return
        runBusy("已删除会话") {
            remoteService.deleteSession(
                sessionId = sessionId,
                serverBaseUrl = serverBaseUrl,
            )
            selectedSessionId = null
            refreshSessions()
        }
    }

    suspend fun selectSession(
        sessionId: Long,
    ) {
        selectedSessionId = sessionId
        messages.replaceAll(
            remoteService.listMessages(
                sessionId = sessionId,
                serverBaseUrl = serverBaseUrl,
            )
        )
    }

    suspend fun sendMessage() {
        val content = draftMessage.trim()
        if (content.isBlank()) {
            statusMessage = "消息不能为空"
            return
        }
        runBusy("消息已写入会话") {
            val sessionId = selectedSessionId ?: remoteService.createSession(
                title = "新会话",
                serverBaseUrl = serverBaseUrl,
            ).also { created ->
                sessions.add(0, created)
                selectedSessionId = created.id
            }.id
            val conversation = remoteService.sendMessage(
                sessionId = sessionId,
                content = content,
                serverBaseUrl = serverBaseUrl,
                provider = currentProviderConfig(),
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
        serverBaseUrl = backendUrl
        providerTransport = transport
        providerVendor = vendor
        this.providerBaseUrl = providerBaseUrl
        providerApiKey = apiKey
        providerModel = model
        providerSystemPrompt = systemPrompt
    }

    fun connectionConfig(): AiChatProviderConfigDto {
        return currentProviderConfig()
    }

    private fun currentProviderConfig(): AiChatProviderConfigDto {
        return AiChatProviderConfigDto(
            transport = providerTransport,
            vendor = providerVendor,
            baseUrl = providerBaseUrl,
            apiKey = providerApiKey,
            model = providerModel,
            systemPrompt = providerSystemPrompt,
        )
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

private fun <T> MutableList<T>.replaceAll(
    newItems: List<T>,
) {
    clear()
    addAll(newItems)
}
