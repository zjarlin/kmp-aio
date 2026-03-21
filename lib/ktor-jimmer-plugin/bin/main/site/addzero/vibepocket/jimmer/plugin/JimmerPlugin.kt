package site.addzero.vibepocket.jimmer.plugin

import io.ktor.server.application.*

/**
 * Jimmer Ktor 插件。
 *
 * 通过 Koin 自动管理 KSqlClient 的生命周期。
 * 核心逻辑定义在 site.addzero.vibepocket.jimmer.di.DatabaseModule 中。
 */
val JimmerPlugin = createApplicationPlugin(name = "JimmerPlugin") {
    // 预留，目前主要依赖 Koin 自动扫描和注册。
}
