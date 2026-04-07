package site.addzero.kcloud.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.core.network.HttpClientModule
import site.addzero.kcloud.plugins.hostconfig.network.HostConfigApiModule

//@Module
//@ComponentScan("site.addzero")
@Module
@ComponentScan
@Configuration
class KCloudUiModule

@KoinApplication(
//    modules = [KCloudUiModule::class, HttpClientModule::class, HostConfigApiModule::class],
)
object KCloudUiKoinApplication

fun initUiKoin() {
    val existingKoin = KoinPlatformTools.defaultContext().getOrNull()
    if (existingKoin == null) {
        startKoin {
            withConfiguration<KCloudUiKoinApplication>()
        }
    }
}
