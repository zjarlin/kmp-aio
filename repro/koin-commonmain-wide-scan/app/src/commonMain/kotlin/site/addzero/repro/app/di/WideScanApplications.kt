package site.addzero.repro.app.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import site.addzero.repro.feature.provider.PlainProviderModule
import site.addzero.repro.feature.provider.ProviderGraphModule

@Module
@Configuration
@ComponentScan("site.addzero.repro")
class DefaultWideScanRootModule

@KoinApplication
object DefaultWideScanOnlyKoinApplication

@KoinApplication
object DefaultWideScanWithAutoProviderStarterKoinApplication

@KoinApplication(
    configurations = ["banner-like"],
)
object BannerLikeStarterOnlyKoinApplication

@KoinApplication(
    configurations = ["no-scan"],
)
object NoScanStarterOnlyKoinApplication

@Module
@Configuration("wide-scan")
@ComponentScan("site.addzero.repro")
class WideScanRootModule

@Module
@Configuration("plain-scan")
@ComponentScan("site.addzero.repro")
class PlainScanRootModule

@KoinApplication(
    configurations = ["wide-scan"],
)
object WideScanOnlyKoinApplication

@KoinApplication(
    configurations = ["plain-scan"],
)
object PlainScanOnlyKoinApplication

@KoinApplication(
    configurations = ["plain-scan"],
    modules = [PlainProviderModule::class, ProviderGraphModule::class],
)
object PlainScanWithExplicitProviderKoinApplication

@KoinApplication(
    configurations = ["wide-scan"],
)
object WideScanWithAutoProviderStarterKoinApplication
