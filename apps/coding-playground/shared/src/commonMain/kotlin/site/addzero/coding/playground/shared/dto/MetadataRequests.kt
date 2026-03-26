package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class LlvmSearchRequest(
    val query: String? = null,
    val moduleId: String? = null,
    val symbol: String? = null,
    val opcode: LlvmInstructionOpcode? = null,
    val typeKind: LlvmTypeKind? = null,
    val linkage: LlvmLinkage? = null,
    val metadataKind: LlvmMetadataKind? = null,
)

@Serializable
data class LlvmReorderRequestDto(
    val orderedIds: List<String>,
)

@Serializable
data class CreateLlvmModuleRequest(
    val name: String,
    val sourceFilename: String,
    val targetTriple: String,
    val dataLayout: String,
    val moduleAsm: String? = null,
    val moduleFlags: Map<String, String> = emptyMap(),
    val description: String? = null,
)

@Serializable
data class UpdateLlvmModuleRequest(
    val name: String,
    val sourceFilename: String,
    val targetTriple: String,
    val dataLayout: String,
    val moduleAsm: String? = null,
    val moduleFlags: Map<String, String> = emptyMap(),
    val description: String? = null,
)

@Serializable
data class CreateLlvmTypeRequest(
    val moduleId: String,
    val name: String,
    val symbol: String,
    val kind: LlvmTypeKind,
    val primitiveWidth: Int? = null,
    val packed: Boolean = false,
    val opaque: Boolean = false,
    val addressSpace: Int? = null,
    val arrayLength: Int? = null,
    val scalable: Boolean = false,
    val variadic: Boolean = false,
    val definitionText: String? = null,
    val elementTypeRefId: String? = null,
    val returnTypeRefId: String? = null,
)

@Serializable
data class UpdateLlvmTypeRequest(
    val name: String,
    val symbol: String,
    val kind: LlvmTypeKind,
    val primitiveWidth: Int? = null,
    val packed: Boolean = false,
    val opaque: Boolean = false,
    val addressSpace: Int? = null,
    val arrayLength: Int? = null,
    val scalable: Boolean = false,
    val variadic: Boolean = false,
    val definitionText: String? = null,
    val elementTypeRefId: String? = null,
    val returnTypeRefId: String? = null,
)

