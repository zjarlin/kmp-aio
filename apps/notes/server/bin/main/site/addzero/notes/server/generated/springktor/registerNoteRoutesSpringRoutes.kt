package site.addzero.notes.server.generated.springktor

import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import site.addzero.springktor.runtime.*

fun Route.registerNoteRoutesSpringRoutes() {
    get("/api/notes/settings") {
        val _springResult = site.addzero.notes.server.routes.readNoteSettings()
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.notes.server.model.StorageSettingsPayload>(),
        )
    }
    
    put("/api/notes/settings") {
        val _springArg0 = call.requireRequestBody<site.addzero.notes.server.model.StorageSettingsUpdateRequest>()
        val _springResult = site.addzero.notes.server.routes.updateNoteSettings(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.notes.server.model.StorageSettingsPayload>(),
        )
    }
    
    get("/api/notes/{source}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("source")
        val _springResult = site.addzero.notes.server.routes.listNotes(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<kotlin.collections.List<site.addzero.notes.server.model.NotePayload>>(),
        )
    }
    
    get("/api/notes/{source}/health") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("source")
        val _springResult = site.addzero.notes.server.routes.readNoteHealth(_springArg0)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.notes.server.model.DataSourceHealthPayload>(),
        )
    }
    
    delete("/api/notes/{source}/{id}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("source")
        val _springArg1 = call.requirePathVariable<kotlin.String>("id")
        site.addzero.notes.server.routes.deleteNote(_springArg0, _springArg1)
        call.completeSpringRoute(returnsUnit = true)
    }
    
    put("/api/notes/{source}/{id}") {
        val _springArg0 = call.requirePathVariable<kotlin.String>("source")
        val _springArg1 = call.requirePathVariable<kotlin.String>("id")
        val _springArg2 = call.requireRequestBody<site.addzero.notes.server.model.NoteUpsertRequest>()
        val _springResult = site.addzero.notes.server.routes.upsertNote(_springArg0, _springArg1, _springArg2)
        call.completeSpringRoute(
            result = _springResult,
            resultType = typeInfo<site.addzero.notes.server.model.NotePayload>(),
        )
    }
}