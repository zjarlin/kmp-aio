package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.entity.BoundedContextMeta
import site.addzero.coding.playground.server.entity.ProjectMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringList
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.BoundedContextMetaDto
import site.addzero.coding.playground.shared.dto.ContextAggregateDto
import site.addzero.coding.playground.shared.dto.CreateBoundedContextMetaRequest
import site.addzero.coding.playground.shared.dto.DeleteCheckResultDto
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.UpdateBoundedContextMetaRequest
import site.addzero.coding.playground.shared.service.ContextMetaService

@Single
class ContextMetaServiceImpl(
    private val support: MetadataPersistenceSupport,
) : ContextMetaService {
    override suspend fun create(request: CreateBoundedContextMetaRequest): BoundedContextMetaDto {
        support.projectOrThrow(request.projectId)
        support.validateName(request.name, "Context name")
        support.validateName(request.code, "Context code")
        support.assertContextCodeUnique(request.projectId, request.code)
        val now = support.now()
        val entity = new(BoundedContextMeta::class).by {
            id = support.newId()
            project = support.projectRef(request.projectId)
            name = request.name
            code = request.code
            description = request.description
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = support.nextContextOrder(request.projectId)
            createdAt = now
            updatedAt = now
        }
        val saved = support.sqlClient.save(entity).modifiedEntity
        support.seedBuiltinTemplates(saved.id)
        return saved.toDto(support.json)
    }

    override suspend fun list(search: MetadataSearchRequest): List<BoundedContextMetaDto> {
        return support.listContexts(search.projectId)
            .map { it.toDto(support.json) }
            .filter {
                support.matchesSearch(
                    search = search,
                    title = it.name,
                    tags = it.tags,
                    projectId = it.projectId,
                    contextId = it.id,
                )
            }
    }

    override suspend fun get(id: String): BoundedContextMetaDto {
        return support.contextOrThrow(id).toDto(support.json)
    }

    override suspend fun aggregate(id: String): ContextAggregateDto {
        return support.buildContextAggregate(id)
    }

    override suspend fun update(id: String, request: UpdateBoundedContextMetaRequest): BoundedContextMetaDto {
        val existing = support.contextOrThrow(id)
        support.validateName(request.name, "Context name")
        support.validateName(request.code, "Context code")
        support.assertContextCodeUnique(existing.projectId, request.code, currentId = id)
        val entity = new(BoundedContextMeta::class).by {
            this.id = id
            project = support.projectRef(existing.projectId)
            name = request.name
            code = request.code
            description = request.description
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto(support.json)
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        support.contextOrThrow(id)
        return DeleteCheckResultDto(
            allowed = true,
            reasons = support.countCascadeSummaryForContext(id),
        )
    }

    override suspend fun delete(id: String) {
        support.contextOrThrow(id)
        support.deleteContextCascade(id)
    }
}
