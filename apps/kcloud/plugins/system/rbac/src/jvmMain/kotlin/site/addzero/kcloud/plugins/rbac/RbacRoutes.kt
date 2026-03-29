package site.addzero.kcloud.plugins.rbac

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import site.addzero.kcloud.plugins.rbac.routes.getCurrentUserProfile
import site.addzero.kcloud.plugins.rbac.routes.saveCurrentUserProfile
import site.addzero.springktor.runtime.requireRequestBody

/**
 * 统一挂载 RBAC 与用户中心相关后端路由。
 */
fun Route.rbacRoutes() {
    get("/api/system/user/profile") {
        call.respond(getCurrentUserProfile())
    }
    put("/api/system/user/profile") {
        call.respond(saveCurrentUserProfile(call.requireRequestBody()))
    }
}
