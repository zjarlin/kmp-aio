package site.addzero.vibepocket.routes

import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.sqlite.SQLiteDataSource
import io.ktor.server.plugins.statuspages.*
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.jimmer.di.initDatabase
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based tests for Persona API round-trip.
 *
 * // Feature: suno-client-completion, Property 11: Persona API round-trip
 * **Validates: Requirements 10.5**
 */
class PersonaRoutesPropertyTest {

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * Generator for random [PersonaSaveRequest] instances.
     * personaId is unique per iteration (UUID) to avoid UNIQUE constraint conflicts.
     */
    private val arbPersonaSaveRequest: Arb<PersonaSaveRequest> = arbitrary {
        PersonaSaveRequest(
            personaId = Arb.uuid().bind().toString(),
            name = Arb.string(minSize = 1, maxSize = 100).bind(),
            description = Arb.string(minSize = 1, maxSize = 500).bind(),
        )
    }

    private fun createTestDataSource(): DataSource = SQLiteDataSource().apply {
        url = "jdbc:sqlite::memory:"
    }

    private fun createTestSqlClient(dataSource: DataSource): KSqlClient = newKSqlClient {
        setDialect(SQLiteDialect())
        setConnectionManager {
            dataSource.connection.use { con ->
                proceed(con)
            }
        }
    }

    private fun ApplicationTestBuilder.configureTestApp(sqlClient: KSqlClient) {
        application {
            install(ContentNegotiation) {
                json(json)
            }
            install(Koin) {
                modules(module { single<KSqlClient> { sqlClient } })
            }
            install(StatusPages) {
                exception<IllegalArgumentException> { call, cause ->
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, cause.message ?: "Bad Request"))
                }
                exception<Throwable> { call, cause ->
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(500, cause.message ?: "Internal Server Error"))
                }
            }
            routing { personaRoutes() }
        }
    }

    /**
     * Property 11: Persona API round-trip
     *
     * For any valid PersonaSaveRequest, POST /api/personas should persist the entry,
     * and GET /api/personas should contain that entry with matching personaId, name,
     * and description data.
     *
     * // Feature: suno-client-completion, Property 11: Persona API round-trip
     * **Validates: Requirements 10.5**
     */
    @Test
    fun personaApiRoundTrip() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbPersonaSaveRequest) { req ->
            val ds = createTestDataSource()
            initDatabase(ds)
            val sqlClient = createTestSqlClient(ds)

            testApplication {
                configureTestApp(sqlClient)

                // 1. POST — save persona
                val postResp = client.post("/api/personas") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(PersonaSaveRequest.serializer(), req))
                }
                assertEquals(HttpStatusCode.OK, postResp.status,
                    "POST /api/personas should return 200 for personaId=${req.personaId}")

                val postBody = json.decodeFromString(PersonaResponse.serializer(), postResp.bodyAsText())
                assertEquals(req.personaId, postBody.personaId)
                assertEquals(req.name, postBody.name)
                assertEquals(req.description, postBody.description)

                // 2. GET — verify it's in the list
                val getResp = client.get("/api/personas")
                assertEquals(HttpStatusCode.OK, getResp.status)
                val list = json.decodeFromString<List<PersonaResponse>>(getResp.bodyAsText())
                val found = list.find { it.personaId == req.personaId }
                assertTrue(found != null,
                    "GET /api/personas should contain personaId=${req.personaId} after POST")
                assertEquals(req.name, found.name)
                assertEquals(req.description, found.description)
            }
        }
    }
}
