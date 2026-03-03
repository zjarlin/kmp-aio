package site.addzero.vibepocket.music

import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based tests for AudioPlayerManager single active player invariant.
 *
 * // Feature: suno-client-completion, Property 1: 单一活跃播放器不变量
 * **Validates: Requirements 1.4**
 *
 * This test validates the STATE MANAGEMENT LOGIC:
 * - After calling play(trackId, audioUrl), currentTrackId.value == trackId
 * - At any point, only the LAST played track is the current one
 * - After stop(), currentTrackId.value == null
 */
@OptIn(ExperimentalKotest::class)
class AudioPlayerManagerPropertyTest {

    /**
     * Generator for a (trackId, audioUrl) pair.
     * trackId: non-empty string (1..30 chars)
     * audioUrl: non-empty string simulating a URL (1..100 chars)
     */
    private val arbTrackPair: Arb<Pair<String, String>> = arbitrary {
        val trackId = Arb.string(minSize = 1, maxSize = 30).bind()
        val audioUrl = Arb.string(minSize = 1, maxSize = 100).bind()
        trackId to audioUrl
    }

    /**
     * Generator for a non-empty list of (trackId, audioUrl) pairs (1..10 items).
     */
    private val arbTrackPairList: Arb<List<Pair<String, String>>> =
        Arb.list(arbTrackPair, range = 1..10)

    /**
     * Property 1: 单一活跃播放器不变量
     *
     * For any sequence of play() calls with different trackIds, after each play(trackId, url)
     * call, currentTrackId.value MUST equal that trackId — proving that at most one track
     * is the "current" track at any point, and it is always the LAST one played.
     *
     * After stop(), currentTrackId.value MUST be null.
     */
    @Test
    fun singleActivePlayerInvariant() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbTrackPairList) { trackPairs ->
            for ((trackId, audioUrl) in trackPairs) {
                AudioPlayerManager.play(trackId, audioUrl)

                // Invariant: currentTrackId must be the LAST played trackId
                assertEquals(
                    trackId,
                    AudioPlayerManager.currentTrackId.value,
                    "After play($trackId, ...), currentTrackId should be $trackId"
                )
            }

            // After stop(), no track should be active
            AudioPlayerManager.stop()
            assertEquals(
                null,
                AudioPlayerManager.currentTrackId.value,
                "After stop(), currentTrackId should be null"
            )
        }
    }

    /**
     * Property 2: 播放/暂停状态切换
     *
     * For any (trackId, audioUrl) pair, the state transitions are deterministic:
     *   play(trackId, audioUrl) → state is PLAYING (dummy implementation)
     *   pause()                 → state is PAUSED
     *   resume()                → state is PLAYING
     *   stop()                  → state is IDLE (cleanup)
     */
    @Test
    fun playPauseStateToggle() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbTrackPair) { (trackId, audioUrl) ->
            // Ensure clean state
            AudioPlayerManager.stop()
            assertEquals(PlayerState.IDLE, AudioPlayerManager.playerState.value)

            // play() → state should be PLAYING (dummy)
            AudioPlayerManager.play(trackId, audioUrl)
            assertEquals(
                PlayerState.PLAYING,
                AudioPlayerManager.playerState.value,
                "After play($trackId, ...), playerState should be PLAYING"
            )

            // pause() → state should be PAUSED
            AudioPlayerManager.pause()
            assertEquals(
                PlayerState.PAUSED,
                AudioPlayerManager.playerState.value,
                "After pause(), playerState should be PAUSED"
            )

            // resume() → state should be PLAYING
            AudioPlayerManager.resume()
            assertEquals(
                PlayerState.PLAYING,
                AudioPlayerManager.playerState.value,
                "After resume(), playerState should be PLAYING"
            )

            // Clean up
            AudioPlayerManager.stop()
            assertEquals(
                PlayerState.IDLE,
                AudioPlayerManager.playerState.value,
                "After stop(), playerState should be IDLE"
            )
        }
    }
}

    /**
     * Supplementary check: for any single play() call, the playerState should not remain IDLE
     * (it transitions to BUFFERING on play). After stop(), it returns to IDLE.
     *
     * // Feature: suno-client-completion, Property 1: 单一活跃播放器不变量
     * **Validates: Requirements 1.4**
     */
    @Test
    fun playTransitionsFromIdle() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbTrackPair) { (trackId, audioUrl) ->
            // Ensure clean state
            AudioPlayerManager.stop()
            assertEquals(PlayerState.IDLE, AudioPlayerManager.playerState.value)

            try {
                AudioPlayerManager.play(trackId, audioUrl)
            } catch (_: Exception) {
                // Audio backend may not be available in test
            }

            // After play(), state should have transitioned away from IDLE
            // (it's set to BUFFERING synchronously in play())
            assertTrue(
                AudioPlayerManager.playerState.value != PlayerState.IDLE,
                "After play(), playerState should not be IDLE (expected BUFFERING or later state)"
            )

            // Clean up
            AudioPlayerManager.stop()
            assertEquals(PlayerState.IDLE, AudioPlayerManager.playerState.value)
        }
    }
}
