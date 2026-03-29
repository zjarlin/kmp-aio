package site.addzero.kcloud.plugins.system.rbac

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import site.addzero.kcloud.plugins.system.rbac.routes.createRbacRole
import site.addzero.kcloud.plugins.system.rbac.routes.deleteRbacRole
import site.addzero.kcloud.plugins.system.rbac.routes.getCurrentUserProfile
import site.addzero.kcloud.plugins.system.rbac.routes.listRbacRoles
import site.addzero.kcloud.plugins.system.rbac.routes.saveCurrentUserProfile
import site.addzero.kcloud.plugins.system.rbac.routes.updateRbacRole
import site.addzero.springktor.runtime.requirePathVariable
import site.addzero.springktor.runtime.requireRequestBody

/**
 * 统一挂载 RBAC 与用户中心相关后端路由。
 */
fun Route.rbacRoutes() {
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
    get("/api/system/user/profile") {
        call.respond(getCurrentUserProfile())
    }
    put("/api/system/user/profile") {
        call.respond(saveCurrentUserProfile(call.requireRequestBody()))
    }
}
