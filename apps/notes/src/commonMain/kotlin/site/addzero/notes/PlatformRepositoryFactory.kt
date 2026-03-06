package site.addzero.notes

import site.addzero.notes.data.NoteRepository

expect object PlatformRepositoryFactory {
    fun createNoteRepository(): NoteRepository
}
