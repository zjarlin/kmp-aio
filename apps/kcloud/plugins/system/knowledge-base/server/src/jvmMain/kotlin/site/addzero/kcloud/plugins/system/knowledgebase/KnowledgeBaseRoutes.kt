package site.addzero.kcloud.plugins.system.knowledgebase

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import site.addzero.kcloud.plugins.system.knowledgebase.routes.createKnowledgeDocument
import site.addzero.kcloud.plugins.system.knowledgebase.routes.createKnowledgeSpace
import site.addzero.kcloud.plugins.system.knowledgebase.routes.deleteKnowledgeDocument
import site.addzero.kcloud.plugins.system.knowledgebase.routes.deleteKnowledgeSpace
import site.addzero.kcloud.plugins.system.knowledgebase.routes.listKnowledgeDocuments
import site.addzero.kcloud.plugins.system.knowledgebase.routes.listKnowledgeSpaces
import site.addzero.kcloud.plugins.system.knowledgebase.routes.updateKnowledgeDocument
import site.addzero.kcloud.plugins.system.knowledgebase.routes.updateKnowledgeSpace
import site.addzero.springktor.runtime.requirePathVariable
import site.addzero.springktor.runtime.requireRequestBody

/**
 * 统一挂载知识库后端路由。
 */
fun Route.knowledgeBaseRoutes() {
    get("/api/system/knowledge-base/spaces") {
        call.respond(listKnowledgeSpaces())
    }
    post("/api/system/knowledge-base/spaces") {
        call.respond(createKnowledgeSpace(call.requireRequestBody()))
    }
    put("/api/system/knowledge-base/spaces/{spaceId}") {
        call.respond(
            updateKnowledgeSpace(
                spaceId = call.requirePathVariable("spaceId"),
                request = call.requireRequestBody(),
            ),
        )
    }
    delete("/api/system/knowledge-base/spaces/{spaceId}") {
        call.respond(deleteKnowledgeSpace(call.requirePathVariable("spaceId")))
    }
    get("/api/system/knowledge-base/spaces/{spaceId}/documents") {
        call.respond(listKnowledgeDocuments(call.requirePathVariable("spaceId")))
    }
    post("/api/system/knowledge-base/spaces/{spaceId}/documents") {
        call.respond(
            createKnowledgeDocument(
                spaceId = call.requirePathVariable("spaceId"),
                request = call.requireRequestBody(),
            ),
        )
    }
    put("/api/system/knowledge-base/documents/{documentId}") {
        call.respond(
            updateKnowledgeDocument(
                documentId = call.requirePathVariable("documentId"),
                request = call.requireRequestBody(),
            ),
        )
    }
    delete("/api/system/knowledge-base/documents/{documentId}") {
        call.respond(deleteKnowledgeDocument(call.requirePathVariable("documentId")))
    }
}
