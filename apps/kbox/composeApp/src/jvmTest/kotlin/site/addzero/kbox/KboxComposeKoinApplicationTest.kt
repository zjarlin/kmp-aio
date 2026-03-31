package site.addzero.kbox

import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kbox.app.KboxRouteCatalog
import site.addzero.kbox.app.KboxShellState
import site.addzero.kbox.plugin.api.KboxPluginManagerService
import site.addzero.kbox.plugins.system.pluginmanager.KboxPluginManagerState
import site.addzero.kbox.plugins.tools.storagetool.KboxStorageToolState
import site.addzero.kbox.plugins.tools.storagetool.KboxSyncToolState
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KboxComposeKoinApplicationTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun composeConfigurationAggregatesBuiltInPlugins() {
        startKoin {
            withConfiguration<KboxComposeKoinApplication>()
        }

        val koin = KoinPlatform.getKoin()
        val routeCatalog = koin.get<KboxRouteCatalog>()

        assertNotNull(koin.getOrNull<KboxShellState>())
        assertNotNull(koin.getOrNull<KboxPluginManagerService>())
        assertNotNull(koin.getOrNull<KboxPluginManagerState>())
        assertNotNull(koin.getOrNull<KboxStorageToolState>())
        assertNotNull(koin.getOrNull<KboxSyncToolState>())
        assertNotNull(routeCatalog.findRoute("system/plugin-manager"))
        assertNotNull(routeCatalog.findRoute("tools/storage-tool"))
    }

    @Test
    fun runtimePluginInstallShouldUpdateHostRouteCatalogWithoutRestart() = runBlocking {
        val previousHome = System.getProperty("user.home")
        val temporaryHome = createTempDirectory("kbox-compose-home").toFile()
        System.setProperty("user.home", temporaryHome.absolutePath)

        try {
            startKoin {
                withConfiguration<KboxComposeKoinApplication>()
            }

            val koin = KoinPlatform.getKoin()
            val manager = koin.get<KboxPluginManagerService>()
            val routeCatalog = koin.get<KboxRouteCatalog>()

            manager.refresh()
            assertNull(routeCatalog.findRoute("runtime/hello-runtime"))

            val installResult = manager.installFromDirectory(runtimeFixtureDir().absolutePath)
            assertTrue(installResult.success, installResult.message)
            assertNotNull(routeCatalog.findRoute("runtime/hello-runtime"))

            val disableResult = manager.disable("hello-runtime")
            assertTrue(disableResult.success, disableResult.message)
            assertNull(routeCatalog.findRoute("runtime/hello-runtime"))

            val uninstallResult = manager.uninstall("hello-runtime")
            assertTrue(uninstallResult.success, uninstallResult.message)
        } finally {
            if (previousHome.isNullOrBlank()) {
                System.clearProperty("user.home")
            } else {
                System.setProperty("user.home", previousHome)
            }
        }
    }

    private fun runtimeFixtureDir() = java.io.File(
        System.getProperty("kbox.runtimeFixtureDir").orEmpty(),
    ).also { directory ->
        assertTrue(directory.isDirectory, "缺少运行时插件目录夹具：${directory.absolutePath}")
    }
}
