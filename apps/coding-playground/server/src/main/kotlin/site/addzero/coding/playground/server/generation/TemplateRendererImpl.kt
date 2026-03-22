package site.addzero.coding.playground.server.generation

import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.util.lowerCamelCase
import site.addzero.coding.playground.server.util.renderTemplateString
import site.addzero.coding.playground.server.util.toLowerCamelIdentifier
import site.addzero.coding.playground.server.util.toPascalIdentifier
import site.addzero.coding.playground.shared.dto.ContextAggregateDto
import site.addzero.coding.playground.shared.dto.DtoFieldMetaDto
import site.addzero.coding.playground.shared.dto.EntityMetaDto
import site.addzero.coding.playground.shared.dto.FieldMetaDto
import site.addzero.coding.playground.shared.dto.FieldType
import site.addzero.coding.playground.shared.dto.GenerationTargetMetaDto
import site.addzero.coding.playground.shared.dto.RelationKind
import site.addzero.coding.playground.shared.dto.RenderedTemplateDto
import site.addzero.coding.playground.shared.dto.TemplateMetaDto
import site.addzero.coding.playground.shared.service.TemplateRenderer

@Single
class TemplateRendererImpl(
    private val builtinTemplateCatalog: BuiltinTemplateCatalog,
) : TemplateRenderer {
    override suspend fun render(
        template: TemplateMetaDto,
        context: ContextAggregateDto,
        target: GenerationTargetMetaDto,
        variables: Map<String, String>,
    ): RenderedTemplateDto {
        val relativePath = renderTemplateString(template.relativeOutputPath, variables)
        val fileName = renderTemplateString(template.fileNameTemplate, variables)
        val content = when (template.key) {
            "jimmer-entity" -> renderJimmerEntity(context, variables)
            "crud-dtos" -> renderCrudDtos(context, variables)
            "repository-skeleton" -> renderRepositorySkeleton(variables)
            "service-skeleton" -> renderServiceSkeleton(context, variables)
            "route-skeleton" -> renderRouteSkeleton(context, variables)
            "koin-module" -> renderKoinModule(variables)
            "metadata-object" -> renderMetadataObject(context, variables)
            "metadata-index" -> renderMetadataIndex(context, variables)
            "plugin-root-gradle" -> renderPluginRootGradle()
            "plugin-settings-gradle" -> renderPluginSettingsGradle(variables)
            "plugin-server-gradle" -> renderPluginServerGradle(variables)
            "plugin-client-gradle" -> renderPluginClientGradle(variables)
            "plugin-spi-gradle" -> renderPluginSpiGradle(variables)
            "client-placeholder" -> renderClientPlaceholder(variables)
            "settings-snippet" -> renderSettingsSnippet(variables)
            else -> renderTemplateString(template.body, variables)
        }
        return RenderedTemplateDto(
            templateId = template.id,
            templateKey = template.key,
            relativePath = relativePath,
            fileName = fileName,
            content = content.trimEnd() + "\n",
        )
    }

    private fun renderJimmerEntity(
        context: ContextAggregateDto,
        variables: Map<String, String>,
    ): String {
        val entityCode = variables.getValue("entityCode")
        val entity = context.entities.first { it.code == entityCode }
        val fields = context.fields.filter { it.entityId == entity.id }
        val relations = context.relations.filter { it.sourceEntityId == entity.id }
        val idField = fields.firstOrNull { it.idField }
        val nonIdFields = fields.filterNot { it.idField }
        val packageName = variables.getValue("packageName")
        return buildString {
            appendLine("package $packageName.model")
            appendLine()
            appendLine("import org.babyfish.jimmer.sql.*")
            appendLine()
            appendLine("@Entity")
            appendLine("interface ${entity.name.toPascalIdentifier()} {")
            if (idField == null) {
                appendLine("    @Id")
                appendLine("    val id: String")
            } else {
                appendLine("    @Id")
                appendLine("    val ${idField.code.toLowerCamelIdentifier()}: ${mapType(idField.type, false)}")
            }
            nonIdFields.forEach { field ->
                if (field.keyField) {
                    appendLine()
                    appendLine("    @Key")
                } else {
                    appendLine()
                }
                appendLine("    val ${field.code.toLowerCamelIdentifier()}: ${mapType(field.type, field.nullable)}")
            }
            relations.forEach { relation ->
                val target = context.entities.first { it.id == relation.targetEntityId }
                val relationName = (relation.sourceFieldName ?: relation.code).toLowerCamelIdentifier()
                appendLine()
                when (relation.kind) {
                    RelationKind.MANY_TO_ONE -> {
                        appendLine("    @ManyToOne")
                        appendLine("    @JoinColumn(name = \"${relationName}_id\")")
                        appendLine("    val $relationName: ${target.name.toPascalIdentifier()}${if (relation.nullable) "?" else ""}")
                    }
                    RelationKind.ONE_TO_ONE -> {
                        appendLine("    @OneToOne")
                        appendLine("    @JoinColumn(name = \"${relationName}_id\")")
                        appendLine("    val $relationName: ${target.name.toPascalIdentifier()}${if (relation.nullable) "?" else ""}")
                    }
                    RelationKind.ONE_TO_MANY -> {
                        appendLine("    @OneToMany(mappedBy = \"${(relation.mappedBy ?: entity.code).toLowerCamelIdentifier()}\")")
                        appendLine("    val $relationName: List<${target.name.toPascalIdentifier()}>")
                    }
                    RelationKind.MANY_TO_MANY -> {
                        appendLine("    @ManyToMany")
                        appendLine("    val $relationName: List<${target.name.toPascalIdentifier()}>")
                    }
                }
            }
            appendLine("}")
        }
    }

    private fun renderCrudDtos(
        context: ContextAggregateDto,
        variables: Map<String, String>,
    ): String {
        val entity = context.entities.first { it.code == variables.getValue("entityCode") }
        val fields = context.fields.filter { it.entityId == entity.id }
        val payloadFields = fields.filterNot { it.idField || it.list }
        val entityName = entity.name.toPascalIdentifier()
        val packageName = variables.getValue("packageName")
        return buildString {
            appendLine("package $packageName.dto")
            appendLine()
            appendLine("import kotlinx.serialization.Serializable")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class ${entityName}Query(")
            payloadFields.forEachIndexed { index, field ->
                append("    val ${field.code.toLowerCamelIdentifier()}: ${mapType(field.type, true)} = null")
                appendLine(if (index == payloadFields.lastIndex) "" else ",")
            }
            if (payloadFields.isEmpty()) {
                appendLine("    val keyword: String? = null")
            }
            appendLine(")")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class ${entityName}PageRequest(")
            appendLine("    val pageIndex: Int = 0,")
            appendLine("    val pageSize: Int = 20,")
            appendLine("    val query: ${entityName}Query = ${entityName}Query(),")
            appendLine(")")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class Create${entityName}Request(")
            payloadFields.forEachIndexed { index, field ->
                append("    val ${field.code.toLowerCamelIdentifier()}: ${mapType(field.type, field.nullable)}")
                appendLine(if (index == payloadFields.lastIndex) "" else ",")
            }
            if (payloadFields.isEmpty()) {
                appendLine("    val placeholder: String = \"\"")
            }
            appendLine(")")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class Update${entityName}Request(")
            payloadFields.forEachIndexed { index, field ->
                append("    val ${field.code.toLowerCamelIdentifier()}: ${mapType(field.type, field.nullable)}")
                appendLine(if (index == payloadFields.lastIndex) "" else ",")
            }
            if (payloadFields.isEmpty()) {
                appendLine("    val placeholder: String = \"\"")
            }
            appendLine(")")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class ${entityName}Response(")
            fields.forEachIndexed { index, field ->
                append("    val ${field.code.toLowerCamelIdentifier()}: ${mapType(field.type, field.nullable)}")
                appendLine(if (index == fields.lastIndex) "" else ",")
            }
            appendLine(")")
        }
    }

    private fun renderRepositorySkeleton(variables: Map<String, String>): String {
        val packageName = variables.getValue("packageName")
        val entityName = variables.getValue("EntityName")
        return """
            package $packageName.service

            import org.babyfish.jimmer.sql.kt.KSqlClient
            import $packageName.dto.${entityName}PageRequest
            import $packageName.dto.${entityName}Response
            import $packageName.dto.Create${entityName}Request
            import $packageName.dto.Update${entityName}Request

            class ${entityName}Repository(
                private val sqlClient: KSqlClient,
            ) {
                fun list(request: ${entityName}PageRequest): List<${entityName}Response> =
                    throw UnsupportedOperationException("TODO: implement generated CRUD query")

                fun findById(id: String): ${entityName}Response =
                    throw UnsupportedOperationException("TODO: implement generated detail query")

                fun create(request: Create${entityName}Request): ${entityName}Response =
                    throw UnsupportedOperationException("TODO: implement generated create command")

                fun update(id: String, request: Update${entityName}Request): ${entityName}Response =
                    throw UnsupportedOperationException("TODO: implement generated update command")

                fun delete(id: String) {
                    throw UnsupportedOperationException("TODO: implement generated delete command")
                }
            }
        """.trimIndent()
    }

    private fun renderServiceSkeleton(
        context: ContextAggregateDto,
        variables: Map<String, String>,
    ): String {
        val packageName = variables.getValue("packageName")
        val entityName = variables.getValue("EntityName")
        val serviceName = "${entityName}Service"
        return """
            package $packageName.service

            import org.koin.core.annotation.Single
            import $packageName.dto.${entityName}PageRequest
            import $packageName.dto.${entityName}Response
            import $packageName.dto.Create${entityName}Request
            import $packageName.dto.Update${entityName}Request

            @Single
            class $serviceName(
                private val repository: ${entityName}Repository,
            ) {
                fun list(request: ${entityName}PageRequest): List<${entityName}Response> = repository.list(request)

                fun get(id: String): ${entityName}Response = repository.findById(id)

                fun create(request: Create${entityName}Request): ${entityName}Response = repository.create(request)

                fun update(id: String, request: Update${entityName}Request): ${entityName}Response = repository.update(id, request)

                fun delete(id: String) {
                    repository.delete(id)
                }
            }
        """.trimIndent()
    }

    private fun renderRouteSkeleton(
        context: ContextAggregateDto,
        variables: Map<String, String>,
    ): String {
        val packageName = variables.getValue("packageName")
        val entityName = variables.getValue("EntityName")
        val entityPath = variables.getValue("entityPath")
        val serviceName = "${entityName}Service"
        return """
            package $packageName.route

            import org.koin.mp.KoinPlatform
            import org.springframework.web.bind.annotation.*
            import $packageName.dto.${entityName}PageRequest
            import $packageName.dto.${entityName}Response
            import $packageName.dto.Create${entityName}Request
            import $packageName.dto.Update${entityName}Request
            import $packageName.service.$serviceName

            @GetMapping("/api/${variables.getValue("contextCode")}/$entityPath")
            suspend fun list${entityName}s(
                @RequestBody request: ${entityName}PageRequest,
            ): List<${entityName}Response> {
                return KoinPlatform.getKoin().get<$serviceName>().list(request)
            }

            @GetMapping("/api/${variables.getValue("contextCode")}/$entityPath/{id}")
            suspend fun get$entityName(
                @PathVariable id: String,
            ): ${entityName}Response {
                return KoinPlatform.getKoin().get<$serviceName>().get(id)
            }

            @PostMapping("/api/${variables.getValue("contextCode")}/$entityPath")
            suspend fun create$entityName(
                @RequestBody request: Create${entityName}Request,
            ): ${entityName}Response {
                return KoinPlatform.getKoin().get<$serviceName>().create(request)
            }

            @PutMapping("/api/${variables.getValue("contextCode")}/$entityPath/{id}")
            suspend fun update$entityName(
                @PathVariable id: String,
                @RequestBody request: Update${entityName}Request,
            ): ${entityName}Response {
                return KoinPlatform.getKoin().get<$serviceName>().update(id, request)
            }

            @DeleteMapping("/api/${variables.getValue("contextCode")}/$entityPath/{id}")
            suspend fun delete$entityName(
                @PathVariable id: String,
            ) {
                KoinPlatform.getKoin().get<$serviceName>().delete(id)
            }
        """.trimIndent()
    }

    private fun renderKoinModule(variables: Map<String, String>): String {
        val packageName = variables.getValue("packageName")
        val contextName = variables.getValue("ContextName")
        return """
            package $packageName.di

            import org.koin.core.annotation.ComponentScan
            import org.koin.core.annotation.Configuration
            import org.koin.core.annotation.Module

            @Module
            @Configuration("${variables.getValue("contextCode")}")
            @ComponentScan("$packageName")
            class ${contextName}ServerModule
        """.trimIndent()
    }

    private fun renderMetadataObject(
        context: ContextAggregateDto,
        variables: Map<String, String>,
    ): String {
        val packageName = variables.getValue("packageName")
        val contextName = variables.getValue("ContextName")
        val models = context.entities.joinToString(",\n        ") { entity ->
            val fields = context.fields.filter { it.entityId == entity.id }.joinToString(",\n                ") { field ->
                """
                FieldDescriptor(
                    name = "${field.name}",
                    code = "${field.code}",
                    type = "${field.type.name}",
                    nullable = ${field.nullable},
                    list = ${field.list},
                    idField = ${field.idField},
                    keyField = ${field.keyField},
                )
                """.trimIndent()
            }
            val relations = context.relations.filter { it.sourceEntityId == entity.id }.joinToString(",\n                ") { relation ->
                val target = context.entities.first { it.id == relation.targetEntityId }
                """
                RelationDescriptor(
                    name = "${relation.name}",
                    code = "${relation.code}",
                    kind = "${relation.kind.name}",
                    targetModel = "${target.name.toPascalIdentifier()}",
                    nullable = ${relation.nullable},
                )
                """.trimIndent()
            }
            """
            ModelDescriptor(
                name = "${entity.name.toPascalIdentifier()}",
                code = "${entity.code}",
                tableName = "${entity.tableName}",
                fields = listOf(
                    ${fields.ifBlank { "" }}
                ),
                relations = listOf(
                    ${relations.ifBlank { "" }}
                ),
            )
            """.trimIndent()
        }
        val dtos = context.dtos.joinToString(",\n        ") { dto ->
            val fields = context.dtoFields.filter { it.dtoId == dto.id }.joinToString(",\n                ") { field ->
                """
                DtoFieldDescriptor(
                    name = "${field.name}",
                    code = "${field.code}",
                    type = "${field.type.name}",
                    nullable = ${field.nullable},
                    list = ${field.list},
                    sourcePath = ${field.sourcePath?.let { "\"$it\"" } ?: "null"},
                )
                """.trimIndent()
            }
            """
            DtoDescriptor(
                name = "${dto.name.toPascalIdentifier()}",
                code = "${dto.code}",
                kind = "${dto.kind.name}",
                sourceModel = ${dto.entityId?.let { entityId ->
                    "\"${context.entities.first { it.id == entityId }.name.toPascalIdentifier()}\""
                } ?: "null"},
                fields = listOf(
                    ${fields.ifBlank { "" }}
                ),
            )
            """.trimIndent()
        }
        val templates = context.templates.joinToString(",\n        ") { template ->
            """
            TemplateDescriptor(
                name = "${template.name}",
                key = "${template.key}",
                outputKind = "${template.outputKind.name}",
                relativeOutputPath = "${template.relativeOutputPath}",
                fileNameTemplate = "${template.fileNameTemplate}",
            )
            """.trimIndent()
        }
        return """
            package $packageName.metadata

            import kotlinx.serialization.Serializable

            @Serializable
            data class FieldDescriptor(
                val name: String,
                val code: String,
                val type: String,
                val nullable: Boolean,
                val list: Boolean,
                val idField: Boolean,
                val keyField: Boolean,
            )

            @Serializable
            data class RelationDescriptor(
                val name: String,
                val code: String,
                val kind: String,
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
                val type: String,
                val nullable: Boolean,
                val list: Boolean,
                val sourcePath: String? = null,
            )

            @Serializable
            data class DtoDescriptor(
                val name: String,
                val code: String,
                val kind: String,
                val sourceModel: String? = null,
                val fields: List<DtoFieldDescriptor>,
            )

            @Serializable
            data class TemplateDescriptor(
                val name: String,
                val key: String,
                val outputKind: String,
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

            object ${contextName}Metadata : ContextMetadataView {
                private val descriptor = ContextMetadataDescriptor(
                    name = "${context.context.name}",
                    code = "${context.context.code}",
                    models = listOf(
                        ${models.ifBlank { "" }}
                    ),
                    dtos = listOf(
                        ${dtos.ifBlank { "" }}
                    ),
                    templates = listOf(
                        ${templates.ifBlank { "" }}
                    ),
                )

                fun descriptor(): ContextMetadataDescriptor = descriptor

                override fun models(): List<ModelDescriptor> = descriptor.models

                override fun dtos(): List<DtoDescriptor> = descriptor.dtos

                override fun templates(): List<TemplateDescriptor> = descriptor.templates

                override fun findModel(name: String): ModelDescriptor? =
                    descriptor.models.firstOrNull { it.name == name || it.code == name }

                override fun findDto(name: String): DtoDescriptor? =
                    descriptor.dtos.firstOrNull { it.name == name || it.code == name }
            }
        """.trimIndent()
    }

    private fun renderMetadataIndex(
        context: ContextAggregateDto,
        variables: Map<String, String>,
    ): String {
        val packageName = variables.getValue("packageName")
        val contextName = variables.getValue("ContextName")
        return """
            package $packageName.metadata

            object GeneratedMetadataIndex {
                fun contexts(): List<ContextMetadataDescriptor> = listOf(${contextName}Metadata.descriptor())

                fun findContext(name: String): ContextMetadataDescriptor? =
                    contexts().firstOrNull { it.name == name || it.code == name }
            }
        """.trimIndent()
    }

    private fun renderPluginRootGradle(): String {
        return """
            plugins {
                base
            }

            description = "Generated plugin aggregate"
        """.trimIndent()
    }

    private fun renderPluginSettingsGradle(variables: Map<String, String>): String {
        return """
            rootProject.name = "${variables.getValue("contextCode")}-generated-plugin"

            include(":server")
            include(":client")
            include(":spi")
        """.trimIndent()
    }

    private fun renderPluginServerGradle(variables: Map<String, String>): String {
        return """
            plugins {
                id("site.addzero.buildlogic.jvm.kotlin-convention")
                id("site.addzero.buildlogic.jvm.jimmer")
                id("site.addzero.buildlogic.jvm.jvm-koin")
                id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
            }

            ksp {
                arg("springKtor.generatedPackage", "${variables.getValue("packageName")}.generated.springktor")
            }

            dependencies {
                implementation(project(":spi"))
                implementation("site.addzero:spring2ktor-server-core:2026.03.13")
                compileOnly("org.springframework:spring-web:5.3.21")
                ksp("site.addzero:spring2ktor-server-processor:2026.03.13")
            }
        """.trimIndent()
    }

    private fun renderPluginClientGradle(variables: Map<String, String>): String {
        return """
            plugins {
                id("site.addzero.buildlogic.kmp.kmp-core")
            }

            kotlin {
                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":spi"))
                    }
                }
            }
        """.trimIndent()
    }

    private fun renderPluginSpiGradle(variables: Map<String, String>): String {
        return """
            plugins {
                id("site.addzero.buildlogic.kmp.kmp-core")
                id("site.addzero.buildlogic.kmp.kmp-json-withtool")
            }
        """.trimIndent()
    }

    private fun renderClientPlaceholder(variables: Map<String, String>): String {
        val packageName = variables.getValue("packageName")
        val contextName = variables.getValue("ContextName")
        return """
            package $packageName.client

            import $packageName.metadata.GeneratedMetadataIndex

            object ${contextName}ClientFeature {
                fun metadataSummary(): String {
                    val descriptor = GeneratedMetadataIndex.findContext("${variables.getValue("contextCode")}") ?: return "missing"
                    return "${'$'}{descriptor.name}:${'$'}{descriptor.models.size}:${'$'}{descriptor.dtos.size}"
                }
            }
        """.trimIndent()
    }

    private fun renderSettingsSnippet(variables: Map<String, String>): String {
        return """includeBuild("plugins/${variables.getValue("contextCode")}")"""
    }

    private fun mapType(type: FieldType, nullable: Boolean): String {
        val base = when (type) {
            FieldType.STRING, FieldType.TEXT, FieldType.ENUM, FieldType.JSON, FieldType.UUID, FieldType.REFERENCE -> "String"
            FieldType.BOOLEAN -> "Boolean"
            FieldType.INT -> "Int"
            FieldType.LONG -> "Long"
            FieldType.DECIMAL -> "Double"
            FieldType.DATE -> "String"
            FieldType.DATETIME -> "String"
        }
        return if (nullable) "$base?" else base
    }
}
