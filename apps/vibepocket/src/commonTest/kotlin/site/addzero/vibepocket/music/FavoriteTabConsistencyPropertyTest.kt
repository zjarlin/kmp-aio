package site.addzero.vibepocket.music

import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import site.addzero.vibepocket.model.FavoriteItem
import site.addzero.vibepocket.model.SunoTrack
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Property-based tests for Favorite Tab data consistency.
 *
 * // Feature: suno-client-completion, Property 6: 收藏 Tab 数据一致性
 * **Validates: Requirements 2.9**
 *
 * Verifies that for any list of FavoriteItem objects, converting them to
 * SunoTrack display format preserves all data — the displayed Track set
 * is fully consistent with the source FavoriteItem list.
 */
@OptIn(ExperimentalKotest::class)
class FavoriteTabConsistencyPropertyTest {

    /** Generates finite Double values only (no NaN, no Infinity). */
    private val arbFiniteDuration: Arb<Double> = arbitrary {
        var value = Arb.double(min = 0.0, max = 86400.0).bind()
        while (!value.isFinite()) {
            value = Arb.double(min = 0.0, max = 86400.0).bind()
        }
        value
    }

    /** Generator for random [FavoriteItem] instances. */
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

    /** Generator for a list of FavoriteItem (0..10 items). */
    private val arbFavoriteList: Arb<List<FavoriteItem>> =
        Arb.list(arbFavoriteItem, range = 0..10)


    /**
     * Property 6: 收藏 Tab 数据一致性
     *
     * For any list of FavoriteItem objects (simulating GET /api/favorites response),
     * converting each to SunoTrack for display must:
     * 1. Produce the same number of display items as source items
     * 2. Preserve trackId (mapped to SunoTrack.id), audioUrl, title, tags, imageUrl, duration
     *
     * This ensures the "收藏" Tab displays data fully consistent with the API response.
     *
     * // Feature: suno-client-completion, Property 6: 收藏 Tab 数据一致性
     * **Validates: Requirements 2.9**
     */
    @Test
    fun favoriteListConversionPreservesAllData() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbFavoriteList) { favorites ->
            val displayTracks: List<SunoTrack> = favorites.map { it.toSunoTrack() }

            // Same count
            assertEquals(
                favorites.size,
                displayTracks.size,
                "Display track count must equal favorite item count"
            )

            // Each item's fields must be preserved
            favorites.zip(displayTracks).forEach { (fav, track) ->
                assertEquals(fav.trackId, track.id,
                    "trackId must map to SunoTrack.id")
                assertEquals(fav.audioUrl, track.audioUrl,
                    "audioUrl must be preserved")
                assertEquals(fav.title, track.title,
                    "title must be preserved")
                assertEquals(fav.tags, track.tags,
                    "tags must be preserved")
                assertEquals(fav.imageUrl, track.imageUrl,
                    "imageUrl must be preserved")
                assertEquals(fav.duration, track.duration,
                    "duration must be preserved")
            }
        }
    }

    /**
     * Property 6 (supplementary): Favorite trackId set consistency.
     *
     * For any list of FavoriteItem objects, the set of trackIds from the source
     * must exactly match the set of ids from the converted SunoTrack list.
     * This ensures no tracks are lost or duplicated during conversion.
     *
     * // Feature: suno-client-completion, Property 6: 收藏 Tab 数据一致性
     * **Validates: Requirements 2.9**
     */
    @Test
    fun favoriteTrackIdSetIsConsistent() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbFavoriteList) { favorites ->
            val sourceTrackIds = favorites.map { it.trackId }
            val displayTrackIds = favorites.map { it.toSunoTrack().id }

            assertEquals(
                sourceTrackIds,
                displayTrackIds,
                "Track ID ordering and values must be preserved after conversion"
            )
        }
    }
}
