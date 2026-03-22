package site.addzero.coding.playground.shared.descriptor

import kotlinx.serialization.Serializable
import site.addzero.coding.playground.shared.dto.DtoKind
import site.addzero.coding.playground.shared.dto.FieldType
import site.addzero.coding.playground.shared.dto.RelationKind
import site.addzero.coding.playground.shared.dto.TemplateOutputKind

@Serializable
data class FieldDescriptor(
    val name: String,
    val code: String,
    val type: FieldType,
    val nullable: Boolean,
    val list: Boolean,
    val idField: Boolean,
    val keyField: Boolean,
)

@Serializable
data class RelationDescriptor(
    val name: String,
    val code: String,
    val kind: RelationKind,
    val targetModel: String,
    val nullable: Boolean,
)

@Serializable
data class ModelDescriptor(
    val name: String,
    val code: String,
    val tableName: String,
    val fields: List<FieldDescriptor>,
    val relations: List<RelationDescriptor>,
)

@Serializable
data class DtoFieldDescriptor(
    val name: String,
    val code: String,
    val type: FieldType,
    val nullable: Boolean,
    val list: Boolean,
    val sourcePath: String? = null,
)

@Serializable
data class DtoDescriptor(
    val name: String,
    val code: String,
    val kind: DtoKind,
    val sourceModel: String? = null,
    val fields: List<DtoFieldDescriptor>,
)

@Serializable
data class TemplateDescriptor(
    val name: String,
    val key: String,
    val outputKind: TemplateOutputKind,
    val relativeOutputPath: String,
    val fileNameTemplate: String,
)

@Serializable
data class ContextMetadataDescriptor(
    val name: String,
    val code: String,
    val models: List<ModelDescriptor>,
    val dtos: List<DtoDescriptor>,
    val templates: List<TemplateDescriptor>,
)

interface ContextMetadataView {
    fun models(): List<ModelDescriptor>
    fun dtos(): List<DtoDescriptor>
    fun templates(): List<TemplateDescriptor>
    fun findModel(name: String): ModelDescriptor?
    fun findDto(name: String): DtoDescriptor?
}

interface GeneratedMetadataIndex {
    fun contexts(): List<ContextMetadataDescriptor>
    fun findContext(name: String): ContextMetadataDescriptor?
}
