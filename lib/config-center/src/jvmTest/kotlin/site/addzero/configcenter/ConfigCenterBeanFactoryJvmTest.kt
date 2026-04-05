package site.addzero.configcenter

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigCenterBeanFactoryJvmTest {
    @Test
    fun `companion env builds snapshot directly from jdbc url`() {
        val root = Files.createTempDirectory("config-center-bean-factory-jvm-test")
        val dbPath = root.resolve("config-center.sqlite")
        val dbUrl = "jdbc:sqlite:${dbPath.toAbsolutePath()}"
        val service = JdbcConfigCenterValueService(
            ConfigCenterJdbcSettings(
                url = dbUrl,
            ),
        )

        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "kcloud",
                active = "prod",
                key = "server.host",
                value = "127.0.0.1",
            ),
        )
        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = "kcloud",
                active = "prod",
                key = "server.base-url",
                value = "http://${'$'}{server.host}:8080",
            ),
        )

        val env = ConfigCenterBeanFactory.env(
            url = dbUrl,
            namespace = "kcloud",
            active = "prod",
        )

        assertEquals("127.0.0.1", env.string("server.host"))
        assertEquals("http://127.0.0.1:8080", env.string("server.base-url"))
    }
}
