package site.addzero.coding.playground.server.entity

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import site.addzero.coding.playground.shared.dto.BoundedContextMetaDto
import site.addzero.coding.playground.shared.dto.DtoFieldMetaDto
import site.addzero.coding.playground.shared.dto.DtoKind
import site.addzero.coding.playground.shared.dto.DtoMetaDto
import site.addzero.coding.playground.shared.dto.EntityMetaDto
import site.addzero.coding.playground.shared.dto.EtlWrapperMetaDto
import site.addzero.coding.playground.shared.dto.FieldMetaDto
import site.addzero.coding.playground.shared.dto.FieldType
import site.addzero.coding.playground.shared.dto.GenerationTargetMetaDto
import site.addzero.coding.playground.shared.dto.ProjectMetaDto
import site.addzero.coding.playground.shared.dto.RelationKind
import site.addzero.coding.playground.shared.dto.RelationMetaDto
import site.addzero.coding.playground.shared.dto.ScaffoldPreset
import site.addzero.coding.playground.shared.dto.TemplateMetaDto
import site.addzero.coding.playground.shared.dto.TemplateOutputKind

private val stringListSerializer = ListSerializer(String.serializer())
private val stringMapSerializer = MapSerializer(String.serializer(), String.serializer())

fun Json.decodeStringList(raw: String?): List<String> {
    if (raw.isNullOrBlank()) {
        return emptyList()
    }
    return decodeFromString(stringListSerializer, raw)
}

fun Json.encodeStringList(value: List<String>): String? {
    if (value.isEmpty()) {
        return null
    }
    return encodeToString(stringListSerializer, value)
}

fun Json.decodeStringMap(raw: String?): Map<String, String> {
    if (raw.isNullOrBlank()) {
        return emptyMap()
    }
    return decodeFromString(stringMapSerializer, raw)
}

fun Json.encodeStringMap(value: Map<String, String>): String? {
    if (value.isEmpty()) {
        return null
    }
    return encodeToString(stringMapSerializer, value)
}

fun ProjectMeta.toDto(json: Json): ProjectMetaDto {
    return ProjectMetaDto(
        id = id,
        name = name,
        slug = slug,
        description = description,
        tags = json.decodeStringList(tagsJson),
        orderIndex = orderIndex,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun BoundedContextMeta.toDto(json: Json): BoundedContextMetaDto {
    return BoundedContextMetaDto(
        id = id,
        projectId = projectId,
        name = name,
        code = code,
        description = description,
        tags = json.decodeStringList(tagsJson),
        orderIndex = orderIndex,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun EntityMeta.toDto(json: Json): EntityMetaDto {
    return EntityMetaDto(
        id = id,
        contextId = contextId,
        name = name,
        code = code,
        tableName = tableName,
        description = description,
        aggregateRoot = aggregateRoot,
        tags = json.decodeStringList(tagsJson),
        orderIndex = orderIndex,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun FieldMeta.toDto(): FieldMetaDto {
    return FieldMetaDto(
        id = id,
        entityId = entityId,
        name = name,
        code = code,
        type = FieldType.valueOf(type),
        nullable = nullable,
        list = list,
        idField = idField,
        keyField = keyField,
        unique = unique,
        searchable = searchable,
        defaultValue = defaultValue,
        description = description,
        orderIndex = orderIndex,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun RelationMeta.toDto(): RelationMetaDto {
    return RelationMetaDto(
        id = id,
        contextId = contextId,
        sourceEntityId = sourceEntityId,
        targetEntityId = targetEntityId,
        name = name,
        code = code,
        kind = RelationKind.valueOf(kind),
        nullable = nullable,
        owner = owner,
        mappedBy = mappedBy,
        sourceFieldName = sourceFieldName,
        targetFieldName = targetFieldName,
        description = description,
        orderIndex = orderIndex,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun DtoMeta.toDto(json: Json): DtoMetaDto {
    return DtoMetaDto(
        id = id,
        contextId = contextId,
        entityId = entityId,
        name = name,
        code = code,
        kind = DtoKind.valueOf(kind),
        description = description,
        tags = json.decodeStringList(tagsJson),
        orderIndex = orderIndex,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun DtoFieldMeta.toDto(): DtoFieldMetaDto {
    return DtoFieldMetaDto(
        id = id,
        dtoId = dtoId,
        entityFieldId = entityFieldId,
        name = name,
        code = code,
        type = FieldType.valueOf(type),
        nullable = nullable,
        list = list,
        sourcePath = sourcePath,
        description = description,
        orderIndex = orderIndex,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun TemplateMeta.toDto(json: Json): TemplateMetaDto {
    return TemplateMetaDto(
        id = id,
        contextId = contextId,
        etlWrapperId = etlWrapperId,
        name = name,
        key = key,
        description = description,
        outputKind = TemplateOutputKind.valueOf(outputKind),
        body = body,
        relativeOutputPath = relativeOutputPath,
        fileNameTemplate = fileNameTemplate,
        tags = json.decodeStringList(tagsJson),
        orderIndex = orderIndex,
        enabled = enabled,
        managedByGenerator = managedByGenerator,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun GenerationTargetMeta.toDto(json: Json, templateIds: List<String>): GenerationTargetMetaDto {
    return GenerationTargetMetaDto(
        id = id,
        projectId = projectId,
        contextId = contextId,
        name = name,
        key = key,
        description = description,
        outputRoot = outputRoot,
        packageName = packageName,
        scaffoldPreset = ScaffoldPreset.valueOf(scaffoldPreset),
        templateIds = templateIds,
        variables = json.decodeStringMap(variablesJson),
        enableEtl = enableEtl,
        autoIntegrateCompositeBuild = autoIntegrateCompositeBuild,
        managedMarker = managedMarker,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun EtlWrapperMeta.toDto(): EtlWrapperMetaDto {
    return EtlWrapperMetaDto(
        id = id,
        projectId = projectId,
        name = name,
        key = key,
        description = description,
        scriptBody = scriptBody,
        enabled = enabled,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}
