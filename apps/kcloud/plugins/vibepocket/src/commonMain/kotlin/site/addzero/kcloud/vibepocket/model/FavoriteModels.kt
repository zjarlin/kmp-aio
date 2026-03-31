package site.addzero.kcloud.vibepocket.model

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
data class FavoriteItem(
    val id: Long? = null,
    val trackId: String,
    val taskId: String,
    val audioUrl: String? = null,
    val title: String? = null,
    val tags: String? = null,
    val imageUrl: String? = null,
    val duration: Double? = null,
    val createdAt: String? = null,
)
