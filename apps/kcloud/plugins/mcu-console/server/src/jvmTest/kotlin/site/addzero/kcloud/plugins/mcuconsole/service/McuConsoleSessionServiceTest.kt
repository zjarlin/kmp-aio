package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuResetRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSerialLineEnding
import site.addzero.kcloud.plugins.mcuconsole.McuSerialTextSendRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionLinesRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun `sends raw serial text and records tx event`() {
        val service = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )

        service.openSession(
            McuSessionOpenRequest(
                portPath = "COM9",
                baudRate = 115200,
            ),
        )
        val response = service.sendSerialText(
            McuSerialTextSendRequest(
                text = "import panel_control as p\np.s(9527)",
                appendLineEnding = true,
                lineEnding = McuSerialLineEnding.CRLF,
            ),
        )
        val connection = gateway.openedConnections.single()
        val events = service.readRecentEvents(McuSessionLinesRequest(limit = 20))

        assertTrue(response.accepted)
        assertEquals(
            "import panel_control as p\r\np.s(9527)\r\n",
            connection.textWrites.last(),
        )
        assertTrue(events.items.any { it.title == "串口直发" })
    }
}
