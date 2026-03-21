package site.addzero.vibepocket.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerSunoResourceRoutesSpringRoutes() {
    get("/api/suno/resources") {
        val _springResult = site.addzero.vibepocket.routes.listSunoTaskResources()
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<kotlin.collections.List<site.addzero.vibepocket.routes.SunoTaskResourceResponse>>(),
        )
    }
    
    post("/api/suno/resources") {
        val _springArg0 = call.requireRequestBody<site.addzero.vibepocket.routes.SunoTaskResourceSaveRequest>()
        val _springResult = site.addzero.vibepocket.routes.saveSunoTaskResource(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.routes.SunoTaskResourceResponse>(),
        )
    }
    
    get("/api/suno/resources/{taskId}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("taskId")
        val _springResult = site.addzero.vibepocket.routes.readSunoTaskResource(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.routes.SunoTaskResourceResponse>(),
        )
    }
}