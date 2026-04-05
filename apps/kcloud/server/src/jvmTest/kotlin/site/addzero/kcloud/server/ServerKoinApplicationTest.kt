package site.addzero.kcloud.server

import io.ktor.server.application.Application
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.config.withFallback
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterJdbcSettings
import site.addzero.configcenter.ConfigCenterValueWriteRequest
import site.addzero.configcenter.JdbcConfigCenterValueService
import site.addzero.configcenter.withConfigCenterOverrides
import site.addzero.kcloud.config.AppConfigKeys
import site.addzero.kcloud.jimmer.di.DatasourceRegistry
import site.addzero.kcloud.plugins.system.configcenter.spi.RuntimeConfigCenterActive
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

class ServerKoinApplicationTest {
    @Test
    fun serverRuntimeAggregatesCoreStarterModulesWithoutConfigurationTags() {
        val tempDir = Files.createTempDirectory("kcloud-server-koin-test")
        val dbFile = tempDir.resolve("kcloud-test.sqlite")
        val configFile = tempDir.resolve("application-test.conf")
        Files.writeString(
            configFile,
            """
            ktor {
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
        seedRuntimeConfigCenterValues(dbFile.toAbsolutePath().toString())

        embeddedApplicationConfigOverride = null
        embeddedDesktopKoinConfigurer = null
        val config = loadServerConfig(configFile.toString())
        val active = resolveConfigCenterActive(config)
        val runtimeSettings = resolveRuntimeSettings(
            config = config,
            active = active,
        )
        val effectiveConfig = HoconApplicationConfig(
            serverRuntimeOverrides().resolve(),
        ).withFallback(
            config.withConfigCenterOverrides(
                namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
                active = active,
            ),
        )
        val environment = applicationEnvironment {
            this.config = effectiveConfig
        }
        val server = embeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>(
            factory = Netty,
            environment = environment,
            configure = {
                connectors += EngineConnectorBuilder().apply {
                    host = runtimeSettings.serverHost
                    port = runtimeSettings.serverPort
                }
            },
            module = Application::module,
        )

        try {
            server.start(wait = false)
            val koin = GlobalContext.get()
            val starters = koin.getAll<AppStarter<Application>>()

            assertTrue(starters.any { starter -> starter is BannerStarter })
            assertTrue(starters.any { starter -> starter is FlywayStarter })
            assertTrue(starters.any { starter -> starter is OpenApiStarter })
            assertTrue(starters.any { starter -> starter is SerializationStarter })
            assertTrue(starters.any { starter -> starter is StatusPagesStarter })
            assertNotNull(koin.get<RuntimeConfigCenterActive>())
            assertNotNull(koin.get<ConfigCenterEnv>())
            assertNotNull(koin.get<DatasourceRegistry>())
        } finally {
            server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
            stopKoin()
        }
    }
}

private fun seedRuntimeConfigCenterValues(
    sqlitePath: String,
) {
    val dbUrl = "jdbc:sqlite:$sqlitePath"
    val service = JdbcConfigCenterValueService(
        ConfigCenterJdbcSettings(
            url = dbUrl,
        ),
    )
    val values = linkedMapOf(
        "banner.text" to "KCLOUD [TEST]",
        "banner.subtitle" to "Koin Smoke",
        "openapi.enabled" to "false",
        "flyway.enabled" to "false",
        "s3.enabled" to "false",
        AppConfigKeys.SERVER_HOST to "127.0.0.1",
        AppConfigKeys.SERVER_PORT to "0",
        AppConfigKeys.DESKTOP_SERVER_PUBLIC_HOST to "127.0.0.1",
        AppConfigKeys.DESKTOP_SERVER_PORT to "18180",
        AppConfigKeys.DESKTOP_APP_DIRECTORY_NAME to "kcloud-test",
        AppConfigKeys.DESKTOP_SQLITE_FILE_NAME to "kcloud-test.db",
        AppConfigKeys.DESKTOP_BANNER_TEXT to "KCLOUD [TEST]",
        AppConfigKeys.DESKTOP_BANNER_SUBTITLE to "Koin Smoke",
        AppConfigKeys.DESKTOP_OPENAPI_ENABLED to "false",
        AppConfigKeys.DESKTOP_OPENAPI_PATH to "/openapi",
        AppConfigKeys.DESKTOP_OPENAPI_SPEC to "/openapi.json",
        AppConfigKeys.DESKTOP_FLYWAY_ENABLED to "false",
        AppConfigKeys.DESKTOP_S3_ENABLED to "false",
        AppConfigKeys.SQLITE_ENABLED to "true",
        AppConfigKeys.SQLITE_URL to dbUrl,
        AppConfigKeys.SQLITE_DRIVER to "org.sqlite.JDBC",
        AppConfigKeys.POSTGRES_ENABLED to "false",
    )
    values.forEach { (key, value) ->
        service.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
                active = "test",
                key = key,
                value = value,
            ),
        )
    }
}
