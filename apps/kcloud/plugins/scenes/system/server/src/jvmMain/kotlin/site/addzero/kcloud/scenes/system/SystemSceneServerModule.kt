package site.addzero.kcloud.scenes.system

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.feature.KCloudScenePlugin

@Module
@Configuration("kcloud")
@ComponentScan("site.addzero.kcloud.scenes.system")
class SystemSceneServerModule

@Single
class SystemScenePlugin : KCloudScenePlugin {
    override val sceneId: String = "system"
    override val displayName: String = "系统"
    override val sort: Int = 4
    override val pages = systemScenePages
}
