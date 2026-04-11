package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import site.addzero.device.driver.modbus.mqtt.ModbusMqttEndpointConfig
import site.addzero.device.driver.modbus.mqtt.ModbusMqttExecutor
import site.addzero.device.driver.modbus.mqtt.UnsupportedModbusMqttExecutor
import site.addzero.esp32_host_computer.generated.modbus.mqtt.DeviceApiGeneratedMqttGateway
import site.addzero.esp32_host_computer.generated.modbus.mqtt.DeviceApiGeneratedMqttConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.mqtt.DeviceWriteApiGeneratedMqttConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.mqtt.GeneratedModbusMqttKoinModule

class DeviceApiGeneratedContractTest {
    @Test
    fun shouldResolveDeviceApiFromKoinAndDecode24PowerLights() {
        val generatedModule = GeneratedModbusMqttKoinModule()
        val fakeExecutor = RecordingModbusMqttExecutor(sampleCoils())
        val koinApplication =
            startKoin {
                modules(
                    module {
                        single<ModbusMqttExecutor> { fakeExecutor }
                        single { generatedModule.deviceApiGeneratedMqttConfigProvider() }
                        single { generatedModule.deviceWriteApiGeneratedMqttConfigProvider() }
                        single {
                            generatedModule.modbusMqttConfigRegistry(
                                get<DeviceApiGeneratedMqttConfigProvider>(),
                                get<DeviceWriteApiGeneratedMqttConfigProvider>(),
                            )
                        }
                        single { generatedModule.deviceApiGeneratedMqttGateway(get(), get()) }
                        single<DeviceApi> { generatedModule.deviceApi(get<DeviceApiGeneratedMqttGateway>()) }
                    },
                )
            }

        try {
            val deviceApi = koinApplication.koin.get<DeviceApi>()
            val actual = runBlocking { deviceApi.get24PowerLights() }

            assertEquals(sampleCoils().toPowerLightsRegisters(), actual)
            assertEquals(DeviceApiGeneratedMqttConfigProvider().defaultConfig(), fakeExecutor.lastConfig)
            assertEquals(0, fakeExecutor.lastAddress)
            assertEquals(24, fakeExecutor.lastQuantity)
        } finally {
            stopKoin()
        }
    }
}

private class RecordingModbusMqttExecutor(
    private val coilValues: List<Int>,
) : UnsupportedModbusMqttExecutor() {
    var lastConfig: ModbusMqttEndpointConfig? = null
        private set

    var lastAddress: Int? = null
        private set

    var lastQuantity: Int? = null
        private set

    override suspend fun readCoils(
        config: ModbusMqttEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int> {
        lastConfig = config
        lastAddress = address
        lastQuantity = quantity
        return coilValues
    }
}

private fun sampleCoils(): List<Int> =
    listOf(
        1,
        0,
        2,
        0,
        1,
        0,
        0,
        3,
        0,
        1,
        0,
        0,
        4,
        0,
        1,
        0,
        0,
        5,
        0,
        1,
        0,
        0,
        6,
        1,
    )

private fun List<Int>.toPowerLightsRegisters(): Device24PowerLightsRegisters {
    require(size == 24) {
        "Expected exactly 24 coil values, but got $size"
    }
    return Device24PowerLightsRegisters(
        light1 = this[0] != 0,
        light2 = this[1] != 0,
        light3 = this[2] != 0,
        light4 = this[3] != 0,
        light5 = this[4] != 0,
        light6 = this[5] != 0,
        light7 = this[6] != 0,
        light8 = this[7] != 0,
        light9 = this[8] != 0,
        light10 = this[9] != 0,
        light11 = this[10] != 0,
        light12 = this[11] != 0,
        light13 = this[12] != 0,
        light14 = this[13] != 0,
        light15 = this[14] != 0,
        light16 = this[15] != 0,
        light17 = this[16] != 0,
        light18 = this[17] != 0,
        light19 = this[18] != 0,
        light20 = this[19] != 0,
        light21 = this[20] != 0,
        light22 = this[21] != 0,
        light23 = this[22] != 0,
        light24 = this[23] != 0,
    )
}
