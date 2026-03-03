package site.addzero.vibepocket.model

import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Property-based tests for MusicHistoryItem serialization round-trip.
 *
 * // Feature: suno-client-completion, Property 14: MusicHistoryItem 序列化 round-trip
 * **Validates: Requirements 16.2**
 */
@OptIn(ExperimentalKotest::class)
class HistoryModelsPropertyTest {

    private val json = Json { encodeDefaults = true }

    /**
     * Generates finite Double values only (no NaN, no Infinity).
     * Duration values are constrained to a reasonable range for audio tracks.
     */
    private val arbFiniteDuration: Arb<Double> = Arb.double(
        min = 0.0,
        max = 86400.0, // up to 24 hours in seconds
    ).let { arb ->
        arbitrary {
            var value = arb.bind()
            while (!value.isFinite()) {
                value = arb.bind()
            }
            value
        }
    }

    /**
     * Generator for random [MusicHistoryTrack] instances.
     * - id, audioUrl, title, tags, imageUrl are nullable strings
     * - duration is a nullable finite Double
     */
    private val arbMusicHistoryTrack: Arb<MusicHistoryTrack> = arbitrary {
        MusicHistoryTrack(
            id = Arb.string(minSize = 1, maxSize = 50).orNull(nullProbability = 0.3).bind(),
            audioUrl = Arb.string(minSize = 1, maxSize = 200).orNull(nullProbability = 0.3).bind(),
            title = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.3).bind(),
            tags = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.3).bind(),
            imageUrl = Arb.string(minSize = 1, maxSize = 200).orNull(nullProbability = 0.3).bind(),
            duration = arbFiniteDuration.orNull(nullProbability = 0.3).bind(),
        )
    }

    /**
     * Generator for random [MusicHistoryItem] instances.
     * - id is a nullable Long
     * - taskId, type, status are non-empty strings (1..50 chars)
     * - tracks is a small list (0..5) of MusicHistoryTrack
     * - createdAt is a nullable string
     */
    private val arbMusicHistoryItem: Arb<MusicHistoryItem> = arbitrary {
        MusicHistoryItem(
            id = Arb.long(1L..Long.MAX_VALUE).orNull(nullProbability = 0.3).bind(),
            taskId = Arb.string(minSize = 1, maxSize = 50).bind(),
            type = Arb.string(minSize = 1, maxSize = 50).bind(),
            status = Arb.string(minSize = 1, maxSize = 50).bind(),
            tracks = Arb.list(arbMusicHistoryTrack, range = 0..5).bind(),
            createdAt = Arb.string(minSize = 1, maxSize = 30).orNull(nullProbability = 0.3).bind(),
        )
    }

    /**
     * Property 14: MusicHistoryItem 序列化 round-trip
     *
     * For any valid MusicHistoryItem object (including nested MusicHistoryTrack lists),
     * serializing it to JSON and then deserializing the JSON back produces an object
     * equivalent to the original.
     *
     * // Feature: suno-client-completion, Property 14: MusicHistoryItem 序列化 round-trip
     * **Validates: Requirements 16.2**
     */
    @Test
    fun musicHistoryItemJsonRoundTrip() = runTest {
        val serializer = serializer<MusicHistoryItem>()
        checkAll(PropTestConfig(iterations = 100), arbMusicHistoryItem) { item ->
            val encoded = json.encodeToString(serializer, item)
            val decoded = json.decodeFromString(serializer, encoded)
            assertEquals(item, decoded)
        }
    }
}
