package site.addzero.vibepocket.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerHistoryRoutesSpringRoutes() {
    get("/api/suno/history") {
        val _springResult = site.addzero.vibepocket.routes.listHistory()
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<kotlin.collections.List<site.addzero.vibepocket.routes.HistoryResponse>>(),
        )
    }
    
    post("/api/suno/history") {
        val _springArg0 = call.requireRequestBody<site.addzero.vibepocket.routes.HistorySaveRequest>()
        val _springResult = site.addzero.vibepocket.routes.saveHistory(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.routes.HistoryResponse>(),
        )
    }
}