package site.addzero.kcloud.vibepocket.routes

import kotlinx.serialization.builtins.ListSerializer
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.core.network.json.json
import site.addzero.kcloud.vibepocket.model.MusicHistory
import site.addzero.kcloud.vibepocket.model.MusicHistoryDraft
import java.time.LocalDateTime

@PostMapping("/api/suno/history")
suspend fun saveHistory(
    @RequestBody request: HistorySaveRequest,
): HistoryResponse {
    val tracksJson = json.encodeToString(
        ListSerializer(HistoryTrackDto.serializer()),
        request.tracks,
    )
    val entity = MusicHistoryDraft.`$`.produce {
        taskId = request.taskId
        type = request.type
        status = request.status
        this.tracksJson = tracksJson
        createdAt = LocalDateTime.now()
    }
    return sqlClient().save(entity).modifiedEntity.toHistoryResponse()
}

@GetMapping("/api/suno/history")
suspend fun getHistory(): List<HistoryResponse> {
    return sqlClient()
        .createQuery(MusicHistory::class) {
            select(table)
        }
        .execute()
        .map { it.toHistoryResponse() }
}

private fun MusicHistory.toHistoryResponse(): HistoryResponse {
    val tracks = runCatching {
        json.decodeFromString(
            ListSerializer(HistoryTrackDto.serializer()),
            tracksJson,
        )
    }.getOrDefault(emptyList())
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
