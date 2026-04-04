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
    fun `embedded admin page serves html and CRUD api`() = testApplication {
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
        assertContains(pageResponse.bodyAsText(), "新增配置")
        assertContains(pageResponse.bodyAsText(), "<div class=\"tree\" id=\"values-tree\"></div>")
        assertContains(pageResponse.bodyAsText(), "function buildTree(items)")
        assertContains(pageResponse.bodyAsText(), "id=\"filter-prefix\"")
        assertContains(pageResponse.bodyAsText(), "id=\"key-suggestions\"")
        assertContains(pageResponse.bodyAsText(), "id=\"filter-namespace\"")
        assertContains(pageResponse.bodyAsText(), "新建命名空间")
        assertContains(pageResponse.bodyAsText(), "function refreshNamespaces(preferredNamespace = \"\")")
        assertContains(pageResponse.bodyAsText(), "function applyClientFilters(items)")
        assertContains(pageResponse.bodyAsText(), "function tokenizeQuery(value)")
        assertContains(pageResponse.bodyAsText(), "class=\"quick-chip active\"")
        assertContains(pageResponse.bodyAsText(), "<select id=\"edit-value-type\">")
        assertContains(pageResponse.bodyAsText(), "<option value=\"kotlin.Boolean\">kotlin.Boolean</option>")

        val createNamespaceResponse = client.put("/config-center/api/namespace") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "namespace": "demo-app"
                }
                """.trimIndent(),
            )
        }
        assertEquals(HttpStatusCode.OK, createNamespaceResponse.status)
        assertContains(createNamespaceResponse.bodyAsText(), "\"namespace\":\"demo-app\"")

        val namespacesResponse = client.get("/config-center/api/namespaces")
        assertEquals(HttpStatusCode.OK, namespacesResponse.status)
        assertContains(namespacesResponse.bodyAsText(), "\"namespace\":\"demo-app\"")

        val putResponse = client.put("/config-center/api/value") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "namespace": "demo-app",
                  "active": "dev",
                  "key": "server.port",
                  "value": "19090",
                  "comment": "from test"
                }
                """.trimIndent(),
            )
        }
        assertEquals(HttpStatusCode.OK, putResponse.status)
        assertContains(putResponse.bodyAsText(), "\"key\":\"server.port\"")

        val listResponse = client.get("/config-center/api/values?namespace=demo-app&active=dev")
        assertEquals(HttpStatusCode.OK, listResponse.status)
        assertContains(listResponse.bodyAsText(), "\"server.port\"")

        val deleteResponse = client.delete("/config-center/api/value?namespace=demo-app&active=dev&key=server.port")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)
        assertContains(deleteResponse.bodyAsText(), "\"deleted\":true")

        val deleteNamespaceResponse = client.delete("/config-center/api/namespace?namespace=demo-app")
        assertEquals(HttpStatusCode.OK, deleteNamespaceResponse.status)
        assertContains(deleteNamespaceResponse.bodyAsText(), "\"deleted\":true")
    }
}
