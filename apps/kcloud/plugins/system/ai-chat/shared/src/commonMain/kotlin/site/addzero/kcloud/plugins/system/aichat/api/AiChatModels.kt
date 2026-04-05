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
    val provider: AiChatProviderConfigDto = AiChatProviderConfigDto(),
)

@Serializable
data class AiChatProviderConfigDto(
    val transport: String = "",
    val vendor: String = "",
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val systemPrompt: String = "",
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

const val AI_CHAT_TRANSPORT_HTTP = "http"
const val AI_CHAT_TRANSPORT_ACP = "acp"

const val AI_CHAT_VENDOR_OPENAI = "openai"
const val AI_CHAT_VENDOR_OPENAI_COMPATIBLE = "openai-compatible"
const val AI_CHAT_VENDOR_OPENROUTER = "openrouter"
const val AI_CHAT_VENDOR_DEEPSEEK = "deepseek"
const val AI_CHAT_VENDOR_ANTHROPIC = "anthropic"
const val AI_CHAT_VENDOR_GOOGLE = "google"
const val AI_CHAT_VENDOR_GEMINI = "gemini"
const val AI_CHAT_VENDOR_OLLAMA = "ollama"
