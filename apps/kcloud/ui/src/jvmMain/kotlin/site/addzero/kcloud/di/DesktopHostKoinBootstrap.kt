package site.addzero.kcloud.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import org.koin.plugin.module.dsl.withConfiguration

@Module
@ComponentScan
@Configuration
class JvmUIModule

/**
 * 桌面宿主把 UI 和内嵌 Ktor server 放在同一个 JVM 时，只启动这一套全局 Koin。
 */
@KoinApplication
object KCloudDesktopHostKoinApplication

fun initDesktopHostKoin() {
    val existingKoin = KoinPlatformTools.defaultContext().getOrNull()
    if (existingKoin == null) {
        startKoin {
            withConfiguration<KCloudDesktopHostKoinApplication>()
        }
    }
}
