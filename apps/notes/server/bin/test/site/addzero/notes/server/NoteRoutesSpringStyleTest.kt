package site.addzero.notes.server

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import site.addzero.notes.server.model.DataSourceHealthPayload
import site.addzero.notes.server.model.NotePayload
import site.addzero.notes.server.model.StorageSettingsPayload
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteRoutesSpringStyleTest {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `spring style note routes serve note http flow`() {
        val sqliteFile = Files.createTempFile("notes-server-test", ".db")
        System.setProperty("NOTES_SERVER_SQLITE_PATH", sqliteFile.toString())
        System.setProperty("NOTES_SERVER_ACTIVE_SOURCE", "sqlite")

        try {
            testApplication {
                application {
                    module()
                }

                val settingsResponse = client.get("/api/notes/settings")
                assertEquals(HttpStatusCode.OK, settingsResponse.status)
                val settings = json.decodeFromString<StorageSettingsPayload>(settingsResponse.bodyAsText())
                assertEquals("sqlite", settings.activeSource)

                val healthResponse = client.get("/api/notes/sqlite/health")
                assertEquals(HttpStatusCode.OK, healthResponse.status)
                val healthBody = healthResponse.bodyAsText()
                val health = json.decodeFromString<DataSourceHealthPayload>(healthBody)
                assertTrue(health.available, healthBody)

                val upsertResponse = client.put("/api/notes/sqlite/note-1") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                            {
                              "id": "",
                              "path": "/notes/note-1.md",
                              "title": "First note",
                              "markdown": "# hello",
                              "pinned": true,
                              "version": 2
                            }
                        """.trimIndent(),
                    )
                }
                assertEquals(HttpStatusCode.OK, upsertResponse.status)
                val saved = json.decodeFromString<NotePayload>(upsertResponse.bodyAsText())
                assertEquals("note-1", saved.id)
                assertEquals("/notes/note-1.md", saved.path)

                val listResponse = client.get("/api/notes/sqlite")
                assertEquals(HttpStatusCode.OK, listResponse.status)
                val notes = json.decodeFromString(
                    ListSerializer(NotePayload.serializer()),
                    listResponse.bodyAsText(),
                )
                assertEquals(1, notes.size)
                assertEquals("note-1", notes.single().id)

                val deleteResponse = client.delete("/api/notes/sqlite/note-1")
                assertEquals(HttpStatusCode.OK, deleteResponse.status)

                val afterDeleteResponse = client.get("/api/notes/sqlite")
                assertEquals(HttpStatusCode.OK, afterDeleteResponse.status)
                val afterDeleteNotes = json.decodeFromString(
                    ListSerializer(NotePayload.serializer()),
                    afterDeleteResponse.bodyAsText(),
                )
                assertTrue(afterDeleteNotes.isEmpty(), afterDeleteResponse.bodyAsText())
            }
        } finally {
            System.clearProperty("NOTES_SERVER_SQLITE_PATH")
            System.clearProperty("NOTES_SERVER_ACTIVE_SOURCE")
            sqliteFile.deleteIfExists()
        }
    }
}
