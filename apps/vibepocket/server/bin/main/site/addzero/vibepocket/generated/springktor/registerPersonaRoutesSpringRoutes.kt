package site.addzero.vibepocket.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerPersonaRoutesSpringRoutes() {
    get("/api/personas") {
        val _springResult = site.addzero.vibepocket.routes.listPersonas()
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<kotlin.collections.List<site.addzero.vibepocket.routes.PersonaResponse>>(),
        )
    }
    
    post("/api/personas") {
        val _springArg0 = call.requireRequestBody<site.addzero.vibepocket.routes.PersonaSaveRequest>()
        val _springResult = site.addzero.vibepocket.routes.savePersona(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.routes.PersonaResponse>(),
        )
    }
}