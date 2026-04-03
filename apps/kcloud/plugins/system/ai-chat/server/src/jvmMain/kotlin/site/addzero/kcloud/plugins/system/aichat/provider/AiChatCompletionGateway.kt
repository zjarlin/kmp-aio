package site.addzero.kcloud.plugins.system.aichat.provider

/**
 * 模型网关统一入口，屏蔽不同厂商和传输协议的差异。
 */
interface AiChatCompletionGateway {
    suspend fun complete(
        request: AiChatCompletionRequest,
    ): String
}
