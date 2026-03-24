package site.addzero.kcloud

import site.addzero.kcloud.app.KCloudHttpServer
import site.addzero.kcloud.app.KCloudCoreKoinModule
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.kcloud.app.module as kCloudCoreModule
import site.addzero.kcloud.feature.KCloudSceneApiKoinModule
import site.addzero.kcloud.feature.module as sceneApiModule
import site.addzero.kcloud.feature.DesktopLifecycleContributor
import site.addzero.kcloud.feature.ServerLifecycleContributor
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import site.addzero.kcloud.scenes.notes.NotesSceneServerModule
import site.addzero.kcloud.scenes.notes.module as notesSceneModule
import site.addzero.kcloud.scenes.ops.OpsSceneServerModule
import site.addzero.kcloud.scenes.ops.module as opsSceneModule
import site.addzero.kcloud.scenes.secondbrain.SecondBrainSceneServerModule
import site.addzero.kcloud.scenes.secondbrain.module as secondBrainSceneModule
import site.addzero.kcloud.scenes.system.SystemSceneServerModule
import site.addzero.kcloud.scenes.system.module as systemSceneModule
import site.addzero.kcloud.scenes.workspace.WorkspaceSceneServerModule
import site.addzero.kcloud.scenes.workspace.module as workspaceSceneModule
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
        koinApplication.close()
    }

    fun stopServer() {
        httpServer.stop()
        serverLifecycleContributors
            .asReversed()
            .forEach { contributor -> contributor.onStop() }
        koinApplication.close()
    }
}

fun createKCloudRuntime(): KCloudRuntime {
    val koinApplication = startKoin {
        modules(kCloudDesktopModules())
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

private fun kCloudDesktopModules(): List<Module> = listOf(
    KCloudCoreKoinModule().kCloudCoreModule(),
    KCloudSceneApiKoinModule().sceneApiModule(),
    WorkspaceSceneServerModule().workspaceSceneModule(),
    NotesSceneServerModule().notesSceneModule(),
    SecondBrainSceneServerModule().secondBrainSceneModule(),
    OpsSceneServerModule().opsSceneModule(),
    SystemSceneServerModule().systemSceneModule(),
)
