package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRuntimeKind
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureRequest
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureState
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class McuRuntimeServicesTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    private val codec = McuVmProtocolCodec(json)

    @Test
    fun `runtime bundle catalog reads builtin micropython manifest`() {
        val catalog = McuRuntimeBundleCatalog(json)

        val bundles = catalog.listBundles()
        val bundle = bundles.items.single()

        assertEquals("micropython-default-generic", bundles.defaultBundleId)
        assertEquals("micropython-default-generic", bundle.bundleId)
        assertEquals(McuFlashRuntimeKind.MICROPYTHON, bundle.runtimeKind)
        assertTrue(bundle.scriptExamples.isNotEmpty())
        assertTrue(bundle.widgetTemplates.isNotEmpty())
    }

    @Test
    fun `runtime asset extractor expands builtin micropython bundle`() {
        val previousDir = System.getProperty("kcloud.mcu.runtime.dir")
        val targetDir = createTempDirectory(prefix = "mcu-runtime-assets-").toFile()
        try {
            System.setProperty("kcloud.mcu.runtime.dir", targetDir.absolutePath)
            val extractor = McuRuntimeAssetExtractor(McuRuntimeBundleCatalog(json))

            val extracted = extractor.extractBundle("micropython-default-generic")
            val prefixText = extracted.artifactFile.inputStream().use { input ->
                input.readNBytes(128).decodeToString()
            }

            assertTrue(extracted.outputDir.exists())
            assertTrue(extracted.artifactFile.exists())
            assertTrue(extracted.artifactFile.name == "micropython-default-generic.bin")
            assertTrue(extracted.artifactFile.length() > 0L)
            assertTrue(!prefixText.contains("MICROPYTHON_DEFAULT_GENERIC_RUNTIME_BUNDLE"))
        } finally {
            restoreProperty("kcloud.mcu.runtime.dir", previousDir)
            targetDir.deleteRecursively()
        }
    }

    @Test
    fun `runtime asset extractor reuses external micropython artifact`() {
        val previousDir = System.getProperty("kcloud.mcu.runtime.dir")
        val targetDir = createTempDirectory(prefix = "mcu-runtime-assets-").toFile()
        try {
            System.setProperty("kcloud.mcu.runtime.dir", targetDir.absolutePath)
            installRealArtifact(
                targetDir = targetDir,
                bundleId = "micropython-default-generic",
                relativePath = "artifacts/micropython-default-generic.bin",
                bytes = byteArrayOf(0x11, 0x22, 0x33),
            )
            val extractor = McuRuntimeAssetExtractor(McuRuntimeBundleCatalog(json))

            val extracted = extractor.extractBundle("micropython-default-generic")

            assertTrue(extracted.outputDir.exists())
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
        withInstalledRealArtifact(
            bundleId = "micropython-default-generic",
            relativePath = "artifacts/micropython-default-generic.bin",
            bytes = byteArrayOf(9, 8, 7),
        ) {
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
    fun `ensure runtime enters error when micropython flash command fails`() = runBlocking {
        val esptoolKey = "kcloud.mcu.esptool.cmd"
        val previousEsptool = System.getProperty(esptoolKey)
        val gateway = FakeSerialPortGateway(runtimeInstalledInitially = false)
        val sessionService = newSessionService(gateway)
        val commandRunner = SuccessfulCommandRunner { _, _ ->
            McuFlashCommandResult(exitCode = 1, stderr = "flash failed")
        }
        try {
            System.setProperty(esptoolKey, "python3 -m esptool")
            withInstalledRealArtifact(
                bundleId = "micropython-default-generic",
                relativePath = "artifacts/micropython-default-generic.bin",
                bytes = byteArrayOf(1, 2),
            ) {
                val service = newRuntimeEnsureService(gateway, sessionService, commandRunner)

                sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
                runCatching {
                    service.ensureRuntime(
                        McuRuntimeEnsureRequest(
                            bundleId = "micropython-default-generic",
                            forceReflash = true,
                        ),
                    )
                }

                assertEquals(McuRuntimeEnsureState.ERROR, service.getStatus().state)
                assertTrue(service.getStatus().lastMessage?.contains("flash failed") == true)
            }
        } finally {
            restoreProperty(esptoolKey, previousEsptool)
        }
    }

    private fun newSessionService(
        gateway: FakeSerialPortGateway,
    ): McuConsoleSessionService {
        return McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
    }

    private fun newRuntimeEnsureService(
        gateway: FakeSerialPortGateway,
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
