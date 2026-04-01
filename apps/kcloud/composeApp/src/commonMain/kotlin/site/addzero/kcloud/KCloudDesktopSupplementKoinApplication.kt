package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        // <managed:plugin-market-desktop-koin:start>
        site.addzero.kcloud.app.KCloudShellKoinModule::class,
        // <managed:plugin-market-desktop-koin:end>
    ],
)
object KCloudDesktopSupplementKoinApplication
