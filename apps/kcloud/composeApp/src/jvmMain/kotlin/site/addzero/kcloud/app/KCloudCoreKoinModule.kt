package site.addzero.kcloud.app

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.scenes.notes.notesSceneScreens
import site.addzero.kcloud.scenes.ops.opsSceneScreens
import site.addzero.kcloud.scenes.secondbrain.secondBrainSceneScreens
import site.addzero.kcloud.scenes.system.systemSceneScreens
import site.addzero.kcloud.scenes.workspace.workspaceSceneScreens
import site.addzero.workbenchshell.ScreenCatalog

@Module
@ComponentScan("site.addzero.kcloud.app")
class KCloudCoreKoinModule {
    @Single
    fun screenCatalog(): ScreenCatalog = ScreenCatalog(
        kCloudShellRootScreens() +
            workspaceSceneScreens +
            notesSceneScreens +
            secondBrainSceneScreens +
            opsSceneScreens +
            systemSceneScreens,
    )
}
