package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class CodegenSearchRequest(
    val query: String? = null,
    val projectId: String? = null,
    val targetId: String? = null,
    val fileId: String? = null,
    val kind: DeclarationKind? = null,
)

@Serializable
data class ReorderRequestDto(
    val orderedIds: List<String>,
)

@Serializable
data class CreateCodegenProjectRequest(
    val name: String,
    val description: String? = null,
)

@Serializable
data class UpdateCodegenProjectRequest(
    val name: String,
    val description: String? = null,
)

@Serializable
data class CreateGenerationTargetRequest(
    val projectId: String,
    val name: String,
    val rootDir: String,
    val sourceSet: String = "main",
    val basePackage: String,
    val indexPackage: String,
    val kspEnabled: Boolean = true,
    val variables: Map<String, String> = emptyMap(),
)

@Serializable
data class UpdateGenerationTargetRequest(
    val name: String,
    val rootDir: String,
    val sourceSet: String,
    val basePackage: String,
    val indexPackage: String,
    val kspEnabled: Boolean,
    val variables: Map<String, String> = emptyMap(),
)

@Serializable
data class CreateSourceFileRequest(
    val targetId: String,
    val packageName: String,
    val fileName: String,
    val docComment: String? = null,
)

@Serializable
data class UpdateSourceFileRequest(
    val packageName: String,
    val fileName: String,
    val docComment: String? = null,
)

@Serializable
data class CreateImportRequest(
    val fileId: String,
    val importPath: String,
    val alias: String? = null,
)

@Serializable
data class UpdateImportRequest(
    val importPath: String,
    val alias: String? = null,
)

@Serializable
data class CreateDeclarationRequest(
    val fileId: String,
    val name: String,
    val kind: DeclarationKind,
    val visibility: CodeVisibility = CodeVisibility.PUBLIC,
    val modifiers: List<String> = emptyList(),
    val superTypes: List<String> = emptyList(),
    val docComment: String? = null,
)

@Serializable
data class UpdateDeclarationRequest(
    val name: String,
    val visibility: CodeVisibility,
    val modifiers: List<String> = emptyList(),
    val superTypes: List<String> = emptyList(),
    val docComment: String? = null,
)

@Serializable
data class CreateConstructorParamRequest(
    val declarationId: String,
    val name: String,
    val type: String,
    val mutable: Boolean = false,
    val nullable: Boolean = false,
    val defaultValue: String? = null,
)

@Serializable
data class UpdateConstructorParamRequest(
    val name: String,
    val type: String,
    val mutable: Boolean = false,
    val nullable: Boolean = false,
    val defaultValue: String? = null,
)

@Serializable
data class CreatePropertyRequest(
    val declarationId: String,
    val name: String,
    val type: String,
    val mutable: Boolean = false,
    val nullable: Boolean = false,
    val initializer: String? = null,
    val visibility: CodeVisibility = CodeVisibility.PUBLIC,
    val isOverride: Boolean = false,
)

@Serializable
data class UpdatePropertyRequest(
    val name: String,
    val type: String,
    val mutable: Boolean = false,
    val nullable: Boolean = false,
    val initializer: String? = null,
    val visibility: CodeVisibility = CodeVisibility.PUBLIC,
    val isOverride: Boolean = false,
)

@Serializable
data class CreateEnumEntryRequest(
    val declarationId: String,
    val name: String,
    val arguments: List<String> = emptyList(),
    val bodyText: String? = null,
)

@Serializable
data class UpdateEnumEntryRequest(
    val name: String,
    val arguments: List<String> = emptyList(),
    val bodyText: String? = null,
)

@Serializable
data class CreateAnnotationUsageRequest(
    val ownerType: AnnotationOwnerType,
    val ownerId: String,
    val annotationClassName: String,
    val useSiteTarget: String? = null,
)

@Serializable
data class UpdateAnnotationUsageRequest(
    val annotationClassName: String,
    val useSiteTarget: String? = null,
)

@Serializable
data class CreateAnnotationArgumentRequest(
    val annotationUsageId: String,
    val name: String? = null,
    val value: String,
)

@Serializable
data class UpdateAnnotationArgumentRequest(
    val name: String? = null,
    val value: String,
)

@Serializable
data class CreateFunctionStubRequest(
    val declarationId: String,
    val name: String,
    val returnType: String = "Unit",
    val visibility: CodeVisibility = CodeVisibility.PUBLIC,
    val modifiers: List<String> = emptyList(),
    val parameters: List<FunctionParameterDto> = emptyList(),
    val bodyMode: FunctionBodyMode = FunctionBodyMode.TEMPLATE,
    val bodyText: String? = null,
)

@Serializable
data class UpdateFunctionStubRequest(
    val name: String,
    val returnType: String = "Unit",
    val visibility: CodeVisibility = CodeVisibility.PUBLIC,
    val modifiers: List<String> = emptyList(),
    val parameters: List<FunctionParameterDto> = emptyList(),
    val bodyMode: FunctionBodyMode = FunctionBodyMode.TEMPLATE,
    val bodyText: String? = null,
)

@Serializable
data class CreateDeclarationPresetRequest(
    val targetId: String,
    val packageName: String,
    val declarationName: String,
    val kind: DeclarationKind,
)

@Serializable
data class ResolveSyncConflictRequest(
    val resolution: SyncConflictResolution,
)
