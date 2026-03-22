package site.addzero.coding.playground

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.withConfiguration

class CodingPlaygroundRuntime(
    private val koinApplication: KoinApplication,
    val state: PlaygroundWorkbenchState,
    private val httpServer: PlaygroundHttpServer,
) {
    fun start() {
        httpServer.start(wait = false)
    }

    fun stop() {
        httpServer.stop()
        koinApplication.close()
    }
}

fun createCodingPlaygroundRuntime(): CodingPlaygroundRuntime {
    val koinApplication = startKoin {
        withConfiguration<CodingPlaygroundKoinApplication>()
    }
    val koin = koinApplication.koin
    return CodingPlaygroundRuntime(
        koinApplication = koinApplication,
        state = koin.get(),
        httpServer = koin.get(),
    )
}
