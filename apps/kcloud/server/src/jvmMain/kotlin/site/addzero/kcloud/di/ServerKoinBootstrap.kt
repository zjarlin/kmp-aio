package site.addzero.kcloud.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kcloud.jimmer.di.JimmerKoinModule

//@Module
//@ComponentScan("site.addzero")
@Module
@ComponentScan
@Configuration
class KCloudServerModule

@KoinApplication(
    modules = [KCloudServerModule::class, JimmerKoinModule::class
    ],
)
object KCloudServerKoinApplication

fun initServerKoin() {
    val existingKoin = KoinPlatformTools.defaultContext().getOrNull()
    if (existingKoin == null) {
        startKoin {
            withConfiguration<KCloudServerKoinApplication>()
        }
    }
}
