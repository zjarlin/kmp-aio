package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.entity.BoundedContextMeta
import site.addzero.coding.playground.server.entity.DtoFieldMeta
import site.addzero.coding.playground.server.entity.DtoMeta
import site.addzero.coding.playground.server.entity.EntityMeta
import site.addzero.coding.playground.server.entity.EtlWrapperMeta
import site.addzero.coding.playground.server.entity.FieldMeta
import site.addzero.coding.playground.server.entity.GenerationTargetMeta
import site.addzero.coding.playground.server.entity.ProjectMeta
import site.addzero.coding.playground.server.entity.RelationMeta
import site.addzero.coding.playground.server.entity.TemplateMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringList
import site.addzero.coding.playground.server.entity.encodeStringMap
import site.addzero.coding.playground.shared.dto.MetadataImportResultDto
import site.addzero.coding.playground.shared.dto.MetadataSnapshotDto
import site.addzero.coding.playground.shared.service.MetadataSnapshotService

@Single
class MetadataSnapshotServiceImpl(
    private val support: MetadataPersistenceSupport,
) : MetadataSnapshotService {
    override suspend fun exportProject(projectId: String): MetadataSnapshotDto {
        val aggregate = support.buildProjectAggregate(projectId)
        return MetadataSnapshotDto(
            exportedAt = support.now().toString(),
            projects = listOf(aggregate.project),
            contexts = aggregate.contexts.map { it.context },
            entities = aggregate.contexts.flatMap { it.entities },
            fields = aggregate.contexts.flatMap { it.fields },
            relations = aggregate.contexts.flatMap { it.relations },
            dtos = aggregate.contexts.flatMap { it.dtos },
            dtoFields = aggregate.contexts.flatMap { it.dtoFields },
            templates = aggregate.contexts.flatMap { it.templates },
            generationTargets = aggregate.contexts.flatMap { it.generationTargets },
            etlWrappers = aggregate.etlWrappers,
        )
    }

    override suspend fun importSnapshot(snapshot: MetadataSnapshotDto): MetadataImportResultDto {
        return support.inTransaction {
            val created = mutableListOf<String>()
            val updated = mutableListOf<String>()
            snapshot.projects.forEach { project ->
                val existed = support.sqlClient.findById(ProjectMeta::class, project.id) != null
                val entity = new(ProjectMeta::class).by {
                    id = project.id
                    name = project.name
                    slug = project.slug
                    description = project.description
                    tagsJson = support.json.encodeStringList(project.tags)
                    orderIndex = project.orderIndex
                    createdAt = java.time.LocalDateTime.parse(project.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(project.updatedAt)
                }
                support.sqlClient.save(entity)
                if (existed) updated += project.id else created += project.id
            }
            snapshot.contexts.forEach { context ->
                val existed = support.sqlClient.findById(BoundedContextMeta::class, context.id) != null
                val entity = new(BoundedContextMeta::class).by {
                    id = context.id
                    project = new(ProjectMeta::class).by { id = context.projectId }
                    name = context.name
                    code = context.code
                    description = context.description
                    tagsJson = support.json.encodeStringList(context.tags)
                    orderIndex = context.orderIndex
                    createdAt = java.time.LocalDateTime.parse(context.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(context.updatedAt)
                }
                support.sqlClient.save(entity)
                if (existed) updated += context.id else created += context.id
            }
            snapshot.entities.forEach { item ->
                val existed = support.sqlClient.findById(EntityMeta::class, item.id) != null
                val entity = new(EntityMeta::class).by {
                    id = item.id
                    context = new(BoundedContextMeta::class).by { id = item.contextId }
                    name = item.name
                    code = item.code
                    tableName = item.tableName
                    description = item.description
                    aggregateRoot = item.aggregateRoot
                    tagsJson = support.json.encodeStringList(item.tags)
                    orderIndex = item.orderIndex
                    createdAt = java.time.LocalDateTime.parse(item.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(item.updatedAt)
                }
                support.sqlClient.save(entity)
                if (existed) updated += item.id else created += item.id
            }
            snapshot.fields.forEach { item ->
                val existed = support.sqlClient.findById(FieldMeta::class, item.id) != null
                val entity = new(FieldMeta::class).by {
                    id = item.id
                    entity = new(EntityMeta::class).by { id = item.entityId }
                    name = item.name
                    code = item.code
                    type = item.type.name
                    nullable = item.nullable
                    list = item.list
                    idField = item.idField
                    keyField = item.keyField
                    unique = item.unique
                    searchable = item.searchable
                    defaultValue = item.defaultValue
                    description = item.description
                    orderIndex = item.orderIndex
                    createdAt = java.time.LocalDateTime.parse(item.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(item.updatedAt)
                }
                support.sqlClient.save(entity)
                if (existed) updated += item.id else created += item.id
            }
            snapshot.relations.forEach { item ->
                val existed = support.sqlClient.findById(RelationMeta::class, item.id) != null
                val entity = new(RelationMeta::class).by {
                    id = item.id
                    context = new(BoundedContextMeta::class).by { id = item.contextId }
                    sourceEntity = new(EntityMeta::class).by { id = item.sourceEntityId }
                    targetEntity = new(EntityMeta::class).by { id = item.targetEntityId }
                    name = item.name
                    code = item.code
                    kind = item.kind.name
                    nullable = item.nullable
                    owner = item.owner
                    mappedBy = item.mappedBy
                    sourceFieldName = item.sourceFieldName
                    targetFieldName = item.targetFieldName
                    description = item.description
                    orderIndex = item.orderIndex
                    createdAt = java.time.LocalDateTime.parse(item.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(item.updatedAt)
                }
                support.sqlClient.save(entity)
                if (existed) updated += item.id else created += item.id
            }
            snapshot.dtos.forEach { item ->
                val existed = support.sqlClient.findById(DtoMeta::class, item.id) != null
                val entity = new(DtoMeta::class).by {
                    id = item.id
                    context = new(BoundedContextMeta::class).by { id = item.contextId }
                    entity = item.entityId?.let { entityId ->
                        new(EntityMeta::class).by { id = entityId }
                    }
                    name = item.name
                    code = item.code
                    kind = item.kind.name
                    description = item.description
                    tagsJson = support.json.encodeStringList(item.tags)
                    orderIndex = item.orderIndex
                    createdAt = java.time.LocalDateTime.parse(item.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(item.updatedAt)
                }
                support.sqlClient.save(entity)
                if (existed) updated += item.id else created += item.id
            }
            snapshot.dtoFields.forEach { item ->
                val existed = support.sqlClient.findById(DtoFieldMeta::class, item.id) != null
                val entity = new(DtoFieldMeta::class).by {
                    id = item.id
                    dto = new(DtoMeta::class).by { id = item.dtoId }
                    entityField = item.entityFieldId?.let { fieldId ->
                        new(FieldMeta::class).by { id = fieldId }
                    }
                    name = item.name
                    code = item.code
                    type = item.type.name
                    nullable = item.nullable
                    list = item.list
                    sourcePath = item.sourcePath
                    description = item.description
                    orderIndex = item.orderIndex
                    createdAt = java.time.LocalDateTime.parse(item.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(item.updatedAt)
                }
                support.sqlClient.save(entity)
                if (existed) updated += item.id else created += item.id
            }
            snapshot.etlWrappers.forEach { item ->
                val existed = support.sqlClient.findById(EtlWrapperMeta::class, item.id) != null
                val entity = new(EtlWrapperMeta::class).by {
                    id = item.id
                    project = new(ProjectMeta::class).by { id = item.projectId }
                    name = item.name
                    key = item.key
                    description = item.description
                    scriptBody = item.scriptBody
                    enabled = item.enabled
                    createdAt = java.time.LocalDateTime.parse(item.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(item.updatedAt)
                }
                support.sqlClient.save(entity)
                if (existed) updated += item.id else created += item.id
            }
            snapshot.templates.forEach { item ->
                val existed = support.sqlClient.findById(TemplateMeta::class, item.id) != null
                val entity = new(TemplateMeta::class).by {
                    id = item.id
                    context = new(BoundedContextMeta::class).by { id = item.contextId }
                    etlWrapper = item.etlWrapperId?.let { wrapperId ->
                        new(EtlWrapperMeta::class).by { id = wrapperId }
                    }
                    name = item.name
                    key = item.key
                    description = item.description
                    outputKind = item.outputKind.name
                    body = item.body
                    relativeOutputPath = item.relativeOutputPath
                    fileNameTemplate = item.fileNameTemplate
                    tagsJson = support.json.encodeStringList(item.tags)
                    orderIndex = item.orderIndex
                    enabled = item.enabled
                    managedByGenerator = item.managedByGenerator
                    createdAt = java.time.LocalDateTime.parse(item.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(item.updatedAt)
                }
                support.sqlClient.save(entity)
                if (existed) updated += item.id else created += item.id
            }
            snapshot.generationTargets.forEach { item ->
                val existed = support.sqlClient.findById(GenerationTargetMeta::class, item.id) != null
                val entity = new(GenerationTargetMeta::class).by {
                    id = item.id
                    project = new(ProjectMeta::class).by { id = item.projectId }
                    context = new(BoundedContextMeta::class).by { id = item.contextId }
                    name = item.name
                    key = item.key
                    description = item.description
                    outputRoot = item.outputRoot
                    packageName = item.packageName
                    scaffoldPreset = item.scaffoldPreset.name
                    variablesJson = support.json.encodeStringMap(item.variables)
                    enableEtl = item.enableEtl
                    autoIntegrateCompositeBuild = item.autoIntegrateCompositeBuild
                    managedMarker = item.managedMarker
                    createdAt = java.time.LocalDateTime.parse(item.createdAt)
                    updatedAt = java.time.LocalDateTime.parse(item.updatedAt)
                }
                support.sqlClient.save(entity)
                support.syncTargetTemplates(item.id, item.templateIds)
                if (existed) updated += item.id else created += item.id
            }
            MetadataImportResultDto(
                createdIds = created,
                updatedIds = updated,
            )
        }
    }
}
