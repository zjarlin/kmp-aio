package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuFlashDownloadRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRunState
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class McuFlashServiceTest {
    private val gateway = FakeSerialPortGateway()
    private val codec = McuVmProtocolCodec(
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    )

    @Test
    fun `flash sends bytes and reports success`() = runBlocking {
        val sessionService = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
        val flashService = McuFlashService(
            gateway = gateway,
            sessionService = sessionService,
            profileCatalog = McuFlashProfileCatalog(),
            commandRunner = FakeMcuFlashCommandRunner(),
        )
        val firmware = File.createTempFile("mcu-console", ".bin").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
            deleteOnExit()
        }

        val status = flashService.flash(
            McuFlashRequest(
                portPath = "COM9",
                baudRate = 9600,
                firmwarePath = firmware.absolutePath,
            ),
        )
        val connection = gateway.openedConnections.last()

        assertEquals(McuFlashRunState.SUCCESS, status.state)
        assertEquals(4, status.bytesSent)
        assertEquals(listOf<Byte>(1, 2, 3, 4), connection.byteWrites)
        assertTrue(connection.textWrites.contains("START_FLASH\r\n"))
        assertTrue(connection.textWrites.contains("DONE\r\n"))
    }

    @Test
    fun `command profile executes template and reports success`() = runBlocking {
        withEsptoolCommand("python3 -m esptool") {
            val sessionService = McuConsoleSessionService(
                gateway = gateway,
                protocolCodec = codec,
            )
            val commandRunner = FakeMcuFlashCommandRunner()
            val flashService = McuFlashService(
                gateway = gateway,
                sessionService = sessionService,
                profileCatalog = McuFlashProfileCatalog(),
                commandRunner = commandRunner,
            )
            val firmware = File.createTempFile("mcu-console", ".bin").apply {
                writeBytes(byteArrayOf(0x1, 0x2, 0x3))
                deleteOnExit()
            }

            val status = flashService.flash(
                McuFlashRequest(
                    profileId = "micropython-generic-command",
                    portPath = "COM9",
                    baudRate = 115200,
                    firmwarePath = firmware.absolutePath,
                ),
            )

            assertEquals(McuFlashRunState.SUCCESS, status.state)
            assertTrue(commandRunner.commands.single().contains("python3 -m esptool"))
            assertTrue(commandRunner.commands.single().contains("erase_flash"))
            assertTrue(commandRunner.commands.single().contains("write_flash -z 0x1000"))
            assertTrue(commandRunner.commands.single().contains(firmware.absolutePath))
            assertEquals(firmware.absolutePath, status.firmwarePath)
        }
    }

    @Test
    fun `micropython profile requires serial port`() = runBlocking {
        val sessionService = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
        val flashService = McuFlashService(
            gateway = gateway,
            sessionService = sessionService,
            profileCatalog = McuFlashProfileCatalog(),
            commandRunner = FakeMcuFlashCommandRunner(),
        )
        val firmware = File.createTempFile("mcu-console", ".bin").apply {
            writeBytes(byteArrayOf(0x1, 0x2))
            deleteOnExit()
        }

        val error = assertFailsWith<IllegalArgumentException> {
            flashService.flash(
                McuFlashRequest(
                    profileId = "micropython-generic-command",
                    firmwarePath = firmware.absolutePath,
                ),
            )
        }

        assertEquals("请先选择串口", error.message)
    }

    @Test
    fun `profiles expose default rhai and micropython entries`() {
        val profiles = McuFlashProfileCatalog().listProfiles()

        assertEquals(2, profiles.items.size)
        assertTrue(profiles.items.any { it.id == "rhai-generic-serial" })
        assertTrue(profiles.items.any { it.id == "micropython-generic-command" })
        assertEquals("rhai-generic-serial", profiles.defaultProfileId)
    }

    @Test
    fun `download firmware resolves latest micropython artifact from catalog page`() = runBlocking {
        val sessionService = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
        val commandRunner = FakeMcuFlashCommandRunner { commandLine, _ ->
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

                else -> McuFlashCommandResult(exitCode = 1, stderr = "unexpected command")
            }
        }
        val flashService = McuFlashService(
            gateway = gateway,
            sessionService = sessionService,
            profileCatalog = McuFlashProfileCatalog(),
            commandRunner = commandRunner,
        )

        val response = flashService.downloadFirmware(
            McuFlashDownloadRequest(
                profileId = "micropython-generic-command",
            ),
        )

        assertEquals(
            "https://micropython.org/resources/firmware/ESP32_GENERIC-20251209-v1.27.0.bin",
            response.resolvedUrl,
        )
        assertTrue(response.downloadPath?.endsWith("ESP32_GENERIC-20251209-v1.27.0.bin") == true)
        assertTrue(commandRunner.commands.any { it.contains("curl -fsSL") })
        assertTrue(commandRunner.commands.any { it.contains("curl -fL") })
    }

    @Test
    fun `download firmware requires explicit url for rhai profile`() = runBlocking {
        val sessionService = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
        val flashService = McuFlashService(
            gateway = gateway,
            sessionService = sessionService,
            profileCatalog = McuFlashProfileCatalog(),
            commandRunner = FakeMcuFlashCommandRunner(),
        )

        val error = assertFailsWith<IllegalArgumentException> {
            flashService.downloadFirmware(
                McuFlashDownloadRequest(
                    profileId = "rhai-generic-serial",
                ),
            )
        }

        assertEquals("当前烧录能力包没有默认在线固件，请填写下载地址", error.message)
    }

    @Test
    fun `micropython command profile reports missing esptool clearly`() = runBlocking {
        val sessionService = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
        val commandRunner = FakeMcuFlashCommandRunner { commandLine, _ ->
            if (commandLine.contains("esptool")) {
                McuFlashCommandResult(exitCode = 1, stderr = "missing")
            } else {
                McuFlashCommandResult(exitCode = 1, stderr = "unexpected")
            }
        }
        val flashService = McuFlashService(
            gateway = gateway,
            sessionService = sessionService,
            profileCatalog = McuFlashProfileCatalog(),
            commandRunner = commandRunner,
        )
        val firmware = File.createTempFile("mcu-console", ".bin").apply {
            writeBytes(byteArrayOf(0x1, 0x2))
            deleteOnExit()
        }

        val error = assertFailsWith<IllegalStateException> {
            flashService.flash(
                McuFlashRequest(
                    profileId = "micropython-generic-command",
                    portPath = "COM9",
                    firmwarePath = firmware.absolutePath,
                ),
            )
        }

        assertTrue(error.message?.contains("未找到可用的 esptool") == true)
    }
}

private inline fun withEsptoolCommand(
    command: String,
    block: () -> Unit,
) {
    val key = "kcloud.mcu.esptool.cmd"
    val previous = System.getProperty(key)
    try {
        System.setProperty(key, command)
        block()
    } finally {
        if (previous == null) {
            System.clearProperty(key)
        } else {
            System.setProperty(key, previous)
        }
    }
}

private class FakeMcuFlashCommandRunner(
    private val onRun: (String, File?) -> McuFlashCommandResult = { _, _ ->
        McuFlashCommandResult(
            exitCode = 0,
            stdout = "ok",
        )
    },
) : McuFlashCommandRunner {
    val commands = mutableListOf<String>()

    override fun run(
        commandLine: String,
        workingDirectory: File?,
    ): McuFlashCommandResult {
        commands += commandLine
        return onRun(commandLine, workingDirectory)
    }
}
