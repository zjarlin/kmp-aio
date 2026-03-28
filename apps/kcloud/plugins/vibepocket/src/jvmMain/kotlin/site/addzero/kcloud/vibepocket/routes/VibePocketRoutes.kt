package site.addzero.vibepocket.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.response.respond
import kotlinx.serialization.json.JsonElement
import site.addzero.springktor.runtime.optionalRequestParam
import site.addzero.springktor.runtime.requirePathVariable
import site.addzero.springktor.runtime.requireRequestBody
import site.addzero.vibepocket.dto.GenerateRequest
import site.addzero.vibepocket.music.UploadCoverSourcePrepareRequest

/** 统一挂载 vibepocket 插件的后端路由。 */
fun Route.vibePocketRoutes() {
    registerConfigRoutes()
    registerFavoriteRoutes()
    registerHistoryRoutes()
    registerMusicRoutes()
    registerPersonaRoutes()
    registerSunoResourceRoutes()
    registerSunoRoutes()
}

private fun Route.registerConfigRoutes() {
    get("/api/config/runtime") {
        val result = readRuntimeConfig(call.application)
        call.respond(result)
    }
    get("/api/config/{key}") {
        val key = call.requirePathVariable<String>("key")
        val result = readConfig(key)
        call.respond(result)
    }
    put("/api/config") {
        val entry = call.requireRequestBody<ConfigEntry>()
        val result = updateConfig(entry)
        call.respond(result)
    }
    get("/api/config/storage") {
        val result = readStorageConfig()
        call.respond(result)
    }
    put("/api/config/storage") {
        val config = call.requireRequestBody<StorageConfig>()
        val result = updateStorageConfig(config)
        call.respond(result)
    }
}

private fun Route.registerFavoriteRoutes() {
    post("/api/favorites") {
        val request = call.requireRequestBody<FavoriteRequest>()
        val result = createFavorite(request)
        call.respond(result)
    }
    get("/api/favorites") {
        val result = listFavorites()
        call.respond(result)
    }
    delete("/api/favorites/{trackId}") {
        val trackId = call.requirePathVariable<String>("trackId")
        val result = deleteFavorite(trackId)
        call.respond(result)
    }
}

private fun Route.registerHistoryRoutes() {
    post("/api/suno/history") {
        val request = call.requireRequestBody<HistorySaveRequest>()
        val result = saveHistory(request)
        call.respond(result)
    }
    get("/api/suno/history") {
        val result = listHistory()
        call.respond(result)
    }
}

private fun Route.registerMusicRoutes() {
    get("/api/music/search") {
        val provider = call.optionalRequestParam<String>("provider")
        val keyword = call.optionalRequestParam<String>("keyword")
        val result = searchMusic(provider, keyword)
        call.respond(result)
    }
    get("/api/music/lyrics") {
        val provider = call.optionalRequestParam<String>("provider")
        val songId = call.optionalRequestParam<String>("songId")
        val result = readLyrics(provider, songId)
        call.respond(result)
    }
    post("/api/music/resolve") {
        val track = call.requireRequestBody<site.addzero.kcloud.api.music.MusicTrack>()
        val result = resolveMusic(track)
        call.respond(result)
    }
    post("/api/music/upload-cover-source/prepare") {
        val request = call.requireRequestBody<UploadCoverSourcePrepareRequest>()
        val result = prepareUploadCoverSource(request)
        call.respond(result)
    }
}

private fun Route.registerPersonaRoutes() {
    post("/api/personas") {
        val request = call.requireRequestBody<PersonaSaveRequest>()
        val result = savePersona(request)
        call.respond(result)
    }
    get("/api/personas") {
        val result = listPersonas()
        call.respond(result)
    }
}

private fun Route.registerSunoResourceRoutes() {
    post("/api/suno/resources") {
        val request = call.requireRequestBody<SunoTaskResourceSaveRequest>()
        val result = saveSunoTaskResource(request)
        call.respond(result)
    }
    get("/api/suno/resources") {
        val result = listSunoTaskResources()
        call.respond(result)
    }
    get("/api/suno/resources/{taskId}") {
        val taskId = call.requirePathVariable<String>("taskId")
        val result = readSunoTaskResource(taskId)
        call.respond(result)
    }
}

private fun Route.registerSunoRoutes() {
    post("/api/suno/generate") {
        val request = call.requireRequestBody<GenerateRequest>()
        val result = generateMusic(request)
        call.respond(result)
    }
    get("/api/suno/tasks") {
        val result = listTasks()
        call.respond(result)
    }
    get("/api/suno/tasks/{taskId}") {
        val taskId = call.requirePathVariable<String>("taskId")
        val result = readTask(taskId)
        call.respond(result)
    }
    post("/api/suno/callback/{kind}") {
        val kind = call.requirePathVariable<String>("kind")
        val requestId = call.optionalRequestParam<String>("requestId")
        val payload = call.requireRequestBody<JsonElement>()
        val result = handleSunoCallback(kind, requestId, payload)
        call.respond(result)
    }
}
