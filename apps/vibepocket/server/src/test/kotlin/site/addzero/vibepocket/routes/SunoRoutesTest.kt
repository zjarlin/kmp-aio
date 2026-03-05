package site.addzero.vibepocket.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.sqlite.SQLiteDataSource
import site.addzero.vibepocket.jimmer.di.initDatabase
import io.ktor.server.plugins.statuspages.*
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.dto.*
import javax.sql.DataSource
import kotlin.test.*

/**
 * Suno 路由测试
 *
 * ⚠️ 所有测试标记 @Ignore — 因为会调用 Suno 付费第三方 API。
 * 需要手动运行时去掉 @Ignore，并确保 DB 中已配置 suno.api.token。
 */
class SunoRoutesTest {

    private lateinit var dataSource: DataSource
    private lateinit var sqlClient: KSqlClient

    @BeforeTest
    fun setup() {
        dataSource = SQLiteDataSource().apply { url = "jdbc:sqlite::memory:" }
        initDatabase(dataSource)
        sqlClient = newKSqlClient {
            setDialect(SQLiteDialect())
            setConnectionManager {
                dataSource.connection.use { con ->
                    proceed(con)
                }
            }
        }
    }

    private fun ApplicationTestBuilder.configureTestApp() {
        application {
            install(Koin) {
                modules(module { single<KSqlClient> { sqlClient } })
            }
            install(ContentNegotiation) {
                json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
            }
            install(StatusPages) {
                exception<IllegalArgumentException> { call, cause ->
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, cause.message ?: "Bad Request"))
                }
                exception<kotlinx.serialization.SerializationException> { call, cause ->
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "Malformed JSON: ${cause.message}"))
                }
                exception<Throwable> { call, cause ->
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(500, cause.message ?: "Internal Server Error"))
                }
            }
            routing { sunoRoutes() }
        }
    }

    // ---------- 不调外部 API 的测试（可以自动跑） ----------

    @Test
    fun `GET tasks - empty list`() = testApplication {
        configureTestApp()
        val resp = client.get("/api/suno/tasks")
        assertEquals(HttpStatusCode.OK, resp.status)
        val body = Json.decodeFromString<TaskListResult>(resp.bodyAsText())
        assertEquals(200, body.code)
        assertTrue(body.data.isEmpty())
    }

    @Test
    fun `GET task by id - not found`() = testApplication {
        configureTestApp()
        val resp = client.get("/api/suno/tasks/nonexistent")
        assertEquals(HttpStatusCode.NotFound, resp.status)
        val body = Json.decodeFromString<ErrorResponse>(resp.bodyAsText())
        assertEquals(404, body.code)
    }

    @Test
    fun `POST generate - no token configured returns 400`() = testApplication {
        configureTestApp()
        val resp = client.post("/api/suno/generate") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"a happy song","title":"Test"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, resp.status)
        val body = Json.decodeFromString<ErrorResponse>(resp.bodyAsText())
        assertTrue(body.message.contains("Token"))
    }

    // ---------- 调外部 API 的测试（花钱，默认 @Ignore） ----------

    @Ignore("调用 Suno 付费 API，手动运行")
    @Test
    fun `POST generate - with real token`() = testApplication {
        configureTestApp()
        // 先写入 token
        sqlClient.setConfig("suno.api.token", "YOUR_REAL_TOKEN_HERE")
        val resp = client.post("/api/suno/generate") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"a chill lofi beat","title":"Test Song","tags":"lofi,chill","mv":"chirp-v5"}""")
        }
        assertEquals(HttpStatusCode.OK, resp.status)
        val body = Json.decodeFromString<TaskResult>(resp.bodyAsText())
        assertEquals(200, body.code)
        assertNotNull(body.data)
    }
}
