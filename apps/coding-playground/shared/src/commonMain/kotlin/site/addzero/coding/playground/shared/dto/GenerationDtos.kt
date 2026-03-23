package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
enum class GenerationScaffoldMode {
    EXISTING_ROOT,
    NEW_ROOT,
}

@Serializable
data class GenerationRequestDto(
    val targetId: String,
    val contextId: String,
    val templateIds: List<String> = emptyList(),
    val previewOnly: Boolean = false,
)

@Serializable
data class RenderedTemplateDto(
    val templateId: String,
    val templateKey: String,
    val relativePath: String,
    val fileName: String,
    val content: String,
)

@Serializable
data class GenerationPlanFileDto(
    val templateId: String,
    val templateKey: String,
    val entityCode: String? = null,
    val relativePath: String,
    val fileName: String,
    val outputKind: TemplateOutputKind,
    val description: String,
)

@Serializable
data class GenerationPlanDto(
    val request: GenerationRequestDto,
    val target: GenerationTargetMetaDto,
    val context: ContextAggregateDto,
    val scaffoldMode: GenerationScaffoldMode,
    val files: List<GenerationPlanFileDto>,
)

@Serializable
data class GeneratedFileDto(
    val absolutePath: String,
    val relativePath: String,
    val templateKey: String,
    val content: String,
    val etlApplied: Boolean = false,
)

@Serializable
data class CompositeIntegrationResultDto(
    val targetFile: String,
    val marker: String,
    val changed: Boolean,
)

@Serializable
data class GenerationResultDto(
    val plan: GenerationPlanDto,
    val files: List<GeneratedFileDto>,
    val integrations: List<CompositeIntegrationResultDto> = emptyList(),
    val warnings: List<String> = emptyList(),
)
