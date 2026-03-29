package site.addzero.kcloud.plugins.system.pluginmarket

import io.ktor.server.routing.*
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.route.config.generated.springktor.registerGeneratedSpringRoutes

/**
 * 插件市场服务端路由入口。
 */
fun Route.pluginMarketRoutes() {
    registerGeneratedSpringRoutes()
}
