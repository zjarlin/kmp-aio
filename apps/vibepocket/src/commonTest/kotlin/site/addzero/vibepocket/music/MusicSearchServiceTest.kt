package site.addzero.vibepocket.music

import kotlinx.coroutines.test.runTest
import site.addzero.vibepocket.api.music.MusicLyric
import site.addzero.vibepocket.api.music.MusicResolvedAsset
import site.addzero.vibepocket.api.music.MusicTrack
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MusicSearchServiceTest {
    @AfterTest
    fun tearDown() {
        MusicSearchService.resetForTests()
    }

    @Test
    fun resolveUsesSessionCacheForSamePlatformAndId() = runTest {
        var resolveCalls = 0
        val expectedAsset = MusicResolvedAsset(
            url = "https://example.com/song.mp3",
            fileName = "song.mp3",
            contentType = "audio/mpeg",
        )
        MusicSearchService.gateway = object : MusicSearchGateway {
            override suspend fun search(
                provider: String,
                keyword: String,
            ): List<MusicTrack> = emptyList()

            override suspend fun getLyrics(
                provider: String,
                songId: String,
            ): MusicLyric = MusicLyric(lrc = "")

            override suspend fun resolve(track: MusicTrack): MusicResolvedAsset {
                resolveCalls += 1
                return expectedAsset
            }
        }

        val track = MusicTrack(
            id = "same-id",
            name = "Song",
            artist = "Artist",
            platform = "netease",
        )

        val first = MusicSearchService.resolve(track)
        val second = MusicSearchService.resolve(track.copy(name = "Song (duplicate)"))

        assertEquals(expectedAsset, first)
        assertEquals(expectedAsset, second)
        assertEquals(1, resolveCalls)
    }

    @Test
    fun searchPlaybackIdDoesNotCollideWithSunoTrackId() {
        val searchPlaybackId = MusicSearchService.playbackId(
            MusicTrack(
                id = "track-123",
                name = "Song",
                artist = "Artist",
                platform = "netease",
            )
        )
        val sunoPlaybackId = "track-123"

        assertNotEquals(sunoPlaybackId, searchPlaybackId)
    }
}
