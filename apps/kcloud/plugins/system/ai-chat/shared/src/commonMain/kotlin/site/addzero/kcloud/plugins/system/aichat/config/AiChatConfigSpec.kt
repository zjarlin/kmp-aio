package site.addzero.kcloud.plugins.system.aichat.config

import site.addzero.kcloud.plugins.system.aichat.api.AI_CHAT_DEFAULT_SYSTEM_PROMPT
import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud.ai",
    objectName = "AiChatConfigKeys",
)
interface AiChatConfigSpec {
    @ConfigCenterItem(
        key = "transport",
        comment = "AI Chat 默认传输协议，例如 http、acp。",
        defaultValue = "http",
    )
    val transport: String

    @ConfigCenterItem(
        key = "vendor",
        comment = "AI Chat 默认模型厂商，例如 openai、openrouter、ollama。",
        defaultValue = "openai",
    )
    val vendor: String

    @ConfigCenterItem(
        key = "apiUrl",
        comment = "AI Chat 默认模型服务 base URL。",
        defaultValue = "https://api.openai.com/v1",
    )
    val apiUrl: String

    @ConfigCenterItem(
        key = "apiKey",
        comment = "AI Chat 默认 API Key。对 Ollama 等本地 provider 可留空。",
    )
    val apiKey: String

    @ConfigCenterItem(
        key = "model",
        comment = "AI Chat 默认模型标识。",
        defaultValue = "gpt-4.1-mini",
    )
    val model: String

    @ConfigCenterItem(
        key = "systemPrompt",
        comment = "AI Chat 默认系统提示词。",
        defaultValue = AI_CHAT_DEFAULT_SYSTEM_PROMPT,
    )
    val systemPrompt: String

    @ConfigCenterItem(
        key = "preset.openai.apiUrl",
        comment = "OpenAI 预设 API URL。",
        defaultValue = "https://api.openai.com/v1",
    )
    val presetOpenaiApiUrl: String

    @ConfigCenterItem(
        key = "preset.openai.model",
        comment = "OpenAI 预设模型。",
        defaultValue = "gpt-4.1-mini",
    )
    val presetOpenaiModel: String

    @ConfigCenterItem(
        key = "preset.anthropic.apiUrl",
        comment = "Anthropic 预设 API URL。",
        defaultValue = "https://api.anthropic.com/v1",
    )
    val presetAnthropicApiUrl: String

    @ConfigCenterItem(
        key = "preset.anthropic.model",
        comment = "Anthropic 预设模型。",
        defaultValue = "claude-3-7-sonnet-latest",
    )
    val presetAnthropicModel: String

    @ConfigCenterItem(
        key = "preset.deepseek.apiUrl",
        comment = "DeepSeek 预设 API URL。",
        defaultValue = "https://api.deepseek.com",
    )
    val presetDeepseekApiUrl: String

    @ConfigCenterItem(
        key = "preset.deepseek.model",
        comment = "DeepSeek 预设模型。",
        defaultValue = "deepseek-chat",
    )
    val presetDeepseekModel: String

    @ConfigCenterItem(
        key = "preset.openrouter.apiUrl",
        comment = "OpenRouter 预设 API URL。",
        defaultValue = "https://openrouter.ai/api/v1",
    )
    val presetOpenrouterApiUrl: String

    @ConfigCenterItem(
        key = "preset.openrouter.model",
        comment = "OpenRouter 预设模型。",
        defaultValue = "openai/gpt-4.1-mini",
    )
    val presetOpenrouterModel: String

    @ConfigCenterItem(
        key = "preset.gemini.apiUrl",
        comment = "Gemini 预设 API URL。",
        defaultValue = "https://generativelanguage.googleapis.com",
    )
    val presetGeminiApiUrl: String

    @ConfigCenterItem(
        key = "preset.gemini.model",
        comment = "Gemini 预设模型。",
        defaultValue = "gemini-2.5-flash",
    )
    val presetGeminiModel: String

    @ConfigCenterItem(
        key = "preset.ollama.apiUrl",
        comment = "Ollama 预设 API URL。",
        defaultValue = "http://localhost:11434",
    )
    val presetOllamaApiUrl: String

    @ConfigCenterItem(
        key = "preset.ollama.model",
        comment = "Ollama 预设模型。",
        defaultValue = "llama3.2",
    )
    val presetOllamaModel: String
}
