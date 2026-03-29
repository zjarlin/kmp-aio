package site.addzero.kcloud.vibepocket.routes

import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.vibepocket.dto.OkResponse
import site.addzero.kcloud.vibepocket.model.FavoriteTrack
import site.addzero.kcloud.vibepocket.model.FavoriteTrackDraft
import java.time.LocalDateTime

@PostMapping("/api/favorites")
suspend fun addFavorite(
    @RequestBody request: FavoriteRequest,
): FavoriteResponse {
    val entity = FavoriteTrackDraft.`$`.produce {
        trackId = request.trackId
        taskId = request.taskId
        audioUrl = request.audioUrl
        title = request.title
        tags = request.tags
        imageUrl = request.imageUrl
        duration = request.duration
        createdAt = LocalDateTime.now()
    }
    return sqlClient().save(entity).modifiedEntity.toResponse()
}

@DeleteMapping("/api/favorites/{trackId}")
suspend fun removeFavorite(
    @PathVariable trackId: String,
): OkResponse {
    val sqlClient = sqlClient()
    val existing = sqlClient.createQuery(FavoriteTrack::class) {
        select(table)
    }.execute().firstOrNull { it.trackId == trackId }
        ?: throw NoSuchElementException("Favorite not found: $trackId")

    sqlClient.deleteById(FavoriteTrack::class, existing.id)
    return OkResponse()
}

@GetMapping("/api/favorites")
suspend fun getFavorites(): List<FavoriteResponse> {
    return sqlClient()
        .createQuery(FavoriteTrack::class) {
            select(table)
        }
        .execute()
        .map { it.toResponse() }
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

private fun sqlClient(): KSqlClient {
    return KoinPlatform.getKoin().get()
}
