package com.kcloud.plugins.notes.server

import com.kcloud.plugin.KCloudServerPlugin
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single
import site.addzero.notes.server.generated.springktor.registerGeneratedSpringRoutes
import site.addzero.notes.server.store.NoteStoreService

@Single
class NotesServerPlugin(
    private val noteStoreService: NoteStoreService
) : KCloudServerPlugin {
    override val pluginId = "notes-server-plugin"
    override val order = 35

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }

    override fun onStop() {
        noteStoreService.close()
    }
}
