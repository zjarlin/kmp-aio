package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortConnection
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
    fun `runtime asset extractor rejects builtin placeholder bundle`() {
        val previousDir = System.getProperty("kcloud.mcu.runtime.dir")
        val targetDir = createTempDirectory(prefix = "mcu-runtime-assets-").toFile()
        try {
            System.setProperty("kcloud.mcu.runtime.dir", targetDir.absolutePath)
            val extractor = McuRuntimeAssetExtractor(McuRuntimeBundleCatalog(json))

            val error = assertFailsWith<IllegalArgumentException> {
                extractor.extractBundle("rhai-default-generic")
            }

            assertTrue(error.message?.contains("占位固件") == true)
        } finally {
            restoreProperty("kcloud.mcu.runtime.dir", previousDir)
            targetDir.deleteRecursively()
        }
    }

    @Test
    fun `runtime asset extractor reuses external real artifact`() {
        val previousDir = System.getProperty("kcloud.mcu.runtime.dir")
        val targetDir = createTempDirectory(prefix = "mcu-runtime-assets-").toFile()
        try {
            System.setProperty("kcloud.mcu.runtime.dir", targetDir.absolutePath)
            installRealArtifact(
                targetDir = targetDir,
                bundleId = "rhai-default-generic",
                relativePath = "artifacts/rhai-default-generic.bin",
                bytes = byteArrayOf(0x11, 0x22, 0x33),
            )
            val extractor = McuRuntimeAssetExtractor(McuRuntimeBundleCatalog(json))

            val extracted = extractor.extractBundle("rhai-default-generic")

            assertTrue(extracted.outputDir.exists())
            assertTrue(extracted.artifactFile.exists())
            assertTrue(extracted.artifactFile.absolutePath.contains("rhai-default-generic"))
            assertEquals(
                listOf(0x11.toByte(), 0x22.toByte(), 0x33.toByte()),
                extracted.artifactFile.readBytes().toList(),
            )
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
        withInstalledRealArtifact("rhai-default-generic", "artifacts/rhai-default-generic.bin", byteArrayOf(1, 2, 3, 4)) {
            val service = newRuntimeEnsureService(gateway, sessionService)

            sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
            val status = service.ensureRuntime(McuRuntimeEnsureRequest(bundleId = "rhai-default-generic"))

            assertEquals(McuRuntimeEnsureState.READY, status.state)
            assertNotNull(status.artifactPath)
            assertTrue(gateway.runtimeInstalled)
            assertTrue(gateway.openedConnections.any { connection -> connection.textWrites.contains("START_FLASH\r\n") })
        }
    }

    @Test
    fun `ensure runtime keeps micropython bundle in flash-only ready state`() = runBlocking {
        val gateway = FakeSerialPortGateway(runtimeInstalledInitially = false)
        val sessionService = newSessionService(gateway)
        val service = newRuntimeEnsureService(gateway, sessionService)

        sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
        val status = service.ensureRuntime(McuRuntimeEnsureRequest(bundleId = "micropython-default-generic"))

        assertEquals(McuRuntimeEnsureState.READY, status.state)
        assertEquals(McuFlashRuntimeKind.MICROPYTHON, status.runtimeKind)
        assertTrue(status.lastMessage?.contains("不支持 VM 在线探测") == true)
        assertTrue(gateway.openedConnections.none { connection -> connection.textWrites.contains("START_FLASH\r\n") })
    }

    @Test
    fun `ensure runtime flashes micropython bundle without vm probe`() = runBlocking {
        val gateway = FakeSerialPortGateway(runtimeInstalledInitially = false)
        val sessionService = newSessionService(gateway)
        withInstalledRealArtifact("micropython-default-generic", "artifacts/micropython-default-generic.bin", byteArrayOf(9, 8, 7)) {
            val service = newRuntimeEnsureService(gateway, sessionService)

            sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
            val status = service.ensureRuntime(
                McuRuntimeEnsureRequest(
                    bundleId = "micropython-default-generic",
                    forceReflash = true,
                ),
            )

            assertEquals(McuRuntimeEnsureState.READY, status.state)
            assertEquals(McuFlashRuntimeKind.MICROPYTHON, status.runtimeKind)
            assertTrue(status.artifactPath?.endsWith("micropython-default-generic.bin") == true)
            assertTrue(status.lastMessage?.contains("MicroPython 固件已刷写") == true)
        }
    }

    @Test
    fun `ensure runtime auto downloads micropython firmware when builtin artifact is placeholder`() = runBlocking {
        val gateway = FakeSerialPortGateway(runtimeInstalledInitially = false)
        val sessionService = newSessionService(gateway)
        val commandRunner = SuccessfulCommandRunner { commandLine, _ ->
            when {
                commandLine.contains("curl -fsSL") -> McuFlashCommandResult(
                    exitCode = 0,
                    stdout = """
                        <html>
                        <body>
                        <a href="/resources/firmware/ESP32_GENERIC-20251209-v1.27.0.bin">stable</a>
                        </body>
                        </html>
                    """.trimIndent(),
                )

                commandLine.contains("curl -fL") -> {
                    val outputPath = commandLine.substringAfter("-o '").substringBefore("' ")
                    File(outputPath).apply {
                        parentFile?.mkdirs()
                        writeBytes(byteArrayOf(0x5, 0x6, 0x7))
                    }
                    McuFlashCommandResult(exitCode = 0, stdout = "downloaded")
                }

                else -> McuFlashCommandResult(exitCode = 0, stdout = commandLine)
            }
        }
        val service = newRuntimeEnsureService(gateway, sessionService, commandRunner)

        sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
        val status = service.ensureRuntime(
            McuRuntimeEnsureRequest(
                bundleId = "micropython-default-generic",
                forceReflash = true,
            ),
        )

        assertEquals(McuRuntimeEnsureState.READY, status.state)
        assertTrue(status.artifactPath?.endsWith("ESP32_GENERIC-20251209-v1.27.0.bin") == true)
        assertTrue(gateway.openedConnections.none { connection -> connection.textWrites.contains("START_FLASH\r\n") })
    }

    @Test
    fun `ensure runtime enters error when flash fails`() = runBlocking {
        val gateway = FailingFlashGateway()
        val sessionService = newSessionService(gateway)
        withInstalledRealArtifact("rhai-default-generic", "artifacts/rhai-default-generic.bin", byteArrayOf(1, 2)) {
            val service = newRuntimeEnsureService(gateway, sessionService)

            sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
            runCatching {
                service.ensureRuntime(McuRuntimeEnsureRequest(bundleId = "rhai-default-generic"))
            }

            assertEquals(McuRuntimeEnsureState.ERROR, service.getStatus().state)
        }
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
        commandRunner: McuFlashCommandRunner = SuccessfulCommandRunner(),
    ): McuRuntimeEnsureService {
        val flashService = McuFlashService(
            gateway = gateway,
            sessionService = sessionService,
            profileCatalog = McuFlashProfileCatalog(),
            commandRunner = commandRunner,
        )
        return McuRuntimeEnsureService(
            bundleCatalog = McuRuntimeBundleCatalog(json),
            assetExtractor = McuRuntimeAssetExtractor(McuRuntimeBundleCatalog(json)),
            flashService = flashService,
            sessionService = sessionService,
            protocolCodec = codec,
        )
    }

    private suspend fun withInstalledRealArtifact(
        bundleId: String,
        relativePath: String,
        bytes: ByteArray,
        block: suspend () -> Unit,
    ) {
        val previousDir = System.getProperty("kcloud.mcu.runtime.dir")
        val targetDir = createTempDirectory(prefix = "mcu-runtime-assets-").toFile()
        try {
            System.setProperty("kcloud.mcu.runtime.dir", targetDir.absolutePath)
            installRealArtifact(
                targetDir = targetDir,
                bundleId = bundleId,
                relativePath = relativePath,
                bytes = bytes,
            )
            block()
        } finally {
            restoreProperty("kcloud.mcu.runtime.dir", previousDir)
            targetDir.deleteRecursively()
        }
    }
}

private class SuccessfulCommandRunner(
    private val onRun: (String, File?) -> McuFlashCommandResult = { commandLine, _ ->
        McuFlashCommandResult(exitCode = 0, stdout = commandLine)
    },
) : McuFlashCommandRunner {
    override fun run(
        commandLine: String,
        workingDirectory: java.io.File?,
    ): McuFlashCommandResult {
        return onRun(commandLine, workingDirectory)
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

private fun installRealArtifact(
    targetDir: File,
    bundleId: String,
    relativePath: String,
    bytes: ByteArray,
) {
    val artifact = File(targetDir, "$bundleId/$relativePath")
    artifact.parentFile?.mkdirs()
    artifact.writeBytes(bytes)
}
