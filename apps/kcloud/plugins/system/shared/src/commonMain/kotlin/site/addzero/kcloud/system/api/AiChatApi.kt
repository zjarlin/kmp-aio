package site.addzero.kcloud.system.api

import de.jensklingenberg.ktorfit.http.*

interface AiChatApi {
    @GET("api/system/ai-chat/sessions")
    suspend fun listSessions(): List<AiChatSessionDto>

    @Headers("Content-Type: application/json")
    @POST("api/system/ai-chat/sessions")
    suspend fun createSession(@Body request: AiChatSessionCreateRequest): AiChatSessionDto

    @DELETE("api/system/ai-chat/sessions/{sessionId}")
    suspend fun deleteSession(@Path("sessionId") sessionId: Long): AiChatDeleteResult

    @GET("api/system/ai-chat/sessions/{sessionId}/messages")
    suspend fun listMessages(@Path("sessionId") sessionId: Long): List<AiChatMessageDto>

    @Headers("Content-Type: application/json")
    @POST("api/system/ai-chat/sessions/{sessionId}/messages")
    suspend fun sendMessage(
        @Path("sessionId") sessionId: Long,
        @Body request: AiChatMessageCreateRequest,
    ): AiChatConversationDto
}
