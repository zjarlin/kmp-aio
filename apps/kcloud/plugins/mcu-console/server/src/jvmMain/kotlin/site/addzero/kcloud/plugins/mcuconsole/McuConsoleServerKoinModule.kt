package site.addzero.kcloud.plugins.mcuconsole

import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigRegistry
import site.addzero.device.driver.modbus.rtu.ModbusRtuExecutor
import site.addzero.device.driver.modbus.rtu.createDefaultModbusRtuExecutor
import site.addzero.esp32_host_computer.generated.modbus.rtu.AutomicModbusApiGeneratedRtuConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.rtu.AutomicModbusApiGeneratedRtuGateway
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuGateway

@Module
@ComponentScan("site.addzero.kcloud.plugins.mcuconsole")
class McuConsoleServerKoinModule {
    @Single
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Single
    fun provideAutomicModbusConfigProvider(): AutomicModbusApiGeneratedRtuConfigProvider {
        return AutomicModbusApiGeneratedRtuConfigProvider()
    }

    @Single
    fun provideDeviceModbusConfigProvider(): DeviceApiGeneratedRtuConfigProvider {
        return DeviceApiGeneratedRtuConfigProvider()
    }

    @Single
    fun provideModbusRtuExecutor(): ModbusRtuExecutor {
        return createDefaultModbusRtuExecutor()
    }

    @Single
    fun provideModbusRtuConfigRegistry(
        configProvider: AutomicModbusApiGeneratedRtuConfigProvider,
        deviceConfigProvider: DeviceApiGeneratedRtuConfigProvider,
    ): ModbusRtuConfigRegistry {
        // 生成的 Modbus Koin 模块目前不会自动并入 *ServerKoinModule 聚合，
        // 因此这里显式收口真实会用到的多个 RTU 契约默认配置。
        return ModbusRtuConfigRegistry(
            listOf(
                configProvider,
                deviceConfigProvider,
            ),
        )
    }

    @Single
    fun provideAutomicModbusGateway(
        configRegistry: ModbusRtuConfigRegistry,
        executor: ModbusRtuExecutor,
    ): AutomicModbusApiGeneratedRtuGateway {
        return AutomicModbusApiGeneratedRtuGateway(
            configRegistry = configRegistry,
            executor = executor,
        )
    }

    @Single
    fun provideDeviceModbusGateway(
        configRegistry: ModbusRtuConfigRegistry,
        executor: ModbusRtuExecutor,
    ): DeviceApiGeneratedRtuGateway {
        return DeviceApiGeneratedRtuGateway(
            configRegistry = configRegistry,
            executor = executor,
        )
    }
}
