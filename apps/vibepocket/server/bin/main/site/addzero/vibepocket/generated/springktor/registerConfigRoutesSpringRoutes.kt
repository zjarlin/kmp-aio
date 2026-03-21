package site.addzero.vibepocket.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerConfigRoutesSpringRoutes() {
    put("/api/config") {
        val _springArg0 = call.requireRequestBody<site.addzero.vibepocket.routes.ConfigEntry>()
        val _springResult = site.addzero.vibepocket.routes.updateConfig(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.dto.OkResponse>(),
        )
    }
    
    get("/api/config/runtime") {
        val _springArg0 = call.application
        val _springResult = site.addzero.vibepocket.routes.readRuntimeConfig(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.routes.ConfigRuntimeInfo>(),
        )
    }
    
    get("/api/config/storage") {
        val _springResult = site.addzero.vibepocket.routes.readStorageConfig()
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.routes.StorageConfig>(),
        )
    }
    
    put("/api/config/storage") {
        val _springArg0 = call.requireRequestBody<site.addzero.vibepocket.routes.StorageConfig>()
        val _springResult = site.addzero.vibepocket.routes.updateStorageConfig(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.dto.OkResponse>(),
        )
    }
    
    get("/api/config/{key}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("key")
        val _springResult = site.addzero.vibepocket.routes.readConfig(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.routes.ConfigResponse>(),
        )
    }
}