package site.addzero.generator

import com.google.devtools.ksp.processing.KSPLogger
import site.addzero.entity2form.processor.context.Settings
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerGeneratedSourceWriter
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeKind
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeRef

/**
 * 生成简化可编译的表单源码。
 */
class FormCodeGenerator(
    private val logger: KSPLogger,
) {
    fun generateFormCodeWithStrategy(
        entity: JimmerEntityMeta,
        packageName: String = "site.addzero.forms",
    ): String {
        val entityClassName = entity.simpleName
        val isoClassName = "${entityClassName}Iso"
        val properties = collectProperties(entity)
        val formFields = properties.joinToString(",\n") { property -> generateFieldCode(entityClassName, property) }
        val formProps = generateFormPropsWithStrategy(entityClassName, properties)

        return """
            |package $packageName
            |
            |import androidx.compose.runtime.*
            |import site.addzero.component.high_level.AddMultiColumnContainer
            |import site.addzero.component.drawer.AddDrawer
            |import site.addzero.component.form.date.AddDateField
            |import site.addzero.component.form.switch.AddSwitchField
            |import site.addzero.component.form.text.AddTextField
            |import ${Settings.isomorphicPackageName}.*
            |import ${Settings.enumOutputPackage}.*
            |
            |$formProps
            |
            |@Composable
            |fun ${entityClassName}Form(
            |    state: MutableState<${isoClassName}>,
            |    visible: Boolean,
            |    title: String,
            |    onClose: () -> Unit,
            |    onSubmit: () -> Unit,
            |    confirmEnabled: Boolean = true,
            |    dslConfig: ${entityClassName}FormDsl.() -> Unit = {}
            |) {
            |    AddDrawer(
            |        visible = visible,
            |        title = title,
            |        onClose = onClose,
            |        onSubmit = onSubmit,
            |        confirmEnabled = confirmEnabled,
            |    ) {
            |        ${entityClassName}FormOriginal(state, dslConfig)
            |    }
            |}
            |
            |@Composable
            |fun ${entityClassName}FormOriginal(
            |    state: MutableState<${isoClassName}>,
            |    dslConfig: ${entityClassName}FormDsl.() -> Unit = {}
            |) {
            |    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
            |    val dsl = ${entityClassName}FormDsl(state, renderMap).apply(dslConfig)
            |    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
            |$formFields
            |    )
            |
            |    val finalItems = remember(renderMap, dsl.hiddenFields, dsl.fieldOrder) {
            |        val orderedFieldNames = if (dsl.fieldOrder.isNotEmpty()) dsl.fieldOrder else defaultRenderMap.keys.toList()
            |        orderedFieldNames
            |            .filterNot { it in dsl.hiddenFields }
            |            .mapNotNull { fieldName -> renderMap[fieldName] ?: defaultRenderMap[fieldName] }
            |    }
            |
            |    AddMultiColumnContainer(
            |        howMuchColumn = 2,
            |        items = finalItems,
            |    )
            |}
            |
            |class ${entityClassName}FormDsl(
            |    val state: MutableState<${isoClassName}>,
            |    private val renderMap: MutableMap<String, @Composable () -> Unit>,
            |) {
            |    val hiddenFields = mutableSetOf<String>()
            |    val fieldOrder = mutableListOf<String>()
            |    private val fieldOrderMap = mutableMapOf<String, Int>()
            |
            |${generateDslMethodsWithStrategy(entityClassName, properties)}
            |
            |    fun hide(vararg fields: String) {
            |        hiddenFields.addAll(fields)
            |    }
            |
            |    fun order(vararg fields: String) {
            |        fieldOrder.clear()
            |        fieldOrder.addAll(fields)
            |    }
            |
            |    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
            |        fieldOrderMap[fieldName] = orderValue
            |        val allFields = ${entityClassName}FormProps.getAllFields()
            |        val sortedFields = allFields.sortedWith { field1, field2 ->
            |            val order1 = fieldOrderMap[field1] ?: Int.MAX_VALUE
            |            val order2 = fieldOrderMap[field2] ?: Int.MAX_VALUE
            |            when {
            |                order1 != Int.MAX_VALUE && order2 != Int.MAX_VALUE -> order1.compareTo(order2)
            |                order1 != Int.MAX_VALUE -> -1
            |                order2 != Int.MAX_VALUE -> 1
            |                else -> allFields.indexOf(field1).compareTo(allFields.indexOf(field2))
            |            }
            |        }
            |        fieldOrder.clear()
            |        fieldOrder.addAll(sortedFields)
            |    }
            |}
            |
            |@Composable
            |fun remember${entityClassName}FormState(current: ${isoClassName}? = null): MutableState<${isoClassName}> {
            |    return remember(current) { mutableStateOf(current ?: ${isoClassName}()) }
            |}
        """.trimMargin()
    }

    fun writeFormFileWithStrategy(
        entity: JimmerEntityMeta,
        outputDir: String,
        packageName: String,
    ) {
        val formCode = generateFormCodeWithStrategy(entity, packageName)
        val fileName = "${entity.simpleName}Form.kt"
        val file = JimmerGeneratedSourceWriter.writeKotlinFile(outputDir, packageName, fileName, formCode)
        logger.info("生成表单文件: ${file.absolutePath}")
    }

    private fun collectProperties(entity: JimmerEntityMeta): List<PropertyModel> {
        val baseEntityFields = setOf("id", "createTime", "updateTime", "createBy", "updateBy", "deleted", "version", "tenantId")
        val byName = linkedMapOf<String, PropertyModel>()
        entity.properties.forEach { property ->
            if (property.name in baseEntityFields || property.formIgnored) {
                return@forEach
            }
            byName[property.name] = createPropertyModel(property.name, property.type, property.type.nullable, property.docComment)
        }
        return byName.values.toList()
    }

    private fun createPropertyModel(
        name: String,
        type: JimmerTypeRef,
        nullable: Boolean,
        comment: String?,
    ): PropertyModel {
        val label = comment?.lineSequence()?.firstOrNull()?.trim()?.takeIf(String::isNotBlank) ?: name
        return PropertyModel(
            name = name,
            label = label,
            nullable = nullable,
            kind = classifyFieldKind(type),
            typeSimpleName = type.simpleName,
        )
    }

    private fun generateFieldCode(
        entityClassName: String,
        property: PropertyModel,
    ): String {
        val name = property.name
        val label = quote(property.label)
        val isRequired = !property.nullable
        return when (property.kind) {
            FieldKind.BOOLEAN ->
                """
                |        ${entityClassName}FormProps.$name to {
                |            AddSwitchField(
                |                value = state.value.$name ?: false,
                |                onValueChange = { state.value = state.value.copy($name = it) },
                |                label = $label
                |            )
                |        }
                """.trimMargin()

            FieldKind.DATE ->
                """
                |        ${entityClassName}FormProps.$name to {
                |            AddDateField(
                |                value = state.value.$name,
                |                onValueChange = { value ->
                |                    ${if (property.nullable) {
                    "state.value = state.value.copy($name = value)"
                } else {
                    "if (value != null) state.value = state.value.copy($name = value)"
                }}
                |                },
                |                label = $label,
                |                isRequired = $isRequired,
                |            )
                |        }
                """.trimMargin()

            FieldKind.STRING ->
                """
                |        ${entityClassName}FormProps.$name to {
                |            AddTextField(
                |                value = state.value.$name?.toString() ?: "",
                |                onValueChange = { value ->
                |                    state.value = state.value.copy($name = ${if (property.nullable) "value.ifEmpty { null }" else "value"})
                |                },
                |                label = $label,
                |                isRequired = $isRequired
                |            )
                |        }
                """.trimMargin()

            FieldKind.LONG ->
                generateNumericFieldCode(
                    entityClassName = entityClassName,
                    property = property,
                    parseExpression = "value.toLongOrNull()",
                )

            FieldKind.INT ->
                generateNumericFieldCode(
                    entityClassName = entityClassName,
                    property = property,
                    parseExpression = "value.toIntOrNull()",
                )

            FieldKind.SHORT ->
                generateNumericFieldCode(
                    entityClassName = entityClassName,
                    property = property,
                    parseExpression = "value.toShortOrNull()",
                )

            FieldKind.BYTE ->
                generateNumericFieldCode(
                    entityClassName = entityClassName,
                    property = property,
                    parseExpression = "value.toByteOrNull()",
                )

            FieldKind.FLOAT ->
                generateNumericFieldCode(
                    entityClassName = entityClassName,
                    property = property,
                    parseExpression = "value.toFloatOrNull()",
                )

            FieldKind.DOUBLE ->
                generateNumericFieldCode(
                    entityClassName = entityClassName,
                    property = property,
                    parseExpression = "value.toDoubleOrNull()",
                )

            FieldKind.ENUM ->
                generateEnumFieldCode(
                    entityClassName = entityClassName,
                    property = property,
                )

            FieldKind.READ_ONLY ->
                """
                |        ${entityClassName}FormProps.$name to {
                |            AddTextField(
                |                value = state.value.$name?.toString() ?: "",
                |                onValueChange = {},
                |                label = $label,
                |                isRequired = $isRequired,
                |                disable = true
                |            )
                |        }
                """.trimMargin()
        }
    }

    private fun generateNumericFieldCode(
        entityClassName: String,
        property: PropertyModel,
        parseExpression: String,
    ): String {
        val name = property.name
        val label = quote(property.label)
        val isRequired = !property.nullable
        val onValueChange =
            if (property.nullable) {
                """
                |                    val parsed = $parseExpression
                |                    when {
                |                        value.isEmpty() -> state.value = state.value.copy($name = null)
                |                        parsed != null -> state.value = state.value.copy($name = parsed)
                |                    }
                """.trimMargin()
            } else {
                """
                |                    val parsed = $parseExpression
                |                    if (parsed != null) {
                |                        state.value = state.value.copy($name = parsed)
                |                    }
                """.trimMargin()
            }

        return """
            |        ${entityClassName}FormProps.$name to {
            |            AddTextField(
            |                value = state.value.$name?.toString() ?: "",
            |                onValueChange = { value ->
            |$onValueChange
            |                },
            |                label = $label,
            |                isRequired = $isRequired
            |            )
            |        }
        """.trimMargin()
    }

    private fun generateEnumFieldCode(
        entityClassName: String,
        property: PropertyModel,
    ): String {
        val name = property.name
        val label = quote(property.label)
        val isRequired = !property.nullable
        val onValueChange =
            if (property.nullable) {
                """
                |                    val parsed = ${property.typeSimpleName}.entries.firstOrNull { entry -> entry.name == value }
                |                    when {
                |                        value.isEmpty() -> state.value = state.value.copy($name = null)
                |                        parsed != null -> state.value = state.value.copy($name = parsed)
                |                    }
                """.trimMargin()
            } else {
                """
                |                    val parsed = ${property.typeSimpleName}.entries.firstOrNull { entry -> entry.name == value }
                |                    if (parsed != null) {
                |                        state.value = state.value.copy($name = parsed)
                |                    }
                """.trimMargin()
            }

        return """
            |        ${entityClassName}FormProps.$name to {
            |            AddTextField(
            |                value = state.value.$name?.toString() ?: "",
            |                onValueChange = { value ->
            |$onValueChange
            |                },
            |                label = $label,
            |                isRequired = $isRequired
            |            )
            |        }
        """.trimMargin()
    }

    private fun generateFormPropsWithStrategy(
        entityClassName: String,
        properties: List<PropertyModel>,
    ): String {
        val propConstants = properties.joinToString("\n") { "    const val ${it.name} = \"${it.name}\"" }
        val allFieldsList = properties.joinToString(", ") { "\"${it.name}\"" }
        return """
            |/**
            | * $entityClassName 表单属性常量
            | */
            |object ${entityClassName}FormProps {
            |$propConstants
            |
            |    fun getAllFields(): List<String> = listOf($allFieldsList)
            |}
        """.trimMargin()
    }

    private fun generateDslMethodsWithStrategy(
        entityClassName: String,
        properties: List<PropertyModel>,
    ): String {
        return properties.joinToString("\n\n") { prop ->
            """
                |    fun ${prop.name}(
                |        hidden: Boolean = false,
                |        order: Int? = null,
                |        render: (@Composable (MutableState<${entityClassName}Iso>) -> Unit)? = null
                |    ) {
                |        when {
                |            hidden -> {
                |                hiddenFields.add("${prop.name}")
                |                renderMap.remove("${prop.name}")
                |            }
                |            render != null -> {
                |                hiddenFields.remove("${prop.name}")
                |                renderMap["${prop.name}"] = { render(state) }
                |            }
                |            else -> {
                |                hiddenFields.remove("${prop.name}")
                |                renderMap.remove("${prop.name}")
                |            }
                |        }
                |        order?.let { updateFieldOrder("${prop.name}", it) }
                |    }
            """.trimMargin()
        }
    }

    private fun classifyFieldKind(type: JimmerTypeRef): FieldKind {
        val typeNames = buildTypeNames(type)
        return when {
            type.kind == JimmerTypeKind.COLLECTION || type.kind == JimmerTypeKind.ARRAY || type.kind == JimmerTypeKind.ENTITY || type.kind == JimmerTypeKind.OTHER ->
                FieldKind.READ_ONLY
            type.kind == JimmerTypeKind.ENUM -> FieldKind.ENUM
            typeNames.any { it == "Boolean" || it == "kotlin.Boolean" || it == "java.lang.Boolean" || it == "boolean" } -> FieldKind.BOOLEAN
            typeNames.any { it == "String" || it == "kotlin.String" || it == "java.lang.String" || it == "BigDecimal" || it == "java.math.BigDecimal" } -> FieldKind.STRING
            typeNames.any { it == "Long" || it == "kotlin.Long" || it == "java.lang.Long" || it == "long" || it == "Date" || it == "java.util.Date" || it == "Timestamp" || it == "java.sql.Timestamp" } ->
                FieldKind.LONG
            typeNames.any { it == "Int" || it == "kotlin.Int" || it == "java.lang.Integer" || it == "int" } -> FieldKind.INT
            typeNames.any { it == "Short" || it == "kotlin.Short" || it == "java.lang.Short" || it == "short" } -> FieldKind.SHORT
            typeNames.any { it == "Byte" || it == "kotlin.Byte" || it == "java.lang.Byte" || it == "byte" } -> FieldKind.BYTE
            typeNames.any { it == "Float" || it == "kotlin.Float" || it == "java.lang.Float" || it == "float" } -> FieldKind.FLOAT
            typeNames.any { it == "Double" || it == "kotlin.Double" || it == "java.lang.Double" || it == "double" } -> FieldKind.DOUBLE
            typeNames.any { it.contains("LocalDate") && !it.contains("LocalDateTime") } -> FieldKind.DATE
            type.kind == JimmerTypeKind.DATE_TIME -> FieldKind.READ_ONLY
            else -> FieldKind.STRING
        }
    }

    private fun buildTypeNames(type: JimmerTypeRef): Set<String> {
        return buildSet {
            listOf(type.qualifiedName, type.simpleName, type.sourceTypeName)
                .filterNotNull()
                .filter(String::isNotBlank)
                .forEach { value ->
                    add(value)
                    add(value.substringAfterLast('.'))
                }
        }
    }

    private fun quote(value: String): String {
        val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
        return "\"$escaped\""
    }

    private enum class FieldKind {
        BOOLEAN,
        DATE,
        STRING,
        LONG,
        INT,
        SHORT,
        BYTE,
        FLOAT,
        DOUBLE,
        ENUM,
        READ_ONLY,
    }

    private data class PropertyModel(
        val name: String,
        val label: String,
        val nullable: Boolean,
        val kind: FieldKind,
        val typeSimpleName: String,
    )
}
