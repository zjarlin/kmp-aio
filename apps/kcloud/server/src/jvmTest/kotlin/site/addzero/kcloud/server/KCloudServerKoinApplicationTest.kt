package site.addzero.kcloud.server

import io.ktor.server.config.*
import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kcloud.jimmer.di.JIMMER_APPLICATION_CONFIG_PROPERTY
import site.addzero.kcloud.plugins.mcuconsole.service.McuFlashService
import site.addzero.kcloud.plugins.system.rbac.UserProfileService
import site.addzero.kcloud.plugins.system.aichat.AiChatService
import site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseService
import site.addzero.kcloud.vibepocket.service.MusicCatalogService
import site.addzero.kcloud.vibepocket.service.MusicLibCatalogService
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
            "datasources.sqlite.enabled" to "true",
            "datasources.sqlite.url" to "jdbc:sqlite:${tempDatabase.absolutePath}",
            "datasources.sqlite.driver" to "org.sqlite.JDBC",
        )
        val application = koinApplication {
            withConfiguration<KCloudServerStarterKoinApplication>()
            properties(
                mapOf(
                    JIMMER_APPLICATION_CONFIG_PROPERTY to config,
                    KCLOUD_APPLICATION_CONFIG_PROPERTY to config,
                    VIBEPOCKET_APPLICATION_CONFIG_PROPERTY to config,
                ),
            )
        }

        try {
            val koin = application.koin
            assertNotNull(koin.getOrNull<McuFlashService>())
            assertNotNull(koin.getOrNull<UserProfileService>())
            assertNotNull(koin.getOrNull<AiChatService>())
            assertNotNull(koin.getOrNull<KnowledgeBaseService>())
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
}

private const val KCLOUD_APPLICATION_CONFIG_PROPERTY = "kcloud.applicationConfig"
private const val VIBEPOCKET_APPLICATION_CONFIG_PROPERTY = "vibepocket.applicationConfig"
private const val VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY = "vibepocket.embedded.desktop"
