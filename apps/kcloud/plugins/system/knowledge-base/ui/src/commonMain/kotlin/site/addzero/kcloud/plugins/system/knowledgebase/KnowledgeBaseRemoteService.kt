package site.addzero.kcloud.plugins.system.knowledgebase

import org.koin.core.annotation.Single
import site.addzero.kcloud.system.api.*

@Single
class KnowledgeBaseRemoteService {
    suspend fun listSpaces(): List<KnowledgeSpaceDto> {
        return KCloudSystemApiClient.knowledgeBaseApi.listSpaces()
    }

    suspend fun createSpace(
        name: String,
        description: String,
    ): KnowledgeSpaceDto {
        return KCloudSystemApiClient.knowledgeBaseApi.createSpace(
            KnowledgeSpaceMutationRequest(
                name = name,
                description = description.ifBlank { null },
            ),
        )
    }

    suspend fun updateSpace(
        spaceId: Long,
        name: String,
        description: String,
    ): KnowledgeSpaceDto {
        return KCloudSystemApiClient.knowledgeBaseApi.updateSpace(
            spaceId = spaceId,
            request = KnowledgeSpaceMutationRequest(
                name = name,
                description = description.ifBlank { null },
            ),
        )
    }

    suspend fun deleteSpace(
        spaceId: Long,
    ) {
        KCloudSystemApiClient.knowledgeBaseApi.deleteSpace(spaceId)
    }

    suspend fun listDocuments(
        spaceId: Long,
    ): List<KnowledgeDocumentDto> {
        return KCloudSystemApiClient.knowledgeBaseApi.listDocuments(spaceId)
    }

    suspend fun createDocument(
        spaceId: Long,
        title: String,
        content: String,
    ): KnowledgeDocumentDto {
        return KCloudSystemApiClient.knowledgeBaseApi.createDocument(
            spaceId = spaceId,
            request = KnowledgeDocumentMutationRequest(title = title, content = content),
        )
    }

    suspend fun updateDocument(
        documentId: Long,
        title: String,
        content: String,
    ): KnowledgeDocumentDto {
        return KCloudSystemApiClient.knowledgeBaseApi.updateDocument(
            documentId = documentId,
            request = KnowledgeDocumentMutationRequest(title = title, content = content),
        )
    }

    suspend fun deleteDocument(
        documentId: Long,
    ) {
        KCloudSystemApiClient.knowledgeBaseApi.deleteDocument(documentId)
    }
}
