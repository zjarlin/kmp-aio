package site.addzero.kcloud.scenes.ops

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.feature.KCloudScenePlugin

@Module
@Configuration("kcloud")
@ComponentScan("site.addzero.kcloud.scenes.ops")
class OpsSceneServerModule

@Single
class OpsScenePlugin : KCloudScenePlugin {
    override val sceneId: String = "ops"
    override val displayName: String = "运维"
    override val sort: Int = 3
    override val pages = opsScenePages
}
