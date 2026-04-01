package site.addzero.kcloud.plugins.mcuconsole.service.modbus

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigRegistry
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuGateway
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.JSerialCommSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService
import java.io.File
import kotlin.test.Test

class DeviceModbusRealDeviceTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `read device info from real modbus rtu device`() = runBlocking {
        val portPath = resolveString("mcu.real.port", "MCU_REAL_PORT")
        if (portPath.isBlank()) {
            println("skip real modbus device test: missing mcu.real.port / MCU_REAL_PORT")
            return@runBlocking
        }
        if (!File(portPath).exists()) {
            println("skip real modbus device test: serial port does not exist: $portPath")
            return@runBlocking
        }

        val baudRate = resolveString("mcu.real.baudRate", "MCU_REAL_BAUD_RATE").toIntOrNull() ?: 115200
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

        try {
            sessionService.openSession(
                McuSessionOpenRequest(
                    portPath = portPath,
                    baudRate = baudRate,
                ),
            )

            val response = service.getDeviceInfo()
            println("real modbus device info: $response")
        } finally {
            sessionService.closeSession("real modbus device info test finished")
        }
    }

    private fun resolveString(
        propertyKey: String,
        envKey: String,
    ): String {
        return System.getProperty(propertyKey)
            ?.takeIf { it.isNotBlank() }
            ?: System.getenv(envKey)
                ?.takeIf { it.isNotBlank() }
            ?: ""
    }
}
