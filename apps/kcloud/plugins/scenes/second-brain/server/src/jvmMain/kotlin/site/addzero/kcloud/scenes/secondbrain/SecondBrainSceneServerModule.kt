package site.addzero.kcloud.scenes.secondbrain

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.feature.KCloudScenePlugin

@Module
@Configuration("kcloud")
@ComponentScan("site.addzero.kcloud.scenes.secondbrain")
class SecondBrainSceneServerModule

@Single
class SecondBrainScenePlugin : KCloudScenePlugin {
    override val sceneId: String = "second-brain"
    override val displayName: String = "第二大脑"
    override val sort: Int = 2
    override val pages = secondBrainScenePages
}
