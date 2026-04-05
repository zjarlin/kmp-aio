package site.addzero.kcloud.music

import kotlinx.serialization.json.Json
import site.addzero.kcloud.api.ApiProvider
import site.addzero.kcloud.api.suno.SunoTaskDetail
import site.addzero.kcloud.api.suno.SunoTrack
import site.addzero.kcloud.vibepocket.model.*
import site.addzero.kcloud.vibepocket.routes.SunoTaskResourceResponse as ApiSunoTaskResourceResponse
import site.addzero.kcloud.vibepocket.routes.SunoTaskResourceSaveRequest as ApiSunoTaskResourceSaveRequest
import site.addzero.kcloud.vibepocket.routes.SunoTaskResourceTrackResponse as ApiSunoTaskResourceTrackResponse

private val sunoTaskPersistenceJson = Json {
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
}

internal data class RefreshedSunoTaskSnapshot(
    val detail: SunoTaskDetail,
    val archivedItem: SunoTaskResourceItem?,
    val archiveStatus: String,
)

internal suspend fun saveSunoTaskArchive(
    taskId: String,
    fallbackType: String,
    requestJson: String?,
    detail: SunoTaskDetail?,
): SunoTaskResourceItem {
    return ApiProvider.sunoTaskResourceApi.save(
        buildSunoTaskResourceSaveRequest(
            taskId = taskId,
            fallbackType = fallbackType,
            requestJson = requestJson,
            detail = detail,
        ),
    ).toTaskResourceItem()
}

internal suspend fun persistSunoHistoryIfSuccess(detail: SunoTaskDetail?) {
    val request = detail?.toHistorySaveRequest() ?: return
    runCatching {
        ApiProvider.historyApi.saveHistory(request)
    }
}

internal suspend fun refreshSunoTaskSnapshotById(
    taskId: String,
    fallbackType: String,
    requestJson: String?,
): RefreshedSunoTaskSnapshot {
    val detail = SunoWorkflowService.getTaskDetailOrNull(taskId)
        ?: throw NoSuchElementException("Suno 远端没有找到任务 $taskId")
    persistSunoHistoryIfSuccess(detail)
    val archiveResult = runCatching {
        saveSunoTaskArchive(
            taskId = taskId,
            fallbackType = fallbackType,
            requestJson = requestJson,
            detail = detail,
        )
    }
    return RefreshedSunoTaskSnapshot(
        detail = detail,
        archivedItem = archiveResult.getOrNull(),
        archiveStatus = archiveResult.fold(
            onSuccess = { it.archiveStatusText() },
            onFailure = { it.archiveFailureText() },
        ),
    )
}

internal fun SunoTaskResourceItem.archiveStatusText(): String {
    return "已记录 #${id ?: "-"}"
}

internal fun Throwable.archiveFailureText(): String {
    return "建档失败: ${message ?: "未知错误"}"
}

internal fun recoveredTaskStatusText(detail: SunoTaskDetail): String {
    return when {
        detail.isSuccess -> "本地轮询异常，但已按 taskId 找回结果：${detail.displayStatus}"
        detail.isFailed -> "本地轮询异常，按 taskId 复核后确认失败：${detail.displayStatus}"
        else -> "本地轮询异常，已按 taskId 补查：${detail.displayStatus}"
    }
}

internal fun buildSunoTaskResourceSaveRequest(
    taskId: String,
    fallbackType: String,
    requestJson: String?,
    detail: SunoTaskDetail?,
): ApiSunoTaskResourceSaveRequest {
    return ApiSunoTaskResourceSaveRequest(
        taskId = taskId,
        type = detail?.type?.takeIf { it.isNotBlank() } ?: fallbackType,
        status = detail?.status?.takeIf { it.isNotBlank() } ?: "PENDING",
        requestJson = requestJson?.takeIf { it.isNotBlank() },
        tracks = detail?.response?.sunoData.orEmpty().map(SunoTrack::toTaskResourceTrackResponse),
        detailJson = detail?.let { sunoTaskPersistenceJson.encodeToString(it) },
        errorMessage = detail?.errorMessage ?: detail?.errorCode,
    )
}

internal fun ApiSunoTaskResourceResponse.toTaskResourceItem(): SunoTaskResourceItem {
    return SunoTaskResourceItem(
        id = id,
        taskId = taskId,
        type = type,
        status = status,
        requestJson = requestJson,
        tracks = tracks.map { track ->
            SunoTaskResourceTrack(
                id = track.id,
                audioUrl = track.audioUrl,
                streamAudioUrl = track.streamAudioUrl,
                title = track.title,
                tags = track.tags,
                imageUrl = track.imageUrl,
                duration = track.duration,
            )
        },
        detailJson = detailJson,
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

internal fun SunoTaskDetail.toHistorySaveRequest(): MusicHistorySaveRequest? {
    val resolvedTaskId = taskId?.takeIf { it.isNotBlank() } ?: return null
    if (!isSuccess) {
        return null
    }
    return MusicHistorySaveRequest(
        taskId = resolvedTaskId,
        type = type ?: "generate",
        status = status ?: "SUCCESS",
        tracks = response?.sunoData.orEmpty().map(SunoTrack::toHistoryTrack),
    )
}

private fun SunoTrack.toHistoryTrack(): MusicHistoryTrack {
    return MusicHistoryTrack(
        id = id,
        audioUrl = audioUrl,
        title = title,
        tags = tags,
        imageUrl = imageUrl,
        duration = duration,
    )
}

private fun SunoTrack.toTaskResourceTrackResponse(): ApiSunoTaskResourceTrackResponse {
    return ApiSunoTaskResourceTrackResponse(
        id = id,
        audioUrl = audioUrl,
        streamAudioUrl = streamAudioUrl,
        title = title,
        tags = tags,
        imageUrl = imageUrl,
        duration = duration,
    )
}
