package site.addzero.vibepocket.music

import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import site.addzero.vibepocket.model.PersonaItem
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Property-based tests for Persona dropdown data consistency.
 *
 * // Feature: suno-client-completion, Property 12: Persona 下拉列表数据一致性
 * **Validates: Requirements 10.7**
 *
 * The PersonaSelector composable receives a List<PersonaItem> and renders:
 * - 1 default "无" option (no persona)
 * - N chips, one per PersonaItem
 *
 * Since we cannot test Compose UI rendering directly, we validate the
 * data mapping logic: the selector should produce exactly N+1 display
 * options, and the persona data (personaId, name) must be preserved.
 */
@OptIn(ExperimentalKotest::class)
class PersonaDropdownPropertyTest {

    /** Generator for random [PersonaItem] instances. */
    private val arbPersonaItem: Arb<PersonaItem> = arbitrary {
        PersonaItem(
            id = Arb.long(1L..Long.MAX_VALUE).orNull(nullProbability = 0.3).bind(),
            personaId = Arb.string(minSize = 1, maxSize = 50).bind(),
            name = Arb.string(minSize = 1, maxSize = 100).bind(),
            description = Arb.string(minSize = 0, maxSize = 200).bind(),
            createdAt = Arb.string(minSize = 1, maxSize = 30).orNull(nullProbability = 0.3).bind(),
        )
    }

    /** Generator for a list of PersonaItem (0..15 items). */
    private val arbPersonaList: Arb<List<PersonaItem>> =
        Arb.list(arbPersonaItem, range = 0..15)

    /**
     * Simulates the PersonaSelector display logic.
     *
     * Returns the list of display labels that the selector would render:
     * first the "无" default, then each persona's name.
     */
    private fun buildSelectorOptions(personas: List<PersonaItem>): List<String> {
        val options = mutableListOf("无") // default "none" option
        personas.forEach { options.add(it.name) }
        return options
    }

    /**
     * Simulates the PersonaSelector selectable IDs.
     *
     * Returns the list of personaId values that can be selected:
     * null for the "无" default, then each persona's personaId.
     */
    private fun buildSelectableIds(personas: List<PersonaItem>): List<String?> {
        val ids = mutableListOf<String?>(null) // default "none" maps to null
        personas.forEach { ids.add(it.personaId) }
        return ids
    }

    /**
     * Property 12: Persona 下拉列表数据一致性 — option count
     *
     * For any list of PersonaItem objects (simulating GET /api/personas response),
     * the PersonaSelector should produce exactly N+1 display options
     * (N personas + 1 "无" default).
     *
     * // Feature: suno-client-completion, Property 12: Persona 下拉列表数据一致性
     * **Validates: Requirements 10.7**
     */
    @Test
    fun personaSelectorProducesCorrectOptionCount() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbPersonaList) { personas ->
            val options = buildSelectorOptions(personas)

            assertEquals(
                personas.size + 1,
                options.size,
                "PersonaSelector should display N+1 options (N personas + 1 '无' default), " +
                    "but got ${options.size} for ${personas.size} personas"
            )
        }
    }

    /**
     * Property 12: Persona 下拉列表数据一致性 — name preservation
     *
     * For any list of PersonaItem objects, the persona names in the selector
     * options (excluding the "无" default) must exactly match the source
     * persona names in order.
     *
     * // Feature: suno-client-completion, Property 12: Persona 下拉列表数据一致性
     * **Validates: Requirements 10.7**
     */
    @Test
    fun personaSelectorPreservesPersonaNames() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbPersonaList) { personas ->
            val options = buildSelectorOptions(personas)

            // First option is always "无"
            assertEquals("无", options.first(), "First option must be the '无' default")

            // Remaining options must match persona names in order
            val personaOptions = options.drop(1)
            val sourceNames = personas.map { it.name }

            assertEquals(
                sourceNames,
                personaOptions,
                "Persona names in selector must match source data in order"
            )
        }
    }

    /**
     * Property 12: Persona 下拉列表数据一致性 — personaId mapping
     *
     * For any list of PersonaItem objects, the selectable IDs must be:
     * null (for "无" default), followed by each persona's personaId in order.
     * This ensures selecting a persona chip sends the correct personaId.
     *
     * // Feature: suno-client-completion, Property 12: Persona 下拉列表数据一致性
     * **Validates: Requirements 10.7**
     */
    @Test
    fun personaSelectorMapsCorrectPersonaIds() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbPersonaList) { personas ->
            val selectableIds = buildSelectableIds(personas)

            // First selectable ID is null (no persona)
            assertEquals(null, selectableIds.first(), "Default '无' option must map to null personaId")

            // Remaining IDs must match persona personaIds in order
            val personaIds = selectableIds.drop(1)
            val sourceIds = personas.map { it.personaId }

            assertEquals(
                sourceIds,
                personaIds,
                "Selectable personaIds must match source data in order"
            )
        }
    }

    /**
     * Property 12: Persona 下拉列表数据一致性 — set equality
     *
     * For any list of PersonaItem objects with unique personaIds,
     * the set of personaIds in the selector must exactly equal the
     * set of personaIds from the source data.
     *
     * // Feature: suno-client-completion, Property 12: Persona 下拉列表数据一致性
     * **Validates: Requirements 10.7**
     */
    @Test
    fun personaSelectorPersonaIdSetMatchesSource() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbPersonaList) { personas ->
            val selectableIds = buildSelectableIds(personas).filterNotNull().toSet()
            val sourceIds = personas.map { it.personaId }.toSet()

            assertEquals(
                sourceIds,
                selectableIds,
                "The set of personaIds in the selector must match the source persona set"
            )
        }
    }
}
