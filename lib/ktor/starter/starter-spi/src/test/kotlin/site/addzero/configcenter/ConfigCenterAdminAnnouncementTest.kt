package site.addzero.configcenter

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
        assertContains(announcement.orEmpty(), "http://0.0.0.0:19090/meta/config")
        assertContains(announcement.orEmpty(), "配置元数据")
    }

    @Test
    fun `resolve admin link throws when port missing`() {
        val applicationConfig = HoconApplicationConfig(
            ConfigFactory.parseString(
                """
                config-center.enabled = true
                config-center.jdbc.url = "jdbc:sqlite:./config-center.sqlite"
                config-center.admin.path = "/config-center"
                """.trimIndent(),
            ),
        )

        assertFailsWith<IllegalStateException> {
            applicationConfig.resolveConfigCenterAdminLink()
        }
    }

    @Test
    fun `resolve admin link uses configured host and port only`() {
        val applicationConfig = HoconApplicationConfig(
            ConfigFactory.parseString(
                """
                config-center.enabled = true
                config-center.jdbc.url = "jdbc:sqlite:./config-center.sqlite"
                config-center.admin.path = "/config-center"
                ktor.deployment.host = "config-center.internal"
                ktor.deployment.port = 61587
                """.trimIndent(),
            ),
        )

        assertEquals(
            "http://config-center.internal:61587/config-center",
            applicationConfig.resolveConfigCenterAdminLink(),
        )
    }

    @Test
    fun `build announcement skips when jdbc config is absent`() {
        val applicationConfig = HoconApplicationConfig(ConfigFactory.empty())

        assertNull(buildConfigCenterAdminAnnouncement(applicationConfig))
    }
}
