package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication
import site.addzero.kcloud.plugins.mcuconsole.McuConsoleServerKoinModule

@KoinApplication(
    configurations = ["vibepocket", "mcuconsole-server"],
    modules = [McuConsoleServerKoinModule::class],
)
object KCloudServerStarterKoinApplication
