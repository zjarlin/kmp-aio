package site.addzero.vibepocket.routes

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.vibepocket.model.*
import java.time.LocalDateTime

@Serializable
data class SunoTaskResourceTrackResponse(
    val id: String? = null,
    val audioUrl: String? = null,
    val streamAudioUrl: String? = null,
    val title: String? = null,
    val tags: String? = null,
    val imageUrl: String? = null,
    val duration: Double? = null,
)

@Serializable
data class SunoTaskResourceSaveRequest(
    val taskId: String,
    val type: String = "generate",
    val status: String,
    val requestJson: String? = null,
    val tracks: List<SunoTaskResourceTrackResponse> = emptyList(),
    val detailJson: String? = null,
    val errorMessage: String? = null,
)

@Serializable
data class SunoTaskResourceResponse(
    val id: Long,
    val taskId: String,
    val type: String,
    val status: String,
    val requestJson: String? = null,
    val tracks: List<SunoTaskResourceTrackResponse> = emptyList(),
    val detailJson: String? = null,
    val errorMessage: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

private val sunoTaskResourceJson = Json {
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
}

@PostMapping("/api/suno/resources")
suspend fun saveSunoTaskResource(
    @RequestBody request: SunoTaskResourceSaveRequest,
): SunoTaskResourceResponse {
    val sqlClient = sqlClient()
    val now = LocalDateTime.now()
    val tracksJson = sunoTaskResourceJson.encodeToString(
        ListSerializer(SunoTaskResourceTrackResponse.serializer()),
        request.tracks,
    )
    val existing = sqlClient.createQuery(SunoTaskResource::class) {
        where(table.taskId eq request.taskId)
        select(table)
    }.execute().firstOrNull()

    val entity = new(SunoTaskResource::class).by {
        if (existing != null) {
            id = existing.id
            createdAt = existing.createdAt
        } else {
            createdAt = now
        }
        taskId = request.taskId
        type = request.type
        status = request.status
        requestJson = request.requestJson
        this.tracksJson = tracksJson
        detailJson = request.detailJson
        errorMessage = request.errorMessage
        updatedAt = now
    }

    return sqlClient.save(entity).modifiedEntity.toResponse()
}

@GetMapping("/api/suno/resources")
suspend fun listSunoTaskResources(): List<SunoTaskResourceResponse> {
    return sqlClient()
        .createQuery(SunoTaskResource::class) {
            select(table)
        }
        .execute()
        .sortedByDescending { it.updatedAt }
        .map { it.toResponse() }
}

@GetMapping("/api/suno/resources/{taskId}")
suspend fun readSunoTaskResource(
    @PathVariable taskId: String,
): SunoTaskResourceResponse {
    val taskResource = sqlClient()
        .createQuery(SunoTaskResource::class) {
            where(table.taskId eq taskId)
            select(table)
        }
        .execute()
        .firstOrNull()
        ?: throw NoSuchElementException("Suno task resource not found: $taskId")
    return taskResource.toResponse()
}

private fun SunoTaskResource.toResponse(): SunoTaskResourceResponse {
    val tracks = runCatching {
        sunoTaskResourceJson.decodeFromString(
            ListSerializer(SunoTaskResourceTrackResponse.serializer()),
            tracksJson,
        )
    }.getOrDefault(emptyList())

    return SunoTaskResourceResponse(
        id = id,
        taskId = taskId,
        type = type,
        status = status,
        requestJson = requestJson,
        tracks = tracks,
        detailJson = detailJson,
        errorMessage = errorMessage,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

private fun sqlClient(): KSqlClient {
    return KoinPlatform.getKoin().get()
}
