package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.BoundedContextMeta
import site.addzero.coding.playground.server.entity.EntityMeta
import site.addzero.coding.playground.server.entity.FieldMeta
import site.addzero.coding.playground.server.entity.RelationMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringList
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.CreateEntityMetaRequest
import site.addzero.coding.playground.shared.dto.CreateFieldMetaRequest
import site.addzero.coding.playground.shared.dto.CreateRelationMetaRequest
import site.addzero.coding.playground.shared.dto.DeleteCheckResultDto
import site.addzero.coding.playground.shared.dto.EntityMetaDto
import site.addzero.coding.playground.shared.dto.FieldMetaDto
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.RelationMetaDto
import site.addzero.coding.playground.shared.dto.ReorderRequestDto
import site.addzero.coding.playground.shared.dto.UpdateEntityMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateFieldMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateRelationMetaRequest
import site.addzero.coding.playground.shared.service.EntityMetaService

@Single
class EntityMetaServiceImpl(
    private val support: MetadataPersistenceSupport,
) : EntityMetaService {
    override suspend fun create(request: CreateEntityMetaRequest): EntityMetaDto {
        support.contextOrThrow(request.contextId)
        support.validateName(request.name, "Entity name")
        support.validateName(request.code, "Entity code")
        support.validateName(request.tableName, "Table name")
        support.assertEntityCodeUnique(request.contextId, request.code)
        val now = support.now()
        val entity = new(EntityMeta::class).by {
            id = support.newId()
            context = support.contextRef(request.contextId)
            name = request.name
            code = request.code
            tableName = request.tableName
            description = request.description
            aggregateRoot = request.aggregateRoot
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = support.nextEntityOrder(request.contextId)
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto(support.json)
    }

    override suspend fun list(search: MetadataSearchRequest): List<EntityMetaDto> {
        return support.listEntities(search.contextId)
            .map { it.toDto(support.json) }
            .filter {
                support.matchesSearch(
                    search = search,
                    title = it.name,
                    tags = it.tags,
                    contextId = it.contextId,
                )
            }
    }

    override suspend fun get(id: String): EntityMetaDto {
        return support.entityOrThrow(id).toDto(support.json)
    }

    override suspend fun update(id: String, request: UpdateEntityMetaRequest): EntityMetaDto {
        val existing = support.entityOrThrow(id)
        support.validateName(request.name, "Entity name")
        support.validateName(request.code, "Entity code")
        support.validateName(request.tableName, "Table name")
        support.assertEntityCodeUnique(existing.contextId, request.code, currentId = id)
        val entity = new(EntityMeta::class).by {
            this.id = id
            context = support.contextRef(existing.contextId)
            name = request.name
            code = request.code
            tableName = request.tableName
            description = request.description
            aggregateRoot = request.aggregateRoot
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto(support.json)
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        support.entityOrThrow(id)
        return support.ensureEntityDeleteAllowed(id)
    }

    override suspend fun delete(id: String) {
        val check = support.ensureEntityDeleteAllowed(id)
        if (!check.allowed) {
            throw PlaygroundValidationException(check.reasons.joinToString("; "))
        }
        support.inTransaction {
            support.deleteEntity(id)
        }
    }

    override suspend fun createField(request: CreateFieldMetaRequest): FieldMetaDto {
        support.entityOrThrow(request.entityId)
        support.validateName(request.name, "Field name")
        support.validateName(request.code, "Field code")
        support.validateFieldType(request.type.name)
        support.assertFieldCodeUnique(request.entityId, request.code)
        if (request.idField && support.listFields(request.entityId).any { it.idField }) {
            throw PlaygroundValidationException("Entity already has an id field")
        }
        val now = support.now()
        val entity = new(FieldMeta::class).by {
            id = support.newId()
            entity = support.entityRef(request.entityId)
            name = request.name
            code = request.code
            type = request.type.name
            nullable = request.nullable
            list = request.list
            idField = request.idField
            keyField = request.keyField
            unique = request.unique
            searchable = request.searchable
            defaultValue = request.defaultValue
            description = request.description
            orderIndex = support.nextFieldOrder(request.entityId)
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateField(id: String, request: UpdateFieldMetaRequest): FieldMetaDto {
        val existing = support.fieldOrThrow(id)
        support.validateName(request.name, "Field name")
        support.validateName(request.code, "Field code")
        support.validateFieldType(request.type.name)
        support.assertFieldCodeUnique(existing.entityId, request.code, currentId = id)
        if (request.idField && support.listFields(existing.entityId).any { it.idField && it.id != id }) {
            throw PlaygroundValidationException("Entity already has another id field")
        }
        val entity = new(FieldMeta::class).by {
            this.id = id
            entity = support.entityRef(existing.entityId)
            name = request.name
            code = request.code
            type = request.type.name
            nullable = request.nullable
            list = request.list
            idField = request.idField
            keyField = request.keyField
            unique = request.unique
            searchable = request.searchable
            defaultValue = request.defaultValue
            description = request.description
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteField(id: String) {
        support.inTransaction {
            support.fieldOrThrow(id)
            support.deleteField(id)
        }
    }

    override suspend fun reorderFields(entityId: String, request: ReorderRequestDto): List<FieldMetaDto> {
        val fields = support.listFields(entityId).associateBy { it.id }
        request.orderedIds.forEachIndexed { index, id ->
            val field = fields[id] ?: return@forEachIndexed
            val entity = new(FieldMeta::class).by {
                this.id = field.id
                entity = support.entityRef(field.entityId)
                name = field.name
                code = field.code
                type = field.type
                nullable = field.nullable
                list = field.list
                idField = field.idField
                keyField = field.keyField
                unique = field.unique
                searchable = field.searchable
                defaultValue = field.defaultValue
                description = field.description
                orderIndex = index
                createdAt = field.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity)
        }
        return support.listFields(entityId).map { it.toDto() }
    }

    override suspend fun createRelation(request: CreateRelationMetaRequest): RelationMetaDto {
        val context = support.contextOrThrow(request.contextId)
        val source = support.entityOrThrow(request.sourceEntityId)
        val target = support.entityOrThrow(request.targetEntityId)
        if (source.contextId != context.id || target.contextId != context.id) {
            throw PlaygroundValidationException("Relation entities must belong to the same context")
        }
        support.validateName(request.name, "Relation name")
        support.validateName(request.code, "Relation code")
        support.validateRelationKind(request.kind.name)
        support.assertRelationCodeUnique(request.contextId, request.code)
        val now = support.now()
        val entity = new(RelationMeta::class).by {
            id = support.newId()
            this.context = support.contextRef(request.contextId)
            sourceEntity = support.entityRef(request.sourceEntityId)
            targetEntity = support.entityRef(request.targetEntityId)
            name = request.name
            code = request.code
            kind = request.kind.name
            nullable = request.nullable
            owner = request.owner
            mappedBy = request.mappedBy
            sourceFieldName = request.sourceFieldName
            targetFieldName = request.targetFieldName
            description = request.description
            orderIndex = support.nextRelationOrder(request.contextId)
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateRelation(id: String, request: UpdateRelationMetaRequest): RelationMetaDto {
        val existing = support.relationOrThrow(id)
        support.validateName(request.name, "Relation name")
        support.validateName(request.code, "Relation code")
        support.validateRelationKind(request.kind.name)
        support.assertRelationCodeUnique(existing.contextId, request.code, currentId = id)
        val entity = new(RelationMeta::class).by {
            this.id = id
            context = support.contextRef(existing.contextId)
            sourceEntity = support.entityRef(existing.sourceEntityId)
            targetEntity = support.entityRef(existing.targetEntityId)
            name = request.name
            code = request.code
            kind = request.kind.name
            nullable = request.nullable
            owner = request.owner
            mappedBy = request.mappedBy
            sourceFieldName = request.sourceFieldName
            targetFieldName = request.targetFieldName
            description = request.description
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteRelation(id: String) {
        support.inTransaction {
            support.relationOrThrow(id)
            support.sqlClient.deleteById(RelationMeta::class, id)
        }
    }
}
