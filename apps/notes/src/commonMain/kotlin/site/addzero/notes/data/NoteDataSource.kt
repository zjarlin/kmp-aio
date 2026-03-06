package site.addzero.notes.data

import site.addzero.notes.model.DataSourceHealth
import site.addzero.notes.model.DataSourceType
import site.addzero.notes.model.Note

interface NoteDataSource {
    val type: DataSourceType

    suspend fun health(): DataSourceHealth

    suspend fun fetchNotes(): List<Note>

    suspend fun upsertNote(note: Note)

    suspend fun deleteNote(noteId: String)
}
