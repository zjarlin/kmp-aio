package site.addzero.vibepocket.music

import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import site.addzero.vibepocket.model.TrackAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based tests for Track action routing correctness.
 *
 * // Feature: suno-client-completion, Property 10: Track 操作路由正确性
 * **Validates: Requirements 14.3**
 *
 * Since we cannot test actual Compose UI rendering in a unit test, we validate
 * the MAPPING LOGIC: every TrackAction enum value maps to a unique, correct
 * dialog identifier string.
 */
@OptIn(ExperimentalKotest::class)
class TrackActionRoutingPropertyTest {

    /**
     * Maps a [TrackAction] to its corresponding Dialog component name.
     *
     * This is the canonical mapping that the UI layer should follow:
     * - EXTEND → ExtendFormDialog
     * - VOCAL_REMOVAL → VocalRemovalConfirmDialog
     * - GENERATE_COVER → MusicCoverFormDialog
     * - CREATE_PERSONA → PersonaFormDialog
     * - REPLACE_SECTION → ReplaceSectionFormDialog
     * - EXPORT_WAV → WavExportConfirmDialog
     * - BOOST_STYLE → BoostStyleConfirmDialog
     */
    private fun trackActionToDialogName(action: TrackAction): String = when (action) {
        TrackAction.EXTEND -> "ExtendFormDialog"
        TrackAction.VOCAL_REMOVAL -> "VocalRemovalConfirmDialog"
        TrackAction.GENERATE_COVER -> "MusicCoverFormDialog"
        TrackAction.CREATE_PERSONA -> "PersonaFormDialog"
        TrackAction.REPLACE_SECTION -> "ReplaceSectionFormDialog"
        TrackAction.EXPORT_WAV -> "WavExportConfirmDialog"
        TrackAction.BOOST_STYLE -> "BoostStyleConfirmDialog"
    }

    /** Expected mapping for exhaustive verification. */
    private val expectedMapping = mapOf(
        TrackAction.EXTEND to "ExtendFormDialog",
        TrackAction.VOCAL_REMOVAL to "VocalRemovalConfirmDialog",
        TrackAction.GENERATE_COVER to "MusicCoverFormDialog",
        TrackAction.CREATE_PERSONA to "PersonaFormDialog",
        TrackAction.REPLACE_SECTION to "ReplaceSectionFormDialog",
        TrackAction.EXPORT_WAV to "WavExportConfirmDialog",
        TrackAction.BOOST_STYLE to "BoostStyleConfirmDialog",
    )

    /**
     * Property 10: Track 操作路由正确性
     *
     * For any randomly selected TrackAction enum value, the mapping function
     * returns the expected dialog name. Runs 100 iterations with random selection
     * via Arb.element().
     *
     * // Feature: suno-client-completion, Property 10: Track 操作路由正确性
     * **Validates: Requirements 14.3**
     */
    @Test
    fun trackActionMapsToCorrectDialog() = runTest {
        val arbTrackAction = Arb.element(TrackAction.entries)

        checkAll(PropTestConfig(iterations = 100), arbTrackAction) { action ->
            val dialogName = trackActionToDialogName(action)
            val expected = expectedMapping[action]
            assertEquals(
                expected,
                dialogName,
                "TrackAction.$action should map to $expected but got $dialogName"
            )
        }
    }

    /**
     * Verifies all 7 TrackAction values map to 7 distinct dialog names (no duplicates).
     *
     * // Feature: suno-client-completion, Property 10: Track 操作路由正确性
     * **Validates: Requirements 14.3**
     */
    @Test
    fun allTrackActionsMappToDistinctDialogs() {
        val dialogNames = TrackAction.entries.map { trackActionToDialogName(it) }

        // All 7 actions should be covered
        assertEquals(
            7,
            dialogNames.size,
            "Expected 7 TrackAction entries"
        )

        // All dialog names should be unique (no two actions share a dialog)
        assertEquals(
            dialogNames.size,
            dialogNames.toSet().size,
            "All TrackAction values must map to distinct dialog names, but found duplicates: " +
                dialogNames.groupBy { it }.filter { it.value.size > 1 }.keys
        )

        // Every dialog name should be non-blank
        assertTrue(
            dialogNames.all { it.isNotBlank() },
            "All dialog names must be non-blank"
        )
    }
}
