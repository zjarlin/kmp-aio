package site.addzero.coding.playground.server.generation

import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.util.toLowerCamelIdentifier
import site.addzero.coding.playground.server.util.toPascalIdentifier
import site.addzero.coding.playground.shared.dto.ContextAggregateDto

data class GenerationFieldIr(
    val id: String,
    val name: String,
    val code: String,
    val sourceType: String,
    val kotlinName: String,
    val kotlinType: String,
    val kotlinNullableType: String,
    val nullable: Boolean,
    val list: Boolean,
    val idField: Boolean,
    val keyField: Boolean,
    val searchable: Boolean,
    val description: String?,
)

data class GenerationRelationIr(
    val id: String,
    val name: String,
    val code: String,
    val kotlinName: String,
    val kind: String,
    val targetEntityName: String,
    val nullable: Boolean,
    val mappedBy: String?,
    val sourceFieldName: String?,
)

data class GenerationDtoFieldIr(
    val id: String,
    val name: String,
    val code: String,
    val sourceType: String,
    val kotlinName: String,
    val kotlinType: String,
    val nullable: Boolean,
    val list: Boolean,
    val sourcePath: String?,
)

data class GenerationDtoIr(
    val id: String,
    val name: String,
    val code: String,
    val kotlinName: String,
    val kind: String,
    val sourceModelName: String?,
    val fields: List<GenerationDtoFieldIr>,
)

data class GenerationEntityIr(
    val id: String,
    val name: String,
    val code: String,
    val kotlinName: String,
    val camelName: String,
    val entityPath: String,
    val tableName: String,
    val fields: List<GenerationFieldIr>,
    val idField: GenerationFieldIr?,
    val payloadFields: List<GenerationFieldIr>,
    val relations: List<GenerationRelationIr>,
    val dtoModels: List<GenerationDtoIr>,
)

data class GenerationContextIr(
    val contextName: String,
    val contextCode: String,
    val entities: List<GenerationEntityIr>,
    val dtos: List<GenerationDtoIr>,
)

@Single
class MetadataIrCompiler {
    fun compile(context: ContextAggregateDto): GenerationContextIr {
        val entities = context.entities.map { entity ->
            val fields = context.fields
                .filter { it.entityId == entity.id }
                .map { field ->
                    GenerationFieldIr(
                        id = field.id,
                        name = field.name,
                        code = field.code,
                        sourceType = field.type.name,
                        kotlinName = field.code.toLowerCamelIdentifier(),
                        kotlinType = mapType(field.type.name, field.list, field.nullable),
                        kotlinNullableType = mapType(field.type.name, field.list, true),
                        nullable = field.nullable,
                        list = field.list,
                        idField = field.idField,
                        keyField = field.keyField,
                        searchable = field.searchable,
                        description = field.description,
                    )
                }
            val relations = context.relations
                .filter { it.sourceEntityId == entity.id }
                .map { relation ->
                    val target = context.entities.first { it.id == relation.targetEntityId }
                    GenerationRelationIr(
                        id = relation.id,
                        name = relation.name,
                        code = relation.code,
                        kotlinName = (relation.sourceFieldName ?: relation.code).toLowerCamelIdentifier(),
                        kind = relation.kind.name,
                        targetEntityName = target.name.toPascalIdentifier(),
                        nullable = relation.nullable,
                        mappedBy = relation.mappedBy,
                        sourceFieldName = relation.sourceFieldName,
                    )
                }
            val dtoModels = context.dtos
                .filter { it.entityId == entity.id }
                .map { dto ->
                    val dtoFields = context.dtoFields
                        .filter { it.dtoId == dto.id }
                        .map { dtoField ->
                            GenerationDtoFieldIr(
                                id = dtoField.id,
                                name = dtoField.name,
                                code = dtoField.code,
                                sourceType = dtoField.type.name,
                                kotlinName = dtoField.code.toLowerCamelIdentifier(),
                                kotlinType = mapType(dtoField.type.name, dtoField.list, dtoField.nullable),
                                nullable = dtoField.nullable,
                                list = dtoField.list,
                                sourcePath = dtoField.sourcePath,
                            )
                        }
                    GenerationDtoIr(
                        id = dto.id,
                        name = dto.name,
                        code = dto.code,
                        kotlinName = dto.name.toPascalIdentifier(),
                        kind = dto.kind.name,
                        sourceModelName = entity.name.toPascalIdentifier(),
                        fields = dtoFields,
                    )
                }
            GenerationEntityIr(
                id = entity.id,
                name = entity.name,
                code = entity.code,
                kotlinName = entity.name.toPascalIdentifier(),
                camelName = entity.name.toLowerCamelIdentifier(),
                entityPath = entity.code.lowercase() + "s",
                tableName = entity.tableName,
                fields = fields,
                idField = fields.firstOrNull { it.idField },
                payloadFields = fields.filterNot { it.idField || it.list },
                relations = relations,
                dtoModels = dtoModels,
            )
        }
        val dtoIndex = context.dtos.map { dto ->
            val dtoFields = context.dtoFields
                .filter { it.dtoId == dto.id }
                .map { dtoField ->
                    GenerationDtoFieldIr(
                        id = dtoField.id,
                        name = dtoField.name,
                        code = dtoField.code,
                        sourceType = dtoField.type.name,
                        kotlinName = dtoField.code.toLowerCamelIdentifier(),
                        kotlinType = mapType(dtoField.type.name, dtoField.list, dtoField.nullable),
                        nullable = dtoField.nullable,
                        list = dtoField.list,
                        sourcePath = dtoField.sourcePath,
                    )
                }
            val sourceModelName = dto.entityId
                ?.let { entityId -> context.entities.firstOrNull { it.id == entityId }?.name?.toPascalIdentifier() }
            GenerationDtoIr(
                id = dto.id,
                name = dto.name,
                code = dto.code,
                kotlinName = dto.name.toPascalIdentifier(),
                kind = dto.kind.name,
                sourceModelName = sourceModelName,
                fields = dtoFields,
            )
        }
        return GenerationContextIr(
            contextName = context.context.name.toPascalIdentifier(),
            contextCode = context.context.code,
            entities = entities,
            dtos = dtoIndex,
        )
    }

    private fun mapType(type: String, list: Boolean, nullable: Boolean): String {
        val base = when (type) {
            "STRING", "TEXT", "ENUM", "JSON", "UUID", "REFERENCE" -> "String"
            "BOOLEAN" -> "Boolean"
            "INT" -> "Int"
            "LONG" -> "Long"
            "DECIMAL" -> "Double"
            "DATE", "DATETIME" -> "String"
            else -> "String"
        }
        val valueType = if (list) "List<$base>" else base
        return if (nullable) "$valueType?" else valueType
    }
}
