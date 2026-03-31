package site.addzero.kcloud.music

import site.addzero.kcloud.api.suno.SunoTrack
import site.addzero.kcloud.vibepocket.model.FavoriteItem
import site.addzero.kcloud.vibepocket.model.MusicHistoryItem
import site.addzero.kcloud.vibepocket.model.MusicHistoryTrack
import site.addzero.kcloud.vibepocket.model.SunoTaskResourceItem
import site.addzero.kcloud.vibepocket.model.SunoTaskResourceTrack
import site.addzero.media.playlist.player.PlaylistAudioSource


internal data class HistoryPlaylistEntry(
    val taskId: String,
    val createdAt: String?,
    val status: String,
    val track: SunoTrack,
)

internal data class FavoritePlaylistEntry(
    val item: FavoriteItem,
    val track: SunoTrack,
)

internal data class TaskResourcePlaylistEntry(
    val item: SunoTaskResourceItem,
    val track: SunoTrack,
)

internal fun SunoTrack.playbackId(scope: String): String {
    val key = id?.takeIf { it.isNotBlank() }
        ?: audioUrl?.takeIf { it.isNotBlank() }
        ?: streamAudioUrl?.takeIf { it.isNotBlank() }
        ?: title?.takeIf { it.isNotBlank() }
        ?: "track"
    return "suno:$scope:$key"
}

internal fun SunoTrack.resolvedAudioSource(): PlaylistAudioSource {
    val resolvedUrl = audioUrl?.takeIf { it.isNotBlank() }
        ?: streamAudioUrl?.takeIf { it.isNotBlank() }
    return PlaylistAudioSource(
        url = resolvedUrl,
        unavailableMessage = "当前歌曲暂无可用音源",
    )
}

internal fun SunoTrack.durationMsOrNull(): Long? {
    val durationSeconds = duration ?: return null
    return (durationSeconds * 1000L).toLong().takeIf { it > 0L }
}

internal fun SunoTrack.displayTitle(): String = title?.ifBlank { null } ?: "未命名音轨"

internal fun SunoTrack.displaySubtitle(vararg extras: String?): String {
    return buildList {
        tags?.takeIf { it.isNotBlank() }?.let(::add)
        extras.forEach { extra ->
            extra?.takeIf { it.isNotBlank() }?.let(::add)
        }
    }.joinToString(" · ")
}

internal fun MusicHistoryItem.toPlaylistEntries(): List<HistoryPlaylistEntry> {
    return tracks.map { track ->
        HistoryPlaylistEntry(
            taskId = taskId,
            createdAt = createdAt,
            status = status,
            track = track.toSunoTrack(),
        )
    }
}

internal fun FavoriteItem.toPlaylistEntry(): FavoritePlaylistEntry {
    return FavoritePlaylistEntry(
        item = this,
        track = toSunoTrack(),
    )
}

internal fun SunoTaskResourceItem.toPlaylistEntries(): List<TaskResourcePlaylistEntry> {
    return tracks.map { track ->
        TaskResourcePlaylistEntry(
            item = this,
            track = track.toSunoTrack(),
        )
    }
}

internal fun MusicHistoryTrack.toSunoTrack(): SunoTrack = SunoTrack(
    id = id,
    audioUrl = audioUrl,
    title = title,
    tags = tags,
    imageUrl = imageUrl,
    duration = duration,
)

internal fun FavoriteItem.toSunoTrack(): SunoTrack = SunoTrack(
    id = trackId,
    audioUrl = audioUrl,
    title = title,
    tags = tags,
    imageUrl = imageUrl,
    duration = duration,
)

internal fun SunoTaskResourceTrack.toSunoTrack(): SunoTrack = SunoTrack(
    id = id,
    audioUrl = audioUrl,
    streamAudioUrl = streamAudioUrl,
    title = title,
    tags = tags,
    imageUrl = imageUrl,
    duration = duration,
)
