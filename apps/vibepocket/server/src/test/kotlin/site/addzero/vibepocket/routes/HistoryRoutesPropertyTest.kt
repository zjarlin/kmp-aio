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
 * Property-based tests for History API round-trip.
 *
 * // Feature: suno-client-completion, Property 7: 历史 API round-trip
 * **Validates: Requirements 3.6, 3.7**
 */
class HistoryRoutesPropertyTest {

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * Generates finite Double values only (no NaN, no Infinity).
     */
    private val arbFiniteDuration: Arb<Double> = arbitrary {
        var value = Arb.double(min = 0.0, max = 86400.0).bind()
        while (!value.isFinite()) {
            value = Arb.double(min = 0.0, max = 86400.0).bind()
        }
        value
    }

    /**
     * Generator for random [HistoryTrackDto] instances.
     */
    private val arbHistoryTrack: Arb<HistoryTrackDto> = arbitrary {
        HistoryTrackDto(
            id = Arb.string(minSize = 1, maxSize = 50).orNull(nullProbability = 0.3).bind(),
            audioUrl = Arb.string(minSize = 1, maxSize = 200).orNull(nullProbability = 0.3).bind(),
            title = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.3).bind(),
            tags = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.3).bind(),
            imageUrl = Arb.string(minSize = 1, maxSize = 200).orNull(nullProbability = 0.3).bind(),
            duration = arbFiniteDuration.orNull(nullProbability = 0.3).bind(),
        )
    }

    /**
     * Generator for random [HistorySaveRequest] instances.
     * taskId is unique per iteration (UUID) to avoid UNIQUE constraint conflicts.
     */
    private val arbHistorySaveRequest: Arb<HistorySaveRequest> = arbitrary {
        HistorySaveRequest(
            taskId = Arb.uuid().bind().toString(),
            type = Arb.element("generate", "extend", "cover", "vocal_removal").bind(),
            status = Arb.element("SUCCESS", "FAILED", "PENDING", "PROCESSING").bind(),
            tracks = Arb.list(arbHistoryTrack, range = 0..5).bind(),
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
            install(ContentNegotiation)
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
            routing { historyRoutes() }
        }
    }

    /**
     * Property 7: 历史 API round-trip
     *
     * For any valid HistorySaveRequest, POST /api/suno/history should persist the entry,
     * and GET /api/suno/history should contain that entry with matching taskId, type,
     * status, and tracks data.
     *
     * // Feature: suno-client-completion, Property 7: 历史 API round-trip
     * **Validates: Requirements 3.6, 3.7**
     */
    @Test
    fun historyApiRoundTrip() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbHistorySaveRequest) { req ->
            val ds = createTestDataSource()
            initDatabase(ds)
            val sqlClient = createTestSqlClient(ds)

            testApplication {
                configureTestApp(sqlClient)

                // 1. POST — save history
                val postResp = client.post("/api/suno/history") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(HistorySaveRequest.serializer(), req))
                }
                assertEquals(HttpStatusCode.OK, postResp.status,
                    "POST /api/suno/history should return 200 for taskId=${req.taskId}")

                val postBody = json.decodeFromString(HistoryResponse.serializer(), postResp.bodyAsText())
                assertEquals(req.taskId, postBody.taskId)
                assertEquals(req.type, postBody.type)
                assertEquals(req.status, postBody.status)
                assertEquals(req.tracks.size, postBody.tracks.size)
                // Verify each track's data matches
                req.tracks.forEachIndexed { i, expected ->
                    val actual = postBody.tracks[i]
                    assertEquals(expected.id, actual.id)
                    assertEquals(expected.audioUrl, actual.audioUrl)
                    assertEquals(expected.title, actual.title)
                    assertEquals(expected.tags, actual.tags)
                    assertEquals(expected.imageUrl, actual.imageUrl)
                    assertEquals(expected.duration, actual.duration)
                }

                // 2. GET — verify it's in the list
                val getResp = client.get("/api/suno/history")
                assertEquals(HttpStatusCode.OK, getResp.status)
                val list = json.decodeFromString<List<HistoryResponse>>(getResp.bodyAsText())
                val found = list.find { it.taskId == req.taskId }
                assertTrue(found != null,
                    "GET /api/suno/history should contain taskId=${req.taskId} after POST")
                assertEquals(req.type, found.type)
                assertEquals(req.status, found.status)
                assertEquals(req.tracks.size, found.tracks.size)
            }
        }
    }
}
