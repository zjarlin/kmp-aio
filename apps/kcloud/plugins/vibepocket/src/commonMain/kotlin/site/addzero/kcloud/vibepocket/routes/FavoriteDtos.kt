package site.addzero.kcloud.vibepocket.routes

import kotlinx.serialization.Serializable

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
