package com.kcloud.plugins.notes

import com.kcloud.plugin.KCloudKoinModuleProvider
import org.koin.core.module.Module
import site.addzero.notes.server.NotesServerKoinModule
import site.addzero.notes.server.module as notesServerKoinModule

class NotesKoinModuleProvider : KCloudKoinModuleProvider {
    override fun modules(): List<Module> {
        return listOf(NotesServerKoinModule().notesServerKoinModule())
    }
}
