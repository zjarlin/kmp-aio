package site.addzero.kcloud.server

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        // <managed:plugin-market-server-koin:start>
        site.addzero.kcloud.server.KCloudServerScanKoinModule::class,
        site.addzero.kcloud.jimmer.di.JimmerKoinModule::class,
        site.addzero.kcloud.plugins.mcuconsole.McuConsoleServerKoinModule::class,
        site.addzero.configcenter.ktor.ConfigCenterKtorKoinModule::class,
        site.addzero.starter.banner.BannerStarterKoinModule::class,
        site.addzero.starter.flyway.FlywayStarterKoinModule::class,
        site.addzero.starter.openapi.OpenApiStarterKoinModule::class,
        site.addzero.starter.serialization.SerializationStarterKoinModule::class,
        site.addzero.starter.statuspages.StatusPagesStarterKoinModule::class,
        // <managed:plugin-market-server-koin:end>
    ],
)
object KCloudServerStarterKoinApplication
