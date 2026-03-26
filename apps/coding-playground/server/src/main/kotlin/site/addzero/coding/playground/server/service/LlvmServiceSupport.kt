package site.addzero.coding.playground.server.service

import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.*
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.LlvmModuleAggregateDto
import site.addzero.coding.playground.shared.dto.LlvmSearchRequest
import java.time.LocalDateTime

internal fun requireText(value: String, label: String) {
    if (value.isBlank()) {
        throw PlaygroundValidationException("$label must not be blank")
    }
}

internal fun parseWireTime(value: String): LocalDateTime = LocalDateTime.parse(value)

internal fun LlvmSearchRequest.matches(
    moduleId: String? = null,
    symbol: String? = null,
    extras: List<String?> = emptyList(),
): Boolean {
    val symbolFilter = this.symbol
    val queryFilter = this.query
    val candidates = listOfNotNull(symbol, *extras.toTypedArray())
    if (this.moduleId != null && this.moduleId != moduleId) {
        return false
    }
    if (symbolFilter != null && candidates.none { candidate -> candidate.contains(symbolFilter, ignoreCase = true) }) {
        return false
    }
    if (queryFilter.isNullOrBlank()) {
        return true
    }
    return candidates.any { candidate -> candidate.contains(queryFilter, ignoreCase = true) }
}

internal fun MetadataPersistenceSupport.buildModuleAggregate(moduleId: String): LlvmModuleAggregateDto {
    val module = moduleOrThrow(moduleId).toDto()
    val types = listTypes(moduleId)
    val typeIds = types.map { it.id }.toSet()
    val typeMembers = listTypeMembers().filter { it.typeId in typeIds }
    val globals = listGlobals(moduleId)
    val aliases = listAliases(moduleId)
    val ifuncs = listIfuncs(moduleId)
    val comdats = listComdats(moduleId)
    val attributeGroups = listAttributeGroups(moduleId)
    val attributeGroupIds = attributeGroups.map { it.id }.toSet()
    val attributeEntries = listAttributeEntries().filter { it.attributeGroupId in attributeGroupIds }
    val constants = listConstants(moduleId)
    val constantIds = constants.map { it.id }.toSet()
    val constantItems = listConstantItems().filter { it.constantId in constantIds }
    val inlineAsms = listInlineAsms(moduleId)
    val functions = listFunctions(moduleId)
    val functionIds = functions.map { it.id }.toSet()
    val params = listParams().filter { it.functionId in functionIds }
    val blocks = listBlocks().filter { it.functionId in functionIds }
    val blockIds = blocks.map { it.id }.toSet()
    val instructions = listInstructions().filter { it.blockId in blockIds }
    val instructionIds = instructions.map { it.id }.toSet()
    val operands = listOperands().filter { it.instructionId in instructionIds }
    val phiIncomings = listPhiIncoming().filter { it.instructionId in instructionIds }
    val instructionClauses = listClauses().filter { it.instructionId in instructionIds }
    val operandBundles = listBundles().filter { it.instructionId in instructionIds }
    val namedMetadata = listNamedMetadata(moduleId)
    val namedMetadataIds = namedMetadata.map { it.id }.toSet()
    val metadataNodes = listMetadataNodes(moduleId)
    val metadataNodeIds = metadataNodes.map { it.id }.toSet()
    val metadataFields = listMetadataFields().filter {
        it.metadataNodeId in metadataNodeIds || it.namedMetadataId in namedMetadataIds
    }
    val metadataAttachments = listMetadataAttachments(moduleId)
    val compileProfiles = listCompileProfiles(moduleId)
    val compileJobs = listCompileJobs(moduleId)
    val compileJobIds = compileJobs.map { it.id }.toSet()
    val compileArtifacts = listCompileArtifacts().filter { it.jobId in compileJobIds }
    return LlvmModuleAggregateDto(
        module = module,
        types = types.map { it.toDto() },
        typeMembers = typeMembers.map { it.toDto() },
        globals = globals.map { it.toDto() },
        aliases = aliases.map { it.toDto() },
        ifuncs = ifuncs.map { it.toDto() },
        comdats = comdats.map { it.toDto() },
        attributeGroups = attributeGroups.map { it.toDto() },
        attributeEntries = attributeEntries.map { it.toDto() },
        constants = constants.map { it.toDto() },
        constantItems = constantItems.map { it.toDto() },
        inlineAsms = inlineAsms.map { it.toDto() },
        functions = functions.map { it.toDto() },
        params = params.map { it.toDto() },
        blocks = blocks.map { it.toDto() },
        instructions = instructions.map { it.toDto() },
        operands = operands.map { it.toDto() },
        phiIncomings = phiIncomings.map { it.toDto() },
        instructionClauses = instructionClauses.map { it.toDto() },
        operandBundles = operandBundles.map { it.toDto() },
        namedMetadata = namedMetadata.map { it.toDto() },
        metadataNodes = metadataNodes.map { it.toDto() },
        metadataFields = metadataFields.map { it.toDto() },
        metadataAttachments = metadataAttachments.map { it.toDto() },
        compileProfiles = compileProfiles.map { it.toDto() },
        compileJobs = compileJobs.map { it.toDto() },
        compileArtifacts = compileArtifacts.map { it.toDto() },
    )
}

