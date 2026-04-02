package site.addzero.configcenter

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConfigCenterTemplateDefinitionTest {
    @Test
    fun `template definition metadata applies to dynamic config key`() {
        val root = Files.createTempDirectory("config-center-template-test")
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
                    key = "datasources.{name}.url",
                    valueType = "kotlin.String",
                    comment = "数据源 {name} 的 JDBC 地址。",
                ),
            ),
        )
        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "kcloud",
                active = "dev",
                key = "datasources.sqlite.url",
                value = "jdbc:sqlite:test.db",
            ),
        )

        val entry = service.readEntry(
            namespace = "kcloud",
            key = "datasources.sqlite.url",
            active = "dev",
        )
        assertEquals("datasources.sqlite.url", entry.key)
        assertEquals("jdbc:sqlite:test.db", entry.value)
        assertEquals("数据源 {name} 的 JDBC 地址。", entry.comment)
        assertEquals("kotlin.String", entry.valueType)

        val listedEntry = service.listEntries(
            namespace = "kcloud",
            active = "dev",
            keyword = "sqlite",
            limit = 20,
        ).firstOrNull { candidate -> candidate.key == "datasources.sqlite.url" }
        assertNotNull(listedEntry)
        assertEquals("数据源 {name} 的 JDBC 地址。", listedEntry.comment)
    }
}
