package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class CodegenSnapshotDto(
    val version: String,
    val projects: List<CodegenProjectDto> = emptyList(),
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

@Serializable
data class SnapshotImportResultDto(
    val projectCount: Int,
    val targetCount: Int,
    val fileCount: Int,
    val declarationCount: Int,
)
