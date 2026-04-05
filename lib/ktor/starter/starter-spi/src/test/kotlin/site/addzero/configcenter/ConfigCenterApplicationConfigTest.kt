package site.addzero.configcenter

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import java.nio.file.Files
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigCenterApplicationConfigTest {
    @Test
    fun `read values auto creates schema and exposes overrides`() {
        val root = Files.createTempDirectory("config-center-config-test")
        val dbPath = root.resolve("config-center.sqlite")
        val dbUrl = "jdbc:sqlite:${dbPath.toAbsolutePath()}"
        val applicationConfig = HoconApplicationConfig(
            ConfigFactory.parseString(
                """
                config-center.jdbc.url = "$dbUrl"
                config-center.jdbc.auto-ddl = true
                """.trimIndent(),
            ),
        )

        val service = JdbcConfigCenterValueService(
            ConfigCenterJdbcSettings(
                url = dbUrl,
            ),
        )
        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "demo-app",
                active = "dev",
                key = "demo.message",
                value = "hello-config-center",
            ),
        )

        val values = applicationConfig.readConfigCenterValues(
            namespace = "demo-app",
            active = "dev",
        )
        val effectiveConfig = applicationConfig.withConfigCenterOverrides(
            namespace = "demo-app",
            active = "dev",
        )

        assertEquals("hello-config-center", values["demo.message"])
        assertEquals("hello-config-center", effectiveConfig.property("demo.message").getString())

        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection(dbUrl).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(
                    """
                    SELECT COUNT(*) FROM sqlite_master
                    WHERE type = 'table' AND name = 'config_center_value'
                    """.trimIndent(),
                ).use { resultSet ->
                    assertTrue(resultSet.next())
                    assertEquals(1, resultSet.getInt(1))
                }
                statement.executeQuery(
                    """
                    PRAGMA table_info(config_center_definition)
                    """.trimIndent(),
                ).use { resultSet ->
                    val columns = linkedSetOf<String>()
                    while (resultSet.next()) {
                        columns += resultSet.getString("name").orEmpty()
                    }
                    assertTrue("builtin" in columns)
                    assertTrue("editable" in columns)
                    assertTrue("deletable" in columns)
                }
            }
        }
    }
}
