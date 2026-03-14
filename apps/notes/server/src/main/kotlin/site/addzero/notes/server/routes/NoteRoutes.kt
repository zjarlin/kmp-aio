package site.addzero.notes.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.*
import site.addzero.notes.server.model.*
import site.addzero.notes.server.store.NoteStoreService

@Single
@RestController
@RequestMapping("/api/notes")
class NoteRoutes(
    private val noteStoreService: NoteStoreService,
) {
    @GetMapping("/settings")
    fun readSettings(): StorageSettingsPayload {
        return noteStoreService.readSettings()
    }

    @PutMapping("/settings")
    fun updateSettings(
        @RequestBody request: StorageSettingsUpdateRequest,
    ): StorageSettingsPayload {
        return noteStoreService.updateSettings(request)
    }

    @GetMapping("/{source}/health")
    fun readHealth(
        call: ApplicationCall,
        @PathVariable source: String,
    ): DataSourceHealthPayload {
        val store = noteStoreService.resolve(source)
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

    @GetMapping("/{source}")
    fun listNotes(
        call: ApplicationCall,
        @PathVariable source: String,
    ): List<NotePayload> {
        val store = noteStoreService.resolve(source)
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

    @PutMapping("/{source}/{id}")
    suspend fun upsertNote(
        call: ApplicationCall,
        @PathVariable source: String,
        @PathVariable id: String,
        @RequestBody request: NoteUpsertRequest,
    ) {
        val store = noteStoreService.resolve(source)
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

    @DeleteMapping("/{source}/{id}")
    suspend fun deleteNote(
        call: ApplicationCall,
        @PathVariable source: String,
        @PathVariable id: String,
    ) {
        val store = noteStoreService.resolve(source)
        if (store == null) {
            call.respond(HttpStatusCode.NotFound)
            return
        }

        store.deleteNote(id)
    }
}
