package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication
import site.addzero.configcenter.ktor.ConfigCenterKtorKoinModule
import site.addzero.kcloud.plugins.mcuconsole.McuConsoleServerKoinModule
import site.addzero.vibepocket.VibePocketKoinModule

@KoinApplication(
    configurations = ["vibepocket"],
    modules = [
        VibePocketKoinModule::class,
        McuConsoleServerKoinModule::class,
        ConfigCenterKtorKoinModule::class,
    ],
)
object KCloudServerStarterKoinApplication
