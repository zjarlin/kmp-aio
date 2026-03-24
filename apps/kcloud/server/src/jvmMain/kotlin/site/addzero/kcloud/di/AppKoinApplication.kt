package site.addzero.kcloud.di

import org.koin.core.annotation.KoinApplication
import site.addzero.kcloud.scenes.notes.NotesSceneServerModule
import site.addzero.kcloud.scenes.ops.OpsSceneServerModule
import site.addzero.kcloud.scenes.secondbrain.SecondBrainSceneServerModule
import site.addzero.kcloud.scenes.system.SystemSceneServerModule
import site.addzero.kcloud.scenes.workspace.WorkspaceSceneServerModule

@KoinApplication(
    configurations = ["kcloud"],
    modules = [
        WorkspaceSceneServerModule::class,
        NotesSceneServerModule::class,
        SecondBrainSceneServerModule::class,
        OpsSceneServerModule::class,
        SystemSceneServerModule::class,
    ],
)
object KCloudServerKoinApplication
