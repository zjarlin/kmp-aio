package site.addzero.coding.playground

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.coding.playground.server.config.defaultPlaygroundHttpServerEnabled

class CodingPlaygroundRuntime(
    private val koinApplication: KoinApplication,
    val state: PlaygroundWorkbenchState,
    val neteaseDemoState: NeteaseDemoState,
    private val httpServer: PlaygroundHttpServer?,
    private val httpServerEnabled: Boolean,
) {
    fun start() {
        state.startBackgroundSync()
        if (httpServerEnabled) {
            httpServer?.start(wait = false)
        }
    }

    fun stop() {
        state.stopBackgroundSync()
        if (httpServerEnabled) {
            httpServer?.stop()
        }
        koinApplication.close()
    }
}

fun createCodingPlaygroundRuntime(
    httpServerEnabled: Boolean = defaultPlaygroundHttpServerEnabled(),
): CodingPlaygroundRuntime {
    val koinApplication = startKoin {
        withConfiguration<CodingPlaygroundKoinApplication>()
    }
    val koin = koinApplication.koin
    return CodingPlaygroundRuntime(
        koinApplication = koinApplication,
        state = koin.get(),
        neteaseDemoState = koin.get(),
        httpServer = if (httpServerEnabled) koin.get() else null,
        httpServerEnabled = httpServerEnabled,
    )
}
