package site.addzero.kcloud.scenes.notes

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.feature.KCloudScenePlugin

@Module
@Configuration("kcloud")
@ComponentScan("site.addzero.kcloud.scenes.notes")
class NotesSceneServerModule

@Single
class NotesScenePlugin : KCloudScenePlugin {
    override val sceneId: String = "notes"
    override val displayName: String = "笔记"
    override val sort: Int = 1
    override val pages = notesScenePages
}
