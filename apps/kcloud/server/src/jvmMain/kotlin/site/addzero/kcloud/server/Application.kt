package site.addzero.kcloud.server

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import site.addzero.kcloud.di.initKoin
import site.addzero.starter.koin.runStarters

/**
 * Server 入口。
 */
fun main(args: Array<String>) {
    embeddedServer(
        factory = Netty,
        host = "0.0.0.0",
        port = 18080,
        module = Application::module,
    ).start(wait = true)
}

/**
 * 由 Ktor 启动配置指定调用。
 */
fun Application.module() {
    initKoin()
    runStarters()
    routing {
//        registerKCloudPluginRoutes()
    }
}
