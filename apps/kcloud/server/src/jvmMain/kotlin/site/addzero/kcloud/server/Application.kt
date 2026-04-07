package site.addzero.kcloud.server

import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import site.addzero.kcloud.di.initKoin
import site.addzero.kcloud.plugins.hostconfig.routes.cloud.generated.springktor.registerGeneratedSpringRoutes
import site.addzero.kcloud.runtime.KCloudHostRuntime
import site.addzero.starter.koin.runStarters

/**
 * Server 入口。
 */
fun main() {
    startServer(wait = true)
}

/**
 * 启动 KCloud 内嵌服务端。
 */
fun startServer(
    wait: Boolean,
): ApplicationEngine {
    val server = embeddedServer(
        factory = Netty,
        host = "0.0.0.0",
        port = KCloudHostRuntime.DEFAULT_SERVER_PORT,
        module = Application::module,
    ).start(wait = wait)
    val engine = server.engine
    return engine
}

/**
 * 由 Ktor 启动配置指定调用。
 */
fun Application.module() {
    initKoin()
    runStarters()
    routing {
        registerGeneratedSpringRoutes()
    }
}
