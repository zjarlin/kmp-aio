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
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.sqlite.SQLiteDataSource
import site.addzero.core.network.json.json
import io.ktor.server.plugins.statuspages.*
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.jimmer.di.initDatabase
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Property-based tests for Favorite API round-trip.
 *
 * // Feature: suno-client-completion, Property 5: 收藏 API round-trip
 * **Validates: Requirements 2.4, 2.5, 2.6**
 */
class FavoriteRoutesPropertyTest {

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
     * Generator for random [FavoriteRequest] instances.
     * trackId is unique per iteration (UUID-like) to avoid UNIQUE constraint conflicts.
     */
    private val arbFavoriteRequest: Arb<FavoriteRequest> = arbitrary {
        FavoriteRequest(
            trackId = Arb.uuid().bind().toString(),
            taskId = Arb.uuid().bind().toString(),
            audioUrl = Arb.string(minSize = 1, maxSize = 200).orNull(nullProbability = 0.3).bind(),
            title = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.3).bind(),
            tags = Arb.string(minSize = 1, maxSize = 100).orNull(nullProbability = 0.3).bind(),
            imageUrl = Arb.string(minSize = 1, maxSize = 200).orNull(nullProbability = 0.3).bind(),
            duration = arbFiniteDuration.orNull(nullProbability = 0.3).bind(),
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
        application.apply {
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
            routing { favoriteRoutes() }
        }
    }

    /**
     * Property 5: 收藏 API round-trip
     *
     * For any valid FavoriteRequest, POST /api/favorites should persist the entry,
     * GET /api/favorites should contain that entry,
     * DELETE /api/favorites/{trackId} should remove it,
     * and GET /api/favorites should no longer contain it.
     *
     * // Feature: suno-client-completion, Property 5: 收藏 API round-trip
     * **Validates: Requirements 2.4, 2.5, 2.6**
     */
    @Test
    fun favoriteApiRoundTrip() = runTest {
        checkAll(PropTestConfig(iterations = 100), arbFavoriteRequest) { req ->
            // Each iteration gets a fresh in-memory DB to avoid cross-iteration state
            val ds = createTestDataSource()
            initDatabase(ds)
            val sqlClient = createTestSqlClient(ds)

            testApplication {
                configureTestApp(sqlClient)

                // 1. POST — add favorite
                val postResp = client.post("/api/favorites") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(FavoriteRequest.serializer(), req))
                }
                assertEquals(HttpStatusCode.OK, postResp.status,
                    "POST /api/favorites should return 200 for trackId=${req.trackId}")

                val postBody = json.decodeFromString(FavoriteResponse.serializer(), postResp.bodyAsText())
                assertEquals(req.trackId, postBody.trackId)
                assertEquals(req.taskId, postBody.taskId)
                assertEquals(req.audioUrl, postBody.audioUrl)
                assertEquals(req.title, postBody.title)
                assertEquals(req.tags, postBody.tags)
                assertEquals(req.imageUrl, postBody.imageUrl)
                assertEquals(req.duration, postBody.duration)

                // 2. GET — verify it's in the list
                val getResp = client.get("/api/favorites")
                assertEquals(HttpStatusCode.OK, getResp.status)
                val list = json.decodeFromString<List<FavoriteResponse>>(getResp.bodyAsText())
                assertTrue(list.any { it.trackId == req.trackId },
                    "GET /api/favorites should contain trackId=${req.trackId} after POST")

                // 3. DELETE — remove favorite
                val delResp = client.delete("/api/favorites/${req.trackId}")
                assertEquals(HttpStatusCode.OK, delResp.status,
                    "DELETE /api/favorites/${req.trackId} should return 200")

                // 4. GET — verify it's removed
                val getAfterDel = client.get("/api/favorites")
                assertEquals(HttpStatusCode.OK, getAfterDel.status)
                val listAfterDel = json.decodeFromString<List<FavoriteResponse>>(getAfterDel.bodyAsText())
                assertFalse(listAfterDel.any { it.trackId == req.trackId },
                    "GET /api/favorites should NOT contain trackId=${req.trackId} after DELETE")
            }
        }
    }
}
