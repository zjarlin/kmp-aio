package site.addzero.kcloud.plugins.mcuconsole.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureRequest
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureState
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortConnection
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec

class McuRuntimeServicesTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    private val codec = McuVmProtocolCodec(json)

    @Test
    fun `runtime bundle catalog reads builtin manifests`() {
        val catalog = McuRuntimeBundleCatalog(json)

        val bundles = catalog.listBundles()

        assertEquals("rhai-default-generic", bundles.defaultBundleId)
        assertTrue(bundles.items.any { it.bundleId == "rhai-default-generic" })
        assertTrue(bundles.items.any { it.bundleId == "micropython-default-generic" })
        assertTrue(bundles.items.first { it.bundleId == "rhai-default-generic" }.widgetTemplates.isNotEmpty())
    }

    @Test
    fun `runtime asset extractor expands builtin bundle to writable directory`() {
        val previousDir = System.getProperty("kcloud.mcu.runtime.dir")
        val targetDir = createTempDirectory(prefix = "mcu-runtime-assets-").toFile()
        try {
            System.setProperty("kcloud.mcu.runtime.dir", targetDir.absolutePath)
            val extractor = McuRuntimeAssetExtractor(McuRuntimeBundleCatalog(json))

            val extracted = extractor.extractBundle("rhai-default-generic")

            assertTrue(extracted.outputDir.exists())
            assertTrue(extracted.artifactFile.exists())
            assertTrue(extracted.artifactFile.absolutePath.contains("rhai-default-generic"))
        } finally {
            restoreProperty("kcloud.mcu.runtime.dir", previousDir)
            targetDir.deleteRecursively()
        }
    }

    @Test
    fun `ensure runtime stays ready when device already responds`() = runBlocking {
        val gateway = FakeSerialPortGateway(runtimeInstalledInitially = true)
        val sessionService = newSessionService(gateway)
        val service = newRuntimeEnsureService(gateway, sessionService)

        sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
        val status = service.ensureRuntime(McuRuntimeEnsureRequest(bundleId = "rhai-default-generic"))

        assertEquals(McuRuntimeEnsureState.READY, status.state)
        assertTrue(status.lastMessage?.contains("runtime-ready") == true)
    }

    @Test
    fun `ensure runtime flashes bundle then reconnects and probes ready`() = runBlocking {
        val gateway = FakeSerialPortGateway(runtimeInstalledInitially = false)
        val sessionService = newSessionService(gateway)
        val service = newRuntimeEnsureService(gateway, sessionService)

        sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
        val status = service.ensureRuntime(McuRuntimeEnsureRequest(bundleId = "rhai-default-generic"))

        assertEquals(McuRuntimeEnsureState.READY, status.state)
        assertNotNull(status.artifactPath)
        assertTrue(gateway.runtimeInstalled)
        assertTrue(gateway.openedConnections.any { connection -> connection.textWrites.contains("START_FLASH\r\n") })
    }

    @Test
    fun `ensure runtime enters error when flash fails`() = runBlocking {
        val gateway = FailingFlashGateway()
        val sessionService = newSessionService(gateway)
        val service = newRuntimeEnsureService(gateway, sessionService)

        sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
        runCatching {
            service.ensureRuntime(McuRuntimeEnsureRequest(bundleId = "rhai-default-generic"))
        }

        assertEquals(McuRuntimeEnsureState.ERROR, service.getStatus().state)
    }

    private fun newSessionService(
        gateway: SerialPortGateway,
    ): McuConsoleSessionService {
        return McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
    }

    private fun newRuntimeEnsureService(
        gateway: SerialPortGateway,
        sessionService: McuConsoleSessionService,
    ): McuRuntimeEnsureService {
        val flashService = McuFlashService(
            gateway = gateway,
            sessionService = sessionService,
            profileCatalog = McuFlashProfileCatalog(),
            commandRunner = SuccessfulCommandRunner(),
        )
        return McuRuntimeEnsureService(
            bundleCatalog = McuRuntimeBundleCatalog(json),
            assetExtractor = McuRuntimeAssetExtractor(McuRuntimeBundleCatalog(json)),
            flashService = flashService,
            sessionService = sessionService,
            protocolCodec = codec,
        )
    }
}

private class SuccessfulCommandRunner : McuFlashCommandRunner {
    override fun run(
        commandLine: String,
        workingDirectory: java.io.File?,
    ): McuFlashCommandResult {
        return McuFlashCommandResult(exitCode = 0, stdout = commandLine)
    }
}

private class FailingFlashGateway : SerialPortGateway {
    override fun listPorts(): List<McuPortSummary> {
        return listOf(
            McuPortSummary(
                portPath = "COM9",
                portName = "Failing Port",
                systemPortName = "COM9",
            ),
        )
    }

    override fun openConnection(
        portPath: String,
        baudRate: Int,
    ): SerialPortConnection {
        return FailingFlashConnection(portPath, baudRate)
    }
}

private class FailingFlashConnection(
    override val portPath: String,
    override val baudRate: Int,
) : SerialPortConnection {
    override val portName: String = "Failing-$portPath"
    override val isOpen: Boolean = true

    override fun writeUtf8(
        text: String,
    ) {
    }

    override fun writeBytes(
        bytes: ByteArray,
        length: Int,
    ) {
    }

    override fun read(
        buffer: ByteArray,
        timeoutMs: Int,
    ): Int {
        return 0
    }

    override fun setDtr(
        enabled: Boolean,
    ) {
    }

    override fun setRts(
        enabled: Boolean,
    ) {
    }

    override fun close() {
    }
}

private fun restoreProperty(
    key: String,
    value: String?,
) {
    if (value == null) {
        System.clearProperty(key)
    } else {
        System.setProperty(key, value)
    }
}
