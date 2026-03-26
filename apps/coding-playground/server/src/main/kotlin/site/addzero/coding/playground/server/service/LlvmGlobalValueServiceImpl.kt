package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.*
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.LlvmGlobalValueService

@Single
class LlvmGlobalValueServiceImpl(
    private val support: MetadataPersistenceSupport,
) : LlvmGlobalValueService {
    override suspend fun createGlobal(request: CreateLlvmGlobalVariableRequest): LlvmGlobalVariableDto {
        support.moduleOrThrow(request.moduleId)
        requireText(request.symbol, "global symbol")
        requireText(request.typeText, "global type")
        if (support.listGlobals(request.moduleId).any { it.symbol == request.symbol }) {
            throw PlaygroundValidationException("LLVM global symbol '${request.symbol}' already exists")
        }
        val now = support.now()
        val entity = new(LlvmGlobalVariable::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            symbol = request.symbol
            typeText = request.typeText
            typeRef = request.typeRefId?.let(support::typeRef)
            linkage = request.linkage.name
            visibility = request.visibility.name
            constant = request.constant
            threadLocal = request.threadLocal
            externallyInitialized = request.externallyInitialized
            initializerText = request.initializerText
            initializerConstant = request.initializerConstantId?.let(support::constantRef)
            sectionName = request.sectionName
            comdat = request.comdatId?.let(support::comdatRef)
            alignment = request.alignment
            addressSpace = request.addressSpace
            attributeGroupIdsJson = encodeStringList(request.attributeGroupIds)
            metadataJson = encodeStringMap(request.metadata)
            orderIndex = support.nextOrder(support.listGlobals(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun listGlobals(search: LlvmSearchRequest): List<LlvmGlobalVariableDto> {
        return support.listGlobals(search.moduleId)
            .map { it.toDto() }
            .filter {
                search.matches(
                    moduleId = it.moduleId,
                    symbol = it.symbol,
                    extras = listOf(it.name, it.typeText, it.sectionName),
                )
            }
            .filter { search.linkage == null || it.linkage == search.linkage }
    }

    override suspend fun getGlobal(id: String): LlvmGlobalVariableDto = support.globalOrThrow(id).toDto()

    override suspend fun updateGlobal(id: String, request: UpdateLlvmGlobalVariableRequest): LlvmGlobalVariableDto {
        val existing = support.globalOrThrow(id)
        if (support.listGlobals(existing.moduleId).any { it.id != id && it.symbol == request.symbol }) {
            throw PlaygroundValidationException("LLVM global symbol '${request.symbol}' already exists")
        }
        val entity = new(LlvmGlobalVariable::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            symbol = request.symbol
            typeText = request.typeText
            typeRef = request.typeRefId?.let(support::typeRef)
            linkage = request.linkage.name
            visibility = request.visibility.name
            constant = request.constant
            threadLocal = request.threadLocal
            externallyInitialized = request.externallyInitialized
            initializerText = request.initializerText
            initializerConstant = request.initializerConstantId?.let(support::constantRef)
            sectionName = request.sectionName
            comdat = request.comdatId?.let(support::comdatRef)
            alignment = request.alignment
            addressSpace = request.addressSpace
            attributeGroupIdsJson = encodeStringList(request.attributeGroupIds)
            metadataJson = encodeStringMap(request.metadata)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteGlobalCheck(id: String): LlvmDeleteCheckResultDto {
        val global = support.globalOrThrow(id)
        val blockers = mutableListOf<String>()
        support.listAliases(global.moduleId).filter { it.aliaseeGlobalId == id }.forEach {
            blockers += "alias '${it.symbol}' references this global"
        }
        support.listOperands().filter { it.referencedGlobalId == id }.forEach {
            blockers += "operand '${it.id}' references this global"
        }
        support.listMetadataAttachments(global.moduleId).filter { it.globalVariableId == id }.forEach {
            blockers += "metadata attachment '${it.id}' targets this global"
        }
        return LlvmDeleteCheckResultDto(id, "global", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun deleteGlobal(id: String) {
        val check = deleteGlobalCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM global cannot be deleted")
        support.sqlClient.deleteById(LlvmGlobalVariable::class, id)
    }

    override suspend fun createAlias(request: CreateLlvmAliasRequest): LlvmAliasDto {
        support.moduleOrThrow(request.moduleId)
        requireText(request.symbol, "alias symbol")
        val now = support.now()
        val entity = new(LlvmAlias::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            symbol = request.symbol
            aliaseeText = request.aliaseeText
            aliaseeGlobal = request.aliaseeGlobalId?.let(support::globalRef)
            linkage = request.linkage.name
            visibility = request.visibility.name
            orderIndex = support.nextOrder(support.listAliases(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateAlias(id: String, request: UpdateLlvmAliasRequest): LlvmAliasDto {
        val existing = support.aliasOrThrow(id)
        val entity = new(LlvmAlias::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            symbol = request.symbol
            aliaseeText = request.aliaseeText
            aliaseeGlobal = request.aliaseeGlobalId?.let(support::globalRef)
            linkage = request.linkage.name
            visibility = request.visibility.name
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteAliasCheck(id: String): LlvmDeleteCheckResultDto {
        val alias = support.aliasOrThrow(id)
        val blockers = support.listOperands().filter { it.text.contains(alias.symbol) }.map { "operand '${it.id}' references alias symbol '${alias.symbol}'" }
        return LlvmDeleteCheckResultDto(id, "alias", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun deleteAlias(id: String) {
        val check = deleteAliasCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM alias cannot be deleted")
        support.sqlClient.deleteById(LlvmAlias::class, id)
    }

    override suspend fun createIfunc(request: CreateLlvmIfuncRequest): LlvmIfuncDto {
        support.moduleOrThrow(request.moduleId)
        val now = support.now()
        val entity = new(LlvmIfunc::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            symbol = request.symbol
            resolverFunction = request.resolverFunctionId?.let(support::functionRef)
            resolverText = request.resolverText
            linkage = request.linkage.name
            visibility = request.visibility.name
            orderIndex = support.nextOrder(support.listIfuncs(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateIfunc(id: String, request: UpdateLlvmIfuncRequest): LlvmIfuncDto {
        val existing = support.ifuncOrThrow(id)
        val entity = new(LlvmIfunc::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            symbol = request.symbol
            resolverFunction = request.resolverFunctionId?.let(support::functionRef)
            resolverText = request.resolverText
            linkage = request.linkage.name
            visibility = request.visibility.name
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteIfuncCheck(id: String): LlvmDeleteCheckResultDto {
        val ifunc = support.ifuncOrThrow(id)
        val blockers = support.listOperands().filter { it.text.contains(ifunc.symbol) }.map { "operand '${it.id}' references ifunc symbol '${ifunc.symbol}'" }
        return LlvmDeleteCheckResultDto(id, "ifunc", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun deleteIfunc(id: String) {
        val check = deleteIfuncCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM ifunc cannot be deleted")
        support.sqlClient.deleteById(LlvmIfunc::class, id)
    }

    override suspend fun createComdat(request: CreateLlvmComdatRequest): LlvmComdatDto {
        support.moduleOrThrow(request.moduleId)
        val now = support.now()
        val entity = new(LlvmComdat::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            selectionKind = request.selectionKind
            orderIndex = support.nextOrder(support.listComdats(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateComdat(id: String, request: UpdateLlvmComdatRequest): LlvmComdatDto {
        val existing = support.comdatOrThrow(id)
        val entity = new(LlvmComdat::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            selectionKind = request.selectionKind
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteComdatCheck(id: String): LlvmDeleteCheckResultDto {
        val comdat = support.comdatOrThrow(id)
        val blockers = mutableListOf<String>()
        support.listGlobals(comdat.moduleId).filter { it.comdatId == id }.forEach { blockers += "global '${it.symbol}' uses this comdat" }
        support.listFunctions(comdat.moduleId).filter { it.comdatId == id }.forEach { blockers += "function '${it.symbol}' uses this comdat" }
        return LlvmDeleteCheckResultDto(id, "comdat", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun deleteComdat(id: String) {
        val check = deleteComdatCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM comdat cannot be deleted")
        support.sqlClient.deleteById(LlvmComdat::class, id)
    }

    override suspend fun createConstant(request: CreateLlvmConstantRequest): LlvmConstantDto {
        support.moduleOrThrow(request.moduleId)
        val now = support.now()
        val entity = new(LlvmConstant::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            kind = request.kind.name
            typeText = request.typeText
            typeRef = request.typeRefId?.let(support::typeRef)
            literalText = request.literalText
            expressionText = request.expressionText
            orderIndex = support.nextOrder(support.listConstants(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun listConstants(search: LlvmSearchRequest): List<LlvmConstantDto> {
        return support.listConstants(search.moduleId)
            .map { it.toDto() }
            .filter { search.matches(moduleId = it.moduleId, symbol = it.name, extras = listOf(it.typeText, it.literalText, it.expressionText)) }
    }

    override suspend fun getConstant(id: String): LlvmConstantDto = support.constantOrThrow(id).toDto()

    override suspend fun updateConstant(id: String, request: UpdateLlvmConstantRequest): LlvmConstantDto {
        val existing = support.constantOrThrow(id)
        val entity = new(LlvmConstant::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            kind = request.kind.name
            typeText = request.typeText
            typeRef = request.typeRefId?.let(support::typeRef)
            literalText = request.literalText
            expressionText = request.expressionText
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteConstantCheck(id: String): LlvmDeleteCheckResultDto {
        val constant = support.constantOrThrow(id)
        val blockers = mutableListOf<String>()
        support.listGlobals(constant.moduleId).filter { it.initializerConstantId == id }.forEach { blockers += "global '${it.symbol}' uses this constant as initializer" }
        support.listConstantItems().filter { it.valueConstantId == id }.forEach { blockers += "constant item '${it.id}' references this constant" }
        support.listOperands().filter { it.referencedConstantId == id }.forEach { blockers += "operand '${it.id}' references this constant" }
        support.listMetadataFields().filter { it.referencedConstantId == id }.forEach { blockers += "metadata field '${it.id}' references this constant" }
        return LlvmDeleteCheckResultDto(id, "constant", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun deleteConstant(id: String) {
        val check = deleteConstantCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM constant cannot be deleted")
        support.inTransaction {
            support.listConstantItems(id).forEach { support.sqlClient.deleteById(LlvmConstantItem::class, it.id) }
            support.sqlClient.deleteById(LlvmConstant::class, id)
        }
    }

    override suspend fun createConstantItem(request: CreateLlvmConstantItemRequest): LlvmConstantItemDto {
        support.constantOrThrow(request.constantId)
        val now = support.now()
        val entity = new(LlvmConstantItem::class).by {
            id = support.newId()
            constant = support.constantRef(request.constantId)
            valueText = request.valueText
            valueConstant = request.valueConstantId?.let(support::constantRef)
            valueTypeRef = request.valueTypeRefId?.let(support::typeRef)
            orderIndex = support.nextOrder(support.listConstantItems(request.constantId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateConstantItem(id: String, request: UpdateLlvmConstantItemRequest): LlvmConstantItemDto {
        val existing = support.constantItemOrThrow(id)
        val entity = new(LlvmConstantItem::class).by {
            this.id = id
            constant = support.constantRef(existing.constantId)
            valueText = request.valueText
            valueConstant = request.valueConstantId?.let(support::constantRef)
            valueTypeRef = request.valueTypeRefId?.let(support::typeRef)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteConstantItem(id: String) {
        support.constantItemOrThrow(id)
        support.sqlClient.deleteById(LlvmConstantItem::class, id)
    }

    override suspend fun reorderConstantItems(constantId: String, request: LlvmReorderRequestDto): List<LlvmConstantItemDto> {
        val existing = support.listConstantItems(constantId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Constant item reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.constantItemOrThrow(id)
                val entity = new(LlvmConstantItem::class).by {
                    this.id = item.id
                    constant = support.constantRef(item.constantId)
                    valueText = item.valueText
                    valueConstant = item.valueConstantId?.let(support::constantRef)
                    valueTypeRef = item.valueTypeRefId?.let(support::typeRef)
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listConstantItems(constantId).map { it.toDto() }
    }

    override suspend fun createInlineAsm(request: CreateLlvmInlineAsmRequest): LlvmInlineAsmDto {
        support.moduleOrThrow(request.moduleId)
        val now = support.now()
        val entity = new(LlvmInlineAsm::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            asmText = request.asmText
            constraints = request.constraints
            sideEffects = request.sideEffects
            alignStack = request.alignStack
            dialect = request.dialect
            orderIndex = support.nextOrder(support.listInlineAsms(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateInlineAsm(id: String, request: UpdateLlvmInlineAsmRequest): LlvmInlineAsmDto {
        val existing = support.inlineAsmOrThrow(id)
        val entity = new(LlvmInlineAsm::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            asmText = request.asmText
            constraints = request.constraints
            sideEffects = request.sideEffects
            alignStack = request.alignStack
            dialect = request.dialect
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteInlineAsmCheck(id: String): LlvmDeleteCheckResultDto {
        val asm = support.inlineAsmOrThrow(id)
        val blockers = support.listOperands().filter { it.referencedInlineAsmId == id }.map { "operand '${it.id}' references inline asm '${asm.name}'" }
        return LlvmDeleteCheckResultDto(id, "inline-asm", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun deleteInlineAsm(id: String) {
        val check = deleteInlineAsmCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM inline asm cannot be deleted")
        support.sqlClient.deleteById(LlvmInlineAsm::class, id)
    }
}
