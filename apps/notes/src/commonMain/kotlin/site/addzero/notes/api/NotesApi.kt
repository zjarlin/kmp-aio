package site.addzero.notes.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path

interface NotesApi {
    @GET("api/notes/{source}/health")
    suspend fun health(
        @Path("source") source: String
    ): DataSourceHealthPayload

    @GET("api/notes/{source}")
    suspend fun listNotes(
        @Path("source") source: String
    ): List<NotePayload>

    @PUT("api/notes/{source}/{id}")
    suspend fun upsertNote(
        @Path("source") source: String,
        @Path("id") id: String,
        @Body request: NoteUpsertRequest
    ): NotePayload

    @DELETE("api/notes/{source}/{id}")
    suspend fun deleteNote(
        @Path("source") source: String,
        @Path("id") id: String
    )
}
