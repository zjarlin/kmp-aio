package site.addzero.kcloud.plugins.system.aichat

import io.ktor.server.routing.Route
import site.addzero.kcloud.plugins.system.aichat.routes.generated.springktor.registerGeneratedSpringRoutes

/**
 * 统一挂载 AI 对话后端路由。
 */
fun Route.aiChatRoutes() {
    registerGeneratedSpringRoutes()
}
