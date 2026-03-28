package site.addzero.vibepocket.api.music

import java.io.File
import java.net.URI

object MusicPlaybackRateUtil {
    fun changePlaybackRate(
        inputPathOrUrl: String,
        playbackRate: Double,
    ): ByteArray {
        val normalized = inputPathOrUrl.trim()
        require(normalized.isNotBlank()) { "inputPathOrUrl is required" }
        require(playbackRate > 0) { "playbackRate must be greater than 0" }

        return when {
            normalized.startsWith("http://") || normalized.startsWith("https://") -> URI(normalized).toURL().readBytes()
            else -> File(normalized).readBytes()
        }
    }
}
