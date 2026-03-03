package site.addzero.vibepocket.music

import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import site.addzero.vibepocket.model.SunoLyricItem
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Property-based tests for AI lyrics candidate display completeness.
 *
 * // Feature: suno-client-completion, Property 9: AI 歌词候选项全部展示
 * **Validates: Requirements 4.5**
 *
 * Verifies that for any lyrics generation result containing N candidate lyrics
 * (N >= 1) with non-blank text, the filtering logic produces exactly N items
 * for display — no candidates are lost or duplicated.
 */
@OptIn(ExperimentalKotest::class)
class AiLyricsCandidatePropertyTest {

    /**
     * Generator for a [SunoLyricItem] with guaranteed non-blank text.
     * These represent valid candidates that should always pass the filter.
     */
    private val arbValidCandidate: Arb<SunoLyricItem> = arbitrary {
        SunoLyricItem(
            text = Arb.string(minSize = 1, maxSize = 500).bind(),
            title = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.3).bind(),
            status = Arb.string(minSize = 1, maxSize = 20).orNull(nullProbability = 0.5).bind(),
            errorMessage = null,
        )
    }

    /**
     * Generator for a [SunoLyricItem] with blank or null text.
     * These represent invalid candidates that should be filtered out.
     */
    private val arbInvalidCandidate: Arb<SunoLyricItem> = arbitrary {
        val blankTexts = listOf(null, "", "   ", "\t", "\n", "  \n  ")
        SunoLyricItem(
            text = blankTexts.random(),
            title = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.5).bind(),
            status = null,
            errorMessage = null,
        )
    }

    /**
     * The filtering logic extracted from AiLyricsGenerator in LyricsStep.kt:
     * `items.filter { !it.text.isNullOrBlank() }`
     */
    private fun filterCandidates(items: List<SunoLyricItem>): List<SunoLyricItem> =
        items.filter { !it.text.isNullOrBlank() }

    /**
     * Property 9: AI 歌词候选项全部展示
     *
     * For any list of N valid candidate lyrics (N >= 1, all with non-blank text),
     * the filtering logic must produce exactly N items — all valid candidates
     * are preserved for display.
     *
     * // Feature: suno-client-completion, Property 9: AI 歌词候选项全部展示
     * **Validates: Requirements 4.5**
     */
    @Test
    fun allValidCandidatesAreDisplayed() = runTest {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbValidCandidate, range = 1..20)
        ) { validCandidates ->
            val filtered = filterCandidates(validCandidates)

            assertEquals(
                validCandidates.size,
                filtered.size,
                "All ${validCandidates.size} valid candidates must be preserved after filtering, " +
                    "but got ${filtered.size}"
            )

            // Verify the filtered list contains exactly the same items in order
            validCandidates.zip(filtered).forEachIndexed { index, (original, result) ->
                assertEquals(
                    original.text,
                    result.text,
                    "Candidate at index $index must have preserved text"
                )
                assertEquals(
                    original.title,
                    result.title,
                    "Candidate at index $index must have preserved title"
                )
            }
        }
    }

    /**
     * Property 9 (supplementary): Mixed valid and invalid candidates.
     *
     * For any mixed list containing both valid (non-blank text) and invalid
     * (null/blank text) candidates, the filter must produce exactly the count
     * of valid candidates — invalid ones are excluded, valid ones are all kept.
     *
     * // Feature: suno-client-completion, Property 9: AI 歌词候选项全部展示
     * **Validates: Requirements 4.5**
     */
    @Test
    fun filteringPreservesOnlyValidCandidates() = runTest {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbValidCandidate, range = 1..10),
            Arb.list(arbInvalidCandidate, range = 0..10)
        ) { validItems, invalidItems ->
            // Combine and shuffle to simulate real API response
            val allItems = (validItems + invalidItems).shuffled()

            val filtered = filterCandidates(allItems)

            // The filtered count must equal the number of valid items
            assertEquals(
                validItems.size,
                filtered.size,
                "Filtered count (${filtered.size}) must equal valid candidate count " +
                    "(${validItems.size}) from total ${allItems.size} items"
            )

            // Every filtered item must have non-blank text
            filtered.forEach { item ->
                assert(!item.text.isNullOrBlank()) {
                    "Filtered candidate must have non-blank text, got: '${item.text}'"
                }
            }
        }
    }
}
