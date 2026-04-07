package site.addzero.kcloud.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import org.koin.plugin.module.dsl.withConfiguration

@Module
@ComponentScan("site.addzero")
@Configuration
class KCloudServerModule

@KoinApplication
object KCloudServerKoinApplication

/**
 * 供独立 server 进程启动完整服务端 Koin。
 *
 * 如果当前 JVM 已由桌面宿主启动过全局 Koin，这里只复用现有上下文。
 */
fun initServerKoin() {
    val existingKoin = KoinPlatformTools.defaultContext().getOrNull()
    if (existingKoin == null) {
        startKoin {
            withConfiguration<KCloudServerKoinApplication>()
        }
    }
}
