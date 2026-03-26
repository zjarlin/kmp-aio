package site.addzero.coding.playground.server.entity

import site.addzero.coding.playground.shared.dto.*
import java.time.LocalDateTime

private fun LocalDateTime.asWire(): String = toString()

private inline fun <reified T : Enum<T>> enumValueOfOrDefault(value: String, default: T): T {
    return runCatching { enumValueOf<T>(value) }.getOrDefault(default)
}

fun LlvmModule.toDto(): LlvmModuleDto = LlvmModuleDto(
    id = id,
    name = name,
    sourceFilename = sourceFilename,
    targetTriple = targetTriple,
    dataLayout = dataLayout,
    moduleAsm = moduleAsm,
    moduleFlags = decodeStringMap(moduleFlagsJson),
    description = description,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmType.toDto(): LlvmTypeDto = LlvmTypeDto(
    id = id,
    moduleId = moduleId,
    name = name,
    symbol = symbol,
    kind = enumValueOfOrDefault(kind, LlvmTypeKind.OPAQUE),
    primitiveWidth = primitiveWidth,
    packed = packed,
    opaque = opaque,
    addressSpace = addressSpace,
    arrayLength = arrayLength,
    scalable = scalable,
    variadic = variadic,
    definitionText = definitionText,
    elementTypeRefId = elementTypeRefId,
    returnTypeRefId = returnTypeRefId,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmTypeMember.toDto(): LlvmTypeMemberDto = LlvmTypeMemberDto(
    id = id,
    typeId = typeId,
    name = name,
    memberTypeText = memberTypeText,
    memberTypeRefId = memberTypeRefId,
    orderIndex = orderIndex,
    metadata = decodeStringMap(metadataJson),
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmComdat.toDto(): LlvmComdatDto = LlvmComdatDto(
    id = id,
    moduleId = moduleId,
    name = name,
    selectionKind = selectionKind,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmAttributeGroup.toDto(): LlvmAttributeGroupDto = LlvmAttributeGroupDto(
    id = id,
    moduleId = moduleId,
    name = name,
    targetKind = targetKind,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmAttributeEntry.toDto(): LlvmAttributeEntryDto = LlvmAttributeEntryDto(
    id = id,
    attributeGroupId = attributeGroupId,
    key = key,
    value = value,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmGlobalVariable.toDto(): LlvmGlobalVariableDto = LlvmGlobalVariableDto(
    id = id,
    moduleId = moduleId,
    name = name,
    symbol = symbol,
    typeText = typeText,
    typeRefId = typeRefId,
    linkage = enumValueOfOrDefault(linkage, LlvmLinkage.EXTERNAL),
    visibility = enumValueOfOrDefault(visibility, LlvmVisibility.DEFAULT),
    constant = constant,
    threadLocal = threadLocal,
    externallyInitialized = externallyInitialized,
    initializerText = initializerText,
    initializerConstantId = initializerConstantId,
    sectionName = sectionName,
    comdatId = comdatId,
    alignment = alignment,
    addressSpace = addressSpace,
    attributeGroupIds = decodeStringList(attributeGroupIdsJson),
    metadata = decodeStringMap(metadataJson),
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmAlias.toDto(): LlvmAliasDto = LlvmAliasDto(
    id = id,
    moduleId = moduleId,
    name = name,
    symbol = symbol,
    aliaseeText = aliaseeText,
    aliaseeGlobalId = aliaseeGlobalId,
    linkage = enumValueOfOrDefault(linkage, LlvmLinkage.EXTERNAL),
    visibility = enumValueOfOrDefault(visibility, LlvmVisibility.DEFAULT),
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmIfunc.toDto(): LlvmIfuncDto = LlvmIfuncDto(
    id = id,
    moduleId = moduleId,
    name = name,
    symbol = symbol,
    resolverFunctionId = resolverFunctionId,
    resolverText = resolverText,
    linkage = enumValueOfOrDefault(linkage, LlvmLinkage.EXTERNAL),
    visibility = enumValueOfOrDefault(visibility, LlvmVisibility.DEFAULT),
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmInlineAsm.toDto(): LlvmInlineAsmDto = LlvmInlineAsmDto(
    id = id,
    moduleId = moduleId,
    name = name,
    asmText = asmText,
    constraints = constraints,
    sideEffects = sideEffects,
    alignStack = alignStack,
    dialect = dialect,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmConstant.toDto(): LlvmConstantDto = LlvmConstantDto(
    id = id,
    moduleId = moduleId,
    name = name,
    kind = enumValueOfOrDefault(kind, LlvmConstantKind.SCALAR),
    typeText = typeText,
    typeRefId = typeRefId,
    literalText = literalText,
    expressionText = expressionText,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmConstantItem.toDto(): LlvmConstantItemDto = LlvmConstantItemDto(
    id = id,
    constantId = constantId,
    valueText = valueText,
    valueConstantId = valueConstantId,
    valueTypeRefId = valueTypeRefId,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmFunction.toDto(): LlvmFunctionDto = LlvmFunctionDto(
    id = id,
    moduleId = moduleId,
    name = name,
    symbol = symbol,
    returnTypeText = returnTypeText,
    returnTypeRefId = returnTypeRefId,
    linkage = enumValueOfOrDefault(linkage, LlvmLinkage.EXTERNAL),
    visibility = enumValueOfOrDefault(visibility, LlvmVisibility.DEFAULT),
    callingConvention = enumValueOfOrDefault(callingConvention, LlvmCallingConvention.C),
    variadic = variadic,
    declarationOnly = declarationOnly,
    gcName = gcName,
    personalityText = personalityText,
    comdatId = comdatId,
    sectionName = sectionName,
    attributeGroupIds = decodeStringList(attributeGroupIdsJson),
    metadata = decodeStringMap(metadataJson),
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmFunctionParam.toDto(): LlvmFunctionParamDto = LlvmFunctionParamDto(
    id = id,
    functionId = functionId,
    name = name,
    typeText = typeText,
    typeRefId = typeRefId,
    attributes = decodeStringList(attributesJson),
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmBasicBlock.toDto(): LlvmBasicBlockDto = LlvmBasicBlockDto(
    id = id,
    functionId = functionId,
    name = name,
    label = label,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmInstruction.toDto(): LlvmInstructionDto = LlvmInstructionDto(
    id = id,
    blockId = blockId,
    opcode = enumValueOfOrDefault(opcode, LlvmInstructionOpcode.CALL),
    resultSymbol = resultSymbol,
    typeText = typeText,
    typeRefId = typeRefId,
    textSuffix = textSuffix,
    flags = decodeStringMap(flagsJson),
    terminator = terminator,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmOperand.toDto(): LlvmOperandDto = LlvmOperandDto(
    id = id,
    instructionId = instructionId,
    kind = enumValueOfOrDefault(kind, LlvmOperandKind.LITERAL),
    text = text,
    referencedInstructionId = referencedInstructionId,
    referencedFunctionId = referencedFunctionId,
    referencedParamId = referencedParamId,
    referencedGlobalId = referencedGlobalId,
    referencedConstantId = referencedConstantId,
    referencedBlockId = referencedBlockId,
    referencedMetadataNodeId = referencedMetadataNodeId,
    referencedTypeId = referencedTypeId,
    referencedInlineAsmId = referencedInlineAsmId,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmPhiIncoming.toDto(): LlvmPhiIncomingDto = LlvmPhiIncomingDto(
    id = id,
    instructionId = instructionId,
    valueText = valueText,
    valueOperandId = valueOperandId,
    incomingBlockId = incomingBlockId,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmInstructionClause.toDto(): LlvmInstructionClauseDto = LlvmInstructionClauseDto(
    id = id,
    instructionId = instructionId,
    clauseKind = clauseKind,
    clauseText = clauseText,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmOperandBundle.toDto(): LlvmOperandBundleDto = LlvmOperandBundleDto(
    id = id,
    instructionId = instructionId,
    tag = tag,
    values = decodeStringList(valuesJson),
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmNamedMetadata.toDto(): LlvmNamedMetadataDto = LlvmNamedMetadataDto(
    id = id,
    moduleId = moduleId,
    name = name,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmMetadataNode.toDto(): LlvmMetadataNodeDto = LlvmMetadataNodeDto(
    id = id,
    moduleId = moduleId,
    name = name,
    kind = enumValueOfOrDefault(kind, LlvmMetadataKind.GENERIC),
    distinct = distinct,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmMetadataField.toDto(): LlvmMetadataFieldDto = LlvmMetadataFieldDto(
    id = id,
    metadataNodeId = metadataNodeId,
    namedMetadataId = namedMetadataId,
    valueKind = enumValueOfOrDefault(valueKind, LlvmMetadataValueKind.STRING),
    valueText = valueText,
    referencedNodeId = referencedNodeId,
    referencedConstantId = referencedConstantId,
    referencedTypeId = referencedTypeId,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmMetadataAttachment.toDto(): LlvmMetadataAttachmentDto = LlvmMetadataAttachmentDto(
    id = id,
    metadataNodeId = metadataNodeId,
    targetKind = enumValueOfOrDefault(targetKind, LlvmAttachmentTargetKind.INSTRUCTION),
    functionId = functionId,
    globalVariableId = globalVariableId,
    instructionId = instructionId,
    key = key,
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmCompileProfile.toDto(): LlvmCompileProfileDto = LlvmCompileProfileDto(
    id = id,
    moduleId = moduleId,
    name = name,
    targetPlatform = targetPlatform,
    outputDirectory = outputDirectory,
    optPath = optPath,
    optArgs = decodeStringList(optArgsJson),
    llcPath = llcPath,
    llcArgs = decodeStringList(llcArgsJson),
    clangPath = clangPath,
    clangArgs = decodeStringList(clangArgsJson),
    environment = decodeStringMap(environmentJson),
    orderIndex = orderIndex,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
)

fun LlvmCompileJob.toDto(): LlvmCompileJobDto = LlvmCompileJobDto(
    id = id,
    moduleId = moduleId,
    profileId = profileId,
    status = enumValueOfOrDefault(status, LlvmCompileJobStatus.PENDING),
    outputDirectory = outputDirectory,
    exportPath = exportPath,
    stdoutText = stdoutText,
    stderrText = stderrText,
    exitCode = exitCode,
    createdAt = createdAt.asWire(),
    updatedAt = updatedAt.asWire(),
    finishedAt = finishedAt?.asWire(),
)

fun LlvmCompileArtifact.toDto(): LlvmCompileArtifactDto = LlvmCompileArtifactDto(
    id = id,
    jobId = jobId,
    kind = enumValueOfOrDefault(kind, LlvmCompileArtifactKind.LLVM_IR),
    filePath = filePath,
    sizeBytes = sizeBytes,
    createdAt = createdAt.asWire(),
)
