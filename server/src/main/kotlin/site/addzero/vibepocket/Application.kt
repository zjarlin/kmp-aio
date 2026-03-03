package site.addzero.vibepocket

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.starter.koin.installKoin
import site.addzero.starter.koin.runStarters
import site.addzero.vibepocket.di.AppKoinApplication
import site.addzero.vibepocket.routes.ioc.generated.iocModule

/**
 * EngineMain 入口 — Ktor 自动加载 application.conf，
 * 读取 ktor.application.modules 配置调用 Application.module()。
 */
fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

/**
 * 由 application.conf 中 ktor.application.modules 指定调用。
 */
fun Application.module() {
    // 1. Koin 必须最先初始化（发现机制依赖它）
    installKoin {
        withConfiguration<AppKoinApplication>()
    }
    // 2. 自动发现并执行所有 Starter
    runStarters()
    // 3. 路由聚合（iocModule 由 @Bean KSP 生成）
    routing { iocModule() }
}

/**
 * 桌面端内嵌启动入口（非阻塞），返回 server 实例。
 * 从 application.conf 读取端口配置，支持环境变量覆盖。
 */
fun ktorApplication(
    configPath: String? = null,
    host: String? = null,
    port: Int? = null,
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    // 加载配置（优先使用指定路径，否则从 classpath 加载 application.conf）
    val config = configPath?.let {
        ApplicationConfig(it)
    } ?: ApplicationConfigFactory.load()

    // 优先级：参数 > 环境变量 > 配置文件 > 默认值
    val finalHost = host
        ?: System.getenv("SERVER_HOST")
        ?: config.propertyOrNull("ktor.deployment.host")?.getString()
        ?: "0.0.0.0"

    val finalPort = port
        ?: System.getenv("SERVER_PORT")?.toIntOrNull()
        ?: config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull()
        ?: 8080

    return embeddedServer(Netty, port = finalPort, host = finalHost, module = Application::module)
}
