package site.addzero.kcloud.plugins.system.rbac

import io.ktor.server.routing.Route
import site.addzero.kcloud.plugins.system.rbac.routes.generated.springktor.registerGeneratedSpringRoutes

/**
 * 统一挂载 RBAC 与用户中心相关后端路由。
 */
fun Route.rbacRoutes() {
    registerGeneratedSpringRoutes()
}
