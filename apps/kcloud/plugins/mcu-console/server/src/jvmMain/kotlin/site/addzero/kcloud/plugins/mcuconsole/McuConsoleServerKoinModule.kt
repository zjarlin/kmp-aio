package site.addzero.kcloud.plugins.mcuconsole

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import site.addzero.kcloud.plugins.mcuconsole.infra.McuConsoleInfraKoinModule
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuConsoleModbusKoinModule

@Module(
    includes = [
        McuConsoleInfraKoinModule::class,
        McuConsoleModbusKoinModule::class,
    ],
)
@ComponentScan("site.addzero.kcloud.plugins.mcuconsole")
class McuConsoleServerKoinModule
