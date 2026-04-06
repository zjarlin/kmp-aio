package site.addzero.kcloud.server

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kcloud.server.context.hardcodedServerContextConfig
import site.addzero.starter.koin.installKoin
import site.addzero.starter.koin.runStarters

/**
 * Server 入口。
 */
fun main(args: Array<String>) {
    val serverConfig = hardcodedServerContextConfig().network
    embeddedServer(
        factory = Netty,
        host = serverConfig.host,
        port = serverConfig.port,
        module = Application::module,
    ).start(wait = true)
}

/**
 * 由 Ktor 启动配置指定调用。
 */
fun Application.module() {
    installKoin {
        withConfiguration<KCloudServerStarterKoinApplication>()
    }
    runStarters()
    routing {
        registerKCloudPluginRoutes()
    }
}
