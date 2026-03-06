package site.addzero.notes.data

import site.addzero.notes.model.DataSourceHealth
import site.addzero.notes.model.DataSourceType
import site.addzero.notes.model.Note

class InMemoryNoteDataSource(
    override val type: DataSourceType,
    private val message: String,
    private val available: Boolean = true,
    seedNotes: List<Note> = emptyList()
) : NoteDataSource {

    private val notes = linkedMapOf<String, Note>().apply {
        seedNotes.forEach { note ->
            this[note.id] = note
        }
    }

    override suspend fun health(): DataSourceHealth {
        return DataSourceHealth(
            type = type,
            available = available,
            message = message
        )
    }

    override suspend fun fetchNotes(): List<Note> {
        if (!available) {
            return emptyList()
        }
        return notes.values.toList()
    }

    override suspend fun upsertNote(note: Note) {
        if (!available) {
            return
        }
        notes[note.id] = note
    }

    override suspend fun deleteNote(noteId: String) {
        if (!available) {
            return
        }
        notes.remove(noteId)
    }
}
