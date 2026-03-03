package site.addzero.vibepocket.model

// ══════════════════════════════════════════════════════════════
//  Track 交互数据模型
// ══════════════════════════════════════════════════════════════

/** Track 操作菜单项 */
enum class TrackAction {
    EXTEND, VOCAL_REMOVAL, GENERATE_COVER,
    CREATE_PERSONA, REPLACE_SECTION, EXPORT_WAV, BOOST_STYLE
}

/** Track 播放状态 */
data class TrackPlayerState(
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentTime: String = "0:00",
    val totalTime: String = "0:00",
)
