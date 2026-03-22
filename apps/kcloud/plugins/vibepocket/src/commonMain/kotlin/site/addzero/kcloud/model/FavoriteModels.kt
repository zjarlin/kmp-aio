package site.addzero.vibepocket.model

import kotlinx.serialization.Serializable


/** 收藏请求 */
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

/** 收藏响应 */
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
