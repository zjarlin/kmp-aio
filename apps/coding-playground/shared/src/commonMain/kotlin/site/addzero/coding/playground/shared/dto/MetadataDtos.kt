package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
enum class LlvmTypeKind {
    VOID,
    INTEGER,
    FLOAT,
    DOUBLE,
    FP128,
    LABEL,
    TOKEN,
    METADATA,
    POINTER,
    ARRAY,
    VECTOR,
    SCALABLE_VECTOR,
    STRUCT,
    FUNCTION,
    OPAQUE,
}

@Serializable
enum class LlvmLinkage {
    EXTERNAL,
    INTERNAL,
    PRIVATE,
    AVAILABLE_EXTERNALLY,
    LINKONCE,
    WEAK,
    COMMON,
    APPENDING,
    EXTERN_WEAK,
    LINKONCE_ODR,
    WEAK_ODR,
}

@Serializable
enum class LlvmVisibility {
    DEFAULT,
    HIDDEN,
    PROTECTED,
}

@Serializable
enum class LlvmCallingConvention {
    C,
    FAST,
    COLD,
    GHC,
    SWIFT,
    PRESERVE_MOST,
    PRESERVE_ALL,
    ANYREG,
}

@Serializable
enum class LlvmInstructionOpcode {
    ALLOCA,
    LOAD,
    STORE,
    ADD,
    SUB,
    MUL,
    UDIV,
    SDIV,
    ICMP,
    FCMP,
    CALL,
    RET,
    BR,
    SWITCH,
    PHI,
    GETELEMENTPTR,
    BITCAST,
    TRUNC,
    ZEXT,
    SEXT,
    AND,
    OR,
    XOR,
    SHL,
    LSHR,
    ASHR,
    SELECT,
    INVOKE,
    LANDINGPAD,
    RESUME,
    ATOMICRMW,
    CMPXCHG,
    INDIRECTBR,
    UNREACHABLE,
}

@Serializable
enum class LlvmOperandKind {
    LOCAL,
    PARAM,
    GLOBAL,
    CONSTANT,
    BLOCK,
    METADATA,
    INLINE_ASM,
    TYPE,
    SYMBOL,
    LITERAL,
}

@Serializable
enum class LlvmConstantKind {
    SCALAR,
    ARRAY,
    STRUCT,
    VECTOR,
    NULL,
    UNDEF,
    POISON,
    ZERO_INITIALIZER,
    EXPR,
    STRING,
}

@Serializable
enum class LlvmMetadataKind {
    GENERIC,
    MODULE_FLAG,
    DEBUG_COMPILE_UNIT,
    DEBUG_FILE,
    DEBUG_SUBPROGRAM,
    DEBUG_LOCATION,
    RANGE,
    TBAA,
    LOOP,
    FPMATH,
}

@Serializable
enum class LlvmMetadataValueKind {
    STRING,
    CONSTANT,
    NODE_REF,
    TYPE_REF,
    SYMBOL_REF,
    BOOLEAN,
    NUMBER,
}

@Serializable
enum class LlvmAttachmentTargetKind {
    FUNCTION,
    GLOBAL,
    INSTRUCTION,
}

@Serializable
enum class LlvmValidationSeverity {
    INFO,
    WARNING,
    ERROR,
}

@Serializable
enum class LlvmCompileJobStatus {
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED,
}

@Serializable
enum class LlvmCompileArtifactKind {
    LLVM_IR,
    OPT_IR,
    BITCODE,
    OBJECT,
    BINARY,
    STDOUT,
    STDERR,
}

@Serializable
data class LlvmDeleteCheckResultDto(
    val id: String,
    val kind: String,
    val deletable: Boolean,
    val blockers: List<String> = emptyList(),
    val message: String? = null,
)

@Serializable
data class LlvmValidationIssueDto(
    val severity: LlvmValidationSeverity,
    val location: String,
    val message: String,
)

