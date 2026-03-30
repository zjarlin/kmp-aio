package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoPollRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 验证设备信息轮询服务的请求发送、回包等待与失败兜底行为。
 */
class McuDeviceInfoServiceTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    private val codec = McuVmProtocolCodec(json)

    /**
     * 校验后端能通过轮询接口拿到 CPU、晶振和 MAC 等核心字段。
     */
    @Test
    fun `poll device info returns cpu xtal and mac`() = runBlocking {
        val gateway = FakeSerialPortGateway()
        val sessionService = newSessionService(gateway)
        val service = newDeviceInfoService(sessionService)

        sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
        val response = service.pollDeviceInfo(McuDeviceInfoPollRequest(timeoutMs = 300))

        assertTrue(response.success)
        assertEquals("COM9", response.portPath)
        assertEquals("ESP32-D0WDQ6", response.chipModel)
        assertEquals("Xtensa LX6", response.cpuModel)
        assertEquals(240_000_000, response.cpuFrequencyHz)
        assertEquals(40_000_000, response.xtalFrequencyHz)
        assertEquals("AA:BB:CC:DD:EE:FF", response.macAddress)
        assertEquals(response, service.getStatus())
    }

    /**
     * 校验设备没有回包时，接口会返回可展示的失败状态而不是直接抛错。
     */
    @Test
    fun `poll device info returns failure on timeout`() = runBlocking {
        val gateway = FakeSerialPortGateway(deviceInfoPayloadJson = null)
        val sessionService = newSessionService(gateway)
        val service = newDeviceInfoService(sessionService)

        sessionService.openSession(McuSessionOpenRequest(portPath = "COM9"))
        val response = service.pollDeviceInfo(McuDeviceInfoPollRequest(timeoutMs = 120))

        assertFalse(response.success)
        assertTrue(response.lastMessage?.contains("未在 120ms 内返回设备信息") == true)
        assertEquals(response, service.getStatus())
    }

    /**
     * 统一构造测试用串口会话服务。
     */
    private fun newSessionService(
        gateway: FakeSerialPortGateway,
    ): McuConsoleSessionService {
        return McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = codec,
        )
    }

    /**
     * 统一构造测试用设备信息服务。
     */
    private fun newDeviceInfoService(
        sessionService: McuConsoleSessionService,
    ): McuDeviceInfoService {
        return McuDeviceInfoService(
            sessionService = sessionService,
            protocolCodec = codec,
        )
    }
}
