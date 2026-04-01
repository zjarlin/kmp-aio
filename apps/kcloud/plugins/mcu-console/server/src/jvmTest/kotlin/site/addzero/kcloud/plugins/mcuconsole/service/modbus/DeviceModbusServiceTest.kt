package site.addzero.kcloud.plugins.mcuconsole.service.modbus

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigProvider
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigRegistry
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig
import site.addzero.device.driver.modbus.rtu.ModbusRtuExecutor
import site.addzero.device.protocol.modbus.ModbusCodecSupport
import site.addzero.device.protocol.modbus.model.ModbusCodec
import site.addzero.device.protocol.modbus.model.ModbusCommandResult
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuGateway
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
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
        assertEquals(29, executor.lastReadInputQuantity)
        assertEquals("COM11", executor.lastReadConfig?.portPath)
        assertEquals("2026.04.01", response.firmwareVersion)
        assertEquals("ESP32-S3", response.cpuModel)
        assertEquals(40_000_000, response.xtalFrequencyHz)
        assertEquals(16_777_216L, response.flashSizeBytes)
        assertEquals("AA:BB:CC:DD:EE:FF", response.macAddress)
        assertTrue(response.success)
    }

    private fun newService(
        executor: RecordingDeviceModbusExecutor,
    ): DeviceModbusService {
        val sessionService = McuConsoleSessionService(
            gateway = FakeSerialPortGateway(),
            protocolCodec = McuVmProtocolCodec(json),
        )
        sessionService.openSession(
            McuSessionOpenRequest(
                portPath = "COM11",
                baudRate = 38400,
            ),
        )
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
        val registers = MutableList(29) { 0 }
        ModbusCodecSupport.encodeString(ModbusCodec.STRING_UTF8, "2026.04.01", 8)
            .forEachIndexed { index, value -> registers[index] = value }
        ModbusCodecSupport.encodeString(ModbusCodec.STRING_UTF8, "ESP32-S3", 8)
            .forEachIndexed { index, value -> registers[8 + index] = value }
        ModbusCodecSupport.encodeValue(ModbusCodec.U32_BE, "40000000")
            .forEachIndexed { index, value -> registers[16 + index] = value }
        ModbusCodecSupport.encodeValue(ModbusCodec.U32_BE, "16777216")
            .forEachIndexed { index, value -> registers[18 + index] = value }
        ModbusCodecSupport.encodeString(ModbusCodec.STRING_ASCII, "AA:BB:CC:DD:EE:FF", 9)
            .forEachIndexed { index, value -> registers[20 + index] = value }
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
