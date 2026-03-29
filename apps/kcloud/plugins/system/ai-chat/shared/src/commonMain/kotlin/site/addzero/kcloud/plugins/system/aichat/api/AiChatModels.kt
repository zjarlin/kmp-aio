package site.addzero.kcloud.plugins.system.aichat.api

import kotlinx.serialization.Serializable

@Serializable
data class AiChatSessionDto(
    val id: Long,
    val sessionKey: String,
    val title: String,
    val archived: Boolean = false,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class AiChatSessionCreateRequest(
    val title: String = "新会话",
)

@Serializable
data class AiChatMessageDto(
    val id: Long,
    val messageKey: String,
    val sessionId: Long,
    val role: String,
    val content: String,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class AiChatMessageCreateRequest(
    val content: String,
)

@Serializable
data class AiChatConversationDto(
    val session: AiChatSessionDto,
    val messages: List<AiChatMessageDto>,
)

@Serializable
data class AiChatDeleteResult(
    val ok: Boolean = true,
)
