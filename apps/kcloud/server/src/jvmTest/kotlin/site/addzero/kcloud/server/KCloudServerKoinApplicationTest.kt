package site.addzero.kcloud.server

import io.ktor.server.config.MapApplicationConfig
import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kcloud.KCloudServerStarterKoinApplication
import site.addzero.kcloud.plugins.mcuconsole.service.McuFlashService
import site.addzero.vibepocket.screens.creativeassets.CreativeAssetsViewModel
import site.addzero.vibepocket.screens.musicstudio.MusicStudioViewModel
import site.addzero.vibepocket.screens.settings.SettingsViewModel
import site.addzero.vibepocket.service.MusicCatalogService
import site.addzero.vibepocket.service.MusicLibCatalogService
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class KCloudServerKoinApplicationTest {
    @Test
    fun serverConfigurationAggregatesPluginModules() {
        val tempDatabase = Files.createTempFile("kcloud-server-koin-test-", ".db").toFile()
        val previousEmbeddedFlag = System.getProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY)
        System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
        val config = MapApplicationConfig(
            "datasources.sqlite.url" to "jdbc:sqlite:${tempDatabase.absolutePath}",
            "datasources.sqlite.driver" to "org.sqlite.JDBC",
        )
        val application = koinApplication {
            withConfiguration<KCloudServerStarterKoinApplication>()
            properties(
                mapOf(
                    KCLOUD_APPLICATION_CONFIG_PROPERTY to config,
                    VIBEPOCKET_APPLICATION_CONFIG_PROPERTY to config,
                ),
            )
        }

        try {
            val koin = application.koin
            assertNotNull(koin.getOrNull<McuFlashService>())
            assertIs<MusicLibCatalogService>(koin.get<MusicCatalogService>())
        } finally {
            application.close()
            if (previousEmbeddedFlag == null) {
                System.clearProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY)
            } else {
                System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, previousEmbeddedFlag)
            }
            tempDatabase.delete()
        }
    }

    @Test
    fun serverConfigurationExposesVibepocketScreenStateHolders() {
        val tempDatabase = Files.createTempFile("kcloud-desktop-koin-test-", ".db").toFile()
        val previousEmbeddedFlag = System.getProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY)
        System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
        val config = MapApplicationConfig(
            "datasources.sqlite.url" to "jdbc:sqlite:${tempDatabase.absolutePath}",
            "datasources.sqlite.driver" to "org.sqlite.JDBC",
        )
        val application = koinApplication {
            withConfiguration<KCloudServerStarterKoinApplication>()
            properties(
                mapOf(
                    KCLOUD_APPLICATION_CONFIG_PROPERTY to config,
                    VIBEPOCKET_APPLICATION_CONFIG_PROPERTY to config,
                ),
            )
        }

        try {
            val koin = application.koin
            assertNotNull(koin.getOrNull<MusicStudioViewModel>())
            assertNotNull(koin.getOrNull<CreativeAssetsViewModel>())
            assertNotNull(koin.getOrNull<SettingsViewModel>())
        } finally {
            application.close()
            if (previousEmbeddedFlag == null) {
                System.clearProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY)
            } else {
                System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, previousEmbeddedFlag)
            }
            tempDatabase.delete()
        }
    }
}

private const val KCLOUD_APPLICATION_CONFIG_PROPERTY = "kcloud.applicationConfig"
private const val VIBEPOCKET_APPLICATION_CONFIG_PROPERTY = "vibepocket.applicationConfig"
private const val VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY = "vibepocket.embedded.desktop"
