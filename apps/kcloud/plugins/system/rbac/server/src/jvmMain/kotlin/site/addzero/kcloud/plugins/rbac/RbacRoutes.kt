package site.addzero.kcloud.plugins.rbac

import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.addzero.kcloud.plugins.rbac.routes.createRbacRole
import site.addzero.kcloud.plugins.rbac.routes.deleteRbacRole
import site.addzero.kcloud.plugins.rbac.routes.getCurrentUserProfile
import site.addzero.kcloud.plugins.rbac.routes.listRbacRoles
import site.addzero.kcloud.plugins.rbac.routes.saveCurrentUserProfile
import site.addzero.kcloud.plugins.rbac.routes.updateRbacRole
import site.addzero.springktor.runtime.requirePathVariable
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
    get("/api/system/rbac/roles") {
        call.respond(listRbacRoles())
    }
    post("/api/system/rbac/roles") {
        call.respond(createRbacRole(call.requireRequestBody()))
    }
    put("/api/system/rbac/roles/{roleId}") {
        call.respond(
            updateRbacRole(
                roleId = call.requirePathVariable("roleId"),
                request = call.requireRequestBody(),
            ),
        )
    }
    delete("/api/system/rbac/roles/{roleId}") {
        call.respond(deleteRbacRole(call.requirePathVariable("roleId")))
    }
}
