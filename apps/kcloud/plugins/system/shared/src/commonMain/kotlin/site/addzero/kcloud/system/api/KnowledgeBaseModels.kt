package site.addzero.kcloud.system.api

import kotlinx.serialization.Serializable

@Serializable
data class KnowledgeSpaceDto(
    val id: Long,
    val spaceKey: String,
    val name: String,
    val description: String? = null,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class KnowledgeSpaceMutationRequest(
    val name: String,
    val description: String? = null,
)

@Serializable
data class KnowledgeDocumentDto(
    val id: Long,
    val documentKey: String,
    val spaceId: Long,
    val title: String,
    val content: String,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class KnowledgeDocumentMutationRequest(
    val title: String,
    val content: String,
)

@Serializable
data class KnowledgeDeleteResult(
    val ok: Boolean = true,
)
