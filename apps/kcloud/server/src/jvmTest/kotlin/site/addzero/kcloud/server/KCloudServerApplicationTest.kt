package site.addzero.kcloud.server

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import site.addzero.kcloud.module

class KCloudServerApplicationTest {
    @Test
    fun `lists registered scenes`() = testApplication {
        application {
            module()
        }

        val response = client.get("/api/scenes")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(body.contains("\"sceneId\": \"workspace\""))
        assertTrue(body.contains("\"sceneId\": \"system\""))
    }

    @Test
    fun `serves scene summary routes`() = testApplication {
        application {
            module()
        }

        val response = client.get("/api/scenes/workspace/summary")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(body.contains("\"pageId\": \"workspace.overview\""))
    }

}
