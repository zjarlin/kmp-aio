package site.addzero.kcloud.scenes.workspace

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.feature.KCloudScenePlugin

@Module
@Configuration("kcloud")
@ComponentScan("site.addzero.kcloud.scenes.workspace")
class WorkspaceSceneServerModule

@Single
class WorkspaceScenePlugin : KCloudScenePlugin {
    override val sceneId: String = "workspace"
    override val displayName: String = "工作台"
    override val sort: Int = 0
    override val pages = workspaceScenePages
}
