package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        // <managed:plugin-market-compose-koin:start>
        site.addzero.kcloud.KCloudComposeScanKoinModule::class,
        site.addzero.kcloud.app.KCloudWorkbenchKoinModule::class,
        // <managed:plugin-market-compose-koin:end>
    ],
)
object KCloudComposeKoinApplication