@Serializable
data class LlvmModuleDto(
    val id: String,
    val name: String,
    val sourceFilename: String,
    val targetTriple: String,
    val dataLayout: String,
    val moduleAsm: String? = null,
    val moduleFlags: Map<String, String> = emptyMap(),
    val description: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmTypeDto(
    val id: String,
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
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmTypeMemberDto(
    val id: String,
    val typeId: String,
    val name: String,
    val memberTypeText: String,
    val memberTypeRefId: String? = null,
    val orderIndex: Int,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmComdatDto(
    val id: String,
    val moduleId: String,
    val name: String,
    val selectionKind: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmAttributeGroupDto(
    val id: String,
    val moduleId: String,
    val name: String,
    val targetKind: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmAttributeEntryDto(
    val id: String,
    val attributeGroupId: String,
    val key: String,
    val value: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmGlobalVariableDto(
    val id: String,
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
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmAliasDto(
    val id: String,
    val moduleId: String,
    val name: String,
    val symbol: String,
    val aliaseeText: String,
    val aliaseeGlobalId: String? = null,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmIfuncDto(
    val id: String,
    val moduleId: String,
    val name: String,
    val symbol: String,
    val resolverFunctionId: String? = null,
    val resolverText: String,
    val linkage: LlvmLinkage = LlvmLinkage.EXTERNAL,
    val visibility: LlvmVisibility = LlvmVisibility.DEFAULT,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmInlineAsmDto(
    val id: String,
    val moduleId: String,
    val name: String,
    val asmText: String,
    val constraints: String,
    val sideEffects: Boolean = false,
    val alignStack: Boolean = false,
    val dialect: String = "att",
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmConstantDto(
    val id: String,
    val moduleId: String,
    val name: String,
    val kind: LlvmConstantKind,
    val typeText: String,
    val typeRefId: String? = null,
    val literalText: String? = null,
    val expressionText: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmConstantItemDto(
    val id: String,
    val constantId: String,
    val valueText: String,
    val valueConstantId: String? = null,
    val valueTypeRefId: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmFunctionDto(
    val id: String,
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
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmFunctionParamDto(
    val id: String,
    val functionId: String,
    val name: String,
    val typeText: String,
    val typeRefId: String? = null,
    val attributes: List<String> = emptyList(),
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmBasicBlockDto(
    val id: String,
    val functionId: String,
    val name: String,
    val label: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmInstructionDto(
    val id: String,
    val blockId: String,
    val opcode: LlvmInstructionOpcode,
    val resultSymbol: String? = null,
    val typeText: String? = null,
    val typeRefId: String? = null,
    val textSuffix: String? = null,
    val flags: Map<String, String> = emptyMap(),
    val terminator: Boolean = false,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmOperandDto(
    val id: String,
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
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmPhiIncomingDto(
    val id: String,
    val instructionId: String,
    val valueText: String,
    val valueOperandId: String? = null,
    val incomingBlockId: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmInstructionClauseDto(
    val id: String,
    val instructionId: String,
    val clauseKind: String,
    val clauseText: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmOperandBundleDto(
    val id: String,
    val instructionId: String,
    val tag: String,
    val values: List<String> = emptyList(),
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmNamedMetadataDto(
    val id: String,
    val moduleId: String,
    val name: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmMetadataNodeDto(
    val id: String,
    val moduleId: String,
    val name: String? = null,
    val kind: LlvmMetadataKind = LlvmMetadataKind.GENERIC,
    val distinct: Boolean = false,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmMetadataFieldDto(
    val id: String,
    val metadataNodeId: String? = null,
    val namedMetadataId: String? = null,
    val valueKind: LlvmMetadataValueKind,
    val valueText: String,
    val referencedNodeId: String? = null,
    val referencedConstantId: String? = null,
    val referencedTypeId: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmMetadataAttachmentDto(
    val id: String,
    val metadataNodeId: String,
    val targetKind: LlvmAttachmentTargetKind,
    val functionId: String? = null,
    val globalVariableId: String? = null,
    val instructionId: String? = null,
    val key: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmCompileProfileDto(
    val id: String,
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
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class LlvmCompileArtifactDto(
    val id: String,
    val jobId: String,
    val kind: LlvmCompileArtifactKind,
    val filePath: String,
    val sizeBytes: Long = 0,
    val createdAt: String,
)

@Serializable
data class LlvmCompileJobDto(
    val id: String,
    val moduleId: String,
    val profileId: String,
    val status: LlvmCompileJobStatus,
    val outputDirectory: String,
    val exportPath: String? = null,
    val stdoutText: String? = null,
    val stderrText: String? = null,
    val exitCode: Int? = null,
    val createdAt: String,
    val updatedAt: String,
    val finishedAt: String? = null,
)

@Serializable
data class LlvmLlExportDto(
    val moduleId: String,
    val moduleName: String,
    val content: String,
    val outputPath: String? = null,
)

@Serializable
data class LlvmModuleAggregateDto(
    val module: LlvmModuleDto,
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
