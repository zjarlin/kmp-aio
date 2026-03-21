package site.addzero.vibepocket.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerS3RoutesSpringRoutes() {
    get("/api/s3/list") {
        val _springArg0 = call.optionalRequestParam<kotlin.String>("prefix")
        val _springResult = site.addzero.vibepocket.routes.listS3Files(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<kotlin.collections.List<site.addzero.vibepocket.dto.S3ObjectDto>>(),
        )
    }
    
    post("/api/s3/upload") {
        val _springArg0 = call
        val _springResult = site.addzero.vibepocket.routes.uploadToS3(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.dto.S3UploadResponse>(),
        )
    }
    
    delete("/api/s3/{key}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("key")
        val _springResult = site.addzero.vibepocket.routes.deleteS3File(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.dto.OkResponse>(),
        )
    }
    
    get("/api/s3/{key}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("key")
        val _springResult = site.addzero.vibepocket.routes.readS3FileUrl(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.dto.S3UrlResponse>(),
        )
    }
}