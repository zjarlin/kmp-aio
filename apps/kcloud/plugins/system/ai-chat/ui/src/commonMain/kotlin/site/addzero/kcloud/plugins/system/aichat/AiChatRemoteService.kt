package site.addzero.kcloud.plugins.system.aichat

import org.koin.core.annotation.Single
import site.addzero.kcloud.system.api.*

@Single
class AiChatRemoteService {
    suspend fun listSessions(): List<AiChatSessionDto> {
        return KCloudSystemApiClient.aiChatApi.listAiChatSessions()
    }

    suspend fun createSession(
        title: String,
    ): AiChatSessionDto {
        return KCloudSystemApiClient.aiChatApi.createAiChatSession(
            AiChatSessionCreateRequest(title = title),
        )
    }

    suspend fun deleteSession(
        sessionId: Long,
    ) {
        KCloudSystemApiClient.aiChatApi.deleteAiChatSession(sessionId)
    }

    suspend fun listMessages(
        sessionId: Long,
    ): List<AiChatMessageDto> {
        return KCloudSystemApiClient.aiChatApi.listAiChatMessages(sessionId)
    }

    suspend fun sendMessage(
        sessionId: Long,
        content: String,
    ): AiChatConversationDto {
        return KCloudSystemApiClient.aiChatApi.sendAiChatMessage(
            sessionId = sessionId,
            request = AiChatMessageCreateRequest(content = content),
        )
    }
}
