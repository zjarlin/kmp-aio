package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication
import site.addzero.kcloud.app.KCloudWorkbenchKoinModule
import site.addzero.kcloud.plugins.mcuconsole.McuConsoleComposeKoinModule
import site.addzero.kcloud.plugins.system.configcenter.ConfigCenterComposeKoinModule
import site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketComposeKoinModule

@KoinApplication(
    modules = [
        // <managed:plugin-market-desktop-koin:start>
        site.addzero.kcloud.app.KCloudWorkbenchKoinModule::class,
        site.addzero.kcloud.plugins.mcuconsole.McuConsoleComposeKoinModule::class,
        site.addzero.kcloud.plugins.system.configcenter.ConfigCenterComposeKoinModule::class,
        site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketComposeKoinModule::class,
        // <managed:plugin-market-desktop-koin:end>
    ],
)
object KCloudDesktopSupplementKoinApplication
