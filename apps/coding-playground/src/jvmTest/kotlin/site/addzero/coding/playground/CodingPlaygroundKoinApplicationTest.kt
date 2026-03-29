package site.addzero.coding.playground

import org.koin.core.context.GlobalContext
import site.addzero.coding.playground.demo.beta.DemoBetaService
import site.addzero.coding.playground.server.config.PlaygroundServerSettings
import site.addzero.coding.playground.shared.dto.CodegenSearchRequest
import site.addzero.coding.playground.shared.service.CodegenProjectService
import site.addzero.demo.gamma.DemoGammaService
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodingPlaygroundKoinApplicationTest {
    @Test
    fun koinApplicationAggregatesServerConfigurationModules() {
        val tempDir = Files.createTempDirectory("coding-playground-koin-test")
        val previousDataDir = System.getProperty("coding.playground.data.dir")
        val previousDbUrl = System.getProperty("coding.playground.db.url")
        val previousHttpEnabled = System.getProperty("coding.playground.http.enabled")
        System.setProperty("coding.playground.data.dir", tempDir.toString())
        System.clearProperty("coding.playground.db.url")
        System.setProperty("coding.playground.http.enabled", "false")

        val runtime = createCodingPlaygroundRuntime(httpServerEnabled = false)
        try {
            val koin = GlobalContext.get()
            val settings = koin.get<PlaygroundServerSettings>()
            val projectService = koin.get<CodegenProjectService>()
            val demoBetaService = koin.get<DemoBetaService>()
            assertTrue(settings.dataDirectory.startsWith(tempDir))
            assertEquals(emptyList(), kotlinx.coroutines.runBlocking {
                projectService.list(CodegenSearchRequest())
            })
            assertEquals("beta:alpha", demoBetaService.marker())
            assertEquals("gamma", koin.get<DemoGammaService>().marker())
        } finally {
            runtime.stop()
            restoreSystemProperty("coding.playground.data.dir", previousDataDir)
            restoreSystemProperty("coding.playground.db.url", previousDbUrl)
            restoreSystemProperty("coding.playground.http.enabled", previousHttpEnabled)
        }
    }
}

private fun restoreSystemProperty(key: String, value: String?) {
    if (value == null) {
        System.clearProperty(key)
    } else {
        System.setProperty(key, value)
    }
}
