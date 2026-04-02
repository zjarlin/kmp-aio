package site.addzero.kcloud.plugins.mcuconsole.modbus

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigRegistry
import site.addzero.device.driver.modbus.rtu.ModbusRtuExecutor
import site.addzero.device.driver.modbus.rtu.createDefaultModbusRtuExecutor
import site.addzero.esp32_host_computer.generated.modbus.rtu.AutomicModbusApiGeneratedRtuConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.rtu.AutomicModbusApiGeneratedRtuGateway
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuGateway
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceWriteApiGeneratedRtuConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceWriteApiGeneratedRtuGateway

/**
 * 收口 Modbus RTU 生成网关与执行器，避免继续堆在插件根模块里。
 */
@Module
class McuConsoleModbusKoinModule {
    @Single
    fun provideAutomicModbusConfigProvider(): AutomicModbusApiGeneratedRtuConfigProvider {
        return AutomicModbusApiGeneratedRtuConfigProvider()
    }

    @Single
    fun provideDeviceModbusConfigProvider(): DeviceApiGeneratedRtuConfigProvider {
        return DeviceApiGeneratedRtuConfigProvider()
    }

    @Single
    fun provideDeviceWriteModbusConfigProvider(): DeviceWriteApiGeneratedRtuConfigProvider {
        return DeviceWriteApiGeneratedRtuConfigProvider()
    }

    @Single
    fun provideModbusRtuExecutor(): ModbusRtuExecutor {
        return createDefaultModbusRtuExecutor()
    }

    @Single
    fun provideModbusRtuConfigRegistry(
        configProvider: AutomicModbusApiGeneratedRtuConfigProvider,
        deviceConfigProvider: DeviceApiGeneratedRtuConfigProvider,
        deviceWriteConfigProvider: DeviceWriteApiGeneratedRtuConfigProvider,
    ): ModbusRtuConfigRegistry {
        // 生成的 Modbus Koin 模块目前不会自动并入插件根模块，因此这里显式收口真实会用到的 RTU 默认配置。
        return ModbusRtuConfigRegistry(
            listOf(
                configProvider,
                deviceConfigProvider,
                deviceWriteConfigProvider,
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

    @Single
    fun provideDeviceWriteModbusGateway(
        configRegistry: ModbusRtuConfigRegistry,
        executor: ModbusRtuExecutor,
    ): DeviceWriteApiGeneratedRtuGateway {
        return DeviceWriteApiGeneratedRtuGateway(
            configRegistry = configRegistry,
            executor = executor,
        )
    }
}
