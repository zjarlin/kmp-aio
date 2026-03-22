package com.kcloud.features.notes.server

import com.kcloud.feature.ServerLifecycleContributor
import org.koin.core.annotation.Single
import site.addzero.notes.server.store.NoteStoreService

@Single
class NotesServerLifecycleContributor(
    private val noteStoreService: NoteStoreService,
) : ServerLifecycleContributor {
    override val order = 35

    override fun onStop() {
        noteStoreService.close()
    }
}
