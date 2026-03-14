package site.addzero.vibepocket.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import site.addzero.ioc.annotation.Bean
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.api.music.MusicTrack
import site.addzero.vibepocket.service.MusicCatalogService

/**
 * 音乐搜索相关路由
 */
@Bean
fun Route.musicRoutes() {
    val musicCatalogService by inject<MusicCatalogService>()

    route("/api/music") {
        get("/search") {
            val provider = call.request.queryParameters["provider"].orEmpty()
            val keyword = call.request.queryParameters["keyword"].orEmpty()
            try {
                val results = musicCatalogService.search(provider, keyword)
                call.respond(results)
            } catch (error: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(400, error.message ?: "Invalid request"),
                )
            } catch (error: Exception) {
                call.respond(
                    HttpStatusCode.BadGateway,
                    ErrorResponse(502, error.message ?: "Music provider request failed"),
                )
            }
        }

        get("/lyrics") {
            val provider = call.request.queryParameters["provider"].orEmpty()
            val songId = call.request.queryParameters["songId"].orEmpty()
            try {
                val result = musicCatalogService.getLyrics(provider, songId)
                call.respond(result)
            } catch (error: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(400, error.message ?: "Invalid request"),
                )
            } catch (error: Exception) {
                call.respond(
                    HttpStatusCode.BadGateway,
                    ErrorResponse(502, error.message ?: "Music provider request failed"),
                )
            }
        }

        post("/resolve") {
            val track = call.receive<MusicTrack>()
            try {
                val asset = musicCatalogService.resolve(track)
                call.respond(asset)
            } catch (error: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(400, error.message ?: "Invalid request"),
                )
            } catch (error: Exception) {
                call.respond(
                    HttpStatusCode.BadGateway,
                    ErrorResponse(502, error.message ?: "Music provider request failed"),
                )
            }
        }
    }
}
