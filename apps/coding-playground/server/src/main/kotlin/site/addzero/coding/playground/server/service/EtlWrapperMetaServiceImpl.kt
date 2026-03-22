package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.EtlWrapperMeta
import site.addzero.coding.playground.server.entity.ProjectMeta
import site.addzero.coding.playground.server.entity.TemplateMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.CreateEtlWrapperMetaRequest
import site.addzero.coding.playground.shared.dto.EtlWrapperMetaDto
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.UpdateEtlWrapperMetaRequest
import site.addzero.coding.playground.shared.service.EtlWrapperMetaService

@Single
class EtlWrapperMetaServiceImpl(
    private val support: MetadataPersistenceSupport,
) : EtlWrapperMetaService {
    override suspend fun create(request: CreateEtlWrapperMetaRequest): EtlWrapperMetaDto {
        support.projectOrThrow(request.projectId)
        support.validateName(request.name, "ETL wrapper name")
        support.validateName(request.key, "ETL wrapper key")
        support.assertEtlKeyUnique(request.projectId, request.key)
        val now = support.now()
        val entity = new(EtlWrapperMeta::class).by {
            id = support.newId()
            project = support.projectRef(request.projectId)
            name = request.name
            key = request.key
            description = request.description
            scriptBody = request.scriptBody
            enabled = request.enabled
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun list(search: MetadataSearchRequest): List<EtlWrapperMetaDto> {
        return support.listEtlWrappers(search.projectId)
            .map { it.toDto() }
            .filter {
                support.matchesSearch(
                    search = search,
                    title = it.name,
                    tags = emptyList(),
                    projectId = it.projectId,
                )
            }
    }

    override suspend fun get(id: String): EtlWrapperMetaDto {
        return support.etlWrapperOrThrow(id).toDto()
    }

    override suspend fun update(id: String, request: UpdateEtlWrapperMetaRequest): EtlWrapperMetaDto {
        val existing = support.etlWrapperOrThrow(id)
        support.validateName(request.name, "ETL wrapper name")
        support.validateName(request.key, "ETL wrapper key")
        support.assertEtlKeyUnique(existing.projectId, request.key, currentId = id)
        val entity = new(EtlWrapperMeta::class).by {
            this.id = id
            project = support.projectRef(existing.projectId)
            name = request.name
            key = request.key
            description = request.description
            scriptBody = request.scriptBody
            enabled = request.enabled
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun delete(id: String) {
        support.etlWrapperOrThrow(id)
        val usageCount = support.listTemplates().count { it.etlWrapperId == id }
        if (usageCount > 0) {
            throw PlaygroundValidationException("ETL wrapper is still referenced by $usageCount template(s)")
        }
        support.sqlClient.deleteById(EtlWrapperMeta::class, id)
    }
}
