package generator

import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerPropertyMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeKind
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeRef

/**
 * 负责把 Jimmer 实体元数据转换成 `commonMain` 可编译的 Iso 数据类。
 */
object IsoCodeGenerator {
    fun generateIsoCode(
        entity: JimmerEntityMeta,
        packageName: String,
        classSuffix: String,
        serializableEnabled: Boolean = true,
    ): String {
        val propertyModels =
            entity.properties.map { property ->
                val typeResult =
                    buildIsoType(
                        type = property.type,
                        classSuffix = classSuffix,
                        serializableEnabled = serializableEnabled,
                    )
                val defaultValueResult = defaultValueFor(property, typeResult)
                val contextualAnnotation =
                    if (serializableEnabled && typeResult.contextual) "@Contextual " else ""
                val nullableSuffix = if (property.type.nullable) "?" else ""
                val declaration =
                    "    ${contextualAnnotation}val ${property.name}: ${typeResult.rendered}$nullableSuffix = ${defaultValueResult.code}"
                val propertyKDoc = renderKDoc(property.docComment, indent = "    ")

                PropertyModel(
                    code = listOfNotNull(propertyKDoc, declaration).joinToString("\n"),
                    imports = typeResult.imports + defaultValueResult.imports,
                )
            }

        val imports = propertyModels.flatMap { it.imports }.toMutableSet()
        if (serializableEnabled) {
            imports.add("import kotlinx.serialization.Serializable")
        }

        return buildString {
            appendLine("package $packageName")
            appendLine()
            if (imports.isNotEmpty()) {
                imports.sorted().forEach(::appendLine)
                appendLine()
            }
            renderKDoc(entity.docComment)?.let(::appendLine)
            if (serializableEnabled) {
                appendLine("@Serializable")
            }
            appendLine("data class ${entity.simpleName}$classSuffix(")
            appendLine(propertyModels.joinToString(",\n") { it.code })
            append(")")
        }.trimEnd()
    }

    private data class PropertyModel(
        val code: String,
        val imports: Set<String>,
    )

    private data class IsoTypeResult(
        val rendered: String,
        val imports: Set<String> = emptySet(),
        val kind: JimmerTypeKind = JimmerTypeKind.OTHER,
        val contextual: Boolean = false,
    )

    private data class DefaultValueResult(
        val code: String,
        val imports: Set<String> = emptySet(),
    )

    private fun buildIsoType(
        type: JimmerTypeRef,
        classSuffix: String,
        serializableEnabled: Boolean,
    ): IsoTypeResult {
        if (type.kind == JimmerTypeKind.ARRAY) {
            return buildIsoArrayType(type, classSuffix, serializableEnabled)
        }

        if (type.kind == JimmerTypeKind.COLLECTION) {
            val renderedArgs =
                type.typeArguments.map {
                    buildIsoType(
                        type = it,
                        classSuffix = classSuffix,
                        serializableEnabled = serializableEnabled,
                    )
                }
            val rendered =
                if (renderedArgs.isNotEmpty()) {
                    "${type.simpleName}<${renderedArgs.joinToString(", ") { it.rendered }}>"
                } else {
                    type.simpleName
                }
            return IsoTypeResult(
                rendered = rendered,
                imports = renderedArgs.flatMap { it.imports }.toSet(),
                kind = JimmerTypeKind.COLLECTION,
            )
        }

        mapToKotlinPrimitive(type.qualifiedName, type.simpleName)?.let { primitive ->
            return IsoTypeResult(rendered = primitive, kind = JimmerTypeKind.BASIC)
        }

        if (type.qualifiedName == "kotlin.String" || type.qualifiedName == "java.lang.String" || type.simpleName == "String") {
            return IsoTypeResult(rendered = "String", kind = JimmerTypeKind.BASIC)
        }

        if (type.qualifiedName == "java.math.BigDecimal" || type.simpleName == "BigDecimal") {
            return IsoTypeResult(rendered = "String", kind = JimmerTypeKind.BASIC)
        }

        mapToKotlinxDateTime(type.qualifiedName, type.simpleName)?.let { mapped ->
            val contextual = serializableEnabled && mapped in setOf("LocalDateTime", "LocalDate", "Instant")
            return IsoTypeResult(
                rendered = mapped,
                imports = setOf("import kotlinx.datetime.$mapped") +
                    if (contextual) setOf("import kotlinx.serialization.Contextual") else emptySet(),
                kind = JimmerTypeKind.DATE_TIME,
                contextual = contextual,
            )
        }

        if (type.qualifiedName in setOf("java.util.Date", "java.sql.Timestamp")) {
            return IsoTypeResult(rendered = "Long", kind = JimmerTypeKind.BASIC)
        }

        if (type.kind == JimmerTypeKind.ENUM) {
            val imports =
                type.qualifiedName
                    ?.takeIf(::shouldImport)
                    ?.let { setOf("import $it") }
                    .orEmpty()
            return IsoTypeResult(rendered = type.simpleName, imports = imports, kind = JimmerTypeKind.ENUM)
        }

        if (type.kind == JimmerTypeKind.ENTITY) {
            return IsoTypeResult(rendered = "${type.simpleName}$classSuffix", kind = JimmerTypeKind.ENTITY)
        }

        val imports =
            type.qualifiedName
                ?.takeIf(::shouldImport)
                ?.let { setOf("import $it") }
                .orEmpty()
        return IsoTypeResult(rendered = type.simpleName, imports = imports, kind = JimmerTypeKind.OTHER)
    }

