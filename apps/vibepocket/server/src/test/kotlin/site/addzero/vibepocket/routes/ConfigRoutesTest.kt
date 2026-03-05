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
import javax.sql.DataSource
import kotlin.test.*

class ConfigRoutesTest {

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
            routing { configRoutes() }
        }
    }

    @Test
    fun `GET config - key not found returns null value`() = testApplication {
        configureTestApp()
        val resp = client.get("/api/config/nonexistent")
        assertEquals(HttpStatusCode.OK, resp.status)
        val body = Json.decodeFromString<ConfigResponse>(resp.bodyAsText())
        assertEquals("nonexistent", body.key)
        assertNull(body.value)
    }

    @Test
    fun `PUT config then GET returns saved value`() = testApplication {
        configureTestApp()
        // write
        val putResp = client.put("/api/config") {
            contentType(ContentType.Application.Json)
            setBody("""{"key":"suno.api.token","value":"test-token-123","description":"测试token"}""")
        }
        assertEquals(HttpStatusCode.OK, putResp.status)

        // read back
        val getResp = client.get("/api/config/suno.api.token")
        assertEquals(HttpStatusCode.OK, getResp.status)
        val body = Json.decodeFromString<ConfigResponse>(getResp.bodyAsText())
        assertEquals("suno.api.token", body.key)
        assertEquals("test-token-123", body.value)
    }

    @Test
    fun `PUT config upsert overwrites existing value`() = testApplication {
        configureTestApp()
        // first write
        client.put("/api/config") {
            contentType(ContentType.Application.Json)
            setBody("""{"key":"app.theme","value":"dark"}""")
        }
        // overwrite
        client.put("/api/config") {
            contentType(ContentType.Application.Json)
            setBody("""{"key":"app.theme","value":"light"}""")
        }
        // verify
        val resp = client.get("/api/config/app.theme")
        val body = Json.decodeFromString<ConfigResponse>(resp.bodyAsText())
        assertEquals("light", body.value)
    }

    @Test
    fun `PUT config with malformed JSON returns 400`() = testApplication {
        configureTestApp()
        val resp = client.put("/api/config") {
            contentType(ContentType.Application.Json)
            setBody("""{"bad json""")
        }
        assertEquals(HttpStatusCode.BadRequest, resp.status)
    }

    @Test
    fun `multiple configs are independent`() = testApplication {
        configureTestApp()
        client.put("/api/config") {
            contentType(ContentType.Application.Json)
            setBody("""{"key":"a","value":"1"}""")
        }
        client.put("/api/config") {
            contentType(ContentType.Application.Json)
            setBody("""{"key":"b","value":"2"}""")
        }

        val a = Json.decodeFromString<ConfigResponse>(client.get("/api/config/a").bodyAsText())
        val b = Json.decodeFromString<ConfigResponse>(client.get("/api/config/b").bodyAsText())
        assertEquals("1", a.value)
        assertEquals("2", b.value)
    }
}
