package site.addzero.kcloud

import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.core.network.HttpClientFactory
import site.addzero.kcloud.app.KCloudRouteCatalog
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.kcloud.plugins.mcuconsole.client.McuConsoleWorkbenchState
import site.addzero.kcloud.plugins.system.rbac.UserCenterWorkbenchState
import site.addzero.kcloud.plugins.system.aichat.AiChatWorkbenchState
import site.addzero.kcloud.plugins.system.configcenter.ConfigCenterWorkbenchState
import site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseWorkbenchState
import site.addzero.kcloud.screens.creativeassets.CreativeAssetsViewModel
import site.addzero.kcloud.screens.musicstudio.MusicStudioViewModel
import site.addzero.kcloud.screens.settings.SettingsViewModel
import kotlin.test.Test
import kotlin.test.assertNotNull

class KCloudComposeKoinApplicationTest {
    @Test
    fun composeConfigurationAggregatesFeatureModules() {
        val application = koinApplication {
            withConfiguration<KCloudComposeKoinApplication>()
        }

        try {
            val koin = application.koin
            assertNotNull(koin.getOrNull<HttpClientFactory>())
            assertNotNull(koin.getOrNull<KCloudShellState>())
            assertNotNull(koin.getOrNull<ShellSettingsService>())
            assertNotNull(koin.getOrNull<McuConsoleWorkbenchState>())
            assertNotNull(koin.getOrNull<ConfigCenterWorkbenchState>())
            assertNotNull(koin.getOrNull<UserCenterWorkbenchState>())
            assertNotNull(koin.getOrNull<AiChatWorkbenchState>())
            assertNotNull(koin.getOrNull<KnowledgeBaseWorkbenchState>())
            assertNotNull(koin.getOrNull<MusicStudioViewModel>())
            assertNotNull(koin.getOrNull<CreativeAssetsViewModel>())
            assertNotNull(koin.getOrNull<SettingsViewModel>())
        } finally {
            application.close()
        }
    }

    @Test
    fun desktopSupplementConfigurationProvidesWorkbenchShell() {
        val application = koinApplication {
            withConfiguration<KCloudDesktopSupplementKoinApplication>()
        }

        try {
            val koin = application.koin
            assertNotNull(koin.getOrNull<HttpClientFactory>())
            assertNotNull(koin.getOrNull<KCloudShellState>())
            assertNotNull(koin.getOrNull<KCloudRouteCatalog>())
            assertNotNull(koin.getOrNull<ShellSettingsService>())
            assertNotNull(koin.getOrNull<AiChatWorkbenchState>())
        } finally {
            application.close()
        }
    }
}
