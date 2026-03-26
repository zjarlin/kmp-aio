package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.entity.*
import site.addzero.coding.playground.shared.dto.LlvmSnapshotDto
import site.addzero.coding.playground.shared.dto.LlvmSnapshotImportResultDto
import site.addzero.coding.playground.shared.service.LlvmSnapshotService

@Single
class LlvmSnapshotServiceImpl(
    private val support: MetadataPersistenceSupport,
) : LlvmSnapshotService {
    override suspend fun exportModule(moduleId: String): LlvmSnapshotDto {
        val aggregate = support.buildModuleAggregate(moduleId)
        return LlvmSnapshotDto(
            modules = listOf(aggregate.module),
            types = aggregate.types,
            typeMembers = aggregate.typeMembers,
            globals = aggregate.globals,
            aliases = aggregate.aliases,
            ifuncs = aggregate.ifuncs,
            comdats = aggregate.comdats,
            attributeGroups = aggregate.attributeGroups,
            attributeEntries = aggregate.attributeEntries,
            constants = aggregate.constants,
            constantItems = aggregate.constantItems,
            inlineAsms = aggregate.inlineAsms,
            functions = aggregate.functions,
            params = aggregate.params,
            blocks = aggregate.blocks,
            instructions = aggregate.instructions,
            operands = aggregate.operands,
            phiIncomings = aggregate.phiIncomings,
            instructionClauses = aggregate.instructionClauses,
            operandBundles = aggregate.operandBundles,
            namedMetadata = aggregate.namedMetadata,
            metadataNodes = aggregate.metadataNodes,
            metadataFields = aggregate.metadataFields,
            metadataAttachments = aggregate.metadataAttachments,
            compileProfiles = aggregate.compileProfiles,
            compileJobs = aggregate.compileJobs,
            compileArtifacts = aggregate.compileArtifacts,
        )
    }

    override suspend fun importSnapshot(snapshot: LlvmSnapshotDto): LlvmSnapshotImportResultDto {
        support.inTransaction {
            snapshot.modules.forEach { item ->
                val entity = new(LlvmModule::class).by {
                    id = item.id
                    name = item.name
                    sourceFilename = item.sourceFilename
                    targetTriple = item.targetTriple
                    dataLayout = item.dataLayout
                    moduleAsm = item.moduleAsm
                    moduleFlagsJson = encodeStringMap(item.moduleFlags)
                    description = item.description
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.types.forEach { item ->
                val entity = new(LlvmType::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    symbol = item.symbol
                    kind = item.kind.name
                    primitiveWidth = item.primitiveWidth
                    packed = item.packed
                    opaque = item.opaque
                    addressSpace = item.addressSpace
                    arrayLength = item.arrayLength
                    scalable = item.scalable
                    variadic = item.variadic
                    definitionText = item.definitionText
                    elementTypeRef = item.elementTypeRefId?.let(support::typeRef)
                    returnTypeRef = item.returnTypeRefId?.let(support::typeRef)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.typeMembers.forEach { item ->
                val entity = new(LlvmTypeMember::class).by {
                    id = item.id
                    type = support.typeRef(item.typeId)
                    name = item.name
                    memberTypeText = item.memberTypeText
                    memberTypeRef = item.memberTypeRefId?.let(support::typeRef)
                    metadataJson = encodeStringMap(item.metadata)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.comdats.forEach { item ->
                val entity = new(LlvmComdat::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    selectionKind = item.selectionKind
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.attributeGroups.forEach { item ->
                val entity = new(LlvmAttributeGroup::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    targetKind = item.targetKind
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.attributeEntries.forEach { item ->
                val entity = new(LlvmAttributeEntry::class).by {
                    id = item.id
                    attributeGroup = support.attributeGroupRef(item.attributeGroupId)
                    key = item.key
                    value = item.value
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.constants.forEach { item ->
                val entity = new(LlvmConstant::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    kind = item.kind.name
                    typeText = item.typeText
                    typeRef = item.typeRefId?.let(support::typeRef)
                    literalText = item.literalText
                    expressionText = item.expressionText
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.constantItems.forEach { item ->
                val entity = new(LlvmConstantItem::class).by {
                    id = item.id
                    constant = support.constantRef(item.constantId)
                    valueText = item.valueText
                    valueConstant = item.valueConstantId?.let(support::constantRef)
                    valueTypeRef = item.valueTypeRefId?.let(support::typeRef)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.inlineAsms.forEach { item ->
                val entity = new(LlvmInlineAsm::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    asmText = item.asmText
                    constraints = item.constraints
                    sideEffects = item.sideEffects
                    alignStack = item.alignStack
                    dialect = item.dialect
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.globals.forEach { item ->
                val entity = new(LlvmGlobalVariable::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    symbol = item.symbol
                    typeText = item.typeText
                    typeRef = item.typeRefId?.let(support::typeRef)
                    linkage = item.linkage.name
                    visibility = item.visibility.name
                    constant = item.constant
                    threadLocal = item.threadLocal
                    externallyInitialized = item.externallyInitialized
                    initializerText = item.initializerText
                    initializerConstant = item.initializerConstantId?.let(support::constantRef)
                    sectionName = item.sectionName
                    comdat = item.comdatId?.let(support::comdatRef)
                    alignment = item.alignment
                    addressSpace = item.addressSpace
                    attributeGroupIdsJson = encodeStringList(item.attributeGroupIds)
                    metadataJson = encodeStringMap(item.metadata)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.functions.forEach { item ->
                val entity = new(LlvmFunction::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    symbol = item.symbol
                    returnTypeText = item.returnTypeText
                    returnTypeRef = item.returnTypeRefId?.let(support::typeRef)
                    linkage = item.linkage.name
                    visibility = item.visibility.name
                    callingConvention = item.callingConvention.name
                    variadic = item.variadic
                    declarationOnly = item.declarationOnly
                    gcName = item.gcName
                    personalityText = item.personalityText
                    comdat = item.comdatId?.let(support::comdatRef)
                    sectionName = item.sectionName
                    attributeGroupIdsJson = encodeStringList(item.attributeGroupIds)
                    metadataJson = encodeStringMap(item.metadata)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.aliases.forEach { item ->
                val entity = new(LlvmAlias::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    symbol = item.symbol
                    aliaseeText = item.aliaseeText
                    aliaseeGlobal = item.aliaseeGlobalId?.let(support::globalRef)
                    linkage = item.linkage.name
                    visibility = item.visibility.name
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.ifuncs.forEach { item ->
                val entity = new(LlvmIfunc::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    symbol = item.symbol
                    resolverFunction = item.resolverFunctionId?.let(support::functionRef)
                    resolverText = item.resolverText
                    linkage = item.linkage.name
                    visibility = item.visibility.name
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.params.forEach { item ->
                val entity = new(LlvmFunctionParam::class).by {
                    id = item.id
                    function = support.functionRef(item.functionId)
                    name = item.name
                    typeText = item.typeText
                    typeRef = item.typeRefId?.let(support::typeRef)
                    attributesJson = encodeStringList(item.attributes)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.blocks.forEach { item ->
                val entity = new(LlvmBasicBlock::class).by {
                    id = item.id
                    function = support.functionRef(item.functionId)
                    name = item.name
                    label = item.label
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.instructions.forEach { item ->
                val entity = new(LlvmInstruction::class).by {
                    id = item.id
                    block = support.blockRef(item.blockId)
                    opcode = item.opcode.name
                    resultSymbol = item.resultSymbol
                    typeText = item.typeText
                    typeRef = item.typeRefId?.let(support::typeRef)
                    textSuffix = item.textSuffix
                    flagsJson = encodeStringMap(item.flags)
                    terminator = item.terminator
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.operands.forEach { item ->
                val entity = new(LlvmOperand::class).by {
                    id = item.id
                    instruction = support.instructionRef(item.instructionId)
                    kind = item.kind.name
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
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.phiIncomings.forEach { item ->
                val entity = new(LlvmPhiIncoming::class).by {
                    id = item.id
                    instruction = support.instructionRef(item.instructionId)
                    valueText = item.valueText
                    valueOperand = item.valueOperandId?.let(support::operandRef)
                    incomingBlock = support.blockRef(item.incomingBlockId)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.instructionClauses.forEach { item ->
                val entity = new(LlvmInstructionClause::class).by {
                    id = item.id
                    instruction = support.instructionRef(item.instructionId)
                    clauseKind = item.clauseKind
                    clauseText = item.clauseText
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.operandBundles.forEach { item ->
                val entity = new(LlvmOperandBundle::class).by {
                    id = item.id
                    instruction = support.instructionRef(item.instructionId)
                    tag = item.tag
                    valuesJson = encodeStringList(item.values)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.namedMetadata.forEach { item ->
                val entity = new(LlvmNamedMetadata::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.metadataNodes.forEach { item ->
                val entity = new(LlvmMetadataNode::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    kind = item.kind.name
                    distinct = item.distinct
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.metadataFields.forEach { item ->
                val entity = new(LlvmMetadataField::class).by {
                    id = item.id
                    metadataNode = item.metadataNodeId?.let(support::metadataNodeRef)
                    namedMetadata = item.namedMetadataId?.let(support::namedMetadataRef)
                    valueKind = item.valueKind.name
                    valueText = item.valueText
                    referencedNode = item.referencedNodeId?.let(support::metadataNodeRef)
                    referencedConstant = item.referencedConstantId?.let(support::constantRef)
                    referencedType = item.referencedTypeId?.let(support::typeRef)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.metadataAttachments.forEach { item ->
                val entity = new(LlvmMetadataAttachment::class).by {
                    id = item.id
                    metadataNode = support.metadataNodeRef(item.metadataNodeId)
                    targetKind = item.targetKind.name
                    function = item.functionId?.let(support::functionRef)
                    globalVariable = item.globalVariableId?.let(support::globalRef)
                    instruction = item.instructionId?.let(support::instructionRef)
                    key = item.key
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.compileProfiles.forEach { item ->
                val entity = new(LlvmCompileProfile::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    name = item.name
                    targetPlatform = item.targetPlatform
                    outputDirectory = item.outputDirectory
                    optPath = item.optPath
                    optArgsJson = encodeStringList(item.optArgs)
                    llcPath = item.llcPath
                    llcArgsJson = encodeStringList(item.llcArgs)
                    clangPath = item.clangPath
                    clangArgsJson = encodeStringList(item.clangArgs)
                    environmentJson = encodeStringMap(item.environment)
                    orderIndex = item.orderIndex
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.compileJobs.forEach { item ->
                val entity = new(LlvmCompileJob::class).by {
                    id = item.id
                    module = support.moduleRef(item.moduleId)
                    profile = support.compileProfileRef(item.profileId)
                    status = item.status.name
                    outputDirectory = item.outputDirectory
                    exportPath = item.exportPath
                    stdoutText = item.stdoutText
                    stderrText = item.stderrText
                    exitCode = item.exitCode
                    finishedAt = item.finishedAt?.let(::parseWireTime)
                    createdAt = parseWireTime(item.createdAt)
                    updatedAt = parseWireTime(item.updatedAt)
                }
                support.sqlClient.save(entity)
            }
            snapshot.compileArtifacts.forEach { item ->
                val entity = new(LlvmCompileArtifact::class).by {
                    id = item.id
                    job = support.compileJobRef(item.jobId)
                    kind = item.kind.name
                    filePath = item.filePath
                    sizeBytes = item.sizeBytes
                    createdAt = parseWireTime(item.createdAt)
                }
                support.sqlClient.save(entity)
            }
        }
        val total = snapshot.modules.size +
            snapshot.types.size +
            snapshot.typeMembers.size +
            snapshot.globals.size +
            snapshot.aliases.size +
            snapshot.ifuncs.size +
            snapshot.comdats.size +
            snapshot.attributeGroups.size +
            snapshot.attributeEntries.size +
            snapshot.constants.size +
            snapshot.constantItems.size +
            snapshot.inlineAsms.size +
            snapshot.functions.size +
            snapshot.params.size +
            snapshot.blocks.size +
            snapshot.instructions.size +
            snapshot.operands.size +
            snapshot.phiIncomings.size +
            snapshot.instructionClauses.size +
            snapshot.operandBundles.size +
            snapshot.namedMetadata.size +
            snapshot.metadataNodes.size +
            snapshot.metadataFields.size +
            snapshot.metadataAttachments.size +
            snapshot.compileProfiles.size +
            snapshot.compileJobs.size +
            snapshot.compileArtifacts.size
        return LlvmSnapshotImportResultDto(
            importedModules = snapshot.modules.size,
            importedRecords = total,
        )
    }
}