internal fun MetadataPersistenceSupport.deleteModuleCascade(moduleId: String) {
    val types = listTypes(moduleId)
    val typeIds = types.map { it.id }.toSet()
    val attributeGroups = listAttributeGroups(moduleId)
    val attributeGroupIds = attributeGroups.map { it.id }.toSet()
    val constants = listConstants(moduleId)
    val constantIds = constants.map { it.id }.toSet()
    val functions = listFunctions(moduleId)
    val functionIds = functions.map { it.id }.toSet()
    val blocks = listBlocks().filter { it.functionId in functionIds }
    val blockIds = blocks.map { it.id }.toSet()
    val instructions = listInstructions().filter { it.blockId in blockIds }
    val instructionIds = instructions.map { it.id }.toSet()
    val namedMetadata = listNamedMetadata(moduleId)
    val namedMetadataIds = namedMetadata.map { it.id }.toSet()
    val metadataNodes = listMetadataNodes(moduleId)
    val metadataNodeIds = metadataNodes.map { it.id }.toSet()
    val compileJobs = listCompileJobs(moduleId)
    val compileJobIds = compileJobs.map { it.id }.toSet()

    listCompileArtifacts().filter { it.jobId in compileJobIds }.forEach { deleteEntity<LlvmCompileArtifact>(it.id) }
    compileJobs.forEach { deleteEntity<LlvmCompileJob>(it.id) }
    listCompileProfiles(moduleId).forEach { deleteEntity<LlvmCompileProfile>(it.id) }
    listMetadataAttachments(moduleId).forEach { deleteEntity<LlvmMetadataAttachment>(it.id) }
    listMetadataFields().filter { it.metadataNodeId in metadataNodeIds || it.namedMetadataId in namedMetadataIds }
        .forEach { deleteEntity<LlvmMetadataField>(it.id) }
    namedMetadata.forEach { deleteEntity<LlvmNamedMetadata>(it.id) }
    metadataNodes.forEach { deleteEntity<LlvmMetadataNode>(it.id) }
    listBundles().filter { it.instructionId in instructionIds }.forEach { deleteEntity<LlvmOperandBundle>(it.id) }
    listClauses().filter { it.instructionId in instructionIds }.forEach { deleteEntity<LlvmInstructionClause>(it.id) }
    listPhiIncoming().filter { it.instructionId in instructionIds }.forEach { deleteEntity<LlvmPhiIncoming>(it.id) }
    listOperands().filter { it.instructionId in instructionIds }.forEach { deleteEntity<LlvmOperand>(it.id) }
    instructions.forEach { deleteEntity<LlvmInstruction>(it.id) }
    blocks.forEach { deleteEntity<LlvmBasicBlock>(it.id) }
    listParams().filter { it.functionId in functionIds }.forEach { deleteEntity<LlvmFunctionParam>(it.id) }
    functions.forEach { deleteEntity<LlvmFunction>(it.id) }
    listConstantItems().filter { it.constantId in constantIds }.forEach { deleteEntity<LlvmConstantItem>(it.id) }
    constants.forEach { deleteEntity<LlvmConstant>(it.id) }
    listInlineAsms(moduleId).forEach { deleteEntity<LlvmInlineAsm>(it.id) }
    listIfuncs(moduleId).forEach { deleteEntity<LlvmIfunc>(it.id) }
    listAliases(moduleId).forEach { deleteEntity<LlvmAlias>(it.id) }
    listGlobals(moduleId).forEach { deleteEntity<LlvmGlobalVariable>(it.id) }
    listAttributeEntries().filter { it.attributeGroupId in attributeGroupIds }.forEach { deleteEntity<LlvmAttributeEntry>(it.id) }
    attributeGroups.forEach { deleteEntity<LlvmAttributeGroup>(it.id) }
    listComdats(moduleId).forEach { deleteEntity<LlvmComdat>(it.id) }
    listTypeMembers().filter { it.typeId in typeIds }.forEach { deleteEntity<LlvmTypeMember>(it.id) }
    types.forEach { deleteEntity<LlvmType>(it.id) }
    sqlClient.deleteById(LlvmModule::class, moduleId)
}

private inline fun <reified E : Any> MetadataPersistenceSupport.deleteEntity(id: String) {
    sqlClient.deleteById(E::class, id)
}
