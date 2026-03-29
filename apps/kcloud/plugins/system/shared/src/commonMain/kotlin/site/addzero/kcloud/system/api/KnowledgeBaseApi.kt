package site.addzero.kcloud.system.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path

interface KnowledgeBaseApi {
    @GET("api/system/knowledge-base/spaces")
    suspend fun listSpaces(): List<KnowledgeSpaceDto>

    @Headers("Content-Type: application/json")
    @POST("api/system/knowledge-base/spaces")
    suspend fun createSpace(@Body request: KnowledgeSpaceMutationRequest): KnowledgeSpaceDto

    @Headers("Content-Type: application/json")
    @PUT("api/system/knowledge-base/spaces/{spaceId}")
    suspend fun updateSpace(
        @Path("spaceId") spaceId: Long,
        @Body request: KnowledgeSpaceMutationRequest,
    ): KnowledgeSpaceDto

    @DELETE("api/system/knowledge-base/spaces/{spaceId}")
    suspend fun deleteSpace(@Path("spaceId") spaceId: Long): KnowledgeDeleteResult

    @GET("api/system/knowledge-base/spaces/{spaceId}/documents")
    suspend fun listDocuments(@Path("spaceId") spaceId: Long): List<KnowledgeDocumentDto>

    @Headers("Content-Type: application/json")
    @POST("api/system/knowledge-base/spaces/{spaceId}/documents")
    suspend fun createDocument(
        @Path("spaceId") spaceId: Long,
        @Body request: KnowledgeDocumentMutationRequest,
    ): KnowledgeDocumentDto

    @Headers("Content-Type: application/json")
    @PUT("api/system/knowledge-base/documents/{documentId}")
    suspend fun updateDocument(
        @Path("documentId") documentId: Long,
        @Body request: KnowledgeDocumentMutationRequest,
    ): KnowledgeDocumentDto

    @DELETE("api/system/knowledge-base/documents/{documentId}")
    suspend fun deleteDocument(@Path("documentId") documentId: Long): KnowledgeDeleteResult
}
