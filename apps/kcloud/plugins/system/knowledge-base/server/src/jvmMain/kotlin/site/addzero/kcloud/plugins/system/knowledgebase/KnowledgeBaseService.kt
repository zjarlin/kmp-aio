package site.addzero.kcloud.plugins.system.knowledgebase

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.knowledgebase.model.KnowledgeDocument
import site.addzero.kcloud.plugins.system.knowledgebase.model.KnowledgeSpace
import site.addzero.kcloud.plugins.system.knowledgebase.model.by
import site.addzero.kcloud.system.api.KnowledgeDeleteResult
import site.addzero.kcloud.system.api.KnowledgeDocumentDto
import site.addzero.kcloud.system.api.KnowledgeSpaceDto
import java.util.*

@Single
class KnowledgeBaseService(
    private val sqlClient: KSqlClient,
) {
    fun listSpaces(): List<KnowledgeSpaceDto> {
        return allSpaces()
            .sortedByDescending { it.updateTime ?: it.createTime }
            .map { it.toDto() }
    }

    fun createSpace(
        name: String,
        description: String?,
    ): KnowledgeSpaceDto {
        require(name.isNotBlank()) { "空间名称不能为空" }
        val saved = sqlClient.save(
            new(KnowledgeSpace::class).by {
                spaceKey = UUID.randomUUID().toString()
                this.name = name.trim()
                this.description = description?.trim()?.ifBlank { null }
            },
        ).modifiedEntity
        return saved.toDto()
    }

    fun updateSpace(
        spaceId: Long,
        name: String,
        description: String?,
    ): KnowledgeSpaceDto {
        require(name.isNotBlank()) { "空间名称不能为空" }
        val existing = spaceOrThrow(spaceId)
        val saved = sqlClient.save(
            new(KnowledgeSpace::class).by {
                id = existing.id
                spaceKey = existing.spaceKey
                this.name = name.trim()
                this.description = description?.trim()?.ifBlank { null }
                createTime = existing.createTime
            },
        ).modifiedEntity
        return saved.toDto()
    }

    fun deleteSpace(
        spaceId: Long,
    ): KnowledgeDeleteResult {
        spaceOrThrow(spaceId)
        listDocumentEntities(spaceId).forEach { document ->
            sqlClient.deleteById(KnowledgeDocument::class, document.id)
        }
        sqlClient.deleteById(KnowledgeSpace::class, spaceId)
        return KnowledgeDeleteResult(ok = true)
    }

    fun listDocuments(
        spaceId: Long,
    ): List<KnowledgeDocumentDto> {
        spaceOrThrow(spaceId)
        return listDocumentEntities(spaceId)
            .sortedByDescending { it.updateTime ?: it.createTime }
            .map { it.toDto() }
    }

    fun createDocument(
        spaceId: Long,
        title: String,
        content: String,
    ): KnowledgeDocumentDto {
        require(title.isNotBlank()) { "文档标题不能为空" }
        val space = spaceOrThrow(spaceId)
        val saved = sqlClient.save(
            new(KnowledgeDocument::class).by {
                documentKey = UUID.randomUUID().toString()
                this.space = spaceRef(space.id)
                this.title = title.trim()
                this.content = content
            },
        ).modifiedEntity
        touchSpace(space.id)
        return saved.toDto()
    }

    fun updateDocument(
        documentId: Long,
        title: String,
        content: String,
    ): KnowledgeDocumentDto {
        require(title.isNotBlank()) { "文档标题不能为空" }
        val existing = documentOrThrow(documentId)
        val saved = sqlClient.save(
            new(KnowledgeDocument::class).by {
                id = existing.id
                documentKey = existing.documentKey
                space = spaceRef(existing.space.id)
                this.title = title.trim()
                this.content = content
                createTime = existing.createTime
            },
        ).modifiedEntity
        touchSpace(existing.space.id)
        return saved.toDto()
    }

    fun deleteDocument(
        documentId: Long,
    ): KnowledgeDeleteResult {
        val existing = documentOrThrow(documentId)
        sqlClient.deleteById(KnowledgeDocument::class, documentId)
        touchSpace(existing.space.id)
        return KnowledgeDeleteResult(ok = true)
    }

    private fun allSpaces(): List<KnowledgeSpace> {
        return sqlClient.createQuery(KnowledgeSpace::class) {
            select(table)
        }.execute()
    }

    private fun listDocumentEntities(
        spaceId: Long,
    ): List<KnowledgeDocument> {
        return sqlClient.createQuery(KnowledgeDocument::class) {
            select(table)
        }.execute().filter { document ->
            document.space.id == spaceId
        }
    }

    private fun spaceOrThrow(
        spaceId: Long,
    ): KnowledgeSpace {
        return sqlClient.findById(KnowledgeSpace::class, spaceId)
            ?: throw NoSuchElementException("知识空间不存在: $spaceId")
    }

    private fun documentOrThrow(
        documentId: Long,
    ): KnowledgeDocument {
        return sqlClient.findById(KnowledgeDocument::class, documentId)
            ?: throw NoSuchElementException("知识文档不存在: $documentId")
    }

    private fun spaceRef(
        spaceId: Long,
    ): KnowledgeSpace {
        return new(KnowledgeSpace::class).by {
            id = spaceId
        }
    }

    private fun touchSpace(
        spaceId: Long,
    ) {
        val space = spaceOrThrow(spaceId)
        sqlClient.save(
            new(KnowledgeSpace::class).by {
                id = space.id
                spaceKey = space.spaceKey
                name = space.name
                description = space.description
                createTime = space.createTime
            },
        )
    }
}

private fun KnowledgeSpace.toDto(): KnowledgeSpaceDto {
    return KnowledgeSpaceDto(
        id = id,
        spaceKey = spaceKey,
        name = name,
        description = description,
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun KnowledgeDocument.toDto(): KnowledgeDocumentDto {
    return KnowledgeDocumentDto(
        id = id,
        documentKey = documentKey,
        spaceId = space.id,
        title = title,
        content = content,
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}
