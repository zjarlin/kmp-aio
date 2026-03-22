package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.entity.BoundedContextMeta
import site.addzero.coding.playground.server.entity.DtoFieldMeta
import site.addzero.coding.playground.server.entity.DtoMeta
import site.addzero.coding.playground.server.entity.EntityMeta
import site.addzero.coding.playground.server.entity.FieldMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringList
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.CreateDtoFieldMetaRequest
import site.addzero.coding.playground.shared.dto.CreateDtoMetaRequest
import site.addzero.coding.playground.shared.dto.DeleteCheckResultDto
import site.addzero.coding.playground.shared.dto.DtoFieldMetaDto
import site.addzero.coding.playground.shared.dto.DtoMetaDto
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.ReorderRequestDto
import site.addzero.coding.playground.shared.dto.UpdateDtoFieldMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateDtoMetaRequest
import site.addzero.coding.playground.shared.service.DtoMetaService

@Single
class DtoMetaServiceImpl(
    private val support: MetadataPersistenceSupport,
) : DtoMetaService {
    override suspend fun create(request: CreateDtoMetaRequest): DtoMetaDto {
        val context = support.contextOrThrow(request.contextId)
        request.entityId?.let {
            val entity = support.entityOrThrow(it)
            if (entity.contextId != context.id) {
                error("DTO entity must belong to the same context")
            }
        }
        support.validateName(request.name, "DTO name")
        support.validateName(request.code, "DTO code")
        support.validateDtoKind(request.kind.name)
        support.assertDtoCodeUnique(request.contextId, request.code)
        val now = support.now()
        val entity = new(DtoMeta::class).by {
            id = support.newId()
            this.context = support.contextRef(request.contextId)
            this.entity = request.entityId?.let(support::entityRef)
            name = request.name
            code = request.code
            kind = request.kind.name
            description = request.description
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = support.nextDtoOrder(request.contextId)
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto(support.json)
    }

    override suspend fun list(search: MetadataSearchRequest): List<DtoMetaDto> {
        return support.listDtos(search.contextId)
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

    override suspend fun get(id: String): DtoMetaDto {
        return support.dtoOrThrow(id).toDto(support.json)
    }

    override suspend fun update(id: String, request: UpdateDtoMetaRequest): DtoMetaDto {
        val existing = support.dtoOrThrow(id)
        request.entityId?.let {
            val entity = support.entityOrThrow(it)
            if (entity.contextId != existing.contextId) {
                error("DTO entity must belong to the same context")
            }
        }
        support.validateName(request.name, "DTO name")
        support.validateName(request.code, "DTO code")
        support.validateDtoKind(request.kind.name)
        support.assertDtoCodeUnique(existing.contextId, request.code, currentId = id)
        val entity = new(DtoMeta::class).by {
            this.id = id
            context = support.contextRef(existing.contextId)
            this.entity = request.entityId?.let(support::entityRef)
            name = request.name
            code = request.code
            kind = request.kind.name
            description = request.description
            tagsJson = support.json.encodeStringList(request.tags)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto(support.json)
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        support.dtoOrThrow(id)
        return DeleteCheckResultDto(
            allowed = true,
            reasons = listOf("Deleting this DTO will cascade to ${support.listDtoFields(id).size} DTO field(s)"),
        )
    }

    override suspend fun delete(id: String) {
        support.dtoOrThrow(id)
        support.deleteDto(id)
    }

    override suspend fun createField(request: CreateDtoFieldMetaRequest): DtoFieldMetaDto {
        val dto = support.dtoOrThrow(request.dtoId)
        request.entityFieldId?.let {
            support.fieldOrThrow(it)
        }
        support.validateName(request.name, "DTO field name")
        support.validateName(request.code, "DTO field code")
        support.validateFieldType(request.type.name)
        support.assertDtoFieldCodeUnique(request.dtoId, request.code)
        val now = support.now()
        val entity = new(DtoFieldMeta::class).by {
            id = support.newId()
            this.dto = support.dtoRef(request.dtoId)
            entityField = request.entityFieldId?.let(support::fieldRef)
            name = request.name
            code = request.code
            type = request.type.name
            nullable = request.nullable
            list = request.list
            sourcePath = request.sourcePath
            description = request.description
            orderIndex = support.nextDtoFieldOrder(dto.id)
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateField(id: String, request: UpdateDtoFieldMetaRequest): DtoFieldMetaDto {
        val existing = support.dtoFieldOrThrow(id)
        request.entityFieldId?.let {
            support.fieldOrThrow(it)
        }
        support.validateName(request.name, "DTO field name")
        support.validateName(request.code, "DTO field code")
        support.validateFieldType(request.type.name)
        support.assertDtoFieldCodeUnique(existing.dtoId, request.code, currentId = id)
        val entity = new(DtoFieldMeta::class).by {
            this.id = id
            dto = support.dtoRef(existing.dtoId)
            entityField = request.entityFieldId?.let(support::fieldRef)
            name = request.name
            code = request.code
            type = request.type.name
            nullable = request.nullable
            list = request.list
            sourcePath = request.sourcePath
            description = request.description
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteField(id: String) {
        support.dtoFieldOrThrow(id)
        support.deleteDtoField(id)
    }

    override suspend fun reorderFields(dtoId: String, request: ReorderRequestDto): List<DtoFieldMetaDto> {
        val fields = support.listDtoFields(dtoId).associateBy { it.id }
        request.orderedIds.forEachIndexed { index, id ->
            val field = fields[id] ?: return@forEachIndexed
            val entity = new(DtoFieldMeta::class).by {
                this.id = field.id
                dto = support.dtoRef(field.dtoId)
                entityField = field.entityFieldId?.let(support::fieldRef)
                name = field.name
                code = field.code
                type = field.type
                nullable = field.nullable
                list = field.list
                sourcePath = field.sourcePath
                description = field.description
                orderIndex = index
                createdAt = field.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity)
        }
        return support.listDtoFields(dtoId).map { it.toDto() }
    }
}
