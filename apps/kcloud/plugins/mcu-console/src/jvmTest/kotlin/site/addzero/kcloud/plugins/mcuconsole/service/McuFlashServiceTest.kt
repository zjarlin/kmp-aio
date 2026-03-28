package site.addzero.kcloud.plugins.mcuconsole.service

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRunState
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec

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
        val firmware = File.createTempFile("mcu-console", ".py").apply {
            writeText("print('ok')")
            deleteOnExit()
        }

        val status = flashService.flash(
            McuFlashRequest(
                profileId = "micropython-script-deploy",
                portPath = "COM9",
                baudRate = 115200,
                firmwarePath = firmware.absolutePath,
            ),
        )

        assertEquals(McuFlashRunState.SUCCESS, status.state)
        assertTrue(commandRunner.commands.single().contains("mpremote connect"))
        assertTrue(commandRunner.commands.single().contains(firmware.absolutePath))
        assertEquals(firmware.absolutePath, status.firmwarePath)
    }

    @Test
    fun `generic command profile allows flashing without serial port`() = runBlocking {
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
            writeBytes(byteArrayOf(0x1, 0x2))
            deleteOnExit()
        }

        val status = flashService.flash(
            McuFlashRequest(
                profileId = "rhai-generic-command",
                firmwarePath = firmware.absolutePath,
            ),
        )

        assertEquals(McuFlashRunState.SUCCESS, status.state)
        assertEquals(null, status.portPath)
        assertEquals("vendor-flasher flash \"${firmware.absolutePath}\"", commandRunner.commands.single())
    }

    @Test
    fun `profiles expose default rhai and micropython entries`() {
        val profiles = McuFlashProfileCatalog().listProfiles()

        assertTrue(profiles.items.any { it.id == "rhai-generic-serial" })
        assertTrue(profiles.items.any { it.id == "micropython-generic-command" })
        assertTrue(profiles.items.any { it.id == "micropython-script-deploy" })
        assertEquals("rhai-generic-serial", profiles.defaultProfileId)
    }
}

private class FakeMcuFlashCommandRunner : McuFlashCommandRunner {
    val commands = mutableListOf<String>()

    override fun run(
        commandLine: String,
        workingDirectory: File?,
    ): McuFlashCommandResult {
        commands += commandLine
        return McuFlashCommandResult(
            exitCode = 0,
            stdout = "ok",
        )
    }
}
