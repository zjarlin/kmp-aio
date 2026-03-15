package site.addzero.notes.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.notes.server.model.DataSourceHealthPayload
import site.addzero.notes.server.model.NotePayload
import site.addzero.notes.server.model.NoteUpsertRequest
import site.addzero.notes.server.model.StorageSettingsPayload
import site.addzero.notes.server.model.StorageSettingsUpdateRequest
import site.addzero.notes.server.store.JdbcNoteStore
import site.addzero.notes.server.store.NoteStoreService

@GetMapping("/api/notes/settings")
fun readNoteSettings(call: ApplicationCall): StorageSettingsPayload {
    return call.noteStoreService().readSettings()
}

@PutMapping("/api/notes/settings")
fun updateNoteSettings(
    call: ApplicationCall,
    @RequestBody request: StorageSettingsUpdateRequest,
): StorageSettingsPayload {
    return call.noteStoreService().updateSettings(request)
}

@GetMapping("/api/notes/{source}/health")
fun readNoteHealth(
    call: ApplicationCall,
    @PathVariable source: String,
): DataSourceHealthPayload {
    val store = call.resolveStoreOrNull(source)
    if (store == null) {
        call.response.status(HttpStatusCode.NotFound)
        return DataSourceHealthPayload(
            source = source,
            available = false,
            message = "unknown source",
        )
    }

    return DataSourceHealthPayload(
        source = source,
        available = store.isReady(),
        message = store.healthMessage(),
    )
}

@GetMapping("/api/notes/{source}")
fun listNotes(
    call: ApplicationCall,
    @PathVariable source: String,
): List<NotePayload> {
    val store = call.resolveStoreOrNull(source)
    if (store == null) {
        call.response.status(HttpStatusCode.NotFound)
        return emptyList()
    }
    if (!store.isReady()) {
        call.response.status(HttpStatusCode.ServiceUnavailable)
        return emptyList()
    }

    return store.listNotes()
}

@PutMapping("/api/notes/{source}/{id}")
suspend fun upsertNote(
    call: ApplicationCall,
    @PathVariable source: String,
    @PathVariable id: String,
    @RequestBody request: NoteUpsertRequest,
) {
    val store = call.resolveStoreOrNull(source)
    if (store == null) {
        call.respond(HttpStatusCode.NotFound)
        return
    }
    if (!store.isReady()) {
        call.respond(HttpStatusCode.ServiceUnavailable)
        return
    }

    val saved = store.upsertNote(
        NotePayload(
            id = if (request.id.isBlank()) id else request.id,
            path = request.path,
            title = request.title,
            markdown = request.markdown,
            pinned = request.pinned,
            version = request.version,
        ),
    )
    call.respond(saved)
}

@DeleteMapping("/api/notes/{source}/{id}")
suspend fun deleteNote(
    call: ApplicationCall,
    @PathVariable source: String,
    @PathVariable id: String,
) {
    val store = call.resolveStoreOrNull(source)
    if (store == null) {
        call.respond(HttpStatusCode.NotFound)
        return
    }

    store.deleteNote(id)
    call.respond(HttpStatusCode.NoContent)
}

private fun ApplicationCall.noteStoreService(): NoteStoreService {
    return application.getKoin().get()
}

private fun ApplicationCall.resolveStoreOrNull(source: String): JdbcNoteStore? {
    return noteStoreService().resolve(source)
}
