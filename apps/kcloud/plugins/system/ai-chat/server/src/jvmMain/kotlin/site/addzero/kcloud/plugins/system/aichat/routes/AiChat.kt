package site.addzero.kcloud.plugins.system.aichat.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.*
import site.addzero.kcloud.plugins.system.aichat.AiChatService
import site.addzero.kcloud.system.api.*

/**
 * AI 对话服务端路由定义，同时作为客户端 API 生成源。
 */
/**
 * 列出 AI 对话会话。
 */
@GetMapping("/api/system/ai-chat/sessions")
fun listAiChatSessions(): List<AiChatSessionDto> {
    return service().listSessions()
}

/**
 * 新建 AI 对话会话。
 */
@PostMapping("/api/system/ai-chat/sessions")
fun createAiChatSession(
    @RequestBody request: AiChatSessionCreateRequest,
): AiChatSessionDto {
    return service().createSession(request.title)
}

/**
 * 删除指定 AI 会话。
 */
@DeleteMapping("/api/system/ai-chat/sessions/{sessionId}")
fun deleteAiChatSession(
    @PathVariable("sessionId") sessionId: Long,
): AiChatDeleteResult {
    return service().deleteSession(sessionId)
}

/**
 * 读取指定会话消息。
 */
@GetMapping("/api/system/ai-chat/sessions/{sessionId}/messages")
fun listAiChatMessages(
    @PathVariable("sessionId") sessionId: Long,
): List<AiChatMessageDto> {
    return service().listMessages(sessionId)
}

/**
 * 写入消息并返回当前完整会话。
 */
@PostMapping("/api/system/ai-chat/sessions/{sessionId}/messages")
fun sendAiChatMessage(
    @PathVariable("sessionId") sessionId: Long,
    @RequestBody request: AiChatMessageCreateRequest,
): AiChatConversationDto {
    return service().sendMessage(sessionId, request.content)
}

private fun service(): AiChatService {
    return KoinPlatform.getKoin().get()
}
