package site.addzero.vibepocket.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import io.ktor.server.plugins.statuspages.*
import site.addzero.starter.statuspages.ErrorResponse
import kotlin.test.*

/**
 * 音乐搜索路由测试
 *
 * ⚠️ musicRoutes 内部直接 new MusicSearchClient() 调用外部 API，
 * 无法 mock，所以实际发请求的测试标记 @Ignore。
 * 仅参数校验类测试可自动运行。
 */
class MusicRoutesTest {

    private fun ApplicationTestBuilder.configureTestApp() {
        application {
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
            routing { musicRoutes() }
        }
    }

    // ---------- 参数校验（不调外部 API） ----------

    @Test
    fun `GET search - missing query returns 400`() = testApplication {
        configureTestApp()
        val resp = client.get("/api/music/search")
        assertEquals(HttpStatusCode.BadRequest, resp.status)
        val body = Json.decodeFromString<ErrorResponse>(resp.bodyAsText())
        assertEquals(400, body.code)
        assertTrue(body.message.contains("query", ignoreCase = true))
    }

    @Test
    fun `GET search - blank query returns 400`() = testApplication {
        configureTestApp()
        val resp = client.get("/api/music/search?query=")
        assertEquals(HttpStatusCode.BadRequest, resp.status)
    }

    @Test
    fun `POST search - malformed JSON returns 400`() = testApplication {
        configureTestApp()
        val resp = client.post("/api/music/search") {
            contentType(ContentType.Application.Json)
            setBody("""{"bad""")
        }
        assertEquals(HttpStatusCode.BadRequest, resp.status)
    }

    // ---------- 调外部 API 的测试（默认 @Ignore） ----------

    @Ignore("调用外部音乐搜索 API，手动运行")
    @Test
    fun `GET search - real query`() = testApplication {
        configureTestApp()
        val resp = client.get("/api/music/search?query=周杰伦")
        assertEquals(HttpStatusCode.OK, resp.status)
        assertTrue(resp.bodyAsText().isNotBlank())
    }

    @Ignore("调用外部音乐搜索 API，手动运行")
    @Test
    fun `POST search - real query`() = testApplication {
        configureTestApp()
        val resp = client.post("/api/music/search") {
            contentType(ContentType.Application.Json)
            setBody("""{"query":"晴天","limit":5}""")
        }
        assertEquals(HttpStatusCode.OK, resp.status)
        assertTrue(resp.bodyAsText().isNotBlank())
    }
}
