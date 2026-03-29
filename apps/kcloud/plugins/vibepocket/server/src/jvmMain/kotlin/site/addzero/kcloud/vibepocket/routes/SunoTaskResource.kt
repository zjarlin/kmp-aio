package site.addzero.kcloud.vibepocket.routes

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.vibepocket.model.SunoTaskResource
import site.addzero.kcloud.vibepocket.model.SunoTaskResourceDraft
import java.time.LocalDateTime

private val sunoTaskResourceJson = Json {
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
}

@PostMapping("/api/suno/resources")
suspend fun save(
    @RequestBody request: SunoTaskResourceSaveRequest,
): SunoTaskResourceResponse {
    val sqlClient = sqlClient()
    val now = LocalDateTime.now()
    val tracksJson = sunoTaskResourceJson.encodeToString(
        ListSerializer(SunoTaskResourceTrackResponse.serializer()),
        request.tracks,
    )
    val existing = sqlClient.createQuery(SunoTaskResource::class) {
        select(table)
    }.execute().firstOrNull { it.taskId == request.taskId }

    val entity = SunoTaskResourceDraft.`$`.produce(existing) {
        if (existing == null) {
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
suspend fun list(): List<SunoTaskResourceResponse> {
    return sqlClient()
        .createQuery(SunoTaskResource::class) {
            select(table)
        }
        .execute()
        .sortedByDescending { it.updatedAt }
        .map { it.toResponse() }
}

@GetMapping("/api/suno/resources/{taskId}")
suspend fun get(
    @PathVariable taskId: String,
): SunoTaskResourceResponse {
    val taskResource = sqlClient()
        .createQuery(SunoTaskResource::class) {
            select(table)
        }
        .execute()
        .firstOrNull { it.taskId == taskId }
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
