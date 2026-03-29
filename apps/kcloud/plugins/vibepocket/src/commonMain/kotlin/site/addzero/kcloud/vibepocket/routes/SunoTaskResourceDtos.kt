package site.addzero.kcloud.vibepocket.routes

import kotlinx.serialization.Serializable

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
