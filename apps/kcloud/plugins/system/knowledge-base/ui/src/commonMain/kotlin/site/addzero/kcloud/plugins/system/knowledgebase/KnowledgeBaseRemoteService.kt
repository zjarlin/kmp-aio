package site.addzero.kcloud.plugins.system.knowledgebase

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.knowledgebase.api.*

@Single
class KnowledgeBaseRemoteService {
    suspend fun listSpaces(): List<KnowledgeSpaceDto> {
        return KnowledgeBaseApiClient.knowledgeBaseApi.listKnowledgeSpaces()
    }

    suspend fun createSpace(
        name: String,
        description: String,
    ): KnowledgeSpaceDto {
        return KnowledgeBaseApiClient.knowledgeBaseApi.createKnowledgeSpace(
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
        return KnowledgeBaseApiClient.knowledgeBaseApi.updateKnowledgeSpace(
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
        KnowledgeBaseApiClient.knowledgeBaseApi.deleteKnowledgeSpace(spaceId)
    }

    suspend fun listDocuments(
        spaceId: Long,
    ): List<KnowledgeDocumentDto> {
        return KnowledgeBaseApiClient.knowledgeBaseApi.listKnowledgeDocuments(spaceId)
    }

    suspend fun createDocument(
        spaceId: Long,
        title: String,
        content: String,
    ): KnowledgeDocumentDto {
        return KnowledgeBaseApiClient.knowledgeBaseApi.createKnowledgeDocument(
            spaceId = spaceId,
            request = KnowledgeDocumentMutationRequest(title = title, content = content),
        )
    }

    suspend fun updateDocument(
        documentId: Long,
        title: String,
        content: String,
    ): KnowledgeDocumentDto {
        return KnowledgeBaseApiClient.knowledgeBaseApi.updateKnowledgeDocument(
            documentId = documentId,
            request = KnowledgeDocumentMutationRequest(title = title, content = content),
        )
    }

    suspend fun deleteDocument(
        documentId: Long,
    ) {
        KnowledgeBaseApiClient.knowledgeBaseApi.deleteKnowledgeDocument(documentId)
    }
}
