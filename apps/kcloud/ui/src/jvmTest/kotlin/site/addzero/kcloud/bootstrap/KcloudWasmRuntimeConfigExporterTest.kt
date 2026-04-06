package site.addzero.kcloud.bootstrap

import site.addzero.configcenter.ConfigCenterJdbcSettings
import site.addzero.configcenter.ConfigCenterValueWriteRequest
import site.addzero.configcenter.JdbcConfigCenterValueService
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KcloudWasmRuntimeConfigExporterTest {
    @Test
    fun `resolve runtime config reads public api base url from config center`() {
        val dbUrl = createTempConfigCenterDbUrl()
        val service = JdbcConfigCenterValueService(
            ConfigCenterJdbcSettings(
                url = dbUrl,
            ),
        )
        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "kcloud",
                active = "dev",
                path = "frontend.api.baseUrl",
                value = "https://pages.example.com/api",
            ),
        )
        val applicationConfig = buildExporterApplicationConfig(
            configPath = null,
            systemProperties = mapOf(
                "ktor.environment" to "dev",
                "config-center.jdbc.url" to dbUrl,
                "config-center.jdbc.auto-ddl" to "true",
            ),
            environmentVariables = emptyMap(),
        )

        val runtimeConfig = resolveWasmRuntimeConfig(applicationConfig)

        assertEquals("https://pages.example.com/api/", runtimeConfig.normalizedApiBaseUrl())
    }

    @Test
    fun `resolve runtime config fails fast when active is missing`() {
        val error = assertFailsWith<IllegalStateException> {
            val applicationConfig = buildExporterApplicationConfig(
                configPath = null,
                systemProperties = mapOf(
                    "config-center.jdbc.url" to "jdbc:sqlite:${Files.createTempFile("kcloud-runtime", ".sqlite")}",
                ),
                environmentVariables = emptyMap(),
            )
            resolveWasmRuntimeConfig(applicationConfig)
        }

        assertContains(error.message.orEmpty(), "ktor.environment")
    }

    @Test
    fun `resolve runtime config fails fast when public api base url is missing`() {
        val dbUrl = createTempConfigCenterDbUrl()
        val applicationConfig = buildExporterApplicationConfig(
            configPath = null,
            systemProperties = mapOf(
                "ktor.environment" to "prod",
                "config-center.jdbc.url" to dbUrl,
                "config-center.jdbc.auto-ddl" to "true",
            ),
            environmentVariables = emptyMap(),
        )

        val error = assertFailsWith<IllegalStateException> {
            resolveWasmRuntimeConfig(applicationConfig)
        }

        assertContains(error.message.orEmpty(), "frontend.api.baseUrl")
    }

    private fun createTempConfigCenterDbUrl(): String {
        val file = Files.createTempFile("kcloud-wasm-runtime-config", ".sqlite")
        return "jdbc:sqlite:${file.toAbsolutePath()}"
    }
}
