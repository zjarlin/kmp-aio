package site.addzero.configcenter

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConfigCenterAdminAnnouncementTest {
    @Test
    fun `build announcement prints chinese h5 admin link`() {
        val applicationConfig = HoconApplicationConfig(
            ConfigFactory.parseString(
                """
                config-center.enabled = true
                config-center.jdbc.url = "jdbc:sqlite:./config-center.sqlite"
                config-center.admin.enabled = true
                config-center.admin.path = "/meta/config"
                ktor.deployment.host = "0.0.0.0"
                ktor.deployment.port = 19090
                """.trimIndent(),
            ),
        )

        val announcement = buildConfigCenterAdminAnnouncement(applicationConfig)

        assertContains(announcement.orEmpty(), "配置中心管理页已启用")
        assertContains(announcement.orEmpty(), "H5 页面")
        assertContains(announcement.orEmpty(), "http://127.0.0.1:19090/meta/config")
        assertContains(announcement.orEmpty(), "配置元数据")
    }

    @Test
    fun `resolve admin link falls back to relative path when port missing`() {
        val applicationConfig = HoconApplicationConfig(
            ConfigFactory.parseString(
                """
                config-center.enabled = true
                config-center.jdbc.url = "jdbc:sqlite:./config-center.sqlite"
                config-center.admin.path = "/config-center"
                """.trimIndent(),
            ),
        )

        assertEquals("/config-center", applicationConfig.resolveConfigCenterAdminLink())
    }

    @Test
    fun `resolve admin link prefers runtime base url property`() {
        val applicationConfig = HoconApplicationConfig(
            ConfigFactory.parseString(
                """
                config-center.enabled = true
                config-center.jdbc.url = "jdbc:sqlite:./config-center.sqlite"
                config-center.admin.path = "/config-center"
                ktor.deployment.host = "0.0.0.0"
                ktor.deployment.port = 8080
                """.trimIndent(),
            ),
        )

        val previous = System.getProperty(CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY)
        try {
            System.setProperty(CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY, "http://localhost:61587/")

            assertEquals(
                "http://localhost:61587/config-center",
                applicationConfig.resolveConfigCenterAdminLink(),
            )
        } finally {
            if (previous == null) {
                System.clearProperty(CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY)
            } else {
                System.setProperty(CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY, previous)
            }
        }
    }

    @Test
    fun `build announcement skips when jdbc config is absent`() {
        val applicationConfig = HoconApplicationConfig(ConfigFactory.empty())

        assertNull(buildConfigCenterAdminAnnouncement(applicationConfig))
    }
}
