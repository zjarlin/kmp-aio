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
}
