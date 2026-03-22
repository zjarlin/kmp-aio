package site.addzero.vibepocket.model

import kotlinx.serialization.Serializable

@Serializable
data class SunoTaskResourceTrack(
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
    val tracks: List<SunoTaskResourceTrack> = emptyList(),
    val detailJson: String? = null,
    val errorMessage: String? = null,
)

@Serializable
data class SunoTaskResourceItem(
    val id: Long? = null,
    val taskId: String,
    val type: String,
    val status: String,
    val requestJson: String? = null,
    val tracks: List<SunoTaskResourceTrack> = emptyList(),
    val detailJson: String? = null,
    val errorMessage: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
