package site.addzero.vibepocket

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.addzero.media.playlist.player.DefaultPlaylistPlayer
import site.addzero.media.playlist.player.PlaylistAudioSource

private data class DemoTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val audioUrl: String,
)

private val demoTracks = listOf(
    DemoTrack(
        id = "demo-track",
        title = "Media Playlist Player Demo",
        artist = "VibePocket",
        durationMs = 180_000L,
        audioUrl = "https://cdn1.suno.ai/b5390e2d-80ba-42c8-820b-b79b0dfc0adb.mp3",
    )
)

@Composable
fun MediaPlayerDemo() {
    DefaultPlaylistPlayer(
        items = demoTracks,
        modifier = Modifier.fillMaxWidth(),
        itemKey = DemoTrack::id,
        titleOf = DemoTrack::title,
        subtitleOf = DemoTrack::artist,
        durationMsOf = DemoTrack::durationMs,
        coverUrlOf = { null },
        resolveAudioSource = { track ->
            PlaylistAudioSource(url = track.audioUrl)
        },
    )
}
