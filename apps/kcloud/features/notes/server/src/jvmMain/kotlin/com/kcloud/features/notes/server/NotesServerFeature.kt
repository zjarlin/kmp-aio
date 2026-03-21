package com.kcloud.features.notes.server

import com.kcloud.feature.KCloudServerFeature
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single
import site.addzero.notes.server.generated.springktor.registerGeneratedSpringRoutes
import site.addzero.notes.server.store.NoteStoreService

@Single
class NotesServerFeature(
    private val noteStoreService: NoteStoreService,
) : KCloudServerFeature {
    override val featureId = "notes"
    override val order = 35

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }

    override fun onStop() {
        noteStoreService.close()
    }
}
