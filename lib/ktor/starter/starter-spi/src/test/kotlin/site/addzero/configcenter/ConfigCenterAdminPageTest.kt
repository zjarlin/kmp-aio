package site.addzero.configcenter

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ConfigCenterAdminPageTest {
    @Test
    fun `embedded admin page serves html and value only CRUD api`() = testApplication {
        val root = Files.createTempDirectory("config-center-admin-test")
        val dbPath = root.resolve("config-center.sqlite")
        val service = JdbcConfigCenterValueService(
            ConfigCenterJdbcSettings(
                url = "jdbc:sqlite:${dbPath.toAbsolutePath()}",
            ),
        )

        application {
            installConfigCenterAdmin(
                service = service,
                adminSettings = ConfigCenterAdminSettings(
                    path = "/config-center",
                    title = "Embedded Config Center",
                ),
            )
        }

        val pageResponse = client.get("/config-center")
        assertEquals(HttpStatusCode.OK, pageResponse.status)
        assertContains(pageResponse.bodyAsText(), "Embedded Config Center")
        assertContains(pageResponse.bodyAsText(), "配置中心管理台")
        assertContains(pageResponse.bodyAsText(), "namespace + active + path")
        assertContains(pageResponse.bodyAsText(), "删除当前 namespace")

        val putResponse = client.put("/config-center/api/value") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "namespace": "demo-app",
                  "active": "dev",
                  "path": "server.port",
                  "value": "19090"
                }
                """.trimIndent(),
            )
        }
        assertEquals(HttpStatusCode.OK, putResponse.status)
        assertContains(putResponse.bodyAsText(), "\"path\":\"server.port\"")

        val namespacesResponse = client.get("/config-center/api/namespaces")
        assertEquals(HttpStatusCode.OK, namespacesResponse.status)
        assertContains(namespacesResponse.bodyAsText(), "\"namespace\":\"demo-app\"")
        assertContains(namespacesResponse.bodyAsText(), "\"entryCount\":1")

        val readResponse = client.get("/config-center/api/value?namespace=demo-app&active=dev&path=server.port")
        assertEquals(HttpStatusCode.OK, readResponse.status)
        assertContains(readResponse.bodyAsText(), "\"value\":\"19090\"")

        val listResponse = client.get("/config-center/api/values?namespace=demo-app&active=dev")
        assertEquals(HttpStatusCode.OK, listResponse.status)
        assertContains(listResponse.bodyAsText(), "\"server.port\"")

        val deleteResponse = client.delete("/config-center/api/value?namespace=demo-app&active=dev&path=server.port")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)
        assertContains(deleteResponse.bodyAsText(), "\"deleted\":true")

        client.put("/config-center/api/value") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "namespace": "demo-app",
                  "active": "prod",
                  "path": "server.host",
                  "value": "127.0.0.1"
                }
                """.trimIndent(),
            )
        }

        val deleteNamespaceResponse = client.delete("/config-center/api/namespace?namespace=demo-app")
        assertEquals(HttpStatusCode.OK, deleteNamespaceResponse.status)
        assertContains(deleteNamespaceResponse.bodyAsText(), "\"deleted\":true")
    }
}
