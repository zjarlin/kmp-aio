package site.addzero.kcloud.plugins.mcuconsole.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuResetRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionLinesRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec

class McuConsoleSessionServiceTest {
    private val gateway = FakeSerialPortGateway()
    private val codec = McuVmProtocolCodec(
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    )

    @Test
    fun `opens resets and records events`() {
        val service = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )

        val opened = service.openSession(
            McuSessionOpenRequest(
                portPath = "COM9",
                baudRate = 115200,
            ),
        )
        val reset = service.resetSession(McuResetRequest(pulseMs = 10))
        val events = service.readRecentEvents(McuSessionLinesRequest(limit = 20))
        val connection = gateway.openedConnections.single()

        assertTrue(opened.isOpen)
        assertFalse(reset.dtrEnabled)
        assertFalse(connection.dtrEnabled)
        assertTrue(events.items.any { it.title == "会话已打开" })
        assertTrue(events.items.any { it.title == "设备复位" })
        assertEquals(115200, opened.baudRate)
    }
}
