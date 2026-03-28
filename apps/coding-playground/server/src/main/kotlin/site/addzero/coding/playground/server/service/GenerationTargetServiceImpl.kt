package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.GenerationTarget
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringMap
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.GenerationTargetService

@Single(binds = [GenerationTargetService::class])
class GenerationTargetServiceImpl(
    private val support: MetadataPersistenceSupport,
    private val serviceSupport: CodegenServiceSupport,
    private val pathResolver: CodegenPathResolver,
) : GenerationTargetService {
    override suspend fun create(request: CreateGenerationTargetRequest): GenerationTargetDto {
        val project = support.projectOrThrow(request.projectId)
        serviceSupport.requireText(request.name, "目标名称")
        serviceSupport.requireText(request.rootDir, "输出根目录")
        serviceSupport.requireText(request.sourceSet, "源码集")
        serviceSupport.requirePackageName(request.basePackage, "基础包名")
        serviceSupport.requirePackageName(request.indexPackage, "索引包名")
        if (support.listTargets(project.id).any { it.name.equals(request.name, ignoreCase = true) }) {
            throw PlaygroundValidationException("同一项目下目标名称不能重复: ${request.name}")
        }
        val now = support.now()
        val entity = new(GenerationTarget::class).by {
            id = support.newId()
            this.project = support.projectRef(project.id)
            name = request.name.trim()
            rootDir = request.rootDir.trim()
            sourceSet = request.sourceSet.trim()
            basePackage = request.basePackage.trim()
            indexPackage = request.indexPackage.trim()
            kspEnabled = request.kspEnabled
            variablesJson = encodeStringMap(request.variables)
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun list(search: CodegenSearchRequest): List<GenerationTargetDto> {
        return support.listTargets(search.projectId)
            .filter {
                search.matches(
                    projectId = it.projectId,
                    targetId = it.id,
                    values = listOf(it.name, it.basePackage, it.indexPackage, it.rootDir),
                )
            }
            .map { it.toDto() }
    }

    override suspend fun get(id: String): GenerationTargetDto = support.targetOrThrow(id).toDto()

    override suspend fun update(id: String, request: UpdateGenerationTargetRequest): GenerationTargetDto {
        val existing = support.targetOrThrow(id)
        serviceSupport.requireText(request.name, "目标名称")
        serviceSupport.requireText(request.rootDir, "输出根目录")
        serviceSupport.requireText(request.sourceSet, "源码集")
        serviceSupport.requirePackageName(request.basePackage, "基础包名")
        serviceSupport.requirePackageName(request.indexPackage, "索引包名")
        if (support.listTargets(existing.projectId).any { it.id != id && it.name.equals(request.name, ignoreCase = true) }) {
            throw PlaygroundValidationException("同一项目下目标名称不能重复: ${request.name}")
        }
        val entity = new(GenerationTarget::class).by {
            this.id = id
            project = support.projectRef(existing.projectId)
            name = request.name.trim()
            rootDir = request.rootDir.trim()
            sourceSet = request.sourceSet.trim()
            basePackage = request.basePackage.trim()
            indexPackage = request.indexPackage.trim()
            kspEnabled = request.kspEnabled
            variablesJson = encodeStringMap(request.variables)
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        val target = support.targetOrThrow(id)
        val files = support.listFiles(id)
        val declarations = support.listDeclarations().filter { it.targetId == id }
        return DeleteCheckResultDto(
            id = id,
            kind = "target",
            canDelete = true,
            warnings = listOf("删除目标 ${target.name} 将同时删除 ${files.size} 个文件、${declarations.size} 个声明和相关托管产物"),
        )
    }

    override suspend fun validate(id: String): List<ValidationIssueDto> {
        val target = support.targetOrThrow(id).toDto()
        val issues = mutableListOf<ValidationIssueDto>()
        if (support.listFiles(id).isEmpty()) {
            issues += serviceSupport.buildValidationIssue("target", id, ValidationSeverity.WARNING, "当前目标还没有 Kotlin 文件")
        }
        runCatching { pathResolver.preview(target) }.onFailure {
            issues += serviceSupport.buildValidationIssue("target", id, ValidationSeverity.ERROR, "输出路径解析失败: ${it.message}")
        }
        return issues
    }

    override suspend fun previewPath(id: String): PathPreviewDto = pathResolver.preview(support.targetOrThrow(id).toDto())

    override suspend fun delete(id: String) {
        support.targetOrThrow(id)
        support.inTransaction {
            support.deleteTargetCascade(id)
        }
    }
}
