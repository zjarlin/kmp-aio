package site.addzero.kcloud

import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.kcloud.plugins.mcuconsole.client.McuConsoleWorkbenchState
import site.addzero.vibepocket.screens.creativeassets.CreativeAssetsViewModel
import site.addzero.vibepocket.screens.musicstudio.MusicStudioViewModel
import site.addzero.vibepocket.screens.settings.SettingsViewModel
import site.addzero.vibepocket.service.MusicCatalogService
import site.addzero.vibepocket.service.MusicLibCatalogService
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class KCloudComposeKoinApplicationTest {
    @Test
    fun composeConfigurationAggregatesFeatureModules() {
        val application = koinApplication {
            withConfiguration<KCloudComposeKoinApplication>()
        }

        try {
            val koin = application.koin
            assertNotNull(koin.getOrNull<KCloudShellState>())
            assertNotNull(koin.getOrNull<McuConsoleWorkbenchState>())
            assertNotNull(koin.getOrNull<MusicStudioViewModel>())
            assertNotNull(koin.getOrNull<CreativeAssetsViewModel>())
            assertNotNull(koin.getOrNull<SettingsViewModel>())
            assertIs<MusicLibCatalogService>(koin.get<MusicCatalogService>())
        } finally {
            application.close()
        }
    }

    @Test
    fun desktopSupplementConfigurationAggregatesShellModules() {
        val application = koinApplication {
            withConfiguration<KCloudDesktopSupplementKoinApplication>()
        }

        try {
            val koin = application.koin
            assertNotNull(koin.getOrNull<KCloudShellState>())
            assertNotNull(koin.getOrNull<McuConsoleWorkbenchState>())
        } finally {
            application.close()
        }
    }
}
