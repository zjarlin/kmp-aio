package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.LlvmAttributeEntry
import site.addzero.coding.playground.server.entity.LlvmAttributeGroup
import site.addzero.coding.playground.server.entity.decodeStringList
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.LlvmAttributeService

@Single
class LlvmAttributeServiceImpl(
    private val support: MetadataPersistenceSupport,
) : LlvmAttributeService {
    override suspend fun createGroup(request: CreateLlvmAttributeGroupRequest): LlvmAttributeGroupDto {
        support.moduleOrThrow(request.moduleId)
        requireText(request.name, "attribute group name")
        requireText(request.targetKind, "attribute target kind")
        if (support.listAttributeGroups(request.moduleId).any { it.name == request.name }) {
            throw PlaygroundValidationException("LLVM attribute group '${request.name}' already exists")
        }
        val now = support.now()
        val entity = new(LlvmAttributeGroup::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            targetKind = request.targetKind
            orderIndex = support.nextOrder(support.listAttributeGroups(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun listGroups(search: LlvmSearchRequest): List<LlvmAttributeGroupDto> {
        return support.listAttributeGroups(search.moduleId)
            .map { it.toDto() }
            .filter { search.matches(moduleId = it.moduleId, symbol = it.name, extras = listOf(it.targetKind)) }
    }

    override suspend fun getGroup(id: String): LlvmAttributeGroupDto = support.attributeGroupOrThrow(id).toDto()

    override suspend fun updateGroup(id: String, request: UpdateLlvmAttributeGroupRequest): LlvmAttributeGroupDto {
        val existing = support.attributeGroupOrThrow(id)
        requireText(request.name, "attribute group name")
        requireText(request.targetKind, "attribute target kind")
        if (support.listAttributeGroups(existing.moduleId).any { it.id != id && it.name == request.name }) {
            throw PlaygroundValidationException("LLVM attribute group '${request.name}' already exists")
        }
        val entity = new(LlvmAttributeGroup::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            targetKind = request.targetKind
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteGroupCheck(id: String): LlvmDeleteCheckResultDto {
        val group = support.attributeGroupOrThrow(id)
        val blockers = mutableListOf<String>()
        support.listGlobals(group.moduleId).filter { decodeStringList(it.attributeGroupIdsJson).contains(id) }.forEach {
            blockers += "global '${it.symbol}' attaches this group"
        }
        support.listFunctions(group.moduleId).filter { decodeStringList(it.attributeGroupIdsJson).contains(id) }.forEach {
            blockers += "function '${it.symbol}' attaches this group"
        }
        return LlvmDeleteCheckResultDto(
            id = id,
            kind = "attribute-group",
            deletable = blockers.isEmpty(),
            blockers = blockers,
            message = if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references",
        )
    }

    override suspend fun deleteGroup(id: String) {
        val check = deleteGroupCheck(id)
        if (!check.deletable) {
            throw PlaygroundValidationException(check.message ?: "LLVM attribute group cannot be deleted")
        }
        support.inTransaction {
            support.listAttributeEntries(id).forEach { support.sqlClient.deleteById(LlvmAttributeEntry::class, it.id) }
            support.sqlClient.deleteById(LlvmAttributeGroup::class, id)
        }
    }

    override suspend fun createEntry(request: CreateLlvmAttributeEntryRequest): LlvmAttributeEntryDto {
        support.attributeGroupOrThrow(request.attributeGroupId)
        requireText(request.key, "attribute key")
        val now = support.now()
        val entity = new(LlvmAttributeEntry::class).by {
            id = support.newId()
            attributeGroup = support.attributeGroupRef(request.attributeGroupId)
            key = request.key
            value = request.value
            orderIndex = support.nextOrder(support.listAttributeEntries(request.attributeGroupId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateEntry(id: String, request: UpdateLlvmAttributeEntryRequest): LlvmAttributeEntryDto {
        val existing = support.attributeEntryOrThrow(id)
        val entity = new(LlvmAttributeEntry::class).by {
            this.id = id
            attributeGroup = support.attributeGroupRef(existing.attributeGroupId)
            key = request.key
            value = request.value
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteEntry(id: String) {
        support.attributeEntryOrThrow(id)
        support.sqlClient.deleteById(LlvmAttributeEntry::class, id)
    }

    override suspend fun reorderEntries(groupId: String, request: LlvmReorderRequestDto): List<LlvmAttributeEntryDto> {
        val existing = support.listAttributeEntries(groupId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Attribute reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.attributeEntryOrThrow(id)
                val entity = new(LlvmAttributeEntry::class).by {
                    this.id = item.id
                    attributeGroup = support.attributeGroupRef(item.attributeGroupId)
                    key = item.key
                    value = item.value
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listAttributeEntries(groupId).map { it.toDto() }
    }
}
