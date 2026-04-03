package site.addzero.kcloud.server

import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import site.addzero.kcloud.jimmer.di.DatasourceRegistry
import site.addzero.starter.AppStarter
import site.addzero.starter.banner.BannerStarter
import site.addzero.starter.flyway.FlywayStarter
import site.addzero.starter.openapi.OpenApiStarter
import site.addzero.starter.serialization.SerializationStarter
import site.addzero.starter.statuspages.StatusPagesStarter
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KCloudServerKoinApplicationTest {
    @Test
    fun serverRuntimeAggregatesCoreStarterModulesWithoutConfigurationTags() {
        val tempDir = Files.createTempDirectory("kcloud-server-koin-test")
        val dbFile = tempDir.resolve("kcloud-test.sqlite")
        val configFile = tempDir.resolve("application-test.conf")
        Files.writeString(
            configFile,
            """
            ktor {
              deployment {
                host = "127.0.0.1"
                port = 0
              }
              environment = "test"
            }
            banner {
              text = "KCLOUD [TEST]"
              subtitle = "Koin Smoke"
            }
            openapi {
              enabled = false
            }
            flyway {
              enabled = false
            }
            s3 {
              enabled = false
            }
            datasources {
              sqlite {
                enabled = true
                url = "jdbc:sqlite:${dbFile.toAbsolutePath()}"
                driver = "org.sqlite.JDBC"
              }
              postgres {
                enabled = false
              }
            }
            """.trimIndent(),
        )

        val server = serverApplication(
            configPath = configFile.toString(),
            host = "127.0.0.1",
            port = 0,
        )

        try {
            server.start(wait = false)
            val koin = GlobalContext.get()
            val starters = koin.getAll<AppStarter>()

            assertTrue(starters.any { starter -> starter is BannerStarter })
            assertTrue(starters.any { starter -> starter is FlywayStarter })
            assertTrue(starters.any { starter -> starter is OpenApiStarter })
            assertTrue(starters.any { starter -> starter is SerializationStarter })
            assertTrue(starters.any { starter -> starter is StatusPagesStarter })
            assertNotNull(koin.get<DatasourceRegistry>())
        } finally {
            server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
            stopKoin()
        }
    }
}
