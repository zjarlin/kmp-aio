package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
enum class MetaNodeType {
    PROJECT,
    CONTEXT,
    ENTITY,
    FIELD,
    RELATION,
    DTO,
    DTO_FIELD,
    TEMPLATE,
    GENERATION_TARGET,
    ETL_WRAPPER,
}

@Serializable
enum class FieldType {
    STRING,
    TEXT,
    BOOLEAN,
    INT,
    LONG,
    DECIMAL,
    DATE,
    DATETIME,
    UUID,
    ENUM,
    JSON,
    REFERENCE,
}

@Serializable
enum class RelationKind {
    MANY_TO_ONE,
    ONE_TO_MANY,
    ONE_TO_ONE,
    MANY_TO_MANY,
}

@Serializable
enum class DtoKind {
    MODEL,
    REQUEST,
    RESPONSE,
    QUERY,
    PAGE,
}

@Serializable
enum class TemplateOutputKind {
    KOTLIN_SOURCE,
    GRADLE_KTS,
    SETTINGS_SNIPPET,
    MARKDOWN,
    TEXT,
}

@Serializable
enum class ScaffoldPreset {
    KCLOUD_STYLE,
    SIMPLE_CRUD,
}

@Serializable
data class ProjectMetaDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val orderIndex: Int = 0,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class BoundedContextMetaDto(
    val id: String,
    val projectId: String,
    val name: String,
    val code: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val orderIndex: Int = 0,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class EntityMetaDto(
    val id: String,
    val contextId: String,
    val name: String,
    val code: String,
    val tableName: String,
    val description: String? = null,
    val aggregateRoot: Boolean = true,
    val tags: List<String> = emptyList(),
    val orderIndex: Int = 0,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class FieldMetaDto(
    val id: String,
    val entityId: String,
    val name: String,
    val code: String,
    val type: FieldType,
    val nullable: Boolean = false,
    val list: Boolean = false,
    val idField: Boolean = false,
    val keyField: Boolean = false,
    val unique: Boolean = false,
    val searchable: Boolean = false,
    val defaultValue: String? = null,
    val description: String? = null,
    val orderIndex: Int = 0,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class RelationMetaDto(
    val id: String,
    val contextId: String,
    val sourceEntityId: String,
    val targetEntityId: String,
    val name: String,
    val code: String,
    val kind: RelationKind,
    val nullable: Boolean = false,
    val owner: Boolean = true,
    val mappedBy: String? = null,
    val sourceFieldName: String? = null,
    val targetFieldName: String? = null,
    val description: String? = null,
    val orderIndex: Int = 0,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class DtoMetaDto(
    val id: String,
    val contextId: String,
    val entityId: String? = null,
    val name: String,
    val code: String,
    val kind: DtoKind,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val orderIndex: Int = 0,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class DtoFieldMetaDto(
    val id: String,
    val dtoId: String,
    val entityFieldId: String? = null,
    val name: String,
    val code: String,
    val type: FieldType,
    val nullable: Boolean = false,
    val list: Boolean = false,
    val sourcePath: String? = null,
    val description: String? = null,
    val orderIndex: Int = 0,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class TemplateMetaDto(
    val id: String,
    val contextId: String,
    val etlWrapperId: String? = null,
    val name: String,
    val key: String,
    val description: String? = null,
    val outputKind: TemplateOutputKind,
    val body: String,
    val relativeOutputPath: String,
    val fileNameTemplate: String,
    val tags: List<String> = emptyList(),
    val orderIndex: Int = 0,
    val enabled: Boolean = true,
    val managedByGenerator: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class GenerationTargetMetaDto(
    val id: String,
    val projectId: String,
    val contextId: String,
    val name: String,
    val key: String,
    val description: String? = null,
    val outputRoot: String,
    val packageName: String,
    val scaffoldPreset: ScaffoldPreset = ScaffoldPreset.KCLOUD_STYLE,
    val templateIds: List<String> = emptyList(),
    val variables: Map<String, String> = emptyMap(),
    val enableEtl: Boolean = false,
    val autoIntegrateCompositeBuild: Boolean = true,
    val managedMarker: String = "CODING_PLAYGROUND",
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class EtlWrapperMetaDto(
    val id: String,
    val projectId: String,
    val name: String,
    val key: String,
    val description: String? = null,
    val scriptBody: String,
    val enabled: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class MetadataTreeNodeDto(
    val id: String,
    val parentId: String? = null,
    val type: MetaNodeType,
    val title: String,
    val subtitle: String? = null,
    val tags: List<String> = emptyList(),
    val children: List<MetadataTreeNodeDto> = emptyList(),
)

@Serializable
data class ContextAggregateDto(
    val context: BoundedContextMetaDto,
    val entities: List<EntityMetaDto> = emptyList(),
    val fields: List<FieldMetaDto> = emptyList(),
    val relations: List<RelationMetaDto> = emptyList(),
    val dtos: List<DtoMetaDto> = emptyList(),
    val dtoFields: List<DtoFieldMetaDto> = emptyList(),
    val templates: List<TemplateMetaDto> = emptyList(),
    val generationTargets: List<GenerationTargetMetaDto> = emptyList(),
)

@Serializable
data class ProjectAggregateDto(
    val project: ProjectMetaDto,
    val contexts: List<ContextAggregateDto> = emptyList(),
    val etlWrappers: List<EtlWrapperMetaDto> = emptyList(),
)

@Serializable
data class ValidationIssueDto(
    val field: String,
    val message: String,
)

@Serializable
data class DeleteCheckResultDto(
    val allowed: Boolean,
    val reasons: List<String> = emptyList(),
)

@Serializable
data class ReorderRequestDto(
    val orderedIds: List<String>,
)
