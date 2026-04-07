package site.addzero.starter

import io.ktor.server.application.*

/**
 * Ktor 应用启动器 SPI 接口。
 *
 * 任何模块只要实现此接口并被发现机制注册（当前使用 Koin），
 * 就会在主应用启动时被自动调用。
 *
 * 当前仓库里的 starter 只服务于 Ktor `Application` 启动链，
 * 所以这里直接暴露 Ktor 目标，而不再保留无收益的泛型抽象。
 */
interface AppStarter {
    /** 排序值，越小越先执行 */
    val order get() = Int.MAX_VALUE

    /** 是否启用当前 starter */
    val enable: Boolean

    /** 执行安装逻辑 */
    fun onInstall(application: Application)
}
