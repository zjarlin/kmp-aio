package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.entity.BoundedContextMeta
import site.addzero.coding.playground.server.entity.GenerationTargetMeta
import site.addzero.coding.playground.server.entity.ProjectMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringMap
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.CreateGenerationTargetMetaRequest
import site.addzero.coding.playground.shared.dto.GenerationTargetMetaDto
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.UpdateGenerationTargetMetaRequest
import site.addzero.coding.playground.shared.service.GenerationTargetMetaService

@Single
class GenerationTargetMetaServiceImpl(
    private val support: MetadataPersistenceSupport,
) : GenerationTargetMetaService {
    override suspend fun create(request: CreateGenerationTargetMetaRequest): GenerationTargetMetaDto {
        support.projectOrThrow(request.projectId)
        support.ensureProjectContextAlignment(request.projectId, request.contextId)
        support.validateName(request.name, "Generation target name")
        support.validateName(request.key, "Generation target key")
        support.validateScaffoldPreset(request.scaffoldPreset.name)
        support.assertTargetKeyUnique(request.projectId, request.key)
        val templateIds = request.templateIds.ifEmpty {
            support.listTemplates(request.contextId).map { it.id }
        }
        val now = support.now()
        val entity = new(GenerationTargetMeta::class).by {
            id = support.newId()
            project = support.projectRef(request.projectId)
            context = support.contextRef(request.contextId)
            name = request.name
            key = request.key
            description = request.description
            outputRoot = request.outputRoot
            packageName = request.packageName
            scaffoldPreset = request.scaffoldPreset.name
            variablesJson = support.json.encodeStringMap(request.variables)
            enableEtl = request.enableEtl
            autoIntegrateCompositeBuild = request.autoIntegrateCompositeBuild
            managedMarker = request.managedMarker
            createdAt = now
            updatedAt = now
        }
        val saved = support.sqlClient.save(entity).modifiedEntity
        support.syncTargetTemplates(saved.id, templateIds)
        return saved.toDto(support.json, support.templateIdsForTarget(saved.id))
    }

    override suspend fun list(search: MetadataSearchRequest): List<GenerationTargetMetaDto> {
        return support.listTargets(projectId = search.projectId, contextId = search.contextId)
            .map { it.toDto(support.json, support.templateIdsForTarget(it.id)) }
            .filter {
                support.matchesSearch(
                    search = search,
                    title = it.name,
                    tags = emptyList(),
                    projectId = it.projectId,
                    contextId = it.contextId,
                )
            }
    }

    override suspend fun get(id: String): GenerationTargetMetaDto {
        val target = support.targetOrThrow(id)
        return target.toDto(support.json, support.templateIdsForTarget(id))
    }

    override suspend fun update(id: String, request: UpdateGenerationTargetMetaRequest): GenerationTargetMetaDto {
        val existing = support.targetOrThrow(id)
        support.validateName(request.name, "Generation target name")
        support.validateName(request.key, "Generation target key")
        support.validateScaffoldPreset(request.scaffoldPreset.name)
        support.assertTargetKeyUnique(existing.projectId, request.key, currentId = id)
        val templateIds = request.templateIds.ifEmpty {
            support.listTemplates(existing.contextId).map { it.id }
        }
        val entity = new(GenerationTargetMeta::class).by {
            this.id = id
            project = support.projectRef(existing.projectId)
            context = support.contextRef(existing.contextId)
            name = request.name
            key = request.key
            description = request.description
            outputRoot = request.outputRoot
            packageName = request.packageName
            scaffoldPreset = request.scaffoldPreset.name
            variablesJson = support.json.encodeStringMap(request.variables)
            enableEtl = request.enableEtl
            autoIntegrateCompositeBuild = request.autoIntegrateCompositeBuild
            managedMarker = request.managedMarker
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        val saved = support.sqlClient.save(entity).modifiedEntity
        support.syncTargetTemplates(saved.id, templateIds)
        return saved.toDto(support.json, support.templateIdsForTarget(saved.id))
    }

    override suspend fun delete(id: String) {
        support.targetOrThrow(id)
        support.deleteTargetTemplateLinksByTarget(id)
        support.sqlClient.deleteById(GenerationTargetMeta::class, id)
    }
}
