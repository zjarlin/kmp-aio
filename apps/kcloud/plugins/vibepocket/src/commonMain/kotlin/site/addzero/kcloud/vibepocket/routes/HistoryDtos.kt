package site.addzero.kcloud.vibepocket.routes

import kotlinx.serialization.Serializable

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
