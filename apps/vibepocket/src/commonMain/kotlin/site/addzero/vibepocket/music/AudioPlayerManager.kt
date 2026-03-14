package site.addzero.vibepocket.music

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
    private val backend by lazy { createAudioPlayerBackend() }

    // ── 内部可变状态 ──────────────────────────────────────────
    private val _currentPlaybackId = MutableStateFlow<String?>(null)
    private val _currentAudio = MutableStateFlow<PlayableAudio?>(null)
    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    private val _progress = MutableStateFlow(0f)
    private val _position = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)
    private val _lastError = MutableStateFlow<String?>(null)

    // ── 对外只读 StateFlow ───────────────────────────────────
    val currentPlaybackId: StateFlow<String?> = _currentPlaybackId.asStateFlow()
    val currentTrackId: StateFlow<String?> = _currentPlaybackId.asStateFlow()
    val currentAudio: StateFlow<PlayableAudio?> = _currentAudio.asStateFlow()
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    val progress: StateFlow<Float> = _progress.asStateFlow()
    val positionMs: StateFlow<Long> = _position.asStateFlow()
    val durationMs: StateFlow<Long> = _duration.asStateFlow()
    val position: StateFlow<Long> = _position.asStateFlow()
    val duration: StateFlow<Long> = _duration.asStateFlow()
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // ── 播放控制 ─────────────────────────────────────────────

    /**
     * 播放指定 Track。
     */
    fun play(trackId: String, audioUrl: String) {
        play(
            PlayableAudio(
                playbackId = trackId,
                url = audioUrl,
            )
        )
    }

    fun play(audio: PlayableAudio) {
        _currentPlaybackId.value = audio.playbackId
        _currentAudio.value = audio
        _lastError.value = null
        applySnapshot(PlayerSnapshot(state = PlayerState.BUFFERING))
        backend.load(audio, ::applySnapshot)
    }

    /**
     * 暂停当前播放。
     */
    fun pause() {
        if (_playerState.value == PlayerState.PLAYING || _playerState.value == PlayerState.BUFFERING) {
            backend.pause()
            _playerState.value = PlayerState.PAUSED
        }
    }

    /**
     * 恢复播放。
     */
    fun resume() {
        if (_playerState.value == PlayerState.PAUSED) {
            backend.play()
            _playerState.value = PlayerState.PLAYING
        }
    }

    /**
     * 停止播放并重置状态。
     */
    fun stop() {
        backend.stop()
        _currentPlaybackId.value = null
        _currentAudio.value = null
        _playerState.value = PlayerState.IDLE
        _progress.value = 0f
        _position.value = 0L
        _duration.value = 0L
        _lastError.value = null
    }

    /**
     * 释放播放器资源。
     */
    fun release() {
        backend.release()
        stop()
    }

    private fun applySnapshot(snapshot: PlayerSnapshot) {
        _playerState.value = snapshot.state
        _position.value = snapshot.positionMs.coerceAtLeast(0L)
        _duration.value = snapshot.durationMs.coerceAtLeast(0L)
        _lastError.value = snapshot.errorMessage?.trim()?.ifBlank { null }
        _progress.value = when {
            snapshot.durationMs > 0L -> {
                (snapshot.positionMs.toFloat() / snapshot.durationMs.toFloat()).coerceIn(0f, 1f)
            }

            else -> 0f
        }
        snapshot.errorMessage?.trim()?.takeIf(String::isNotBlank)?.let { error ->
            println("[AudioPlayer] ${snapshot.state}: $error")
        }
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
