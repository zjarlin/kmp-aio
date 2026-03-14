package site.addzero.vibepocket.music

data class PlayableAudio(
    val playbackId: String,
    val url: String,
    val title: String? = null,
    val subtitle: String? = null,
    val coverUrl: String? = null,
)

internal data class PlayerSnapshot(
    val state: PlayerState,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val errorMessage: String? = null,
)

internal interface AudioPlayerBackend {
    fun load(
        audio: PlayableAudio,
        onSnapshot: (PlayerSnapshot) -> Unit,
    )

    fun play()

    fun pause()

    fun stop()

    fun release()
}

internal expect fun createAudioPlayerBackend(): AudioPlayerBackend