    private fun buildIsoArrayType(
        type: JimmerTypeRef,
        classSuffix: String,
        serializableEnabled: Boolean,
    ): IsoTypeResult {
        if (type.simpleName != "Array" && (type.qualifiedName?.startsWith("kotlin.") == true)) {
            return IsoTypeResult(rendered = type.simpleName, kind = JimmerTypeKind.ARRAY)
        }

        val arg =
            type.typeArguments.firstOrNull()?.let {
                buildIsoType(
                    type = it,
                    classSuffix = classSuffix,
                    serializableEnabled = serializableEnabled,
                )
            }
        return IsoTypeResult(
            rendered = if (arg != null) "Array<${arg.rendered}>" else "Array<Any>",
            imports = arg?.imports.orEmpty(),
            kind = JimmerTypeKind.ARRAY,
        )
    }

    private fun defaultValueFor(
        property: JimmerPropertyMeta,
        type: IsoTypeResult,
    ): DefaultValueResult {
        if (property.type.nullable) {
            return DefaultValueResult("null")
        }

        return when (type.kind) {
            JimmerTypeKind.BASIC -> when (type.rendered) {
                "String" -> DefaultValueResult("\"\"")
                "Int" -> DefaultValueResult("0")
                "Long" -> DefaultValueResult("0L")
                "Double" -> DefaultValueResult("0.0")
                "Float" -> DefaultValueResult("0f")
                "Boolean" -> DefaultValueResult("false")
                "Byte" -> DefaultValueResult("0")
                "Short" -> DefaultValueResult("0")
                "Char" -> DefaultValueResult("' '")
                else -> DefaultValueResult("TODO()")
            }

            JimmerTypeKind.COLLECTION ->
                DefaultValueResult(
                    when {
                        type.rendered.startsWith("MutableList") -> "mutableListOf()"
                        type.rendered.startsWith("MutableSet") -> "mutableSetOf()"
                        type.rendered.startsWith("MutableMap") -> "mutableMapOf()"
                        type.rendered.startsWith("List") -> "emptyList()"
                        type.rendered.startsWith("Set") -> "emptySet()"
                        type.rendered.startsWith("Map") -> "emptyMap()"
                        else -> "emptyList()"
                    },
                )

            JimmerTypeKind.ARRAY -> DefaultValueResult("emptyArray()")
            JimmerTypeKind.ENUM -> DefaultValueResult("${type.rendered}.entries.first()")
            JimmerTypeKind.ENTITY -> DefaultValueResult("${type.rendered}()")
            JimmerTypeKind.DATE_TIME ->
                when (type.rendered) {
                    "Instant" -> DefaultValueResult("kotlinx.datetime.Clock.System.now()", setOf("import kotlinx.datetime.Clock"))
                    "LocalDateTime" ->
                        DefaultValueResult(
                            "kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())",
                            setOf("import kotlinx.datetime.Clock", "import kotlinx.datetime.TimeZone", "import kotlinx.datetime.toLocalDateTime"),
                        )
                    "LocalDate" ->
                        DefaultValueResult(
                            "kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())",
                            setOf("import kotlinx.datetime.Clock", "import kotlinx.datetime.TimeZone", "import kotlinx.datetime.todayIn"),
                        )
                    else -> DefaultValueResult("TODO()")
                }

            JimmerTypeKind.OTHER -> DefaultValueResult("TODO()")
        }
    }

    private fun renderKDoc(rawDocComment: String, indent: String = ""): String? {
        val lines =
            rawDocComment
                .trim()
                .removePrefix("/**")
                .removeSuffix("*/")
                .trim()
                .lines()
                .map { it.trim().removePrefix("*").trim() }
                .filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return null
        }
        return buildString {
            append(indent)
            appendLine("/**")
            lines.forEach { line ->
                append(indent)
                appendLine(" * $line")
            }
            append(indent)
            append(" */")
        }
    }

    private fun shouldImport(qualifiedName: String): Boolean {
        return !qualifiedName.startsWith("kotlin.") &&
            !qualifiedName.startsWith("kotlinx.datetime.") &&
            !qualifiedName.startsWith("java.lang.")
    }

    private fun mapToKotlinPrimitive(
        qualifiedName: String?,
        simpleName: String,
    ): String? {
        return when (qualifiedName ?: simpleName) {
            "kotlin.Int", "java.lang.Integer", "Int" -> "Int"
            "kotlin.Long", "java.lang.Long", "Long" -> "Long"
            "kotlin.Double", "java.lang.Double", "Double" -> "Double"
            "kotlin.Float", "java.lang.Float", "Float" -> "Float"
            "kotlin.Boolean", "java.lang.Boolean", "Boolean" -> "Boolean"
            "kotlin.Byte", "java.lang.Byte", "Byte" -> "Byte"
            "kotlin.Short", "java.lang.Short", "Short" -> "Short"
            "kotlin.Char", "java.lang.Character", "Char" -> "Char"
            else -> null
        }
    }

    private fun mapToKotlinxDateTime(
        qualifiedName: String?,
        simpleName: String,
    ): String? {
        return when (qualifiedName ?: simpleName) {
            "java.time.Instant", "Instant" -> "Instant"
            "java.time.LocalDateTime", "LocalDateTime" -> "LocalDateTime"
            "java.time.LocalDate", "LocalDate" -> "LocalDate"
            else -> null
        }
    }
}
