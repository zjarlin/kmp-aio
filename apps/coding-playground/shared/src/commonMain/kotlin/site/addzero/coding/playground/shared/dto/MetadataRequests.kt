package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class MetadataSearchRequest(
    val query: String? = null,
    val nodeTypes: Set<MetaNodeType> = emptySet(),
    val projectId: String? = null,
    val contextId: String? = null,
    val tag: String? = null,
    val includeDisabled: Boolean = true,
)

@Serializable
data class CreateProjectMetaRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class UpdateProjectMetaRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class CreateBoundedContextMetaRequest(
    val projectId: String,
    val name: String,
    val code: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class UpdateBoundedContextMetaRequest(
    val name: String,
    val code: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class CreateEntityMetaRequest(
    val contextId: String,
    val name: String,
    val code: String,
    val tableName: String,
    val description: String? = null,
    val aggregateRoot: Boolean = true,
    val tags: List<String> = emptyList(),
)

@Serializable
data class UpdateEntityMetaRequest(
    val name: String,
    val code: String,
    val tableName: String,
    val description: String? = null,
    val aggregateRoot: Boolean = true,
    val tags: List<String> = emptyList(),
)

@Serializable
data class CreateFieldMetaRequest(
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
)

@Serializable
data class UpdateFieldMetaRequest(
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
)

@Serializable
data class CreateRelationMetaRequest(
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
)

@Serializable
data class UpdateRelationMetaRequest(
    val name: String,
    val code: String,
    val kind: RelationKind,
    val nullable: Boolean = false,
    val owner: Boolean = true,
    val mappedBy: String? = null,
    val sourceFieldName: String? = null,
    val targetFieldName: String? = null,
    val description: String? = null,
)

@Serializable
data class CreateDtoMetaRequest(
    val contextId: String,
    val entityId: String? = null,
    val name: String,
    val code: String,
    val kind: DtoKind,
    val description: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class UpdateDtoMetaRequest(
    val entityId: String? = null,
    val name: String,
    val code: String,
    val kind: DtoKind,
    val description: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class CreateDtoFieldMetaRequest(
    val dtoId: String,
    val entityFieldId: String? = null,
    val name: String,
    val code: String,
    val type: FieldType,
    val nullable: Boolean = false,
    val list: Boolean = false,
    val sourcePath: String? = null,
    val description: String? = null,
)

@Serializable
data class UpdateDtoFieldMetaRequest(
    val entityFieldId: String? = null,
    val name: String,
    val code: String,
    val type: FieldType,
    val nullable: Boolean = false,
    val list: Boolean = false,
    val sourcePath: String? = null,
    val description: String? = null,
)

@Serializable
data class CreateTemplateMetaRequest(
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
    val enabled: Boolean = true,
    val managedByGenerator: Boolean = true,
)

@Serializable
data class UpdateTemplateMetaRequest(
    val etlWrapperId: String? = null,
    val name: String,
    val key: String,
    val description: String? = null,
    val outputKind: TemplateOutputKind,
    val body: String,
    val relativeOutputPath: String,
    val fileNameTemplate: String,
    val tags: List<String> = emptyList(),
    val enabled: Boolean = true,
    val managedByGenerator: Boolean = true,
)

@Serializable
data class CreateGenerationTargetMetaRequest(
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
)

@Serializable
data class UpdateGenerationTargetMetaRequest(
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
)

@Serializable
data class CreateEtlWrapperMetaRequest(
    val projectId: String,
    val name: String,
    val key: String,
    val description: String? = null,
    val scriptBody: String,
    val enabled: Boolean = true,
)

@Serializable
data class UpdateEtlWrapperMetaRequest(
    val name: String,
    val key: String,
    val description: String? = null,
    val scriptBody: String,
    val enabled: Boolean = true,
)
