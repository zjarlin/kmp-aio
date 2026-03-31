package site.addzero.kcloud.vibepocket.model

import kotlinx.serialization.Serializable

@Serializable
data class MusicHistorySaveRequest(
    val taskId: String,
    val type: String = "generate",
    val status: String,
    val tracks: List<MusicHistoryTrack> = emptyList(),
)

@Serializable
data class MusicHistoryTrack(
    val id: String? = null,
    val audioUrl: String? = null,
    val title: String? = null,
    val tags: String? = null,
    val imageUrl: String? = null,
    val duration: Double? = null,
)

@Serializable
data class MusicHistoryItem(
    val id: Long? = null,
    val taskId: String,
    val type: String,
    val status: String,
    val tracks: List<MusicHistoryTrack> = emptyList(),
    val createdAt: String? = null,
)
