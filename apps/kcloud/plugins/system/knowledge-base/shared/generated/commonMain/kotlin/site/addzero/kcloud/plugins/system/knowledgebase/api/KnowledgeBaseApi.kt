package site.addzero.kcloud.plugins.system.knowledgebase.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceDto
import site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceMutationRequest
import site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentDto
import site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentMutationRequest
import site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDeleteResult

/**
 * 原始文件: site.addzero.kcloud.plugins.system.knowledgebase.routes.KnowledgeBase.kt
 * 基础路径: 
 */
interface KnowledgeBaseApi {

/**
 * listKnowledgeSpaces
 * HTTP方法: GET
 * 路径: /api/system/knowledge-base/spaces
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceDto>
 */
    @GET("/api/system/knowledge-base/spaces")    suspend fun listKnowledgeSpaces(): kotlin.collections.List<site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceDto>

/**
 * listKnowledgeDocuments
 * HTTP方法: GET
 * 路径: /api/system/knowledge-base/spaces/{spaceId}/documents
 * 参数:
 *   - spaceId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentDto>
 */
    @GET("/api/system/knowledge-base/spaces/{spaceId}/documents")    suspend fun listKnowledgeDocuments(
        @Path("spaceId") spaceId: kotlin.Long
    ): kotlin.collections.List<site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentDto>

/**
 * createKnowledgeSpace
 * HTTP方法: POST
 * 路径: /api/system/knowledge-base/spaces
 * 参数:
 *   - request: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceDto
 */
    @POST("/api/system/knowledge-base/spaces")    suspend fun createKnowledgeSpace(
        @Body request: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceMutationRequest
    ): site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceDto

/**
 * createKnowledgeDocument
 * HTTP方法: POST
 * 路径: /api/system/knowledge-base/spaces/{spaceId}/documents
 * 参数:
 *   - spaceId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentDto
 */
    @POST("/api/system/knowledge-base/spaces/{spaceId}/documents")    suspend fun createKnowledgeDocument(
        @Path("spaceId") spaceId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentMutationRequest
    ): site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentDto

/**
 * updateKnowledgeSpace
 * HTTP方法: PUT
 * 路径: /api/system/knowledge-base/spaces/{spaceId}
 * 参数:
 *   - spaceId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceDto
 */
    @PUT("/api/system/knowledge-base/spaces/{spaceId}")    suspend fun updateKnowledgeSpace(
        @Path("spaceId") spaceId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceMutationRequest
    ): site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeSpaceDto

/**
 * updateKnowledgeDocument
 * HTTP方法: PUT
 * 路径: /api/system/knowledge-base/documents/{documentId}
 * 参数:
 *   - documentId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentDto
 */
    @PUT("/api/system/knowledge-base/documents/{documentId}")    suspend fun updateKnowledgeDocument(
        @Path("documentId") documentId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentMutationRequest
    ): site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDocumentDto

/**
 * deleteKnowledgeSpace
 * HTTP方法: DELETE
 * 路径: /api/system/knowledge-base/spaces/{spaceId}
 * 参数:
 *   - spaceId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDeleteResult
 */
    @DELETE("/api/system/knowledge-base/spaces/{spaceId}")    suspend fun deleteKnowledgeSpace(
        @Path("spaceId") spaceId: kotlin.Long
    ): site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDeleteResult

/**
 * deleteKnowledgeDocument
 * HTTP方法: DELETE
 * 路径: /api/system/knowledge-base/documents/{documentId}
 * 参数:
 *   - documentId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDeleteResult
 */
    @DELETE("/api/system/knowledge-base/documents/{documentId}")    suspend fun deleteKnowledgeDocument(
        @Path("documentId") documentId: kotlin.Long
    ): site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeDeleteResult

}