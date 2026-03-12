package com.kcloud.plugins.notes.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.server.routes.installNotesRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import site.addzero.notes.server.store.NoteStoreRegistry

private val notesServerPluginModule = module {
    single { NoteStoreRegistry() }
    singleOf(::NotesServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object NotesServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(notesServerPluginModule)
}

class NotesServerPlugin : KCloudServerPlugin {
    override val pluginId = "notes-server-plugin"
    override val order = 35

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installNotesRoutes(koin.get())
    }

    override fun onStop(koin: org.koin.core.Koin) {
        koin.get<NoteStoreRegistry>().close()
    }
}
