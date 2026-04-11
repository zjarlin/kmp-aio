package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.device.driver.modbus.rtu.DefaultModbusRtuEndpointConfig
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig
import site.addzero.device.driver.modbus.rtu.ModbusRtuExecutor
import site.addzero.device.driver.modbus.rtu.ModbusSerialParity
import site.addzero.device.driver.modbus.rtu.createDefaultModbusRtuExecutor
import site.addzero.esp32_host_computer.generated.modbus.rtu.GeneratedModbusRtuKoinModule
import site.addzero.esp32_host_computer.generated.modbus.rtu.module

class DeviceApiGeneratedContractTest {
    @Test
    fun shouldRead24PowerLightsFromRealBoardThroughKoin() {
        val executor = createDefaultModbusRtuExecutor()
        val koinApplication =
            startKoin {
                withConfiguration<>()
                modules(
                    w
                    GeneratedModbusRtuKoinModule().module(),
                    module {
                        single<ModbusRtuEndpointConfig> { REAL_DEVICE_CONFIG }
                        single<ModbusRtuExecutor> { executor }
                    },
                )
            }

        try {
            val deviceApi = koinApplication.koin.get<DeviceApi>()
            val actual = runBlocking { deviceApi.get24PowerLights() }

            println("REAL_MODBUS_RTU_CONFIG_BEGIN")
            println(REAL_DEVICE_CONFIG)
            println("REAL_MODBUS_RTU_CONFIG_END")
            println("REAL_24_POWER_LIGHTS_BEGIN")
            println(actual)
            println("REAL_24_POWER_LIGHTS_END")

            assertEquals(24, actual.toLights().size)
        } finally {
            stopKoin()
        }
    }
}

private const val REAL_PORT_PATH = "/dev/cu.usbserial-2140"

private val REAL_DEVICE_CONFIG =
    DefaultModbusRtuEndpointConfig(
        portPath = REAL_PORT_PATH,
        unitId = 1,
        baudRate = 9600,
        dataBits = 8,
        stopBits = 1,
        parity = ModbusSerialParity.NONE,
        timeoutMs = 400,
        retries = 0,
    )

/**
 * 把 24 路电源灯结果转成顺序列表，便于断言和打印。
 */
private fun Device24PowerLightsRegisters.toLights(): List<Boolean> =
    listOf(
        light1,
        light2,
        light3,
        light4,
        light5,
        light6,
        light7,
        light8,
        light9,
        light10,
        light11,
        light12,
        light13,
        light14,
        light15,
        light16,
        light17,
        light18,
        light19,
        light20,
        light21,
        light22,
        light23,
        light24,
    )
