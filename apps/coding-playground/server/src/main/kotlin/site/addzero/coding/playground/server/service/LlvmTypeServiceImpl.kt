package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.LlvmType
import site.addzero.coding.playground.server.entity.LlvmTypeMember
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringMap
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.LlvmTypeService

@Single
class LlvmTypeServiceImpl(
    private val support: MetadataPersistenceSupport,
) : LlvmTypeService {
    override suspend fun create(request: CreateLlvmTypeRequest): LlvmTypeDto {
        support.moduleOrThrow(request.moduleId)
        requireText(request.name, "type name")
        requireText(request.symbol, "type symbol")
        if (support.listTypes(request.moduleId).any { it.symbol == request.symbol }) {
            throw PlaygroundValidationException("LLVM type symbol '${request.symbol}' already exists in the module")
        }
        val now = support.now()
        val entity = new(LlvmType::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            symbol = request.symbol
            kind = request.kind.name
            primitiveWidth = request.primitiveWidth
            packed = request.packed
            opaque = request.opaque
            addressSpace = request.addressSpace
            arrayLength = request.arrayLength
            scalable = request.scalable
            variadic = request.variadic
            definitionText = request.definitionText
            elementTypeRef = request.elementTypeRefId?.let(support::typeRef)
            returnTypeRef = request.returnTypeRefId?.let(support::typeRef)
            orderIndex = support.nextOrder(support.listTypes(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun list(search: LlvmSearchRequest): List<LlvmTypeDto> {
        return support.listTypes(search.moduleId)
            .map { it.toDto() }
            .filter {
                search.matches(
                    moduleId = it.moduleId,
                    symbol = it.symbol,
                    extras = listOf(it.name, it.kind.name, it.definitionText),
                )
            }
            .filter { search.typeKind == null || it.kind == search.typeKind }
    }

    override suspend fun get(id: String): LlvmTypeDto = support.typeOrThrow(id).toDto()

    override suspend fun update(id: String, request: UpdateLlvmTypeRequest): LlvmTypeDto {
        val existing = support.typeOrThrow(id)
        requireText(request.name, "type name")
        requireText(request.symbol, "type symbol")
        if (support.listTypes(existing.moduleId).any { it.id != id && it.symbol == request.symbol }) {
            throw PlaygroundValidationException("LLVM type symbol '${request.symbol}' already exists in the module")
        }
        val entity = new(LlvmType::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            symbol = request.symbol
            kind = request.kind.name
            primitiveWidth = request.primitiveWidth
            packed = request.packed
            opaque = request.opaque
            addressSpace = request.addressSpace
            arrayLength = request.arrayLength
            scalable = request.scalable
            variadic = request.variadic
            definitionText = request.definitionText
            elementTypeRef = request.elementTypeRefId?.let(support::typeRef)
            returnTypeRef = request.returnTypeRefId?.let(support::typeRef)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteCheck(id: String): LlvmDeleteCheckResultDto {
        val type = support.typeOrThrow(id)
        val blockers = mutableListOf<String>()
        support.listTypes(type.moduleId).filter { it.elementTypeRefId == id || it.returnTypeRefId == id }.forEach {
            blockers += "type '${it.symbol}' references this type"
        }
        support.listTypeMembers().filter { it.memberTypeRefId == id }.forEach {
            blockers += "type member '${it.name}' references this type"
        }
        support.listGlobals(type.moduleId).filter { it.typeRefId == id }.forEach {
            blockers += "global '${it.symbol}' references this type"
        }
        support.listFunctions(type.moduleId).filter { it.returnTypeRefId == id }.forEach {
            blockers += "function '${it.symbol}' returns this type"
        }
        support.listParams().filter { it.typeRefId == id }.forEach {
            blockers += "param '${it.name}' references this type"
        }
        support.listConstants(type.moduleId).filter { it.typeRefId == id }.forEach {
            blockers += "constant '${it.name}' references this type"
        }
        support.listOperands().filter { it.referencedTypeId == id }.forEach {
            blockers += "operand '${it.id}' references this type"
        }
        support.listMetadataFields().filter { it.referencedTypeId == id }.forEach {
            blockers += "metadata field '${it.id}' references this type"
        }
        return LlvmDeleteCheckResultDto(
            id = id,
            kind = "type",
            deletable = blockers.isEmpty(),
            blockers = blockers,
            message = if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references",
        )
    }

    override suspend fun validate(id: String): List<LlvmValidationIssueDto> {
        val type = support.typeOrThrow(id)
        val issues = mutableListOf<LlvmValidationIssueDto>()
        if ((type.kind == LlvmTypeKind.ARRAY.name || type.kind == LlvmTypeKind.VECTOR.name) && type.arrayLength == null && !type.scalable) {
            issues += LlvmValidationIssueDto(LlvmValidationSeverity.ERROR, "type:${type.id}", "array/vector type requires arrayLength or scalable flag")
        }
        if (type.kind == LlvmTypeKind.INTEGER.name && type.primitiveWidth == null) {
            issues += LlvmValidationIssueDto(LlvmValidationSeverity.ERROR, "type:${type.id}", "integer type requires primitiveWidth")
        }
        if (type.kind == LlvmTypeKind.STRUCT.name && !type.opaque && support.listTypeMembers(type.id).isEmpty()) {
            issues += LlvmValidationIssueDto(LlvmValidationSeverity.WARNING, "type:${type.id}", "non-opaque struct type has no members")
        }
        return issues
    }

    override suspend fun delete(id: String) {
        val check = deleteCheck(id)
        if (!check.deletable) {
            throw PlaygroundValidationException(check.message ?: "LLVM type cannot be deleted")
        }
        support.inTransaction {
            support.listTypeMembers(id).forEach { support.sqlClient.deleteById(LlvmTypeMember::class, it.id) }
            support.sqlClient.deleteById(LlvmType::class, id)
        }
    }

    override suspend fun createMember(request: CreateLlvmTypeMemberRequest): LlvmTypeMemberDto {
        support.typeOrThrow(request.typeId)
        requireText(request.name, "type member name")
        requireText(request.memberTypeText, "type member text")
        val now = support.now()
        val entity = new(LlvmTypeMember::class).by {
            id = support.newId()
            type = support.typeRef(request.typeId)
            name = request.name
            memberTypeText = request.memberTypeText
            memberTypeRef = request.memberTypeRefId?.let(support::typeRef)
            metadataJson = encodeStringMap(request.metadata)
            orderIndex = support.nextOrder(support.listTypeMembers(request.typeId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateMember(id: String, request: UpdateLlvmTypeMemberRequest): LlvmTypeMemberDto {
        val existing = support.typeMemberOrThrow(id)
        val entity = new(LlvmTypeMember::class).by {
            this.id = id
            type = support.typeRef(existing.typeId)
            name = request.name
            memberTypeText = request.memberTypeText
            memberTypeRef = request.memberTypeRefId?.let(support::typeRef)
            metadataJson = encodeStringMap(request.metadata)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteMember(id: String) {
        support.typeMemberOrThrow(id)
        support.sqlClient.deleteById(LlvmTypeMember::class, id)
    }

    override suspend fun reorderMembers(typeId: String, request: LlvmReorderRequestDto): List<LlvmTypeMemberDto> {
        val existing = support.listTypeMembers(typeId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Type member reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.typeMemberOrThrow(id)
                val entity = new(LlvmTypeMember::class).by {
                    this.id = item.id
                    type = support.typeRef(item.typeId)
                    name = item.name
                    memberTypeText = item.memberTypeText
                    memberTypeRef = item.memberTypeRefId?.let(support::typeRef)
                    metadataJson = item.metadataJson
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listTypeMembers(typeId).map { it.toDto() }
    }
}
