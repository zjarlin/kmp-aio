package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class LlvmSnapshotDto(
    val version: Int = 1,
    val modules: List<LlvmModuleDto> = emptyList(),
    val types: List<LlvmTypeDto> = emptyList(),
    val typeMembers: List<LlvmTypeMemberDto> = emptyList(),
    val globals: List<LlvmGlobalVariableDto> = emptyList(),
    val aliases: List<LlvmAliasDto> = emptyList(),
    val ifuncs: List<LlvmIfuncDto> = emptyList(),
    val comdats: List<LlvmComdatDto> = emptyList(),
    val attributeGroups: List<LlvmAttributeGroupDto> = emptyList(),
    val attributeEntries: List<LlvmAttributeEntryDto> = emptyList(),
    val constants: List<LlvmConstantDto> = emptyList(),
    val constantItems: List<LlvmConstantItemDto> = emptyList(),
    val inlineAsms: List<LlvmInlineAsmDto> = emptyList(),
    val functions: List<LlvmFunctionDto> = emptyList(),
    val params: List<LlvmFunctionParamDto> = emptyList(),
    val blocks: List<LlvmBasicBlockDto> = emptyList(),
    val instructions: List<LlvmInstructionDto> = emptyList(),
    val operands: List<LlvmOperandDto> = emptyList(),
    val phiIncomings: List<LlvmPhiIncomingDto> = emptyList(),
    val instructionClauses: List<LlvmInstructionClauseDto> = emptyList(),
    val operandBundles: List<LlvmOperandBundleDto> = emptyList(),
    val namedMetadata: List<LlvmNamedMetadataDto> = emptyList(),
    val metadataNodes: List<LlvmMetadataNodeDto> = emptyList(),
    val metadataFields: List<LlvmMetadataFieldDto> = emptyList(),
    val metadataAttachments: List<LlvmMetadataAttachmentDto> = emptyList(),
    val compileProfiles: List<LlvmCompileProfileDto> = emptyList(),
    val compileJobs: List<LlvmCompileJobDto> = emptyList(),
    val compileArtifacts: List<LlvmCompileArtifactDto> = emptyList(),
)

@Serializable
data class LlvmSnapshotImportResultDto(
    val importedModules: Int,
    val importedRecords: Int,
)
