package site.addzero.kcloud.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import org.koin.plugin.module.dsl.withConfiguration

/**
 * UI 根扫描只负责聚合 app 自己依赖到的叶子模块。
 *
 * 子模块如果需要暴露 provider module，应在自己的模块里提供 public
 * `@Configuration` 入口，而不是要求根应用再手动 include。
 */
@Module
@ComponentScan("site.addzero")
@Configuration
class KCloudUiModule

@KoinApplication
object KCloudUiKoinApplication

/**
 * 供纯前端入口启动 UI-only Koin。
 *
 * 桌面宿主如果已经启动了 host 级全局 Koin，这里只复用现有上下文，不再切换根图。
 */
fun initUiKoin() {
    val existingKoin = KoinPlatformTools.defaultContext().getOrNull()
    if (existingKoin == null) {
        startKoin {
            withConfiguration<KCloudUiKoinApplication>()
        }
    }
}
