package site.addzero.kcloud.plugins.system.aichat

import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.addzero.kcloud.plugins.system.aichat.routes.*
import site.addzero.springktor.runtime.requirePathVariable
import site.addzero.springktor.runtime.requireRequestBody

/**
 * 统一挂载 AI 对话后端路由。
 */
fun Route.aiChatRoutes() {
    get("/api/system/ai-chat/sessions") {
        call.respond(listAiChatSessions())
    }
    post("/api/system/ai-chat/sessions") {
        call.respond(createAiChatSession(call.requireRequestBody()))
    }
    delete("/api/system/ai-chat/sessions/{sessionId}") {
        call.respond(deleteAiChatSession(call.requirePathVariable("sessionId")))
    }
    get("/api/system/ai-chat/sessions/{sessionId}/messages") {
        call.respond(listAiChatMessages(call.requirePathVariable("sessionId")))
    }
    post("/api/system/ai-chat/sessions/{sessionId}/messages") {
        call.respond(
            sendAiChatMessage(
                sessionId = call.requirePathVariable("sessionId"),
                request = call.requireRequestBody(),
            ),
        )
    }
}
