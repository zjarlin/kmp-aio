package site.addzero.kcloud.server

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import site.addzero.configcenter.ConfigCenterRuntimeModule

@Module(
    includes = [
        ConfigCenterRuntimeModule::class,
        ServerRuntimeConfigCenterModule::class,
    ],
)
@ComponentScan("site.addzero")
class ServerScanModule
