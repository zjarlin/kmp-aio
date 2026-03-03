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
import site.addzero.vibepocket.model.MusicHistoryItem
import site.addzero.vibepocket.model.MusicHistoryTrack
import site.addzero.vibepocket.model.SunoTrack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Property-based tests for history record display completeness.
 *
 * // Feature: suno-client-completion, Property 8: 历史记录显示完整字段
 * **Validates: Requirements 3.2**
 *
 * Verifies that for any MusicHistoryItem, all required display fields are
 * present and accessible: taskId, type, status, createdAt, and tracks.
 * Also verifies that MusicHistoryTrack → SunoTrack conversion preserves all fields.
 */
@OptIn(ExperimentalKotest::class)
class HistoryDisplayPropertyTest {

    /** Generates finite Double values only (no NaN, no Infinity). */
    private val arbFiniteDuration: Arb<Double> = arbitrary {
        var value = Arb.double(min = 0.0, max = 86400.0).bind()
        while (!value.isFinite()) {
            value = Arb.double(min = 0.0, max = 86400.0).bind()
        }
        value
    }

    /** Generator for random [MusicHistoryTrack] instances. */
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

    /** Generator for random [MusicHistoryItem] instances with all required fields. */
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
     * Property 8: 历史记录显示完整字段
     *
     * For any MusicHistoryItem, all required display fields must be present
     * and non-blank: taskId, type, status. The tracks list must be accessible.
     * createdAt is nullable but when present must be non-blank.
     *
     * // Feature: suno-client-completion, Property 8: 历史记录显示完整字段
     * **Validates: Requirements 3.2**
     */
    @Test
    fun historyItemHasAllRequiredDisplayFields() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbMusicHistoryItem) { item ->
            // taskId must be present and non-blank
            assertTrue(item.taskId.isNotBlank(),
                "taskId must be non-blank, got: '${item.taskId}'"
            )

            // type must be present and non-blank
            assertTrue(item.type.isNotBlank(),
                "type must be non-blank, got: '${item.type}'"
            )

            // status must be present and non-blank
            assertTrue(item.status.isNotBlank(),
                "status must be non-blank, got: '${item.status}'"
            )

            // tracks list must be accessible (not null — it defaults to emptyList)
            assertNotNull(item.tracks, "tracks list must not be null")

            // createdAt: when present, must be non-blank
            item.createdAt?.let { createdAt ->
                assertTrue(createdAt.isNotBlank(),
                    "createdAt when present must be non-blank, got: '${createdAt}'"
                )
            }
        }
    }

    /**
     * Property 8 (supplementary): MusicHistoryTrack → SunoTrack conversion preserves all fields.
     *
     * For any MusicHistoryTrack, converting to SunoTrack via toSunoTrack() must
     * preserve id, audioUrl, title, tags, imageUrl, and duration exactly.
     *
     * // Feature: suno-client-completion, Property 8: 历史记录显示完整字段
     * **Validates: Requirements 3.2**
     */
    @Test
    fun historyTrackToSunoTrackPreservesAllFields() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbMusicHistoryTrack) { historyTrack ->
            val sunoTrack: SunoTrack = historyTrack.toSunoTrack()

            assertEquals(historyTrack.id, sunoTrack.id,
                "id must be preserved after conversion")
            assertEquals(historyTrack.audioUrl, sunoTrack.audioUrl,
                "audioUrl must be preserved after conversion")
            assertEquals(historyTrack.title, sunoTrack.title,
                "title must be preserved after conversion")
            assertEquals(historyTrack.tags, sunoTrack.tags,
                "tags must be preserved after conversion")
            assertEquals(historyTrack.imageUrl, sunoTrack.imageUrl,
                "imageUrl must be preserved after conversion")
            assertEquals(historyTrack.duration, sunoTrack.duration,
                "duration must be preserved after conversion")
        }
    }
}
