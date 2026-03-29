package site.addzero.kcloud.plugins.system.knowledgebase.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.*
import site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseService
import site.addzero.kcloud.plugins.system.knowledgebase.api.*

/**
 * 知识库服务端路由定义，同时作为客户端 API 生成源。
 */
/**
 * 列出知识空间。
 */
@GetMapping("/api/system/knowledge-base/spaces")
fun listKnowledgeSpaces(): List<KnowledgeSpaceDto> {
    return service().listSpaces()
}

/**
 * 新建知识空间。
 */
@PostMapping("/api/system/knowledge-base/spaces")
fun createKnowledgeSpace(
    @RequestBody request: KnowledgeSpaceMutationRequest,
): KnowledgeSpaceDto {
    return service().createSpace(
        name = request.name,
        description = request.description,
    )
}

/**
 * 更新知识空间。
 */
@PutMapping("/api/system/knowledge-base/spaces/{spaceId}")
fun updateKnowledgeSpace(
    @PathVariable("spaceId") spaceId: Long,
    @RequestBody request: KnowledgeSpaceMutationRequest,
): KnowledgeSpaceDto {
    return service().updateSpace(
        spaceId = spaceId,
        name = request.name,
        description = request.description,
    )
}

/**
 * 删除知识空间。
 */
@DeleteMapping("/api/system/knowledge-base/spaces/{spaceId}")
fun deleteKnowledgeSpace(
    @PathVariable("spaceId") spaceId: Long,
): KnowledgeDeleteResult {
    return service().deleteSpace(spaceId)
}

/**
 * 列出指定空间文档。
 */
@GetMapping("/api/system/knowledge-base/spaces/{spaceId}/documents")
fun listKnowledgeDocuments(
    @PathVariable("spaceId") spaceId: Long,
): List<KnowledgeDocumentDto> {
    return service().listDocuments(spaceId)
}

/**
 * 新建空间文档。
 */
@PostMapping("/api/system/knowledge-base/spaces/{spaceId}/documents")
fun createKnowledgeDocument(
    @PathVariable("spaceId") spaceId: Long,
    @RequestBody request: KnowledgeDocumentMutationRequest,
): KnowledgeDocumentDto {
    return service().createDocument(
        spaceId = spaceId,
        title = request.title,
        content = request.content,
    )
}

/**
 * 更新文档。
 */
@PutMapping("/api/system/knowledge-base/documents/{documentId}")
fun updateKnowledgeDocument(
    @PathVariable("documentId") documentId: Long,
    @RequestBody request: KnowledgeDocumentMutationRequest,
): KnowledgeDocumentDto {
    return service().updateDocument(
        documentId = documentId,
        title = request.title,
        content = request.content,
    )
}

/**
 * 删除文档。
 */
@DeleteMapping("/api/system/knowledge-base/documents/{documentId}")
fun deleteKnowledgeDocument(
    @PathVariable("documentId") documentId: Long,
): KnowledgeDeleteResult {
    return service().deleteDocument(documentId)
}

private fun service(): KnowledgeBaseService {
    return KoinPlatform.getKoin().get()
}
