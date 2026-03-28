package site.addzero.kcloud.plugins.mcuconsole.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptRunState
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec

class McuScriptServiceTest {
    private val gateway = FakeSerialPortGateway()
    private val codec = McuVmProtocolCodec(
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
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
}
