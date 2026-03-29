package site.addzero.kcloud.plugins.system.configcenter

import io.ktor.server.routing.Route
import site.addzero.kcloud.plugins.system.configcenter.routes.generated.springktor.registerGeneratedSpringRoutes

fun Route.configCenterRoutes() {
    registerGeneratedSpringRoutes()
}
