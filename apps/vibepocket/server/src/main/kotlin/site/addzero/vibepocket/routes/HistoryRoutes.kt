package site.addzero.vibepocket.routes

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.ktor.ext.inject
import site.addzero.ioc.annotation.Bean
import site.addzero.vibepocket.jimmer.model.entity.MusicHistory
import site.addzero.vibepocket.jimmer.model.entity.by
import java.time.LocalDateTime

@Serializable
data class HistorySaveRequest(
    val taskId: String,
    val type: String = "generate",
    val status: String,
    val tracks: List<HistoryTrackDto> = emptyList(),
)

@Serializable
data class HistoryTrackDto(
    val id: String? = null,
    val audioUrl: String? = null,
    val title: String? = null,
    val tags: String? = null,
    val imageUrl: String? = null,
    val duration: Double? = null,
)

@Serializable
data class HistoryResponse(
    val id: Long,
    val taskId: String,
    val type: String,
    val status: String,
    val tracks: List<HistoryTrackDto> = emptyList(),
    val createdAt: String? = null,
)

/**
 * 音乐历史相关路由
 */
@Bean
fun Route.historyRoutes() {
    val sqlClient by inject<KSqlClient>()

    route("/api/suno/history") {

        post {
            val req = call.receive<HistorySaveRequest>()
            val tracksJson = kotlinx.serialization.json.Json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(HistoryTrackDto.serializer()),
                req.tracks
            )
            val entity = new(MusicHistory::class).by {
                taskId = req.taskId
                type = req.type
                status = req.status
                this.tracksJson = tracksJson
                createdAt = LocalDateTime.now()
            }
            val saved = sqlClient.save(entity)
            call.respond(saved.modifiedEntity.toHistoryResponse())
        }

        get {
            val list = sqlClient.createQuery(MusicHistory::class) {
                select(table)
            }.execute()
            call.respond(list.map { it.toHistoryResponse() })
        }
    }
}

private fun MusicHistory.toHistoryResponse(): HistoryResponse {
    val tracks = try {
        kotlinx.serialization.json.Json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(HistoryTrackDto.serializer()),
            tracksJson
        )
    } catch (_: Exception) {
        emptyList()
    }
    return HistoryResponse(
        id = id,
        taskId = taskId,
        type = type,
        status = status,
        tracks = tracks,
        createdAt = createdAt.toString(),
    )
}
