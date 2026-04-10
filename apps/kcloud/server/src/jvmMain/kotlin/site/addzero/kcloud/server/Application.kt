package site.addzero.kcloud.server

import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.di.initServerKoin
import site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.generated.springktor.registerGeneratedSpringRoutes as registerCodegenContextSpringRoutes
import site.addzero.kcloud.plugins.hostconfig.routes.catalog.generated.springktor.registerGeneratedSpringRoutes as registerHostConfigSpringRoutes
import site.addzero.kcloud.plugins.mcuconsole.routes.generated.springktor.registerGeneratedSpringRoutes as registerMcuConsoleSpringRoutes
import site.addzero.kcloud.server.module
import site.addzero.kcloud.server.runStarters
import site.addzero.kcloud.server.startServer
import site.addzero.starter.AppStarter
import site.addzero.starter.AppStarterTest
import kotlin.collections.filter

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
        port = 18080,
        module = Application::module,
    ).start(wait = wait)
    val engine = server.engine
    return engine
}

/**
 * 由 Ktor 启动配置指定调用。
 */
fun Application.module() {
    initServerKoin()
    runStarters()
    routing {
        //插件路由
        registerHostConfigSpringRoutes()
        //插件路由
        registerCodegenContextSpringRoutes()
        //插件路由
        registerMcuConsoleSpringRoutes()
    }
}

/**
 * 执行所有已注册的 AppStarter。
 *
 * 从 Koin 容器中获取所有 AppStarter 实现，按 order 排序，
 * 过滤条件后依次执行 onInstall()。
 */
fun Application.runStarters() {
    val app = this
    val koin = KoinPlatform.getKoin()
    val starters = koin.getAll<AppStarter>()
        .filter { starter -> starter.enable }
        .sortedBy { it.order }
    for (starter in starters) {
        log.info("Installing starter: ${starter::class.simpleName} (order=${starter.order})")
        starter.onInstall(app)
    }
}
