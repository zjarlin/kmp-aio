package site.addzero.vibepocket.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerFavoriteRoutesSpringRoutes() {
    post("/api/favorites") {
        val _springArg0 = call.requireRequestBody<site.addzero.vibepocket.routes.FavoriteRequest>()
        val _springResult = site.addzero.vibepocket.routes.createFavorite(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.routes.FavoriteResponse>(),
        )
    }
    
    get("/api/favorites") {
        val _springResult = site.addzero.vibepocket.routes.listFavorites()
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<kotlin.collections.List<site.addzero.vibepocket.routes.FavoriteResponse>>(),
        )
    }
    
    delete("/api/favorites/{trackId}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("trackId")
        val _springResult = site.addzero.vibepocket.routes.deleteFavorite(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.dto.OkResponse>(),
        )
    }
}