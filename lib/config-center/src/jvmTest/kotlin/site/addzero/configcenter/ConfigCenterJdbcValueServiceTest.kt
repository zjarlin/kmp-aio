package site.addzero.configcenter

import java.nio.file.Files
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConfigCenterJdbcValueServiceTest {
    @Test
    fun `same namespace active path uses upsert and preserves create time`() {
        val service = createService()

        val first = service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "demo",
                active = "dev",
                path = "server.port",
                value = "18080",
            ),
        )
        Thread.sleep(10)
        val second = service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "demo",
                active = "dev",
                path = "server.port",
                value = "19090",
            ),
        )

        assertEquals("18080", first.value)
        assertEquals("19090", second.value)
        assertNotNull(first.createTimeMillis)
        assertNotNull(first.updateTimeMillis)
        assertEquals(first.createTimeMillis, second.createTimeMillis)
        assertTrue(second.updateTimeMillis >= first.updateTimeMillis)
    }

    @Test
    fun `different active values do not override each other`() {
        val service = createService()

        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "demo",
                active = "dev",
                path = "server.host",
                value = "127.0.0.1",
            ),
        )
        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "demo",
                active = "prod",
                path = "server.host",
                value = "10.0.0.8",
            ),
        )

        assertEquals("127.0.0.1", service.readValue("demo", "server.host", "dev").value)
        assertEquals("10.0.0.8", service.readValue("demo", "server.host", "prod").value)
    }

    @Test
    fun `bean factory env resolves placeholders`() {
        val settings = createSettings()
        val service = JdbcConfigCenterValueService(settings)
        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "demo",
                active = "dev",
                path = "server.host",
                value = "127.0.0.1",
            ),
        )
        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "demo",
                active = "dev",
                path = "server.port",
                value = "18080",
            ),
        )
        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "demo",
                active = "dev",
                path = "server.base-url",
                value = "http://${'$'}{server.host}:${'$'}{server.port}",
            ),
        )

        val env = ConfigCenterBeanFactory.env(
            settings = settings,
            namespace = "demo",
            active = "dev",
        )

        assertEquals("http://127.0.0.1:18080", env.string("server.base-url"))
    }

    @Test
    fun `legacy config_center_value table is migrated on startup`() {
        val settings = createSettings()
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection(settings.url).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE config_center_value (
                        namespace TEXT NOT NULL,
                        active_profile TEXT NOT NULL,
                        config_key TEXT NOT NULL,
                        config_value TEXT NOT NULL,
                        create_time INTEGER NOT NULL,
                        update_time INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                statement.execute(
                    """
                    INSERT INTO config_center_value (
                        namespace,
                        active_profile,
                        config_key,
                        config_value,
                        create_time,
                        update_time
                    ) VALUES (
                        'demo',
                        'dev',
                        'server.port',
                        '18080',
                        11,
                        22
                    )
                    """.trimIndent(),
                )
            }
        }

        val service = JdbcConfigCenterValueService(settings)
        val migrated = service.readValue("demo", "server.port", "dev")

        assertEquals("18080", migrated.value)
        assertEquals(11L, migrated.createTimeMillis)
        assertEquals(22L, migrated.updateTimeMillis)
    }

    private fun createService(): JdbcConfigCenterValueService {
        return JdbcConfigCenterValueService(createSettings())
    }

    private fun createSettings(): ConfigCenterJdbcSettings {
        val dbFile = Files.createTempFile("config-center-jvm-test", ".sqlite")
        return ConfigCenterJdbcSettings(
            url = "jdbc:sqlite:${dbFile.toAbsolutePath()}",
        )
    }
}
