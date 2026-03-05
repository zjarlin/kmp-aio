package site.addzero.vibepocket.routes

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.addzero.ioc.annotation.Bean
import site.addzero.network.call.music.MusicSearchClient
import site.addzero.network.call.music.model.MusicSearchRequest
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.dto.SearchRequest

/**
 * 音乐搜索相关路由（网易云等）
 */
@Bean
fun Route.musicRoutes() {
    route("/api/music") {

        /**
         * 按关键词搜索歌曲（GET）
         */
        get("/search") {
            val query = call.request.queryParameters["query"]
            if (query.isNullOrBlank()) {
                call.respond(io.ktor.http.HttpStatusCode.BadRequest, ErrorResponse(400, "Query parameter 'query' is required."))
                return@get
            }
            val client = MusicSearchClient()
            val results = client.search(MusicSearchRequest(query))
            // 假设 results 已经是序列化友好的（或者是 Map，Ktor 也能直接转 JSON）
            call.respond(results ?: emptyMap<String, Any>())
        }

        /**
         * 按关键词搜索歌曲（POST JSON）
         */
        post("/search") {
            val req = call.receive<SearchRequest>()
            val client = MusicSearchClient()
            val results = client.search(MusicSearchRequest(req.query))
            call.respond(results ?: emptyMap<String, Any>())
        }
    }
}
