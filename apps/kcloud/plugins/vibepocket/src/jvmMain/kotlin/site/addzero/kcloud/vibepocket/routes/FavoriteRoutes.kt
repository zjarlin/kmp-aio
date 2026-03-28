@file:RequestMapping("/api/favorites")
package site.addzero.kcloud.routes

import org.springframework.web.bind.annotation.RequestMapping



import kotlinx.serialization.Serializable
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.vibepocket.dto.OkResponse
import site.addzero.vibepocket.model.*
import site.addzero.vibepocket.model.by
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
@PostMapping("/api/favorites")
suspend fun createFavorite(
    @RequestBody request: FavoriteRequest,
): FavoriteResponse {
    val entity = new(FavoriteTrack::class).by {
        trackId = request.trackId
        taskId = request.taskId
        audioUrl = request.audioUrl
        title = request.title
        tags = request.tags
        imageUrl = request.imageUrl
        duration = request.duration
        createdAt = LocalDateTime.now()
    }
    val saved = sqlClient().save(entity)
    return saved.modifiedEntity.toResponse()
}

@DeleteMapping("/api/favorites/{trackId}")
suspend fun deleteFavorite(
    @PathVariable trackId: String,
): OkResponse {
    val sqlClient = sqlClient()
    val existing = sqlClient.createQuery(FavoriteTrack::class) {
        where(table.trackId eq trackId)
        select(table)
    }.execute().firstOrNull()

    if (existing == null) {
        throw NoSuchElementException("Favorite not found")
    }

    sqlClient.deleteById(FavoriteTrack::class, existing.id)
    return OkResponse()
}

@GetMapping("/api/favorites")
suspend fun listFavorites(): List<FavoriteResponse> {
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
