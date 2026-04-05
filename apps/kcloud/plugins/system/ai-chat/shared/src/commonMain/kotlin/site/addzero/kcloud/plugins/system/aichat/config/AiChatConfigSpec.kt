package site.addzero.kcloud.plugins.system.aichat.config

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
        required = true,
    )
    val transport: String

    @ConfigCenterItem(
        key = "vendor",
        comment = "AI Chat 默认模型厂商，例如 openai、openrouter、ollama。",
        required = true,
    )
    val vendor: String

    @ConfigCenterItem(
        key = "apiUrl",
        comment = "AI Chat 默认模型服务 base URL。",
        required = true,
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
        required = true,
    )
    val model: String

    @ConfigCenterItem(
        key = "systemPrompt",
        comment = "AI Chat 默认系统提示词。",
        required = true,
    )
    val systemPrompt: String
}
