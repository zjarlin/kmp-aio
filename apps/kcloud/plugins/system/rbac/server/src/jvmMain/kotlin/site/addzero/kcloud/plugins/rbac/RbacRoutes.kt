package site.addzero.kcloud.plugins.rbac

import io.ktor.server.response.*
import io.ktor.server.routing.*
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
