package site.addzero.kcloud.plugins.system.knowledgebase

import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.addzero.kcloud.plugins.system.knowledgebase.routes.*
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
