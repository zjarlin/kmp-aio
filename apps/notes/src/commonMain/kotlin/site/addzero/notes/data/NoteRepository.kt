package site.addzero.notes.data

import site.addzero.notes.model.DataSourceHealth
import site.addzero.notes.model.Note
import site.addzero.notes.model.StorageSettings
import site.addzero.notes.model.StorageSettingsUpdate
import site.addzero.notes.model.SyncResult

interface NoteRepository {
    suspend fun listNotes(): List<Note>

    suspend fun createNote(): Note

    suspend fun saveNote(note: Note): Note

    suspend fun togglePinned(noteId: String): Note?

    suspend fun deleteNote(noteId: String)

    suspend fun sync(): SyncResult

    suspend fun dataSourceHealth(): List<DataSourceHealth>

    suspend fun storageSettings(): StorageSettings

    suspend fun updateStorageSettings(update: StorageSettingsUpdate): StorageSettings
}
