package site.addzero.kcloud.plugins.system.aichat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.system.api.AiChatMessageDto
import site.addzero.kcloud.system.api.AiChatSessionDto

@Single
class AiChatWorkbenchState(
    private val remoteService: AiChatRemoteService,
) {
    val sessions = mutableStateListOf<AiChatSessionDto>()
    val messages = mutableStateListOf<AiChatMessageDto>()

    var selectedSessionId by mutableStateOf<Long?>(null)
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
            val loadedSessions = remoteService.listSessions()
            sessions.replaceAll(loadedSessions)
            val nextId = selectedSessionId?.takeIf { currentId ->
                loadedSessions.any { session -> session.id == currentId }
            } ?: loadedSessions.firstOrNull()?.id
            if (nextId == null) {
                selectedSessionId = null
                messages.clear()
            } else {
                selectSession(nextId)
            }
        }
    }

    suspend fun createSession() {
        runBusy("已创建新会话") {
            val created = remoteService.createSession("新会话")
            sessions.add(0, created)
            selectSession(created.id)
        }
    }

    suspend fun deleteSelectedSession() {
        val sessionId = selectedSessionId ?: return
        runBusy("已删除会话") {
            remoteService.deleteSession(sessionId)
            selectedSessionId = null
            refreshSessions()
        }
    }

    suspend fun selectSession(
        sessionId: Long,
    ) {
        selectedSessionId = sessionId
        val loadedMessages = remoteService.listMessages(sessionId)
        messages.replaceAll(loadedMessages)
    }

    suspend fun sendMessage() {
        val content = draftMessage.trim()
        if (content.isBlank()) {
            statusMessage = "消息不能为空"
            return
        }
        runBusy("消息已写入会话") {
            val sessionId = selectedSessionId ?: remoteService.createSession("新会话").also { created ->
                sessions.add(0, created)
                selectedSessionId = created.id
            }.id
            val conversation = remoteService.sendMessage(sessionId, content)
            upsertSession(conversation.session)
            messages.replaceAll(conversation.messages)
            selectedSessionId = conversation.session.id
            draftMessage = ""
        }
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
