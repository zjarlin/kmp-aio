package site.addzero.kcloud.plugins.system.aichat

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.aichat.api.*

@Single
class AiChatRemoteService {
    suspend fun listSessions(): List<AiChatSessionDto> {
        return AiChatApiClient.aiChatApi.listAiChatSessions()
    }

    suspend fun createSession(
        title: String,
    ): AiChatSessionDto {
        return AiChatApiClient.aiChatApi.createAiChatSession(
            AiChatSessionCreateRequest(title = title),
        )
    }

    suspend fun deleteSession(
        sessionId: Long,
    ) {
        AiChatApiClient.aiChatApi.deleteAiChatSession(sessionId)
    }

    suspend fun listMessages(
        sessionId: Long,
    ): List<AiChatMessageDto> {
        return AiChatApiClient.aiChatApi.listAiChatMessages(sessionId)
    }

    suspend fun sendMessage(
        sessionId: Long,
        content: String,
    ): AiChatConversationDto {
        return AiChatApiClient.aiChatApi.sendAiChatMessage(
            sessionId = sessionId,
            request = AiChatMessageCreateRequest(content = content),
        )
    }
}
