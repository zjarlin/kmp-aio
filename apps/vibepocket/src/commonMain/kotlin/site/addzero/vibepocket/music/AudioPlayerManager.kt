package site.addzero.vibepocket.music

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 播放器状态枚举
 */
enum class PlayerState {
    IDLE, BUFFERING, PLAYING, PAUSED, ERROR
}

/**
 * 全局单例播放管理器
 *
 * (由于 GadulkaPlayer 存在 JDK 版本冲突，当前已移除其实际实现，播放逻辑待替换为其他 KMP 播放器)
 *
 * 确保同时只有一首 Track 在播放（需求 1.4），
 * 通过 StateFlow 暴露播放状态供 UI 层观察。
 */
object AudioPlayerManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var pollingJob: Job? = null

    // ── 内部可变状态 ──────────────────────────────────────────
    private val _currentTrackId = MutableStateFlow<String?>(null)
    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    private val _progress = MutableStateFlow(0f)
    private val _position = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)

    // ── 对外只读 StateFlow ───────────────────────────────────
    val currentTrackId: StateFlow<String?> = _currentTrackId.asStateFlow()
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    val progress: StateFlow<Float> = _progress.asStateFlow()
    val position: StateFlow<Long> = _position.asStateFlow()
    val duration: StateFlow<Long> = _duration.asStateFlow()

    // ── 播放控制 ─────────────────────────────────────────────

    /**
     * 播放指定 Track。
     */
    fun play(trackId: String, audioUrl: String) {
        // 停止当前播放
        stop()

        _currentTrackId.value = trackId
        _playerState.value = PlayerState.PLAYING
        _progress.value = 0.5f // Dummy progress
        _position.value = 1000L
        _duration.value = 2000L

        println("Dummy Play: $trackId -> $audioUrl")
    }

    /**
     * 暂停当前播放。
     */
    fun pause() {
        if (_playerState.value == PlayerState.PLAYING) {
            _playerState.value = PlayerState.PAUSED
        }
    }

    /**
     * 恢复播放。
     */
    fun resume() {
        if (_playerState.value == PlayerState.PAUSED) {
            _playerState.value = PlayerState.PLAYING
        }
    }

    /**
     * 停止播放并重置状态。
     */
    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        _playerState.value = PlayerState.IDLE
        _currentTrackId.value = null
        _progress.value = 0f
        _position.value = 0L
        _duration.value = 0L
    }

    /**
     * 释放播放器资源。
     */
    fun release() {
        stop()
    }

    // ── 工具方法 ─────────────────────────────────────────────

    /**
     * 格式化毫秒为 "m:ss" 字符串。
     */
    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

// ── 工具方法 ─────────────────────────────────────────────

/**
 * 格式化毫秒为 "m:ss" 字符串，供 TrackPlayerState 使用。
 */
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
