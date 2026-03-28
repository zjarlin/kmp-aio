package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication
import site.addzero.kcloud.app.KCloudWorkbenchKoinModule
import site.addzero.kcloud.plugins.mcuconsole.McuConsoleComposeKoinModule
import site.addzero.vibepocket.VibePocketKoinModule

@KoinApplication(
    configurations = ["vibepocket", "mcuconsole-compose"],
    modules = [KCloudWorkbenchKoinModule::class, VibePocketKoinModule::class, McuConsoleComposeKoinModule::class],
)
object KCloudComposeKoinApplication
