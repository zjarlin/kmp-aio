package site.addzero.vibepocket.model

import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
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
 * Property-based tests for FavoriteItem serialization round-trip.
 *
 * // Feature: suno-client-completion, Property 13: FavoriteItem 序列化 round-trip
 * **Validates: Requirements 16.1**
 */
@OptIn(ExperimentalKotest::class)
class FavoriteModelsPropertyTest {

    private val json = Json { encodeDefaults = true }

    /**
     * Generator for random [FavoriteItem] instances.
     * - id is a nullable Long
     * - trackId and taskId are non-empty strings (1..50 chars)
     * - audioUrl, title, tags, imageUrl, createdAt are nullable strings
     * - duration is a nullable finite Double (excludes NaN and Infinity)
     */
    private val arbFavoriteItem: Arb<FavoriteItem> = arbitrary {
        FavoriteItem(
            id = Arb.long(1L..Long.MAX_VALUE).orNull(nullProbability = 0.3).bind(),
            trackId = Arb.string(minSize = 1, maxSize = 50).bind(),
            taskId = Arb.string(minSize = 1, maxSize = 50).bind(),
            audioUrl = Arb.string(minSize = 1, maxSize = 200).orNull(nullProbability = 0.3).bind(),
            title = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.3).bind(),
            tags = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.3).bind(),
            imageUrl = Arb.string(minSize = 1, maxSize = 200).orNull(nullProbability = 0.3).bind(),
            duration = arbFiniteDuration.orNull(nullProbability = 0.3).bind(),
            createdAt = Arb.string(minSize = 1, maxSize = 30).orNull(nullProbability = 0.3).bind(),
        )
    }

    /**
     * Property 13: FavoriteItem 序列化 round-trip
     *
     * For any valid FavoriteItem object, serializing it to JSON and then
     * deserializing the JSON back produces an object equivalent to the original.
     *
     * // Feature: suno-client-completion, Property 13: FavoriteItem 序列化 round-trip
     * **Validates: Requirements 16.1**
     */
    @Test
    fun favoriteItemJsonRoundTrip() = runTest {
        val serializer = serializer<FavoriteItem>()
        checkAll(PropTestConfig(iterations = 100), arbFavoriteItem) { item ->
            val encoded = json.encodeToString(serializer, item)
            val decoded = json.decodeFromString(serializer, encoded)
            assertEquals(item, decoded)
        }
    }

    companion object {
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
    }
}
