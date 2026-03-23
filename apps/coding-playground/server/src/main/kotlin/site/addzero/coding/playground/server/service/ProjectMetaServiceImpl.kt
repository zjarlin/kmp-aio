package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.entity.ProjectMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringList
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.CreateProjectMetaRequest
import site.addzero.coding.playground.shared.dto.DeleteCheckResultDto
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.ProjectAggregateDto
import site.addzero.coding.playground.shared.dto.ProjectMetaDto
import site.addzero.coding.playground.shared.dto.UpdateProjectMetaRequest
import site.addzero.coding.playground.shared.service.ProjectMetaService

@Single
class ProjectMetaServiceImpl(
    private val support: MetadataPersistenceSupport,
) : ProjectMetaService {
    override suspend fun create(request: CreateProjectMetaRequest): ProjectMetaDto {
        support.validateName(request.name, "Project name")
        support.validateName(request.slug, "Project slug")
        support.assertProjectSlugUnique(request.slug)
        val now = support.now()
        val entity = new(ProjectMeta::class).by {
            id = support.newId()
            name = request.name
            slug = request.slug
            description = request.description
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = support.listProjects().size
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto(support.json)
    }

    override suspend fun list(search: MetadataSearchRequest): List<ProjectMetaDto> {
        return support.listProjects()
            .map { it.toDto(support.json) }
            .filter {
                support.matchesSearch(
                    search = search,
                    title = it.name,
                    tags = it.tags,
                    projectId = it.id,
                )
            }
    }

    override suspend fun get(id: String): ProjectMetaDto {
        return support.projectOrThrow(id).toDto(support.json)
    }

    override suspend fun update(id: String, request: UpdateProjectMetaRequest): ProjectMetaDto {
        support.projectOrThrow(id)
        support.validateName(request.name, "Project name")
        support.validateName(request.slug, "Project slug")
        support.assertProjectSlugUnique(request.slug, currentId = id)
        val existing = support.projectOrThrow(id)
        val entity = new(ProjectMeta::class).by {
            this.id = id
            name = request.name
            slug = request.slug
            description = request.description
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto(support.json)
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        support.projectOrThrow(id)
        return DeleteCheckResultDto(
            allowed = true,
            reasons = support.countCascadeSummaryForProject(id),
        )
    }

    override suspend fun delete(id: String) {
        support.inTransaction {
            support.projectOrThrow(id)
            support.deleteProjectCascade(id)
        }
    }

    override suspend fun tree(id: String): ProjectAggregateDto {
        return support.buildProjectAggregate(id)
    }
}
