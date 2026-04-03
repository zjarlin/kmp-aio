package site.addzero.kcloud.plugins.system.aichat.provider

/**
 * 发给模型网关的标准化请求。
 */
data class AiChatCompletionRequest(
    val transport: String,
    val vendor: String,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val systemPrompt: String,
    val messages: List<AiChatTurn>,
)

/**
 * 历史消息统一抽象。
 */
data class AiChatTurn(
    val role: String,
    val content: String,
)
