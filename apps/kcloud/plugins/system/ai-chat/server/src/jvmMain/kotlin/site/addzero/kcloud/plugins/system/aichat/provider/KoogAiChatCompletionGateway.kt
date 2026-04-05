package site.addzero.kcloud.plugins.system.aichat.provider

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekClientSettings
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.google.GoogleClientSettings
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openrouter.OpenRouterClientSettings
import ai.koog.prompt.executor.clients.openrouter.OpenRouterLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_TRANSPORT_ACP
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_TRANSPORT_HTTP
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_ANTHROPIC
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_DEEPSEEK
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_GEMINI
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_GOOGLE
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OLLAMA
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENAI
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENAI_COMPATIBLE
import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_VENDOR_OPENROUTER

/**
 * 基于 Koog 的模型网关实现。
 */
@Single
class KoogAiChatCompletionGateway : AiChatCompletionGateway {
    /**
     * 共享 HTTP 客户端，避免每次请求重复建连。
     */
    private val baseClient = HttpClient(CIO)

    override suspend fun complete(
        request: AiChatCompletionRequest,
    ): String {
        return when (request.transport.normalizeTransport()) {
            AI_CHAT_TRANSPORT_HTTP -> completeByHttp(request)
            AI_CHAT_TRANSPORT_ACP -> completeByAcp(request)
            else -> error("不支持的传输协议: ${request.transport}")
        }
    }

    private suspend fun completeByHttp(
        request: AiChatCompletionRequest,
    ): String {
        val client = createClient(request)
        val executor = SingleLLMPromptExecutor(client)
        val prompt = prompt(id = "ai-chat-${request.vendor}-${request.model.ifBlank { "default" }}") {
            system(request.systemPrompt.requireNonBlank("systemPrompt"))
            request.messages.forEach { turn ->
                when (turn.role.normalizeRole()) {
                    "system" -> system(turn.content)
                    "assistant" -> assistant(turn.content)
                    else -> user(turn.content)
                }
            }
        }
        val response = executor.execute(
            prompt = prompt,
            model = createModel(request),
            tools = emptyList(),
        ).firstOrNull()
            ?: error("模型没有返回任何内容")
        return response.content.trim().ifBlank { "模型返回了空响应" }
    }

    private suspend fun completeByAcp(
        request: AiChatCompletionRequest,
    ): String {
        error(
            "ACP transport 已预留在网关分发层，但当前部署还没有接入 ACP runtime。请先改用 HTTP transport，或继续补 ACP adapter。",
        )
    }

    private fun createClient(
        request: AiChatCompletionRequest,
    ): LLMClient {
        val vendor = request.vendor.normalizeVendor()
        val baseUrl = request.baseUrl.requireNonBlank("baseUrl").trimEnd('/')
        return when (vendor) {
            AI_CHAT_VENDOR_OPENAI,
            AI_CHAT_VENDOR_OPENAI_COMPATIBLE,
            -> OpenAILLMClient(
                apiKey = request.requiredApiKey(vendor),
                settings = OpenAIClientSettings(baseUrl = baseUrl),
                baseClient = baseClient,
            )

            AI_CHAT_VENDOR_OPENROUTER -> OpenRouterLLMClient(
                apiKey = request.requiredApiKey(vendor),
                settings = OpenRouterClientSettings(baseUrl = baseUrl),
                baseClient = baseClient,
            )

            AI_CHAT_VENDOR_DEEPSEEK -> DeepSeekLLMClient(
                apiKey = request.requiredApiKey(vendor),
                settings = DeepSeekClientSettings(baseUrl = baseUrl),
                baseClient = baseClient,
            )

            AI_CHAT_VENDOR_ANTHROPIC -> AnthropicLLMClient(
                apiKey = request.requiredApiKey(vendor),
                settings = AnthropicClientSettings(baseUrl = baseUrl),
                baseClient = baseClient,
            )

            AI_CHAT_VENDOR_GOOGLE,
            AI_CHAT_VENDOR_GEMINI,
            -> GoogleLLMClient(
                apiKey = request.requiredApiKey(vendor),
                settings = GoogleClientSettings(baseUrl = baseUrl),
                baseClient = baseClient,
            )

            AI_CHAT_VENDOR_OLLAMA -> OllamaClient(
                baseUrl = baseUrl,
                baseClient = baseClient,
            )

            else -> error("不支持的模型厂商: ${request.vendor}")
        }
    }

    private fun createModel(
        request: AiChatCompletionRequest,
    ): LLModel {
        val vendor = request.vendor.normalizeVendor()
        return LLModel(
            provider = vendor.toProvider(),
            id = request.model.requireNonBlank("model"),
            capabilities = DEFAULT_CAPABILITIES,
            contextLength = 128_000,
            maxOutputTokens = 8_192,
        )
    }
}

private val DEFAULT_CAPABILITIES = listOf(
    LLMCapability.Temperature,
    LLMCapability.Completion,
    LLMCapability.Tools,
    LLMCapability.ToolChoice,
    LLMCapability.MultipleChoices,
    LLMCapability.Schema.JSON.Basic,
    LLMCapability.Schema.JSON.Standard,
)

private fun String.normalizeTransport(): String {
    return trim().lowercase().ifBlank { error("AiChatCompletionRequest.transport 不能为空。") }
}

private fun String.normalizeVendor(): String {
    return trim().lowercase().ifBlank { error("AiChatCompletionRequest.vendor 不能为空。") }
}

private fun String.normalizeRole(): String {
    return trim().lowercase().ifBlank { "user" }
}

private fun String.toProvider(): LLMProvider {
    return when (this) {
        AI_CHAT_VENDOR_OPENAI,
        AI_CHAT_VENDOR_OPENAI_COMPATIBLE,
        -> LLMProvider.OpenAI

        AI_CHAT_VENDOR_OPENROUTER -> LLMProvider.OpenRouter
        AI_CHAT_VENDOR_DEEPSEEK -> LLMProvider.DeepSeek
        AI_CHAT_VENDOR_ANTHROPIC -> LLMProvider.Anthropic
        AI_CHAT_VENDOR_GOOGLE,
        AI_CHAT_VENDOR_GEMINI,
        -> LLMProvider.Google

        AI_CHAT_VENDOR_OLLAMA -> LLMProvider.Ollama
        else -> error("不支持的模型厂商: $this")
    }
}

private fun AiChatCompletionRequest.requiredApiKey(
    vendor: String,
): String {
    if (vendor == AI_CHAT_VENDOR_OLLAMA) {
        return apiKey
    }
    return apiKey.trim().ifBlank {
        error("厂商 $vendor 缺少 API Key")
    }
}

private fun String.requireNonBlank(
    fieldName: String,
): String {
    return trim().ifBlank {
        error("AiChatCompletionRequest.$fieldName 不能为空。")
    }
}
