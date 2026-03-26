package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.*
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.LlvmFunctionService

@Single
class LlvmFunctionServiceImpl(
    private val support: MetadataPersistenceSupport,
) : LlvmFunctionService {
    override suspend fun create(request: CreateLlvmFunctionRequest): LlvmFunctionDto {
        support.moduleOrThrow(request.moduleId)
        requireText(request.symbol, "function symbol")
        if (support.listFunctions(request.moduleId).any { it.symbol == request.symbol }) {
            throw PlaygroundValidationException("LLVM function symbol '${request.symbol}' already exists")
        }
        val now = support.now()
        val entity = new(LlvmFunction::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            symbol = request.symbol
            returnTypeText = request.returnTypeText
            returnTypeRef = request.returnTypeRefId?.let(support::typeRef)
            linkage = request.linkage.name
            visibility = request.visibility.name
            callingConvention = request.callingConvention.name
            variadic = request.variadic
            declarationOnly = request.declarationOnly
            gcName = request.gcName
            personalityText = request.personalityText
            comdat = request.comdatId?.let(support::comdatRef)
            sectionName = request.sectionName
            attributeGroupIdsJson = encodeStringList(request.attributeGroupIds)
            metadataJson = encodeStringMap(request.metadata)
            orderIndex = support.nextOrder(support.listFunctions(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun list(search: LlvmSearchRequest): List<LlvmFunctionDto> {
        return support.listFunctions(search.moduleId)
            .map { it.toDto() }
            .filter {
                search.matches(
                    moduleId = it.moduleId,
                    symbol = it.symbol,
                    extras = listOf(it.name, it.returnTypeText, it.callingConvention.name, it.sectionName),
                )
            }
            .filter { search.linkage == null || it.linkage == search.linkage }
    }

    override suspend fun get(id: String): LlvmFunctionDto = support.functionOrThrow(id).toDto()

    override suspend fun update(id: String, request: UpdateLlvmFunctionRequest): LlvmFunctionDto {
        val existing = support.functionOrThrow(id)
        if (support.listFunctions(existing.moduleId).any { it.id != id && it.symbol == request.symbol }) {
            throw PlaygroundValidationException("LLVM function symbol '${request.symbol}' already exists")
        }
        val entity = new(LlvmFunction::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            symbol = request.symbol
            returnTypeText = request.returnTypeText
            returnTypeRef = request.returnTypeRefId?.let(support::typeRef)
            linkage = request.linkage.name
            visibility = request.visibility.name
            callingConvention = request.callingConvention.name
            variadic = request.variadic
            declarationOnly = request.declarationOnly
            gcName = request.gcName
            personalityText = request.personalityText
            comdat = request.comdatId?.let(support::comdatRef)
            sectionName = request.sectionName
            attributeGroupIdsJson = encodeStringList(request.attributeGroupIds)
            metadataJson = encodeStringMap(request.metadata)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteCheck(id: String): LlvmDeleteCheckResultDto {
        val function = support.functionOrThrow(id)
        val blockers = mutableListOf<String>()
        support.listIfuncs(function.moduleId).filter { it.resolverFunctionId == id }.forEach {
            blockers += "ifunc '${it.symbol}' uses this function as resolver"
        }
        support.listOperands().filter { it.referencedFunctionId == id }.forEach {
            blockers += "operand '${it.id}' references this function"
        }
        support.listMetadataAttachments(function.moduleId).filter { it.functionId == id }.forEach {
            blockers += "metadata attachment '${it.id}' targets this function"
        }
        return LlvmDeleteCheckResultDto(id, "function", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun validate(id: String): List<LlvmValidationIssueDto> {
        val function = support.functionOrThrow(id)
        val issues = mutableListOf<LlvmValidationIssueDto>()
        val blocks = support.listBlocks(function.id)
        if (!function.declarationOnly && blocks.isEmpty()) {
            issues += LlvmValidationIssueDto(LlvmValidationSeverity.ERROR, "function:${id}", "function must contain at least one basic block")
        }
        blocks.forEach { block ->
            val instructions = support.listInstructions(block.id)
            if (instructions.isNotEmpty() && !instructions.last().terminator) {
                issues += LlvmValidationIssueDto(LlvmValidationSeverity.ERROR, "block:${block.id}", "last instruction must be a terminator")
            }
        }
        return issues
    }

    override suspend fun delete(id: String) {
        val check = deleteCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM function cannot be deleted")
        val function = support.functionOrThrow(id)
        support.inTransaction {
            support.listMetadataAttachments(function.moduleId).filter { it.functionId == id }.forEach {
                support.sqlClient.deleteById(LlvmMetadataAttachment::class, it.id)
            }
            support.listBlocks(id).forEach { deleteBlockInternal(it.id) }
            support.listParams(id).forEach { support.sqlClient.deleteById(LlvmFunctionParam::class, it.id) }
            support.sqlClient.deleteById(LlvmFunction::class, id)
        }
    }

    override suspend fun createParam(request: CreateLlvmFunctionParamRequest): LlvmFunctionParamDto {
        support.functionOrThrow(request.functionId)
        val now = support.now()
        val entity = new(LlvmFunctionParam::class).by {
            id = support.newId()
            function = support.functionRef(request.functionId)
            name = request.name
            typeText = request.typeText
            typeRef = request.typeRefId?.let(support::typeRef)
            attributesJson = encodeStringList(request.attributes)
            orderIndex = support.nextOrder(support.listParams(request.functionId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateParam(id: String, request: UpdateLlvmFunctionParamRequest): LlvmFunctionParamDto {
        val existing = support.paramOrThrow(id)
        val entity = new(LlvmFunctionParam::class).by {
            this.id = id
            function = support.functionRef(existing.functionId)
            name = request.name
            typeText = request.typeText
            typeRef = request.typeRefId?.let(support::typeRef)
            attributesJson = encodeStringList(request.attributes)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteParam(id: String) {
        if (support.listOperands().any { it.referencedParamId == id }) {
            throw PlaygroundValidationException("Cannot delete param '$id' because operands still reference it")
        }
        support.sqlClient.deleteById(LlvmFunctionParam::class, id)
    }

    override suspend fun reorderParams(functionId: String, request: LlvmReorderRequestDto): List<LlvmFunctionParamDto> {
        val existing = support.listParams(functionId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Function param reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.paramOrThrow(id)
                val entity = new(LlvmFunctionParam::class).by {
                    this.id = item.id
                    function = support.functionRef(item.functionId)
                    name = item.name
                    typeText = item.typeText
                    typeRef = item.typeRefId?.let(support::typeRef)
                    attributesJson = item.attributesJson
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listParams(functionId).map { it.toDto() }
    }

    override suspend fun createBlock(request: CreateLlvmBasicBlockRequest): LlvmBasicBlockDto {
        support.functionOrThrow(request.functionId)
        if (support.listBlocks(request.functionId).any { it.label == request.label }) {
            throw PlaygroundValidationException("Basic block label '${request.label}' already exists")
        }
        val now = support.now()
        val entity = new(LlvmBasicBlock::class).by {
            id = support.newId()
            function = support.functionRef(request.functionId)
            name = request.name
            label = request.label
            orderIndex = support.nextOrder(support.listBlocks(request.functionId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateBlock(id: String, request: UpdateLlvmBasicBlockRequest): LlvmBasicBlockDto {
        val existing = support.blockOrThrow(id)
        if (support.listBlocks(existing.functionId).any { it.id != id && it.label == request.label }) {
            throw PlaygroundValidationException("Basic block label '${request.label}' already exists")
        }
        val entity = new(LlvmBasicBlock::class).by {
            this.id = id
            function = support.functionRef(existing.functionId)
            name = request.name
            label = request.label
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteBlockCheck(id: String): LlvmDeleteCheckResultDto {
        val blockers = mutableListOf<String>()
        support.listOperands().filter { it.referencedBlockId == id }.forEach { blockers += "operand '${it.id}' targets this block" }
        support.listPhiIncoming().filter { it.incomingBlockId == id }.forEach { blockers += "phi incoming '${it.id}' references this block" }
        return LlvmDeleteCheckResultDto(id, "basic-block", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun deleteBlock(id: String) {
        deleteBlockInternal(id)
    }

    override suspend fun reorderBlocks(functionId: String, request: LlvmReorderRequestDto): List<LlvmBasicBlockDto> {
        val existing = support.listBlocks(functionId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Basic block reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.blockOrThrow(id)
                val entity = new(LlvmBasicBlock::class).by {
                    this.id = item.id
                    function = support.functionRef(item.functionId)
                    name = item.name
                    label = item.label
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listBlocks(functionId).map { it.toDto() }
    }

    override suspend fun createInstruction(request: CreateLlvmInstructionRequest): LlvmInstructionDto {
        support.blockOrThrow(request.blockId)
        val now = support.now()
        val entity = new(LlvmInstruction::class).by {
            id = support.newId()
            block = support.blockRef(request.blockId)
            opcode = request.opcode.name
            resultSymbol = request.resultSymbol
            typeText = request.typeText
            typeRef = request.typeRefId?.let(support::typeRef)
            textSuffix = request.textSuffix
            flagsJson = encodeStringMap(request.flags)
            terminator = request.terminator
            orderIndex = support.nextOrder(support.listInstructions(request.blockId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateInstruction(id: String, request: UpdateLlvmInstructionRequest): LlvmInstructionDto {
        val existing = support.instructionOrThrow(id)
        val entity = new(LlvmInstruction::class).by {
            this.id = id
            block = support.blockRef(existing.blockId)
            opcode = request.opcode.name
            resultSymbol = request.resultSymbol
            typeText = request.typeText
            typeRef = request.typeRefId?.let(support::typeRef)
            textSuffix = request.textSuffix
            flagsJson = encodeStringMap(request.flags)
            terminator = request.terminator
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteInstructionCheck(id: String): LlvmDeleteCheckResultDto {
        val blockers = mutableListOf<String>()
        support.listOperands().filter { it.referencedInstructionId == id }.forEach { blockers += "operand '${it.id}' references this instruction" }
        support.listMetadataAttachments().filter { it.instructionId == id }.forEach { blockers += "metadata attachment '${it.id}' targets this instruction" }
        return LlvmDeleteCheckResultDto(id, "instruction", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} references")
    }

    override suspend fun deleteInstruction(id: String) {
        deleteInstructionInternal(id)
    }

    override suspend fun reorderInstructions(blockId: String, request: LlvmReorderRequestDto): List<LlvmInstructionDto> {
        val existing = support.listInstructions(blockId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Instruction reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.instructionOrThrow(id)
                val entity = new(LlvmInstruction::class).by {
                    this.id = item.id
                    block = support.blockRef(item.blockId)
                    opcode = item.opcode
                    resultSymbol = item.resultSymbol
                    typeText = item.typeText
                    typeRef = item.typeRefId?.let(support::typeRef)
                    textSuffix = item.textSuffix
                    flagsJson = item.flagsJson
                    terminator = item.terminator
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listInstructions(blockId).map { it.toDto() }
    }

    override suspend fun createOperand(request: CreateLlvmOperandRequest): LlvmOperandDto {
        support.instructionOrThrow(request.instructionId)
        val now = support.now()
        val entity = new(LlvmOperand::class).by {
            id = support.newId()
            instruction = support.instructionRef(request.instructionId)
            kind = request.kind.name
            text = request.text
            referencedInstruction = request.referencedInstructionId?.let(support::instructionRef)
            referencedFunction = request.referencedFunctionId?.let(support::functionRef)
            referencedParam = request.referencedParamId?.let(support::paramRef)
            referencedGlobal = request.referencedGlobalId?.let(support::globalRef)
            referencedConstant = request.referencedConstantId?.let(support::constantRef)
            referencedBlock = request.referencedBlockId?.let(support::blockRef)
            referencedMetadataNode = request.referencedMetadataNodeId?.let(support::metadataNodeRef)
            referencedType = request.referencedTypeId?.let(support::typeRef)
            referencedInlineAsm = request.referencedInlineAsmId?.let(support::inlineAsmRef)
            orderIndex = support.nextOrder(support.listOperands(request.instructionId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateOperand(id: String, request: UpdateLlvmOperandRequest): LlvmOperandDto {
        val existing = support.operandOrThrow(id)
        val entity = new(LlvmOperand::class).by {
            this.id = id
            instruction = support.instructionRef(existing.instructionId)
            kind = request.kind.name
            text = request.text
            referencedInstruction = request.referencedInstructionId?.let(support::instructionRef)
            referencedFunction = request.referencedFunctionId?.let(support::functionRef)
            referencedParam = request.referencedParamId?.let(support::paramRef)
            referencedGlobal = request.referencedGlobalId?.let(support::globalRef)
            referencedConstant = request.referencedConstantId?.let(support::constantRef)
            referencedBlock = request.referencedBlockId?.let(support::blockRef)
            referencedMetadataNode = request.referencedMetadataNodeId?.let(support::metadataNodeRef)
            referencedType = request.referencedTypeId?.let(support::typeRef)
            referencedInlineAsm = request.referencedInlineAsmId?.let(support::inlineAsmRef)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteOperand(id: String) {
        support.operandOrThrow(id)
        support.sqlClient.deleteById(LlvmOperand::class, id)
    }

    override suspend fun reorderOperands(instructionId: String, request: LlvmReorderRequestDto): List<LlvmOperandDto> {
        val existing = support.listOperands(instructionId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Operand reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.operandOrThrow(id)
                val entity = new(LlvmOperand::class).by {
                    this.id = item.id
                    instruction = support.instructionRef(item.instructionId)
                    kind = item.kind
                    text = item.text
                    referencedInstruction = item.referencedInstructionId?.let(support::instructionRef)
                    referencedFunction = item.referencedFunctionId?.let(support::functionRef)
                    referencedParam = item.referencedParamId?.let(support::paramRef)
                    referencedGlobal = item.referencedGlobalId?.let(support::globalRef)
                    referencedConstant = item.referencedConstantId?.let(support::constantRef)
                    referencedBlock = item.referencedBlockId?.let(support::blockRef)
                    referencedMetadataNode = item.referencedMetadataNodeId?.let(support::metadataNodeRef)
                    referencedType = item.referencedTypeId?.let(support::typeRef)
                    referencedInlineAsm = item.referencedInlineAsmId?.let(support::inlineAsmRef)
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listOperands(instructionId).map { it.toDto() }
    }

    override suspend fun createPhiIncoming(request: CreateLlvmPhiIncomingRequest): LlvmPhiIncomingDto {
        val now = support.now()
        val entity = new(LlvmPhiIncoming::class).by {
            id = support.newId()
            instruction = support.instructionRef(request.instructionId)
            valueText = request.valueText
            valueOperand = request.valueOperandId?.let(support::operandRef)
            incomingBlock = support.blockRef(request.incomingBlockId)
            orderIndex = support.nextOrder(support.listPhiIncoming(request.instructionId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updatePhiIncoming(id: String, request: UpdateLlvmPhiIncomingRequest): LlvmPhiIncomingDto {
        val existing = support.phiIncomingOrThrow(id)
        val entity = new(LlvmPhiIncoming::class).by {
            this.id = id
            instruction = support.instructionRef(existing.instructionId)
            valueText = request.valueText
            valueOperand = request.valueOperandId?.let(support::operandRef)
            incomingBlock = support.blockRef(request.incomingBlockId)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deletePhiIncoming(id: String) {
        support.phiIncomingOrThrow(id)
        support.sqlClient.deleteById(LlvmPhiIncoming::class, id)
    }

    override suspend fun reorderPhiIncoming(instructionId: String, request: LlvmReorderRequestDto): List<LlvmPhiIncomingDto> {
        val existing = support.listPhiIncoming(instructionId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Phi reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.phiIncomingOrThrow(id)
                val entity = new(LlvmPhiIncoming::class).by {
                    this.id = item.id
                    instruction = support.instructionRef(item.instructionId)
                    valueText = item.valueText
                    valueOperand = item.valueOperandId?.let(support::operandRef)
                    incomingBlock = support.blockRef(item.incomingBlockId)
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listPhiIncoming(instructionId).map { it.toDto() }
    }

    override suspend fun createClause(request: CreateLlvmInstructionClauseRequest): LlvmInstructionClauseDto {
        val now = support.now()
        val entity = new(LlvmInstructionClause::class).by {
            id = support.newId()
            instruction = support.instructionRef(request.instructionId)
            clauseKind = request.clauseKind
            clauseText = request.clauseText
            orderIndex = support.nextOrder(support.listClauses(request.instructionId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateClause(id: String, request: UpdateLlvmInstructionClauseRequest): LlvmInstructionClauseDto {
        val existing = support.clauseOrThrow(id)
        val entity = new(LlvmInstructionClause::class).by {
            this.id = id
            instruction = support.instructionRef(existing.instructionId)
            clauseKind = request.clauseKind
            clauseText = request.clauseText
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteClause(id: String) {
        support.clauseOrThrow(id)
        support.sqlClient.deleteById(LlvmInstructionClause::class, id)
    }

    override suspend fun reorderClauses(instructionId: String, request: LlvmReorderRequestDto): List<LlvmInstructionClauseDto> {
        val existing = support.listClauses(instructionId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Clause reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.clauseOrThrow(id)
                val entity = new(LlvmInstructionClause::class).by {
                    this.id = item.id
                    instruction = support.instructionRef(item.instructionId)
                    clauseKind = item.clauseKind
                    clauseText = item.clauseText
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listClauses(instructionId).map { it.toDto() }
    }

    override suspend fun createBundle(request: CreateLlvmOperandBundleRequest): LlvmOperandBundleDto {
        val now = support.now()
        val entity = new(LlvmOperandBundle::class).by {
            id = support.newId()
            instruction = support.instructionRef(request.instructionId)
            tag = request.tag
            valuesJson = encodeStringList(request.values)
            orderIndex = support.nextOrder(support.listBundles(request.instructionId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateBundle(id: String, request: UpdateLlvmOperandBundleRequest): LlvmOperandBundleDto {
        val existing = support.bundleOrThrow(id)
        val entity = new(LlvmOperandBundle::class).by {
            this.id = id
            instruction = support.instructionRef(existing.instructionId)
            tag = request.tag
            valuesJson = encodeStringList(request.values)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteBundle(id: String) {
        support.bundleOrThrow(id)
        support.sqlClient.deleteById(LlvmOperandBundle::class, id)
    }

    override suspend fun reorderBundles(instructionId: String, request: LlvmReorderRequestDto): List<LlvmOperandBundleDto> {
        val existing = support.listBundles(instructionId)
        if (existing.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("Bundle reorder payload must contain the same ids")
        }
        support.inTransaction {
            request.orderedIds.forEachIndexed { index, id ->
                val item = support.bundleOrThrow(id)
                val entity = new(LlvmOperandBundle::class).by {
                    this.id = item.id
                    instruction = support.instructionRef(item.instructionId)
                    tag = item.tag
                    valuesJson = item.valuesJson
                    orderIndex = index
                    createdAt = item.createdAt
                    updatedAt = support.now()
                }
                support.sqlClient.save(entity)
            }
        }
        return support.listBundles(instructionId).map { it.toDto() }
    }

    private fun deleteBlockInternal(id: String) {
        val blockers = mutableListOf<String>()
        support.listOperands().filter { it.referencedBlockId == id }.forEach { blockers += "operand '${it.id}' targets this block" }
        support.listPhiIncoming().filter { it.incomingBlockId == id }.forEach { blockers += "phi incoming '${it.id}' references this block" }
        if (blockers.isNotEmpty()) {
            throw PlaygroundValidationException("LLVM basic block cannot be deleted: ${blockers.joinToString("; ")}")
        }
        support.inTransaction {
            support.listInstructions(id).forEach { deleteInstructionInternal(it.id) }
            support.sqlClient.deleteById(LlvmBasicBlock::class, id)
        }
    }

    private fun deleteInstructionInternal(id: String) {
        val blockers = mutableListOf<String>()
        support.listOperands().filter { it.referencedInstructionId == id }.forEach { blockers += "operand '${it.id}' references this instruction" }
        support.listMetadataAttachments().filter { it.instructionId == id }.forEach { blockers += "metadata attachment '${it.id}' targets this instruction" }
        if (blockers.isNotEmpty()) {
            throw PlaygroundValidationException("LLVM instruction cannot be deleted: ${blockers.joinToString("; ")}")
        }
        support.inTransaction {
            support.listMetadataAttachments().filter { it.instructionId == id }.forEach { support.sqlClient.deleteById(LlvmMetadataAttachment::class, it.id) }
            support.listBundles(id).forEach { support.sqlClient.deleteById(LlvmOperandBundle::class, it.id) }
            support.listClauses(id).forEach { support.sqlClient.deleteById(LlvmInstructionClause::class, it.id) }
            support.listPhiIncoming(id).forEach { support.sqlClient.deleteById(LlvmPhiIncoming::class, it.id) }
            support.listOperands(id).forEach { support.sqlClient.deleteById(LlvmOperand::class, it.id) }
            support.sqlClient.deleteById(LlvmInstruction::class, id)
        }
    }
}
