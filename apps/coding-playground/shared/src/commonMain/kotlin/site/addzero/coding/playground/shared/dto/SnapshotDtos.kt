package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class MetadataSnapshotDto(
    val version: Int = 1,
    val exportedAt: String,
    val projects: List<ProjectMetaDto> = emptyList(),
    val contexts: List<BoundedContextMetaDto> = emptyList(),
    val entities: List<EntityMetaDto> = emptyList(),
    val fields: List<FieldMetaDto> = emptyList(),
    val relations: List<RelationMetaDto> = emptyList(),
    val dtos: List<DtoMetaDto> = emptyList(),
    val dtoFields: List<DtoFieldMetaDto> = emptyList(),
    val templates: List<TemplateMetaDto> = emptyList(),
    val generationTargets: List<GenerationTargetMetaDto> = emptyList(),
    val etlWrappers: List<EtlWrapperMetaDto> = emptyList(),
)

@Serializable
data class MetadataImportResultDto(
    val createdIds: List<String> = emptyList(),
    val updatedIds: List<String> = emptyList(),
)
