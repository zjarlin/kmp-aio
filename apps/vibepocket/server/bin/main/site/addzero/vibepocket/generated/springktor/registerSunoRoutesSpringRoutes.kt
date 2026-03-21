package site.addzero.vibepocket.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerSunoRoutesSpringRoutes() {
    post("/api/suno/callback/{kind}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("kind")
        val _springArg1 = call.optionalRequestParam<kotlin.String>("requestId")
        val _springArg2 = call.requireRequestBody<kotlinx.serialization.json.JsonElement>()
        val _springResult = site.addzero.vibepocket.routes.handleSunoCallback(_springArg0, _springArg1, _springArg2)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<kotlin.String>(),
        )
    }
    
    post("/api/suno/generate") {
        val _springArg0 = call.requireRequestBody<site.addzero.vibepocket.dto.GenerateRequest>()
        val _springResult = site.addzero.vibepocket.routes.generateMusic(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.dto.TaskResult>(),
        )
    }
    
    get("/api/suno/tasks") {
        val _springResult = site.addzero.vibepocket.routes.listTasks()
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.dto.TaskListResult>(),
        )
    }
    
    get("/api/suno/tasks/{taskId}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("taskId")
        val _springResult = site.addzero.vibepocket.routes.readTask(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.dto.TaskResult>(),
        )
    }
}