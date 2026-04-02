package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigRegistry
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuGateway
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.JSerialCommSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusCommandConfig
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService
import java.io.File
import kotlin.test.Test

private const val REAL_PORT_PATH = "/dev/cu.usbserial-2130"
private const val REAL_BAUD_RATE = 9600
private const val REAL_UNIT_ID = 1
private const val REAL_DATA_BITS = 8
private const val REAL_STOP_BITS = 1
private const val REAL_TIMEOUT_MS = 2_000L
private const val REAL_RETRIES = 1

class DeviceModbusRealDeviceTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `read device info from real modbus rtu device`() = runBlocking {
        if (!File(REAL_PORT_PATH).exists()) {
            println("skip real modbus device test: serial port does not exist: $REAL_PORT_PATH")
            return@runBlocking
        }

        val gateway = JSerialCommSerialPortGateway()
        val sessionService = McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = McuVmProtocolCodec(json),
        )
        val modbusGateway = DeviceApiGeneratedRtuGateway(
            configRegistry = ModbusRtuConfigRegistry(listOf(DeviceApiGeneratedRtuConfigProvider())),
            executor = site.addzero.device.driver.modbus.rtu.createDefaultModbusRtuExecutor(),
        )
        val service = DeviceModbusService(
            sessionService = sessionService,
            gateway = modbusGateway,
        )

        val lights = service.get24PowerLights(
            config = realConfig(),
        )
        println("real modbus 24 power lights: $lights")

        val response = service.getDeviceInfo(
            config = realConfig(),
        )
        println("real modbus device info: $response")
    }

    @Test
    fun `read device info directly from gateway without session lock`() = runBlocking {
        if (!File(REAL_PORT_PATH).exists()) {
            println("skip direct real modbus gateway test: serial port does not exist: $REAL_PORT_PATH")
            return@runBlocking
        }

        val configRegistry = ModbusRtuConfigRegistry(listOf(DeviceApiGeneratedRtuConfigProvider()))
        val gateway = DeviceApiGeneratedRtuGateway(
            configRegistry = configRegistry,
            executor = site.addzero.device.driver.modbus.rtu.createDefaultModbusRtuExecutor(),
        )
        val config = gateway.defaultConfig().copy(
            portPath = REAL_PORT_PATH,
            unitId = REAL_UNIT_ID,
            baudRate = REAL_BAUD_RATE,
            dataBits = REAL_DATA_BITS,
            stopBits = REAL_STOP_BITS,
            timeoutMs = REAL_TIMEOUT_MS,
            retries = REAL_RETRIES,
        )

        val response = gateway.getDeviceInfo(
            config = ModbusRtuEndpointConfig(
                serviceId = config.serviceId,
                portPath = config.portPath,
                unitId = config.unitId,
                baudRate = config.baudRate,
                dataBits = config.dataBits,
                stopBits = config.stopBits,
                parity = config.parity,
                timeoutMs = config.timeoutMs,
                retries = config.retries,
            ),
        )
        println("real modbus direct gateway device info: $response")

        val lights = gateway.get24PowerLights(config = config)
        println("real modbus direct gateway 24 power lights: ${lights.asList()}")
    }

    private fun realConfig(): McuModbusCommandConfig {
        return McuModbusCommandConfig(
            portPath = REAL_PORT_PATH,
            baudRate = REAL_BAUD_RATE,
            unitId = REAL_UNIT_ID,
            dataBits = REAL_DATA_BITS,
            stopBits = REAL_STOP_BITS,
            parity = McuModbusSerialParity.NONE,
            timeoutMs = REAL_TIMEOUT_MS,
            retries = REAL_RETRIES,
        )
    }
}
