package site.addzero.kcloud.plugins.system.aichat.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionCreateRequest
import site.addzero.kcloud.plugins.system.aichat.api.AiChatConversationDto
import site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageCreateRequest
import site.addzero.kcloud.plugins.system.aichat.api.AiChatDeleteResult

/**
 * 原始文件: site.addzero.kcloud.plugins.system.aichat.routes.AiChat.kt
 * 基础路径: 
 */
interface AiChatApi {

/**
 * listAiChatSessions
 * HTTP方法: GET
 * 路径: /api/system/ai-chat/sessions
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionDto>
 */
    @GET("/api/system/ai-chat/sessions")    suspend fun listAiChatSessions(): kotlin.collections.List<site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionDto>

/**
 * listAiChatMessages
 * HTTP方法: GET
 * 路径: /api/system/ai-chat/sessions/{sessionId}/messages
 * 参数:
 *   - sessionId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageDto>
 */
    @GET("/api/system/ai-chat/sessions/{sessionId}/messages")    suspend fun listAiChatMessages(
        @Path("sessionId") sessionId: kotlin.Long
    ): kotlin.collections.List<site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageDto>

/**
 * createAiChatSession
 * HTTP方法: POST
 * 路径: /api/system/ai-chat/sessions
 * 参数:
 *   - request: site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionDto
 */
    @POST("/api/system/ai-chat/sessions")    suspend fun createAiChatSession(
        @Body request: site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionCreateRequest
    ): site.addzero.kcloud.plugins.system.aichat.api.AiChatSessionDto

/**
 * sendAiChatMessage
 * HTTP方法: POST
 * 路径: /api/system/ai-chat/sessions/{sessionId}/messages
 * 参数:
 *   - sessionId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.aichat.api.AiChatConversationDto
 */
    @POST("/api/system/ai-chat/sessions/{sessionId}/messages")    suspend fun sendAiChatMessage(
        @Path("sessionId") sessionId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.aichat.api.AiChatMessageCreateRequest
    ): site.addzero.kcloud.plugins.system.aichat.api.AiChatConversationDto

/**
 * deleteAiChatSession
 * HTTP方法: DELETE
 * 路径: /api/system/ai-chat/sessions/{sessionId}
 * 参数:
 *   - sessionId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.system.aichat.api.AiChatDeleteResult
 */
    @DELETE("/api/system/ai-chat/sessions/{sessionId}")    suspend fun deleteAiChatSession(
        @Path("sessionId") sessionId: kotlin.Long
    ): site.addzero.kcloud.plugins.system.aichat.api.AiChatDeleteResult

}