package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.BoundedContextMeta
import site.addzero.coding.playground.server.entity.EtlWrapperMeta
import site.addzero.coding.playground.server.entity.TemplateMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringList
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.CreateTemplateMetaRequest
import site.addzero.coding.playground.shared.dto.DeleteCheckResultDto
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.TemplateMetaDto
import site.addzero.coding.playground.shared.dto.UpdateTemplateMetaRequest
import site.addzero.coding.playground.shared.dto.ValidationIssueDto
import site.addzero.coding.playground.shared.dto.ReorderRequestDto
import site.addzero.coding.playground.shared.service.TemplateMetaService

@Single
class TemplateMetaServiceImpl(
    private val support: MetadataPersistenceSupport,
) : TemplateMetaService {
    override suspend fun create(request: CreateTemplateMetaRequest): TemplateMetaDto {
        val context = support.contextOrThrow(request.contextId)
        request.etlWrapperId?.let { wrapperId ->
            val wrapper = support.etlWrapperOrThrow(wrapperId)
            if (wrapper.projectId != context.projectId) {
                throw PlaygroundValidationException("Template ETL wrapper must belong to the same project")
            }
        }
        support.validateName(request.name, "Template name")
        support.validateName(request.key, "Template key")
        support.validateTemplateOutputKind(request.outputKind.name)
        support.assertTemplateKeyUnique(request.contextId, request.key)
        val now = support.now()
        val entity = new(TemplateMeta::class).by {
            id = support.newId()
            this.context = support.contextRef(request.contextId)
            etlWrapper = request.etlWrapperId?.let(support::etlWrapperRef)
            name = request.name
            key = request.key
            description = request.description
            outputKind = request.outputKind.name
            body = request.body
            relativeOutputPath = request.relativeOutputPath
            fileNameTemplate = request.fileNameTemplate
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = support.nextTemplateOrder(request.contextId)
            enabled = request.enabled
            managedByGenerator = request.managedByGenerator
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto(support.json)
    }

    override suspend fun list(search: MetadataSearchRequest): List<TemplateMetaDto> {
        return support.listTemplates(search.contextId)
            .map { it.toDto(support.json) }
            .filter { search.includeDisabled || it.enabled }
            .filter {
                support.matchesSearch(
                    search = search,
                    title = it.name,
                    tags = it.tags,
                    contextId = it.contextId,
                )
            }
    }

    override suspend fun get(id: String): TemplateMetaDto {
        return support.templateOrThrow(id).toDto(support.json)
    }

    override suspend fun update(id: String, request: UpdateTemplateMetaRequest): TemplateMetaDto {
        val existing = support.templateOrThrow(id)
        val context = support.contextOrThrow(existing.contextId)
        request.etlWrapperId?.let { wrapperId ->
            val wrapper = support.etlWrapperOrThrow(wrapperId)
            if (wrapper.projectId != context.projectId) {
                throw PlaygroundValidationException("Template ETL wrapper must belong to the same project")
            }
        }
        support.validateName(request.name, "Template name")
        support.validateName(request.key, "Template key")
        support.validateTemplateOutputKind(request.outputKind.name)
        support.assertTemplateKeyUnique(existing.contextId, request.key, currentId = id)
        val entity = new(TemplateMeta::class).by {
            this.id = id
            this.context = support.contextRef(existing.contextId)
            etlWrapper = request.etlWrapperId?.let(support::etlWrapperRef)
            name = request.name
            key = request.key
            description = request.description
            outputKind = request.outputKind.name
            body = request.body
            relativeOutputPath = request.relativeOutputPath
            fileNameTemplate = request.fileNameTemplate
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = existing.orderIndex
            enabled = request.enabled
            managedByGenerator = request.managedByGenerator
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto(support.json)
    }

    override suspend fun reorder(contextId: String, request: ReorderRequestDto): List<TemplateMetaDto> {
        val templates = support.listTemplates(contextId).associateBy { it.id }
        request.orderedIds.forEachIndexed { index, id ->
            val template = templates[id] ?: return@forEachIndexed
            val entity = new(TemplateMeta::class).by {
                this.id = template.id
                context = support.contextRef(template.contextId)
                etlWrapper = template.etlWrapperId?.let(support::etlWrapperRef)
                name = template.name
                key = template.key
                description = template.description
                outputKind = template.outputKind
                body = template.body
                relativeOutputPath = template.relativeOutputPath
                fileNameTemplate = template.fileNameTemplate
                tagsJson = template.tagsJson
                orderIndex = index
                enabled = template.enabled
                managedByGenerator = template.managedByGenerator
                createdAt = template.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity)
        }
        return support.listTemplates(contextId).map { it.toDto(support.json) }
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        support.templateOrThrow(id)
        return support.ensureTemplateDeleteAllowed(id)
    }

    override suspend fun delete(id: String) {
        val check = support.ensureTemplateDeleteAllowed(id)
        if (!check.allowed) {
            throw PlaygroundValidationException(check.reasons.joinToString("; "))
        }
        support.deleteTargetTemplateLinksByTemplate(id)
        support.sqlClient.deleteById(TemplateMeta::class, id)
    }

    override suspend fun validate(id: String): List<ValidationIssueDto> {
        val template = support.templateOrThrow(id)
        return buildList {
            if (template.relativeOutputPath.isBlank()) {
                add(ValidationIssueDto(field = "relativeOutputPath", message = "Relative output path cannot be blank"))
            }
            if (template.fileNameTemplate.isBlank()) {
                add(ValidationIssueDto(field = "fileNameTemplate", message = "File name template cannot be blank"))
            }
        }
    }
}
