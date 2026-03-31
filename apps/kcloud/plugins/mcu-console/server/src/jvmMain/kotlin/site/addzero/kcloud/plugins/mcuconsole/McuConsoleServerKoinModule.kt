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
    fun provideModbusRtuExecutor(): ModbusRtuExecutor {
        return createDefaultModbusRtuExecutor()
    }

    @Single
    fun provideModbusRtuConfigRegistry(
        configProvider: AutomicModbusApiGeneratedRtuConfigProvider,
    ): ModbusRtuConfigRegistry {
        return ModbusRtuConfigRegistry(listOf(configProvider))
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
}
