package site.addzero.vibepocket.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.ktor.ext.inject
import site.addzero.ioc.annotation.Bean
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.dto.OkResponse
import site.addzero.vibepocket.jimmer.model.entity.FavoriteTrack
import site.addzero.vibepocket.jimmer.model.entity.by
import site.addzero.vibepocket.jimmer.model.entity.trackId
import java.time.LocalDateTime

@Serializable
data class FavoriteRequest(
    val trackId: String,
    val taskId: String,
    val audioUrl: String? = null,
    val title: String? = null,
    val tags: String? = null,
    val imageUrl: String? = null,
    val duration: Double? = null,
)

@Serializable
data class FavoriteResponse(
    val id: Long,
    val trackId: String,
    val taskId: String,
    val audioUrl: String? = null,
    val title: String? = null,
    val tags: String? = null,
    val imageUrl: String? = null,
    val duration: Double? = null,
    val createdAt: String? = null,
)

/**
 * 收藏相关路由
 */
@Bean
fun Route.favoriteRoutes() {
    val sqlClient by inject<KSqlClient>()

    route("/api/favorites") {

        post {
            val req = call.receive<FavoriteRequest>()
            val entity = new(FavoriteTrack::class).by {
                trackId = req.trackId
                taskId = req.taskId
                audioUrl = req.audioUrl
                title = req.title
                tags = req.tags
                imageUrl = req.imageUrl
                duration = req.duration
                createdAt = LocalDateTime.now()
            }
            val saved = sqlClient.save(entity)
            call.respond(saved.modifiedEntity.toResponse())
        }

        delete("/{trackId}") {
            val tid = call.parameters["trackId"]
                ?: throw IllegalArgumentException("trackId is required")

            val existing = sqlClient.createQuery(FavoriteTrack::class) {
                where(table.trackId eq tid)
                select(table)
            }.execute().firstOrNull()

            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(404, "Favorite not found"))
            } else {
                sqlClient.deleteById(FavoriteTrack::class, existing.id)
                call.respond(OkResponse())
            }
        }

        get {
            val list = sqlClient.createQuery(FavoriteTrack::class) {
                select(table)
            }.execute()
            call.respond(list.map { it.toResponse() })
        }
    }
}

private fun FavoriteTrack.toResponse() = FavoriteResponse(
    id = id,
    trackId = trackId,
    taskId = taskId,
    audioUrl = audioUrl,
    title = title,
    tags = tags,
    imageUrl = imageUrl,
    duration = duration,
    createdAt = createdAt.toString(),
)
