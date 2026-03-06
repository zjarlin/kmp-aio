package site.addzero.notes.data

import site.addzero.notes.api.NotesApi
import site.addzero.notes.api.NotesApiClient
import site.addzero.notes.api.NoteUpsertRequest
import site.addzero.notes.model.DataSourceHealth
import site.addzero.notes.model.DataSourceType
import site.addzero.notes.model.Note

class ApiNoteDataSource(
    override val type: DataSourceType,
    private val sourceKey: String,
    private val apiProvider: () -> NotesApi = { NotesApiClient.notesApi() }
) : NoteDataSource {
    override suspend fun health(): DataSourceHealth {
        return runCatching {
            val response = apiProvider().health(sourceKey)
            DataSourceHealth(
                type = type,
                available = response.available,
                message = response.message
            )
        }.getOrElse { throwable ->
            DataSourceHealth(
                type = type,
                available = false,
                message = throwable.message.orEmpty().ifBlank { "网络请求失败" }
            )
        }
    }

    override suspend fun fetchNotes(): List<Note> {
        return apiProvider().listNotes(sourceKey).map { payload ->
            Note(
                id = payload.id,
                path = payload.path,
                title = payload.title,
                markdown = payload.markdown,
                pinned = payload.pinned,
                version = payload.version
            )
        }
    }

    override suspend fun upsertNote(note: Note) {
        apiProvider().upsertNote(
            source = sourceKey,
            id = note.id,
            request = NoteUpsertRequest(
                id = note.id,
                path = note.path,
                title = note.title,
                markdown = note.markdown,
                pinned = note.pinned,
                version = note.version
            )
        )
    }

    override suspend fun deleteNote(noteId: String) {
        apiProvider().deleteNote(source = sourceKey, id = noteId)
    }
}
