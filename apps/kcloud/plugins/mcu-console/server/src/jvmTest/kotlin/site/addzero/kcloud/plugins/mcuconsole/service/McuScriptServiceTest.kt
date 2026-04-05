package site.addzero.kcloud.plugins.mcuconsole.service

import site.addzero.core.network.json.json
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptRunState
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class McuScriptServiceTest {
    private val gateway = FakeSerialPortGateway()
    private val codec = McuVmProtocolCodec(
        json,
    )

    @Test
    fun `execute switches script state to running and receives ack`() {
        val sessionService = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
        val scriptService = McuScriptService(
            sessionService = sessionService,
            protocolCodec = codec,
        )

        sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
        val status = scriptService.execute(
            McuScriptExecuteRequest(script = "println(1);"),
        )
        Thread.sleep(120)
        val refreshed = scriptService.queryStatus()

        assertEquals(McuScriptRunState.RUNNING, status.state)
        assertEquals(McuScriptRunState.RUNNING, refreshed.state)
        assertTrue(refreshed.lastMessage?.contains("ready") == true)
    }

    @Test
    fun `result frame keeps payload and frame type`() {
        val sessionService = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
        val scriptService = McuScriptService(
            sessionService = sessionService,
            protocolCodec = codec,
        )

        sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
        val running = scriptService.execute(
            McuScriptExecuteRequest(script = "adc_read(1);"),
        )
        gateway.openedConnections.last().enqueueIncomingText(
            """{"requestId":"${running.activeRequestId.orEmpty()}","type":"result","success":true,"message":"done","payload":{"value":512}}""" + "\n",
        )
        Thread.sleep(120)
        val refreshed = scriptService.queryStatus()

        assertEquals(McuScriptRunState.IDLE, refreshed.state)
        assertEquals("result", refreshed.lastFrameType)
        assertTrue(refreshed.lastPayload.toString().contains("512"))
    }
}
