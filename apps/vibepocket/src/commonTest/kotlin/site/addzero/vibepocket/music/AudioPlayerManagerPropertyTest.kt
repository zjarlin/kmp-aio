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

@OptIn(ExperimentalKotest::class)
class AudioPlayerManagerPropertyTest {
    private val arbTrackPair: Arb<Pair<String, String>> = arbitrary {
        val trackId = Arb.string(minSize = 1, maxSize = 30).bind()
        val audioUrl = Arb.string(minSize = 1, maxSize = 100).bind()
        trackId to audioUrl
    }

    private val arbTrackPairList: Arb<List<Pair<String, String>>> =
        Arb.list(arbTrackPair, range = 1..10)

    @Test
    fun singleActivePlayerInvariant() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbTrackPairList) { trackPairs ->
            AudioPlayerManager.stop()
            for ((trackId, audioUrl) in trackPairs) {
                AudioPlayerManager.play(trackId, audioUrl)
                assertEquals(
                    trackId,
                    AudioPlayerManager.currentTrackId.value,
                    "After play($trackId, ...), currentTrackId should be $trackId"
                )
            }

            AudioPlayerManager.stop()
            assertEquals(
                null,
                AudioPlayerManager.currentTrackId.value,
                "After stop(), currentTrackId should be null"
            )
        }
    }

    @Test
    fun playTransitionsFromIdle() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbTrackPair) { (trackId, audioUrl) ->
            AudioPlayerManager.stop()
            assertEquals(PlayerState.IDLE, AudioPlayerManager.playerState.value)

            AudioPlayerManager.play(trackId, audioUrl)
            assertTrue(
                AudioPlayerManager.playerState.value != PlayerState.IDLE,
                "After play(), playerState should leave IDLE immediately"
            )

            AudioPlayerManager.stop()
            assertEquals(
                PlayerState.IDLE,
                AudioPlayerManager.playerState.value,
                "After stop(), playerState should be IDLE"
            )
        }
    }
}
