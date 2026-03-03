package site.addzero.vibepocket.model

import kotlinx.serialization.Serializable

// ══════════════════════════════════════════════════════════════
//  音乐历史数据模型
// ══════════════════════════════════════════════════════════════

/** 历史保存请求 */
@Serializable
data class MusicHistorySaveRequest(
    val taskId: String,
    val type: String = "generate",
    val status: String,
    val tracks: List<MusicHistoryTrack> = emptyList(),
)

/** 历史音轨 */
@Serializable
data class MusicHistoryTrack(
    val id: String? = null,
    val audioUrl: String? = null,
    val title: String? = null,
    val tags: String? = null,
    val imageUrl: String? = null,
    val duration: Double? = null,
)

/** 历史响应 */
@Serializable
data class MusicHistoryItem(
    val id: Long? = null,
    val taskId: String,
    val type: String,
    val status: String,
    val tracks: List<MusicHistoryTrack> = emptyList(),
    val createdAt: String? = null,
)
