package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
enum class DeclarationKind {
    DATA_CLASS,
    ENUM_CLASS,
    INTERFACE,
    OBJECT,
    ANNOTATION_CLASS,
}

@Serializable
enum class CodeVisibility {
    PUBLIC,
    INTERNAL,
    PRIVATE,
}

@Serializable
enum class AnnotationOwnerType {
    FILE,
    DECLARATION,
    CONSTRUCTOR_PARAM,
    PROPERTY,
    FUNCTION,
}

@Serializable
enum class FunctionBodyMode {
    TEMPLATE,
    RAW_TEXT,
}

@Serializable
enum class ManagedArtifactSyncStatus {
    CLEAN,
    METADATA_DIRTY,
    SOURCE_DIRTY,
    CONFLICT,
    MISSING,
}

@Serializable
enum class ConflictReason {
    BOTH_CHANGED,
    PARSE_FAILED,
    UNSUPPORTED_SOURCE,
    FILE_NOT_MANAGED,
    MARKER_MISMATCH,
}

@Serializable
enum class SyncConflictResolution {
    METADATA_WINS,
    SOURCE_WINS,
}

@Serializable
enum class ValidationSeverity {
    INFO,
    WARNING,
    ERROR,
}

@Serializable
data class FunctionParameterDto(
    val name: String,
    val type: String,
    val nullable: Boolean = false,
    val defaultValue: String? = null,
)

@Serializable
data class CodegenProjectDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class GenerationTargetDto(
    val id: String,
    val projectId: String,
    val name: String,
    val rootDir: String,
    val sourceSet: String,
    val basePackage: String,
    val indexPackage: String,
    val kspEnabled: Boolean,
    val variables: Map<String, String> = emptyMap(),
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SourceFileMetaDto(
    val id: String,
    val targetId: String,
    val projectId: String,
    val packageName: String,
    val fileName: String,
    val docComment: String? = null,
    val orderIndex: Int,
    val relativePath: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class DeclarationMetaDto(
    val id: String,
    val fileId: String,
    val targetId: String,
    val packageName: String,
    val fqName: String,
    val name: String,
    val kind: DeclarationKind,
    val visibility: CodeVisibility,
    val modifiers: List<String> = emptyList(),
    val superTypes: List<String> = emptyList(),
    val docComment: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class ConstructorParamMetaDto(
    val id: String,
    val declarationId: String,
    val name: String,
    val type: String,
    val mutable: Boolean = false,
    val nullable: Boolean = false,
    val defaultValue: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PropertyMetaDto(
    val id: String,
    val declarationId: String,
    val name: String,
    val type: String,
    val mutable: Boolean = false,
    val nullable: Boolean = false,
    val initializer: String? = null,
    val visibility: CodeVisibility,
    val isOverride: Boolean = false,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class EnumEntryMetaDto(
    val id: String,
    val declarationId: String,
    val name: String,
    val arguments: List<String> = emptyList(),
    val bodyText: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class AnnotationUsageMetaDto(
    val id: String,
    val ownerType: AnnotationOwnerType,
    val ownerId: String,
    val annotationClassName: String,
    val useSiteTarget: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class AnnotationArgumentMetaDto(
    val id: String,
    val annotationUsageId: String,
    val name: String? = null,
    val value: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class ImportMetaDto(
    val id: String,
    val fileId: String,
    val importPath: String,
    val alias: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class FunctionStubMetaDto(
    val id: String,
    val declarationId: String,
    val name: String,
    val returnType: String,
    val visibility: CodeVisibility,
    val modifiers: List<String> = emptyList(),
    val parameters: List<FunctionParameterDto> = emptyList(),
    val bodyMode: FunctionBodyMode,
    val bodyText: String? = null,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class ManagedArtifactMetaDto(
    val id: String,
    val projectId: String,
    val targetId: String,
    val fileId: String,
    val declarationIds: List<String> = emptyList(),
    val absolutePath: String,
    val markerText: String,
    val metadataHash: String,
    val sourceHash: String? = null,
    val contentHash: String,
    val syncStatus: ManagedArtifactSyncStatus,
    val lastExportedAt: String? = null,
    val lastImportedAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SyncConflictMetaDto(
    val id: String,
    val projectId: String,
    val targetId: String,
    val fileId: String,
    val artifactId: String? = null,
    val reason: ConflictReason,
    val message: String,
    val metadataHash: String,
    val sourceHash: String? = null,
    val sourcePath: String? = null,
    val resolved: Boolean = false,
    val resolution: SyncConflictResolution? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SourceFileAggregateDto(
    val file: SourceFileMetaDto,
    val imports: List<ImportMetaDto> = emptyList(),
    val declarations: List<DeclarationMetaDto> = emptyList(),
    val constructorParams: List<ConstructorParamMetaDto> = emptyList(),
    val properties: List<PropertyMetaDto> = emptyList(),
    val enumEntries: List<EnumEntryMetaDto> = emptyList(),
    val annotations: List<AnnotationUsageMetaDto> = emptyList(),
    val annotationArguments: List<AnnotationArgumentMetaDto> = emptyList(),
    val functionStubs: List<FunctionStubMetaDto> = emptyList(),
    val artifacts: List<ManagedArtifactMetaDto> = emptyList(),
    val conflicts: List<SyncConflictMetaDto> = emptyList(),
)

@Serializable
data class CodegenProjectAggregateDto(
    val project: CodegenProjectDto,
    val targets: List<GenerationTargetDto> = emptyList(),
    val files: List<SourceFileMetaDto> = emptyList(),
    val declarations: List<DeclarationMetaDto> = emptyList(),
    val constructorParams: List<ConstructorParamMetaDto> = emptyList(),
    val properties: List<PropertyMetaDto> = emptyList(),
    val enumEntries: List<EnumEntryMetaDto> = emptyList(),
    val annotations: List<AnnotationUsageMetaDto> = emptyList(),
    val annotationArguments: List<AnnotationArgumentMetaDto> = emptyList(),
    val imports: List<ImportMetaDto> = emptyList(),
    val functionStubs: List<FunctionStubMetaDto> = emptyList(),
    val artifacts: List<ManagedArtifactMetaDto> = emptyList(),
    val conflicts: List<SyncConflictMetaDto> = emptyList(),
)
