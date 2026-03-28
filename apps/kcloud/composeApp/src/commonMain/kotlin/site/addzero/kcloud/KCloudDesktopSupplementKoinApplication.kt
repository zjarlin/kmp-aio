package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication
import site.addzero.kcloud.app.KCloudWorkbenchKoinModule
import site.addzero.kcloud.plugins.mcuconsole.McuConsoleComposeKoinModule

@KoinApplication(
    modules = [KCloudWorkbenchKoinModule::class, McuConsoleComposeKoinModule::class],
)
object KCloudDesktopSupplementKoinApplication
