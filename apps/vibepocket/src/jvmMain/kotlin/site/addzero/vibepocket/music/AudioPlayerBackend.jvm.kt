package site.addzero.vibepocket.music

import javafx.application.Platform as JfxPlatform
import javafx.scene.media.Media
import javafx.scene.media.MediaException as JfxMediaException
import javafx.scene.media.MediaPlayer as JfxMediaPlayer
import javafx.util.Duration as JfxDuration
import java.io.File
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

internal actual fun createAudioPlayerBackend(): AudioPlayerBackend = JvmAudioPlayerBackend()

private class JvmAudioPlayerBackend : AudioPlayerBackend {
    private var mediaPlayer: JfxMediaPlayer? = null
    private var currentToken = 0L

    override fun load(
        audio: PlayableAudio,
        onSnapshot: (PlayerSnapshot) -> Unit,
    ) {
        stop()
        val token = ++currentToken
        onSnapshot(PlayerSnapshot(state = PlayerState.BUFFERING))
        println("[AudioPlayer] load ${audio.playbackId} -> ${audio.url}")

        try {
            val mediaSource = normalizeMediaSource(audio.url)
            JavaFxRuntime.runLater {
                if (token != currentToken) {
                    return@runLater
                }

                try {
                    val media = Media(mediaSource)
                    media.setOnError {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = PlayerSnapshot(
                                state = PlayerState.ERROR,
                                errorMessage = media.error?.message ?: "音频资源加载失败",
                            ),
                        )
                    }

                    val player = JfxMediaPlayer(media)
                    mediaPlayer = player

                    player.setOnReady {
                        if (token != currentToken) {
                            player.dispose()
                            return@setOnReady
                        }
                        println("[AudioPlayer] ready ${audio.playbackId}")
                        player.play()
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlayerState.PLAYING),
                        )
                    }
                    player.setOnPlaying {
                        println("[AudioPlayer] playing ${audio.playbackId}")
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlayerState.PLAYING),
                        )
                    }
                    player.setOnPaused {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlayerState.PAUSED),
                        )
                    }
                    player.setOnStopped {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlayerState.IDLE),
                        )
                    }
                    player.setOnStalled {
                        println("[AudioPlayer] stalled ${audio.playbackId}")
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlayerState.BUFFERING),
                        )
                    }
                    player.setOnEndOfMedia {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = PlayerSnapshot(
                                state = PlayerState.PAUSED,
                                positionMs = player.totalDuration.safeMillis(),
                                durationMs = player.totalDuration.safeMillis(),
                            ),
                        )
                    }
                    player.setOnError {
                        val message = player.error?.message ?: "播放器初始化失败"
                        println("[AudioPlayer] error ${audio.playbackId}: $message")
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = PlayerSnapshot(
                                state = PlayerState.ERROR,
                                positionMs = player.currentTime.safeMillis(),
                                durationMs = player.totalDuration.safeMillis(),
                                errorMessage = message,
                            ),
                        )
                    }
                    player.currentTimeProperty().addListener { _, _, newValue ->
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = PlayerSnapshot(
                                state = player.status.toPlayerState(),
                                positionMs = newValue.safeMillis(),
                                durationMs = player.totalDuration.safeMillis(),
                            ),
                        )
                    }
                } catch (error: JfxMediaException) {
                    emitIfCurrent(
                        token = token,
                        onSnapshot = onSnapshot,
                        snapshot = PlayerSnapshot(
                            state = PlayerState.ERROR,
                            errorMessage = error.message,
                        ),
                    )
                }
            }
        } catch (error: Exception) {
            emitIfCurrent(
                token = token,
                onSnapshot = onSnapshot,
                snapshot = PlayerSnapshot(
                    state = PlayerState.ERROR,
                    errorMessage = error.message,
                ),
            )
        }
    }

    override fun play() {
        JavaFxRuntime.runLater {
            mediaPlayer?.play()
        }
    }

    override fun pause() {
        JavaFxRuntime.runLater {
            mediaPlayer?.pause()
        }
    }

    override fun stop() {
        currentToken++
        JavaFxRuntime.runLater {
            mediaPlayer?.stop()
            mediaPlayer?.dispose()
            mediaPlayer = null
        }
    }

    override fun release() {
        stop()
    }

    private fun normalizeMediaSource(url: String): String {
        return when {
            url.startsWith("file:/") -> URI.create(url).toASCIIString()
            url.startsWith("http://") || url.startsWith("https://") -> URI.create(url).toASCIIString()
            else -> File(url).toURI().toString()
        }
    }

    private fun emitIfCurrent(
        token: Long,
        onSnapshot: (PlayerSnapshot) -> Unit,
        snapshot: PlayerSnapshot,
    ) {
        if (token == currentToken) {
            onSnapshot(snapshot)
        }
    }
}

private object JavaFxRuntime {
    private val initialized = AtomicBoolean(false)

    fun ensureStarted() {
        if (initialized.compareAndSet(false, true)) {
            val latch = CountDownLatch(1)
            runCatching {
                JfxPlatform.startup {
                    latch.countDown()
                }
            }.onFailure { error ->
                if (error.message?.contains("Toolkit already initialized") == true) {
                    latch.countDown()
                } else {
                    throw error
                }
            }
            latch.await()
        }
    }

    fun runLater(action: () -> Unit) {
        ensureStarted()
        JfxPlatform.runLater(action)
    }
}

private fun JfxMediaPlayer.Status?.toPlayerState(): PlayerState {
    return when (this) {
        JfxMediaPlayer.Status.PLAYING -> PlayerState.PLAYING
        JfxMediaPlayer.Status.PAUSED -> PlayerState.PAUSED
        JfxMediaPlayer.Status.READY,
        JfxMediaPlayer.Status.STALLED,
        JfxMediaPlayer.Status.UNKNOWN,
        JfxMediaPlayer.Status.HALTED,
        null -> PlayerState.BUFFERING

        JfxMediaPlayer.Status.STOPPED,
        JfxMediaPlayer.Status.DISPOSED -> PlayerState.IDLE
    }
}

private fun JfxMediaPlayer.toSnapshot(state: PlayerState = status.toPlayerState()): PlayerSnapshot {
    return PlayerSnapshot(
        state = state,
        positionMs = currentTime.safeMillis(),
        durationMs = totalDuration.safeMillis(),
        errorMessage = error?.message,
    )
}

private fun JfxDuration?.safeMillis(): Long {
    return this?.toMillis()?.takeIf { it.isFinite() && it >= 0.0 }?.toLong() ?: 0L
}
