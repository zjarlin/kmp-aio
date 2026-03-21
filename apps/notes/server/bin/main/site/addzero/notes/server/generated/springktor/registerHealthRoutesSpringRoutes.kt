package site.addzero.notes.server.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerHealthRoutesSpringRoutes() {
    get("/health") {
        val _springResult = site.addzero.notes.server.routes.readHealth()
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<kotlin.String>(),
        )
    }
}