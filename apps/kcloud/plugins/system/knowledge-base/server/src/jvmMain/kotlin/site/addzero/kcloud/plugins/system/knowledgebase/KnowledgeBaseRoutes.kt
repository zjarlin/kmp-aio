package site.addzero.kcloud.plugins.system.knowledgebase

import io.ktor.server.routing.Route
import site.addzero.kcloud.plugins.system.knowledgebase.routes.generated.springktor.registerGeneratedSpringRoutes

/**
 * 统一挂载知识库后端路由。
 */
fun Route.knowledgeBaseRoutes() {
    registerGeneratedSpringRoutes()
}
