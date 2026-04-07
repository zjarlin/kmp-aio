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
interface AppStarterTest {
    fun onstart()

}
