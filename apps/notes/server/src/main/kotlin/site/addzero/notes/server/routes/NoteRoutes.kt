package site.addzero.notes.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import site.addzero.notes.server.model.DataSourceHealthPayload
import site.addzero.notes.server.model.NotePayload
import site.addzero.notes.server.model.NoteUpsertRequest
import site.addzero.notes.server.model.StorageSettingsUpdateRequest
import site.addzero.notes.server.store.NoteStoreRegistry

fun Route.noteRoutes(registry: NoteStoreRegistry) {
    route("/api/notes") {
        get("/settings") {
            call.respond(registry.readSettings())
        }

        put("/settings") {
            val request = call.receive<StorageSettingsUpdateRequest>()
            val updated = registry.updateSettings(request)
            call.respond(updated)
        }

        get("/{source}/health") {
            val source = call.parameters["source"].orEmpty()
            val store = registry.resolve(source)
            if (store == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    DataSourceHealthPayload(
                        source = source,
                        available = false,
                        message = "unknown source"
                    )
                )
                return@get
            }
            call.respond(
                DataSourceHealthPayload(
                    source = source,
                    available = store.isReady(),
                    message = store.healthMessage()
                )
            )
        }

        get("/{source}") {
            val source = call.parameters["source"].orEmpty()
            val store = registry.resolve(source)
            if (store == null) {
                call.respond(HttpStatusCode.NotFound, emptyList<NotePayload>())
                return@get
            }
            if (!store.isReady()) {
                call.respond(HttpStatusCode.ServiceUnavailable, emptyList<NotePayload>())
                return@get
            }
            call.respond(store.listNotes())
        }

        put("/{source}/{id}") {
            val source = call.parameters["source"].orEmpty()
            val id = call.parameters["id"].orEmpty()
            val store = registry.resolve(source)
            if (store == null) {
                call.respond(HttpStatusCode.NotFound)
                return@put
            }
            if (!store.isReady()) {
                call.respond(HttpStatusCode.ServiceUnavailable)
                return@put
            }
            val request = call.receive<NoteUpsertRequest>()
            val saved = store.upsertNote(
                NotePayload(
                    id = if (request.id.isBlank()) id else request.id,
                    path = request.path,
                    title = request.title,
                    markdown = request.markdown,
                    pinned = request.pinned,
                    version = request.version
                )
            )
            call.respond(saved)
        }

        delete("/{source}/{id}") {
            val source = call.parameters["source"].orEmpty()
            val id = call.parameters["id"].orEmpty()
            val store = registry.resolve(source)
            if (store == null) {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }
            store.deleteNote(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
