package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.*
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.LlvmMetadataService

@Single
class LlvmMetadataServiceImpl(
    private val support: MetadataPersistenceSupport,
) : LlvmMetadataService {
    override suspend fun createNamed(request: CreateLlvmNamedMetadataRequest): LlvmNamedMetadataDto {
        support.moduleOrThrow(request.moduleId)
        requireText(request.name, "named metadata name")
        val now = support.now()
        val entity = new(LlvmNamedMetadata::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            orderIndex = support.nextOrder(support.listNamedMetadata(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun listNamed(search: LlvmSearchRequest): List<LlvmNamedMetadataDto> {
        return support.listNamedMetadata(search.moduleId)
            .map { it.toDto() }
            .filter { search.matches(moduleId = it.moduleId, symbol = it.name) }
    }

    override suspend fun getNamed(id: String): LlvmNamedMetadataDto = support.namedMetadataOrThrow(id).toDto()

    override suspend fun updateNamed(id: String, request: UpdateLlvmNamedMetadataRequest): LlvmNamedMetadataDto {
        val existing = support.namedMetadataOrThrow(id)
        val entity = new(LlvmNamedMetadata::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteNamedCheck(id: String): LlvmDeleteCheckResultDto {
        val blockers = support.listMetadataFields(namedMetadataId = id).map { "metadata field '${it.id}' belongs to this named metadata" }
        return LlvmDeleteCheckResultDto(id, "named-metadata", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} children")
    }

    override suspend fun deleteNamed(id: String) {
        val check = deleteNamedCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM named metadata cannot be deleted")
        support.sqlClient.deleteById(LlvmNamedMetadata::class, id)
    }

    override suspend fun reorderNamed(moduleId: String, request: LlvmReorderRequestDto): List<LlvmNamedMetadataDto> {
        val existing = support.listNamedMetadata(moduleId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Named metadata reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.namedMetadataOrThrow(id)
                val entity = new(LlvmNamedMetadata::class).by {
                    this.id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listNamedMetadata(moduleId).map { it.toDto() }
    }

    override suspend fun createNode(request: CreateLlvmMetadataNodeRequest): LlvmMetadataNodeDto {
        support.moduleOrThrow(request.moduleId)
        val now = support.now()
        val entity = new(LlvmMetadataNode::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            kind = request.kind.name
            distinct = request.distinct
            orderIndex = support.nextOrder(support.listMetadataNodes(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun listNodes(search: LlvmSearchRequest): List<LlvmMetadataNodeDto> {
        return support.listMetadataNodes(search.moduleId)
            .map { it.toDto() }
            .filter {
                search.matches(moduleId = it.moduleId, symbol = it.name, extras = listOf(it.kind.name))
            }
            .filter { search.metadataKind == null || it.kind == search.metadataKind }
    }

    override suspend fun getNode(id: String): LlvmMetadataNodeDto = support.metadataNodeOrThrow(id).toDto()

    override suspend fun updateNode(id: String, request: UpdateLlvmMetadataNodeRequest): LlvmMetadataNodeDto {
        val existing = support.metadataNodeOrThrow(id)
        val entity = new(LlvmMetadataNode::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            kind = request.kind.name
            distinct = request.distinct
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteNodeCheck(id: String): LlvmDeleteCheckResultDto {
        val blockers = mutableListOf<String>()
        support.listMetadataFields().filter { it.referencedNodeId == id }.forEach { blockers += "metadata field '${it.id}' references this node" }
        support.listOperands().filter { it.referencedMetadataNodeId == id }.forEach { blockers += "operand '${it.id}' references this node" }
        support.listMetadataAttachments().filter { it.metadataNodeId == id }.forEach { blockers += "metadata attachment '${it.id}' uses this node" }
        support.listMetadataFields(metadataNodeId = id).forEach { blockers += "metadata field '${it.id}' belongs to this node" }
        return LlvmDeleteCheckResultDto(id, "metadata-node", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun deleteNode(id: String) {
        val check = deleteNodeCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM metadata node cannot be deleted")
        support.sqlClient.deleteById(LlvmMetadataNode::class, id)
    }

    override suspend fun createField(request: CreateLlvmMetadataFieldRequest): LlvmMetadataFieldDto {
        if (request.metadataNodeId == null && request.namedMetadataId == null) {
            throw PlaygroundValidationException("Metadata field must belong to a metadata node or named metadata")
        }
        val now = support.now()
        val entity = new(LlvmMetadataField::class).by {
            id = support.newId()
            metadataNode = request.metadataNodeId?.let(support::metadataNodeRef)
            namedMetadata = request.namedMetadataId?.let(support::namedMetadataRef)
            valueKind = request.valueKind.name
            valueText = request.valueText
            referencedNode = request.referencedNodeId?.let(support::metadataNodeRef)
            referencedConstant = request.referencedConstantId?.let(support::constantRef)
            referencedType = request.referencedTypeId?.let(support::typeRef)
            orderIndex = support.nextOrder(
                support.listMetadataFields(
                    metadataNodeId = request.metadataNodeId,
                    namedMetadataId = request.namedMetadataId,
                ),
            )
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateField(id: String, request: UpdateLlvmMetadataFieldRequest): LlvmMetadataFieldDto {
        val existing = support.metadataFieldOrThrow(id)
        val entity = new(LlvmMetadataField::class).by {
            this.id = id
            metadataNode = existing.metadataNodeId?.let(support::metadataNodeRef)
            namedMetadata = existing.namedMetadataId?.let(support::namedMetadataRef)
            valueKind = request.valueKind.name
            valueText = request.valueText
            referencedNode = request.referencedNodeId?.let(support::metadataNodeRef)
            referencedConstant = request.referencedConstantId?.let(support::constantRef)
            referencedType = request.referencedTypeId?.let(support::typeRef)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteField(id: String) {
        support.metadataFieldOrThrow(id)
        support.sqlClient.deleteById(LlvmMetadataField::class, id)
    }

    override suspend fun reorderNodeFields(metadataNodeId: String, request: LlvmReorderRequestDto): List<LlvmMetadataFieldDto> {
        return reorderFields(metadataNodeId = metadataNodeId, namedMetadataId = null, request = request)
    }

    override suspend fun reorderNamedFields(namedMetadataId: String, request: LlvmReorderRequestDto): List<LlvmMetadataFieldDto> {
        return reorderFields(metadataNodeId = null, namedMetadataId = namedMetadataId, request = request)
    }

    private fun reorderFields(
        metadataNodeId: String?,
        namedMetadataId: String?,
        request: LlvmReorderRequestDto,
    ): List<LlvmMetadataFieldDto> {
        val existing = support.listMetadataFields(metadataNodeId = metadataNodeId, namedMetadataId = namedMetadataId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Metadata field reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.metadataFieldOrThrow(id)
                val entity = new(LlvmMetadataField::class).by {
                    this.id = item.id
                    metadataNode = item.metadataNodeId?.let(support::metadataNodeRef)
                    namedMetadata = item.namedMetadataId?.let(support::namedMetadataRef)
                    valueKind = item.valueKind
                    valueText = item.valueText
                    referencedNode = item.referencedNodeId?.let(support::metadataNodeRef)
                    referencedConstant = item.referencedConstantId?.let(support::constantRef)
                    referencedType = item.referencedTypeId?.let(support::typeRef)
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listMetadataFields(metadataNodeId = metadataNodeId, namedMetadataId = namedMetadataId).map { it.toDto() }
    }

    override suspend fun createAttachment(request: CreateLlvmMetadataAttachmentRequest): LlvmMetadataAttachmentDto {
        val now = support.now()
        val entity = new(LlvmMetadataAttachment::class).by {
            id = support.newId()
            metadataNode = support.metadataNodeRef(request.metadataNodeId)
            targetKind = request.targetKind.name
            function = request.functionId?.let(support::functionRef)
            globalVariable = request.globalVariableId?.let(support::globalRef)
            instruction = request.instructionId?.let(support::instructionRef)
            key = request.key
            orderIndex = support.nextOrder(support.listMetadataAttachments())
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateAttachment(id: String, request: UpdateLlvmMetadataAttachmentRequest): LlvmMetadataAttachmentDto {
        val existing = support.metadataAttachmentOrThrow(id)
        val entity = new(LlvmMetadataAttachment::class).by {
            this.id = id
            metadataNode = support.metadataNodeRef(request.metadataNodeId)
            targetKind = request.targetKind.name
            function = request.functionId?.let(support::functionRef)
            globalVariable = request.globalVariableId?.let(support::globalRef)
            instruction = request.instructionId?.let(support::instructionRef)
            key = request.key
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteAttachment(id: String) {
        support.metadataAttachmentOrThrow(id)
        support.sqlClient.deleteById(LlvmMetadataAttachment::class, id)
    }
}
