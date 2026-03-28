package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.CodegenProject
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.CodegenProjectService

@Single
class CodegenProjectServiceImpl(
    private val support: MetadataPersistenceSupport,
    private val serviceSupport: CodegenServiceSupport,
) : CodegenProjectService {
    override suspend fun create(request: CreateCodegenProjectRequest): CodegenProjectDto {
        serviceSupport.requireText(request.name, "项目名称")
        if (support.listProjects().any { it.name.equals(request.name, ignoreCase = true) }) {
            throw PlaygroundValidationException("项目名称已存在: ${request.name}")
        }
        val now = support.now()
        val entity = new(CodegenProject::class).by {
            id = support.newId()
            name = request.name.trim()
            description = request.description?.trim()?.ifBlank { null }
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun list(search: CodegenSearchRequest): List<CodegenProjectDto> {
        return support.listProjects()
            .filter { search.matches(projectId = it.id, it.name, it.description) }
            .map { it.toDto() }
    }

    override suspend fun get(id: String): CodegenProjectDto = support.projectOrThrow(id).toDto()

    override suspend fun aggregate(id: String): CodegenProjectAggregateDto = support.buildProjectAggregate(id)

    override suspend fun update(id: String, request: UpdateCodegenProjectRequest): CodegenProjectDto {
        val existing = support.projectOrThrow(id)
        serviceSupport.requireText(request.name, "项目名称")
        if (support.listProjects().any { it.id != id && it.name.equals(request.name, ignoreCase = true) }) {
            throw PlaygroundValidationException("项目名称已存在: ${request.name}")
        }
        val entity = new(CodegenProject::class).by {
            this.id = id
            name = request.name.trim()
            description = request.description?.trim()?.ifBlank { null }
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        val aggregate = support.buildProjectAggregate(id)
        return DeleteCheckResultDto(
            id = id,
            kind = "project",
            canDelete = true,
            warnings = listOf("删除后会级联清理 ${aggregate.targets.size} 个生成目标、${aggregate.files.size} 个文件、${aggregate.declarations.size} 个声明"),
        )
    }

    override suspend fun validate(id: String): List<ValidationIssueDto> {
        val aggregate = support.buildProjectAggregate(id)
        if (aggregate.targets.isNotEmpty()) {
            return emptyList()
        }
        return listOf(
            serviceSupport.buildValidationIssue("project", id, ValidationSeverity.WARNING, "当前项目还没有生成目标"),
        )
    }

    override suspend fun delete(id: String) {
        support.projectOrThrow(id)
        support.inTransaction {
            support.deleteProjectCascade(id)
        }
    }
}
