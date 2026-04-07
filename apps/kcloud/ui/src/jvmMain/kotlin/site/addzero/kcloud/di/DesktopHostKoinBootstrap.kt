package site.addzero.kcloud.di

import org.koin.core.annotation.KoinApplication
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import org.koin.plugin.module.dsl.withConfiguration

/**
 * 桌面宿主把 UI 和内嵌 Ktor server 放在同一个 JVM 时，只启动这一套全局 Koin。
 *
 * server 侧会复用这一套完整根图，而不是再起第二套上下文。
 * `KCloudServerModule` 自己已经 `includes = [JimmerKoinModule::class]`，所以桌面宿主
 * 只需要自动发现 server root，不需要再直连内部 plain module。
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
