package site.addzero.kcloud.plugins.system.aichat

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.aichat.api.*

@Single
class AiChatRemoteService {
    private fun api(
        serverBaseUrl: String,
    ): AiChatApi {
        return if (serverBaseUrl.isBlank()) {
            AiChatApiClient.aiChatApi
        } else {
            AiChatApiClient.createApi(serverBaseUrl)
        }
    }

    suspend fun listSessions(
        serverBaseUrl: String,
    ): List<AiChatSessionDto> {
        return api(serverBaseUrl).listAiChatSessions()
    }

    suspend fun createSession(
        title: String,
        serverBaseUrl: String,
    ): AiChatSessionDto {
        return api(serverBaseUrl).createAiChatSession(
            AiChatSessionCreateRequest(title = title),
        )
    }

    suspend fun deleteSession(
        sessionId: Long,
        serverBaseUrl: String,
    ) {
        api(serverBaseUrl).deleteAiChatSession(sessionId)
    }

    suspend fun listMessages(
        sessionId: Long,
        serverBaseUrl: String,
    ): List<AiChatMessageDto> {
        return api(serverBaseUrl).listAiChatMessages(sessionId)
    }

    suspend fun sendMessage(
        sessionId: Long,
        content: String,
        serverBaseUrl: String,
        provider: AiChatProviderConfigDto,
    ): AiChatConversationDto {
        return api(serverBaseUrl).sendAiChatMessage(
            sessionId = sessionId,
            request = AiChatMessageCreateRequest(
                content = content,
                provider = provider,
            ),
        )
    }
}
