package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeleteCheckResultDto(
    val id: String,
    val kind: String,
    val canDelete: Boolean,
    val reasons: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
)

@Serializable
data class ValidationIssueDto(
    val scopeType: String,
    val scopeId: String? = null,
    val severity: ValidationSeverity,
    val message: String,
)

@Serializable
data class PathPreviewDto(
    val targetId: String,
    val resolvedRootDir: String,
    val sourceRoot: String,
)

@Serializable
data class CodeRenderPreviewDto(
    val file: SourceFileMetaDto,
    val outputPath: String,
    val declarationIds: List<String>,
    val metadataHash: String,
    val contentHash: String,
    val content: String,
)

@Serializable
data class SyncExportRequest(
    val fileId: String? = null,
    val targetId: String? = null,
    val force: Boolean = false,
)

@Serializable
data class SyncExportResultDto(
    val previews: List<CodeRenderPreviewDto> = emptyList(),
    val artifacts: List<ManagedArtifactMetaDto> = emptyList(),
    val conflicts: List<SyncConflictMetaDto> = emptyList(),
    val messages: List<String> = emptyList(),
)

@Serializable
data class SyncImportRequest(
    val fileId: String? = null,
    val artifactId: String? = null,
    val absolutePath: String? = null,
)

@Serializable
data class SyncImportResultDto(
    val files: List<SourceFileAggregateDto> = emptyList(),
    val conflicts: List<SyncConflictMetaDto> = emptyList(),
    val messages: List<String> = emptyList(),
)

@Serializable
data class KspIndexPreviewDto(
    val targetId: String,
    val packageName: String,
    val fileCount: Int,
    val declarationCount: Int,
    val content: String,
)
