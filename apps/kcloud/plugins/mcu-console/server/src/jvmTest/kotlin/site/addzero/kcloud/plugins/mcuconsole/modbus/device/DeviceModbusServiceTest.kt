package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigProvider
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigRegistry
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig
import site.addzero.device.driver.modbus.rtu.ModbusRtuExecutor
import site.addzero.device.protocol.modbus.ModbusCodecSupport
import site.addzero.device.protocol.modbus.model.ModbusCodec
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuGateway
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusCommandConfig
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceModbusServiceTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `get24PowerLights uses active session port and decodes 24 coils`() = runBlocking {
        val executor = RecordingDeviceModbusExecutor(
            coils = List(24) { index -> index % 2 == 0 },
        )
        val service = newService(executor)

        val response = service.get24PowerLights()

        assertEquals(0, executor.lastReadCoilsAddress)
        assertEquals(24, executor.lastReadCoilsQuantity)
        assertEquals("COM11", executor.lastReadConfig?.portPath)
        assertEquals(38400, executor.lastReadConfig?.baudRate)
        assertEquals(12, response.onCount)
        assertEquals(24, response.lights.size)
        assertTrue(response.lights.first())
        assertEquals(false, response.lights[1])
        assertEquals(true, response.success)
    }

    @Test
    fun `getDeviceInfo decodes firmware cpu xtal flash and mac`() = runBlocking {
        val executor = RecordingDeviceModbusExecutor(
            inputRegisters = buildDeviceInfoRegisters(),
        )
        val service = newService(executor)

        val response = service.getDeviceInfo()

        assertEquals(100, executor.lastReadInputAddress)
        assertEquals(20, executor.lastReadInputQuantity)
        assertEquals("COM11", executor.lastReadConfig?.portPath)
        assertEquals(3, response.protocolVersion)
        assertEquals(24, response.channelCount)
        assertEquals(1, response.unitId)
        assertEquals(9600, response.baudRateCode)
        assertEquals("OKM-X自控", response.deviceName)
        assertTrue(response.success)
    }

    @Test
    fun `explicit modbus config does not require opened session`() = runBlocking {
        val executor = RecordingDeviceModbusExecutor(
            inputRegisters = buildDeviceInfoRegisters(),
        )
        val service = newService(executor, openSession = false)

        val response = service.getDeviceInfo(
            config = McuModbusCommandConfig(
                portPath = "/dev/cu.usbserial-2130",
                baudRate = 9600,
                unitId = 1,
                dataBits = 8,
                stopBits = 1,
                parity = McuModbusSerialParity.NONE,
                timeoutMs = 2_000,
                retries = 1,
            ),
        )

        assertEquals("/dev/cu.usbserial-2130", executor.lastReadConfig?.portPath)
        assertEquals(9600, executor.lastReadConfig?.baudRate)
        assertEquals(1, executor.lastReadConfig?.unitId)
        assertEquals("OKM-X自控", response.deviceName)
        assertTrue(response.success)
    }

    private fun newService(
        executor: RecordingDeviceModbusExecutor,
        openSession: Boolean = true,
    ): DeviceModbusService {
        val sessionService = McuConsoleSessionService(
            gateway = FakeSerialPortGateway(),
            protocolCodec = McuVmProtocolCodec(json),
        )
        if (openSession) {
            sessionService.openSession(
                McuSessionOpenRequest(
                    portPath = "COM11",
                    baudRate = 38400,
                ),
            )
        }
        val registry = ModbusRtuConfigRegistry(
            listOf(
                object : ModbusRtuConfigProvider {
                    override val serviceId: String = "device"

                    override fun defaultConfig(): ModbusRtuEndpointConfig {
                        return ModbusRtuEndpointConfig(
                            serviceId = serviceId,
                            portPath = "/dev/ttyUSB0",
                            unitId = 1,
                            baudRate = 115200,
                            timeoutMs = 1_000,
                            retries = 2,
                        )
                    }
                },
            ),
        )
        return DeviceModbusService(
            sessionService = sessionService,
            gateway = DeviceApiGeneratedRtuGateway(
                configRegistry = registry,
                executor = executor,
            ),
        )
    }

    private fun buildDeviceInfoRegisters(): List<Int> {
        val registers = MutableList(20) { 0 }
        ModbusCodecSupport.encodeValue(ModbusCodec.U16, "3")
            .forEachIndexed { index, value -> registers[index] = value }
        ModbusCodecSupport.encodeValue(ModbusCodec.U16, "24")
            .forEachIndexed { index, value -> registers[1 + index] = value }
        ModbusCodecSupport.encodeValue(ModbusCodec.U16, "1")
            .forEachIndexed { index, value -> registers[2 + index] = value }
        ModbusCodecSupport.encodeValue(ModbusCodec.U16, "9600")
            .forEachIndexed { index, value -> registers[3 + index] = value }
        ModbusCodecSupport.encodeString(ModbusCodec.STRING_UTF8, "OKM-X自控", 16)
            .forEachIndexed { index, value -> registers[4 + index] = value }
        return registers
    }
}

private class RecordingDeviceModbusExecutor(
    private val coils: List<Boolean> = List(24) { false },
    private val inputRegisters: List<Int> = emptyList(),
) : ModbusRtuExecutor {
    var lastReadConfig: ModbusRtuEndpointConfig? = null
    var lastReadCoilsAddress: Int? = null
    var lastReadCoilsQuantity: Int? = null
    var lastReadInputAddress: Int? = null
    var lastReadInputQuantity: Int? = null

    override suspend fun readCoils(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int> {
        lastReadConfig = config
        lastReadCoilsAddress = address
        lastReadCoilsQuantity = quantity
        return coils.take(quantity).map { if (it) 1 else 0 }
    }

    override suspend fun readDiscreteInputs(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int> = error("not used")

    override suspend fun readHoldingRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int> = error("not used")

    override suspend fun readInputRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int> {
        lastReadConfig = config
        lastReadInputAddress = address
        lastReadInputQuantity = quantity
        return inputRegisters.take(quantity)
    }

    override suspend fun writeSingleCoil(
        config: ModbusRtuEndpointConfig,
        address: Int,
        value: Boolean,
    ) = error("not used")

    override suspend fun writeMultipleCoils(
        config: ModbusRtuEndpointConfig,
        address: Int,
        values: List<Boolean>,
    ) = error("not used")

    override suspend fun writeSingleRegister(
        config: ModbusRtuEndpointConfig,
        address: Int,
        value: Int,
    ) = error("not used")

    override suspend fun writeMultipleRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        values: List<Int>,
    ) = error("not used")
}
