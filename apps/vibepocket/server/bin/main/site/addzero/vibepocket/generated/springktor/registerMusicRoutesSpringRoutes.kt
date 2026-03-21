package site.addzero.vibepocket.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerMusicRoutesSpringRoutes() {
    get("/api/music/lyrics") {
        val _springArg0 = call.optionalRequestParam<kotlin.String>("provider")
        val _springArg1 = call.optionalRequestParam<kotlin.String>("songId")
        val _springResult = site.addzero.vibepocket.routes.readLyrics(_springArg0, _springArg1)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.api.music.MusicLyric>(),
        )
    }
    
    post("/api/music/resolve") {
        val _springArg0 = call.requireRequestBody<site.addzero.vibepocket.api.music.MusicTrack>()
        val _springResult = site.addzero.vibepocket.routes.resolveMusic(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.api.music.MusicResolvedAsset>(),
        )
    }
    
    get("/api/music/search") {
        val _springArg0 = call.optionalRequestParam<kotlin.String>("provider")
        val _springArg1 = call.optionalRequestParam<kotlin.String>("keyword")
        val _springResult = site.addzero.vibepocket.routes.searchMusic(_springArg0, _springArg1)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<kotlin.collections.List<site.addzero.vibepocket.api.music.MusicTrack>>(),
        )
    }
    
    post("/api/music/upload-cover-source/prepare") {
        val _springArg0 = call.requireRequestBody<site.addzero.vibepocket.music.UploadCoverSourcePrepareRequest>()
        val _springResult = site.addzero.vibepocket.routes.prepareUploadCoverSource(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.vibepocket.music.UploadCoverSourcePrepareResponse>(),
        )
    }
}