@Serializable
data class CreateLlvmTypeMemberRequest(
    val typeId: String,
    val name: String,
    val memberTypeText: String,
    val memberTypeRefId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

@Serializable
data class UpdateLlvmTypeMemberRequest(
    val name: String,
    val memberTypeText: String,
    val memberTypeRefId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

@Serializable
data class CreateLlvmComdatRequest(
    val moduleId: String,
    val name: String,
    val selectionKind: String,
)

@Serializable
data class UpdateLlvmComdatRequest(
    val name: String,
    val selectionKind: String,
)

@Serializable
data class CreateLlvmAttributeGroupRequest(
    val moduleId: String,
    val name: String,
    val targetKind: String,
)

@Serializable
data class UpdateLlvmAttributeGroupRequest(
    val name: String,
    val targetKind: String,
)

@Serializable
data class CreateLlvmAttributeEntryRequest(
    val attributeGroupId: String,
    val key: String,
    val value: String? = null,
)

@Serializable
data class UpdateLlvmAttributeEntryRequest(
    val key: String,
    val value: String? = null,
)

@Serializable
data class CreateLlvmGlobalVariableRequest(
    val moduleId: String,
    val name: String,
    val symbol: String,
    val typeText: String,
    val typeRefId: String? = null,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
    val constant: Boolean = false,
    val threadLocal: Boolean = false,
    val externallyInitialized: Boolean = false,
    val initializerText: String? = null,
    val initializerConstantId: String? = null,
    val sectionName: String? = null,
    val comdatId: String? = null,
    val alignment: Int? = null,
    val addressSpace: Int? = null,
    val attributeGroupIds: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)

@Serializable
data class UpdateLlvmGlobalVariableRequest(
    val name: String,
    val symbol: String,
    val typeText: String,
    val typeRefId: String? = null,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
    val constant: Boolean = false,
    val threadLocal: Boolean = false,
    val externallyInitialized: Boolean = false,
    val initializerText: String? = null,
    val initializerConstantId: String? = null,
    val sectionName: String? = null,
    val comdatId: String? = null,
    val alignment: Int? = null,
    val addressSpace: Int? = null,
    val attributeGroupIds: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)

@Serializable
data class CreateLlvmAliasRequest(
    val moduleId: String,
    val name: String,
    val symbol: String,
    val aliaseeText: String,
    val aliaseeGlobalId: String? = null,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
)

@Serializable
data class UpdateLlvmAliasRequest(
    val name: String,
    val symbol: String,
    val aliaseeText: String,
    val aliaseeGlobalId: String? = null,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
)

@Serializable
data class CreateLlvmIfuncRequest(
    val moduleId: String,
    val name: String,
    val symbol: String,
    val resolverFunctionId: String? = null,
    val resolverText: String,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
)

@Serializable
data class UpdateLlvmIfuncRequest(
    val name: String,
    val symbol: String,
    val resolverFunctionId: String? = null,
    val resolverText: String,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
)

@Serializable
data class CreateLlvmInlineAsmRequest(
    val moduleId: String,
    val name: String,
    val asmText: String,
    val constraints: String,
    val sideEffects: Boolean = false,
    val alignStack: Boolean = false,
    val dialect: String = "att",
)

@Serializable
data class UpdateLlvmInlineAsmRequest(
    val name: String,
    val asmText: String,
    val constraints: String,
    val sideEffects: Boolean = false,
    val alignStack: Boolean = false,
    val dialect: String = "att",
)

@Serializable
data class CreateLlvmConstantRequest(
    val moduleId: String,
    val name: String,
    val kind: LlvmConstantKind,
    val typeText: String,
    val typeRefId: String? = null,
    val literalText: String? = null,
    val expressionText: String? = null,
)

@Serializable
data class UpdateLlvmConstantRequest(
    val name: String,
    val kind: LlvmConstantKind,
    val typeText: String,
    val typeRefId: String? = null,
    val literalText: String? = null,
    val expressionText: String? = null,
)

@Serializable
data class CreateLlvmConstantItemRequest(
    val constantId: String,
    val valueText: String,
    val valueConstantId: String? = null,
    val valueTypeRefId: String? = null,
)

@Serializable
data class UpdateLlvmConstantItemRequest(
    val valueText: String,
    val valueConstantId: String? = null,
    val valueTypeRefId: String? = null,
)

@Serializable
data class CreateLlvmFunctionRequest(
    val moduleId: String,
    val name: String,
    val symbol: String,
    val returnTypeText: String,
    val returnTypeRefId: String? = null,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
    val callingConvention: LlvmCallingConvention = LlvmCallingConvention.C,
    val variadic: Boolean = false,
    val declarationOnly: Boolean = false,
    val gcName: String? = null,
    val personalityText: String? = null,
    val comdatId: String? = null,
    val sectionName: String? = null,
    val attributeGroupIds: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)

@Serializable
data class UpdateLlvmFunctionRequest(
    val name: String,
    val symbol: String,
    val returnTypeText: String,
    val returnTypeRefId: String? = null,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
    val callingConvention: LlvmCallingConvention = LlvmCallingConvention.C,
    val variadic: Boolean = false,
    val declarationOnly: Boolean = false,
    val gcName: String? = null,
    val personalityText: String? = null,
    val comdatId: String? = null,
    val sectionName: String? = null,
    val attributeGroupIds: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)

@Serializable
data class CreateLlvmFunctionParamRequest(
    val functionId: String,
    val name: String,
    val typeText: String,
    val typeRefId: String? = null,
    val attributes: List<String> = emptyList(),
)

@Serializable
data class UpdateLlvmFunctionParamRequest(
    val name: String,
    val typeText: String,
    val typeRefId: String? = null,
    val attributes: List<String> = emptyList(),
)

@Serializable
data class CreateLlvmBasicBlockRequest(
    val functionId: String,
    val name: String,
    val label: String,
)

@Serializable
data class UpdateLlvmBasicBlockRequest(
    val name: String,
    val label: String,
)

@Serializable
data class CreateLlvmInstructionRequest(
    val blockId: String,
    val opcode: LlvmInstructionOpcode,
    val resultSymbol: String? = null,
    val typeText: String? = null,
    val typeRefId: String? = null,
    val textSuffix: String? = null,
    val flags: Map<String, String> = emptyMap(),
    val terminator: Boolean = false,
)

@Serializable
data class UpdateLlvmInstructionRequest(
    val opcode: LlvmInstructionOpcode,
    val resultSymbol: String? = null,
    val typeText: String? = null,
    val typeRefId: String? = null,
    val textSuffix: String? = null,
    val flags: Map<String, String> = emptyMap(),
    val terminator: Boolean = false,
)

@Serializable
data class CreateLlvmOperandRequest(
    val instructionId: String,
    val kind: LlvmOperandKind,
    val text: String,
    val referencedInstructionId: String? = null,
    val referencedFunctionId: String? = null,
    val referencedParamId: String? = null,
    val referencedGlobalId: String? = null,
    val referencedConstantId: String? = null,
    val referencedBlockId: String? = null,
    val referencedMetadataNodeId: String? = null,
    val referencedTypeId: String? = null,
    val referencedInlineAsmId: String? = null,
)

@Serializable
data class UpdateLlvmOperandRequest(
    val kind: LlvmOperandKind,
    val text: String,
    val referencedInstructionId: String? = null,
    val referencedFunctionId: String? = null,
    val referencedParamId: String? = null,
    val referencedGlobalId: String? = null,
    val referencedConstantId: String? = null,
    val referencedBlockId: String? = null,
    val referencedMetadataNodeId: String? = null,
    val referencedTypeId: String? = null,
    val referencedInlineAsmId: String? = null,
)

@Serializable
data class CreateLlvmPhiIncomingRequest(
    val instructionId: String,
    val valueText: String,
    val valueOperandId: String? = null,
    val incomingBlockId: String,
)

@Serializable
data class UpdateLlvmPhiIncomingRequest(
    val valueText: String,
    val valueOperandId: String? = null,
    val incomingBlockId: String,
)

@Serializable
data class CreateLlvmInstructionClauseRequest(
    val instructionId: String,
    val clauseKind: String,
    val clauseText: String,
)

@Serializable
data class UpdateLlvmInstructionClauseRequest(
    val clauseKind: String,
    val clauseText: String,
)

@Serializable
data class CreateLlvmOperandBundleRequest(
    val instructionId: String,
    val tag: String,
    val values: List<String> = emptyList(),
)

@Serializable
data class UpdateLlvmOperandBundleRequest(
    val tag: String,
    val values: List<String> = emptyList(),
)

@Serializable
data class CreateLlvmNamedMetadataRequest(
    val moduleId: String,
    val name: String,
)

@Serializable
data class UpdateLlvmNamedMetadataRequest(
    val name: String,
)

@Serializable
data class CreateLlvmMetadataNodeRequest(
    val moduleId: String,
    val name: String? = null,
    val kind: LlvmMetadataKind = LlvmMetadataKind.GENERIC,
    val distinct: Boolean = false,
)

@Serializable
data class UpdateLlvmMetadataNodeRequest(
    val name: String? = null,
    val kind: LlvmMetadataKind = LlvmMetadataKind.GENERIC,
    val distinct: Boolean = false,
)

@Serializable
data class CreateLlvmMetadataFieldRequest(
    val metadataNodeId: String? = null,
    val namedMetadataId: String? = null,
    val valueKind: LlvmMetadataValueKind,
    val valueText: String,
    val referencedNodeId: String? = null,
    val referencedConstantId: String? = null,
    val referencedTypeId: String? = null,
)

@Serializable
data class UpdateLlvmMetadataFieldRequest(
    val valueKind: LlvmMetadataValueKind,
    val valueText: String,
    val referencedNodeId: String? = null,
    val referencedConstantId: String? = null,
    val referencedTypeId: String? = null,
)

@Serializable
data class CreateLlvmMetadataAttachmentRequest(
    val metadataNodeId: String,
    val targetKind: LlvmAttachmentTargetKind,
    val functionId: String? = null,
    val globalVariableId: String? = null,
    val instructionId: String? = null,
    val key: String,
)

@Serializable
data class UpdateLlvmMetadataAttachmentRequest(
    val metadataNodeId: String,
    val targetKind: LlvmAttachmentTargetKind,
    val functionId: String? = null,
    val globalVariableId: String? = null,
    val instructionId: String? = null,
    val key: String,
)

@Serializable
data class CreateLlvmCompileProfileRequest(
    val moduleId: String,
    val name: String,
    val targetPlatform: String,
    val outputDirectory: String,
    val optPath: String? = null,
    val optArgs: List<String> = emptyList(),
    val llcPath: String? = null,
    val llcArgs: List<String> = emptyList(),
    val clangPath: String? = null,
    val clangArgs: List<String> = emptyList(),
    val environment: Map<String, String> = emptyMap(),
)

@Serializable
data class UpdateLlvmCompileProfileRequest(
    val name: String,
    val targetPlatform: String,
    val outputDirectory: String,
    val optPath: String? = null,
    val optArgs: List<String> = emptyList(),
    val llcPath: String? = null,
    val llcArgs: List<String> = emptyList(),
    val clangPath: String? = null,
    val clangArgs: List<String> = emptyList(),
    val environment: Map<String, String> = emptyMap(),
)

@Serializable
data class CreateLlvmCompileJobRequest(
    val moduleId: String,
    val profileId: String,
    val runNow: Boolean = true,
)
