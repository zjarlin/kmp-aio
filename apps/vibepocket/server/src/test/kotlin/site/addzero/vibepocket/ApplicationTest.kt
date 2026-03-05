package site.addzero.vibepocket

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import site.addzero.vibepocket.plugins.ioc.generated.iocModule
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            iocModule()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
