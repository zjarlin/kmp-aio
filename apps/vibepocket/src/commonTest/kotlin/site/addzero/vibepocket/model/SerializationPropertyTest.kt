package site.addzero.vibepocket.model

import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import site.addzero.vibepocket.settings.ApiConfig
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Property-based tests for serialization round-trip consistency.
 *
 * // Feature: vibepocket-ui-overhaul, Property 1: ApiConfig JSON 序列化往返一致性
 * **Validates: Requirements 6.3**
 */
@OptIn(ExperimentalKotest::class)
class SerializationPropertyTest {

    private val json = Json { encodeDefaults = true }

    /**
     * Generator for random [ApiConfig] instances.
     * - key, baseUrl, and label are arbitrary strings (0..100 chars)
     */
    private val arbApiConfig: Arb<ApiConfig> = arbitrary {
        ApiConfig(
            key = Arb.string(minSize = 0, maxSize = 100).bind(),
            baseUrl = Arb.string(minSize = 0, maxSize = 100).bind(),
            label = Arb.string(minSize = 0, maxSize = 100).bind()
        )
    }

    /**
     * Property 1: ApiConfig JSON 序列化往返一致性
     *
     * For any valid ApiConfig object (with any string key, baseUrl, and label),
     * serializing it to JSON via kotlinx.serialization and then deserializing
     * the JSON back produces an object equivalent to the original.
     *
     * // Feature: vibepocket-ui-overhaul, Property 1: ApiConfig JSON 序列化往返一致性
     * **Validates: Requirements 6.3**
     */
    @Test
    fun apiConfigJsonRoundTrip() = runTest {
        val serializer = serializer<ApiConfig>()
        checkAll(PropTestConfig(iterations = 5), arbApiConfig) { config ->
            val encoded = json.encodeToString(serializer, config)
            val decoded = json.decodeFromString(serializer, encoded)
            assertEquals(config, decoded)
        }
    }
}
