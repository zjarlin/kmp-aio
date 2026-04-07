package site.addzero.starter

import io.ktor.server.application.*

/**
 * 应用启动器 SPI 接口。
 *
 * 任何模块只要实现此接口并被发现机制注册（当前使用 Koin），
 * 就会在主应用启动时被自动调用。
 *
 * 设计为框架无关：接口本身不绑定 Koin 或 ServiceLoader，
 * 未来可无缝切换到 KMP 标准 SPI。
 */
interface AppStarter <T>{
    /** 排序值，越小越先执行 */
    val order get() = Int.MAX_VALUE

    /** 是否启用当前 starter */
    val enable: Boolean

    /** 执行安装逻辑 */
    fun T.onInstall()
}

/**
 * Ktor `Application` 启动器的稳定 DI 入口。
 *
 * Koin 自动绑定不会把 `AppStarter<T>` 的泛型参数当成区分 key，
 * 所以运行时注入不要直接依赖 `AppStarter<Application>`，而是统一依赖这个特化接口。
 */
interface KtorAppStarter : AppStarter<Application>
