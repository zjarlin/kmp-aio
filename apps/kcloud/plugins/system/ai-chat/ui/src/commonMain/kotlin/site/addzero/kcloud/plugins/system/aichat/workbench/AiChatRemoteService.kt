package site.addzero.kcloud.plugins.system.aichat.workbench

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.mp.KoinPlatform
import org.koin.core.annotation.Single
import site.addzero.core.network.HttpClientFactory
import site.addzero.kcloud.plugins.system.aichat.api.*

@Single
class AiChatRemoteService {
    private val httpClientFactory: HttpClientFactory
        get() = KoinPlatform.getKoin().get()

    private fun resolveApi(
        serverBaseUrl: String,
    ): AiChatApi {
        return if (serverBaseUrl.isBlank()) {
            Apis.aiChatApi
        } else {
            Ktorfit.Builder()
                .baseUrl(serverBaseUrl.trim().trimEnd('/') + "/")
                .httpClient(httpClientFactory.get("kcloud-system"))
                .build()
                .createAiChatApi()
        }
    }

    suspend fun listSessions(
        serverBaseUrl: String,
    ): List<AiChatSessionDto> {
        return resolveApi(serverBaseUrl).listAiChatSessions()
    }

    suspend fun createSession(
        title: String,
        serverBaseUrl: String,
    ): AiChatSessionDto {
        return resolveApi(serverBaseUrl).createAiChatSession(
            AiChatSessionCreateRequest(title = title),
        )
    }

    suspend fun deleteSession(
        sessionId: Long,
        serverBaseUrl: String,
    ) {
        resolveApi(serverBaseUrl).deleteAiChatSession(sessionId)
    }

    suspend fun listMessages(
        sessionId: Long,
        serverBaseUrl: String,
    ): List<AiChatMessageDto> {
        return resolveApi(serverBaseUrl).listAiChatMessages(sessionId)
    }

    suspend fun sendMessage(
        sessionId: Long,
        content: String,
        serverBaseUrl: String,
        provider: AiChatProviderConfigDto,
    ): AiChatConversationDto {
        return resolveApi(serverBaseUrl).sendAiChatMessage(
            sessionId = sessionId,
            request = AiChatMessageCreateRequest(
                content = content,
                provider = provider,
            ),
        )
    }
}
