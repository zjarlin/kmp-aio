package site.addzero.kcloud.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import org.koin.plugin.module.dsl.withConfiguration

@Module
@ComponentScan("site.addzero")
class KoinRootModule

@KoinApplication(
    modules = [KoinRootModule::class],
)
object KoinApp

fun initKoin() {
    val orNull = KoinPlatformTools.defaultContext().getOrNull()
    if (orNull == null) {
        startKoin {
            withConfiguration<KoinApp>()
        }
    }
}
