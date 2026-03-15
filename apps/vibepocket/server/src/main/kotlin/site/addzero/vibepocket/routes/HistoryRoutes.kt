package site.addzero.vibepocket.routes

import kotlinx.serialization.Serializable
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.vibepocket.model.MusicHistory
import site.addzero.vibepocket.model.by
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
@PostMapping("/api/suno/history")
suspend fun saveHistory(
    @RequestBody request: HistorySaveRequest,
): HistoryResponse {
    val tracksJson = kotlinx.serialization.json.Json.encodeToString(
        kotlinx.serialization.builtins.ListSerializer(HistoryTrackDto.serializer()),
        request.tracks,
    )
    val entity = new(MusicHistory::class).by {
        taskId = request.taskId
        type = request.type
        status = request.status
        this.tracksJson = tracksJson
        createdAt = LocalDateTime.now()
    }
    val saved = sqlClient().save(entity)
    return saved.modifiedEntity.toHistoryResponse()
}

@GetMapping("/api/suno/history")
suspend fun listHistory(): List<HistoryResponse> {
    return sqlClient()
        .createQuery(MusicHistory::class) {
            select(table)
        }
        .execute()
        .map { it.toHistoryResponse() }
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

private fun sqlClient(): KSqlClient {
    return KoinPlatform.getKoin().get()
}
