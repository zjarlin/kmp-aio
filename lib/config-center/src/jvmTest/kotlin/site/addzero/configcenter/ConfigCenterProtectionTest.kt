package site.addzero.configcenter

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class ConfigCenterProtectionTest {
    @Test
    fun `builtin required definitions keep metadata locked`() {
        val root = Files.createTempDirectory("config-center-protection-test")
        val dbPath = root.resolve("config-center.sqlite")
        val service = JdbcConfigCenterValueService(
            ConfigCenterJdbcSettings(
                url = "jdbc:sqlite:${dbPath.toAbsolutePath()}",
            ),
        )

        service.upsertDefinitions(
            listOf(
                ConfigCenterKeyDefinition(
                    namespace = "kcloud",
                    key = "ktor.environment",
                    valueType = "kotlin.String",
                    comment = "Active runtime environment.",
                    defaultValue = "dev",
                    required = true,
                    source = "ksp",
                    builtin = true,
                    editable = false,
                    deletable = false,
                ),
            ),
        )

        val updated = service.writeEntry(
            ConfigCenterEntryWriteRequest(
                namespace = "kcloud",
                active = "dev",
                key = "ktor.environment",
                value = "prod",
                comment = "should be ignored",
                defaultValue = "test",
                valueType = "kotlin.Int",
                required = false,
            ),
        )

        assertEquals("prod", updated.value)
        assertEquals("Active runtime environment.", updated.comment)
        assertEquals("dev", updated.defaultValue)
        assertEquals("kotlin.String", updated.valueType)
        assertTrue(updated.required)
        assertEquals("ksp", updated.source)
        assertTrue(updated.builtin)
        assertFalse(updated.editable)
        assertFalse(updated.deletable)
    }

    @Test
    fun `builtin namespaces and entries cannot be deleted or renamed`() {
        val root = Files.createTempDirectory("config-center-protection-namespace-test")
        val dbPath = root.resolve("config-center.sqlite")
        val service = JdbcConfigCenterValueService(
            ConfigCenterJdbcSettings(
                url = "jdbc:sqlite:${dbPath.toAbsolutePath()}",
            ),
        )

        service.upsertDefinitions(
            listOf(
                ConfigCenterKeyDefinition(
                    namespace = "kcloud",
                    key = "ktor.environment",
                    valueType = "kotlin.String",
                    required = true,
                    source = "ksp",
                    builtin = true,
                    editable = false,
                    deletable = false,
                ),
            ),
        )

        assertFalse(
            service.deleteEntry(
                namespace = "kcloud",
                key = "ktor.environment",
                active = "dev",
            ),
        )
        assertFalse(service.deleteNamespace("kcloud"))
        assertFailsWith<IllegalStateException> {
            service.writeNamespace(
                ConfigCenterNamespaceWriteRequest(
                    namespace = "renamed-kcloud",
                    renameFrom = "kcloud",
                ),
            )
        }
    }
}
