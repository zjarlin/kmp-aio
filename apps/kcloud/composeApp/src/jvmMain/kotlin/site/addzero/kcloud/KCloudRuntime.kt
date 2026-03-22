package site.addzero.kcloud

import site.addzero.kcloud.app.KCloudHttpServer
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.kcloud.app.generated.ioc.aggregate.registerAggregatedIocModules
import site.addzero.kcloud.db.Database
import site.addzero.kcloud.feature.DesktopLifecycleContributor
import site.addzero.kcloud.feature.ServerLifecycleContributor
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.workbenchshell.ScreenCatalog

class KCloudRuntime(
    private val koinApplication: KoinApplication,
    val shellState: KCloudShellState,
    val screenCatalog: ScreenCatalog,
    val desktopLifecycleContributors: List<DesktopLifecycleContributor>,
    val serverLifecycleContributors: List<ServerLifecycleContributor>,
    private val httpServer: KCloudHttpServer,
) {
    val koin: Koin
        get() = koinApplication.koin

    fun startDesktop() {
        httpServer.start(wait = false)
        serverLifecycleContributors.forEach { contributor ->
            contributor.onStart()
        }
        desktopLifecycleContributors.forEach { contributor ->
            contributor.onStart(koin)
        }
    }

    fun startServer(wait: Boolean) {
        httpServer.start(wait = wait)
        serverLifecycleContributors.forEach { contributor ->
            contributor.onStart()
        }
    }

    fun stopDesktop() {
        desktopLifecycleContributors
            .asReversed()
            .forEach { contributor -> contributor.onStop(koin) }
        httpServer.stop()
        serverLifecycleContributors
            .asReversed()
            .forEach { contributor -> contributor.onStop() }
        koin.get<Database>().close()
        koinApplication.close()
    }

    fun stopServer() {
        httpServer.stop()
        serverLifecycleContributors
            .asReversed()
            .forEach { contributor -> contributor.onStop() }
        koin.get<Database>().close()
        koinApplication.close()
    }
}

fun createKCloudRuntime(): KCloudRuntime {
    registerAggregatedIocModules()
    val koinApplication = startKoin {
        withConfiguration<KCloudKoinApplication>()
    }
    val koin = koinApplication.koin
    return KCloudRuntime(
        koinApplication = koinApplication,
        shellState = koin.get(),
        screenCatalog = koin.get(),
        desktopLifecycleContributors = koin.getAll<DesktopLifecycleContributor>().sortedBy { it.order },
        serverLifecycleContributors = koin.getAll<ServerLifecycleContributor>().sortedBy { it.order },
        httpServer = koin.get(),
    )
}
