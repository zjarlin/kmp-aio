package site.addzero.notes.server.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.starter.statuspages.ServiceUnavailableHttpException
import site.addzero.notes.server.model.DataSourceHealthPayload
import site.addzero.notes.server.model.NotePayload
import site.addzero.notes.server.model.NoteUpsertRequest
import site.addzero.notes.server.model.StorageSettingsPayload
import site.addzero.notes.server.model.StorageSettingsUpdateRequest
import site.addzero.notes.server.store.JdbcNoteStore
import site.addzero.notes.server.store.NoteStoreService

@GetMapping("/api/notes/settings")
fun readNoteSettings(): StorageSettingsPayload {
    return noteStoreService().readSettings()
}

@PutMapping("/api/notes/settings")
fun updateNoteSettings(
    @RequestBody request: StorageSettingsUpdateRequest,
): StorageSettingsPayload {
    return noteStoreService().updateSettings(request)
}

@GetMapping("/api/notes/{source}/health")
fun readNoteHealth(
    @PathVariable source: String,
): DataSourceHealthPayload {
    val store = resolveStoreOrNull(source)
        ?: throw NoSuchElementException("unknown source")

    return DataSourceHealthPayload(
        source = source,
        available = store.isReady(),
        message = store.healthMessage(),
    )
}

@GetMapping("/api/notes/{source}")
fun listNotes(
    @PathVariable source: String,
): List<NotePayload> {
    val store = resolveStoreOrNull(source)
        ?: throw NoSuchElementException("unknown source")
    if (!store.isReady()) {
        throw ServiceUnavailableHttpException("source is not ready")
    }

    return store.listNotes()
}

@PutMapping("/api/notes/{source}/{id}")
fun upsertNote(
    @PathVariable source: String,
    @PathVariable id: String,
    @RequestBody request: NoteUpsertRequest,
): NotePayload {
    val store = resolveStoreOrNull(source)
        ?: throw NoSuchElementException("unknown source")
    if (!store.isReady()) {
        throw ServiceUnavailableHttpException("source is not ready")
    }

    return store.upsertNote(
        NotePayload(
            id = if (request.id.isBlank()) id else request.id,
            path = request.path,
            title = request.title,
            markdown = request.markdown,
            pinned = request.pinned,
            version = request.version,
        ),
    )
}

@DeleteMapping("/api/notes/{source}/{id}")
fun deleteNote(
    @PathVariable source: String,
    @PathVariable id: String,
) {
    val store = resolveStoreOrNull(source)
        ?: throw NoSuchElementException("unknown source")

    store.deleteNote(id)
}

private fun noteStoreService(): NoteStoreService {
    return KoinPlatform.getKoin().get()
}

private fun resolveStoreOrNull(source: String): JdbcNoteStore? {
    return noteStoreService().resolve(source)
}
