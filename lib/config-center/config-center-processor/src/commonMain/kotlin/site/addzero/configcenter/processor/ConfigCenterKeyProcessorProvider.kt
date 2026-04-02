package site.addzero.configcenter.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import site.addzero.configcenter.ConfigCenterDefinitionProvider
import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterKeyDefinition
import site.addzero.configcenter.ConfigCenterNamespace

class ConfigCenterKeyProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment,
    ): SymbolProcessor {
        return ConfigCenterKeyProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
    }
}

private class ConfigCenterKeyProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(
        resolver: Resolver,
    ): List<KSAnnotated> {
        val annotationName = ConfigCenterNamespace::class.qualifiedName.orEmpty()
        resolver.getSymbolsWithAnnotation(annotationName)
            .filterIsInstance<KSClassDeclaration>()
            .forEach(::generateSpec)
        return emptyList()
    }

    private fun generateSpec(
        declaration: KSClassDeclaration,
    ) {
        val namespaceAnnotation = declaration.findAnnotation(ConfigCenterNamespace::class.qualifiedName.orEmpty())
            ?: return
        val namespace = namespaceAnnotation.argumentValue("namespace")
            ?.toString()
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: run {
                logger.error("@ConfigCenterNamespace.namespace 不能为空", declaration)
                return
            }

        val packageName = declaration.packageName.asString()
        val simpleName = declaration.simpleName.asString().trim()
        val generatedObjectName = namespaceAnnotation.argumentValue("objectName")
            ?.toString()
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: "${simpleName}Keys"
        val generatedProviderName = namespaceAnnotation.argumentValue("providerName")
            ?.toString()
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: "${generatedObjectName}Provider"

        val items = declaration.getAllProperties()
            .mapNotNull { property -> property.toModelOrNull(logger) }
            .sortedBy(ConfigItemModel::key)

        val objectBuilder = TypeSpec.objectBuilder(generatedObjectName)
            .addModifiers(KModifier.PUBLIC)
            .addProperty(
                PropertySpec.builder("NAMESPACE", STRING)
                    .addModifiers(KModifier.CONST)
                    .initializer("%S", namespace)
                    .build(),
            )

        val definitionClassName = ConfigCenterKeyDefinition::class.asTypeName()
        val allDefinitionReferences = mutableListOf<CodeBlock>()
        var hasTemplateItem = false
        items.forEach { item ->
            objectBuilder.addProperty(
                PropertySpec.builder(item.constantName, STRING)
                    .addModifiers(KModifier.CONST)
                    .initializer("%S", item.key)
                    .build(),
            )
            if (item.templateParameters.isEmpty()) {
                objectBuilder.addProperty(
                    PropertySpec.builder(item.propertyName, definitionClassName)
                        .initializer(item.definitionInitializerCode(item.constantName))
                        .build(),
                )
                allDefinitionReferences += CodeBlock.of(item.propertyName)
            } else {
                hasTemplateItem = true
                objectBuilder.addProperty(
                    PropertySpec.builder(item.templatePropertyName, definitionClassName)
                        .initializer(item.definitionInitializerCode(item.constantName))
                        .build(),
                )
                objectBuilder.addFunction(
                    FunSpec.builder(item.propertyName)
                        .addParameters(
                            item.templateParameters.map { parameterName ->
                                ParameterSpec.builder(parameterName, STRING).build()
                            },
                        )
                        .returns(STRING)
                        .addStatement(
                            "return materializeTemplate(%L, %L)",
                            item.constantName,
                            item.templateParameters.joinToString(", ") { parameterName ->
                                "\"$parameterName\" to $parameterName"
                            },
                        )
                        .build(),
                )
                objectBuilder.addFunction(
                    FunSpec.builder(item.definitionFunctionName)
                        .addParameters(
                            item.templateParameters.map { parameterName ->
                                ParameterSpec.builder(parameterName, STRING).build()
                            },
                        )
                        .returns(definitionClassName)
                        .addStatement(
                            "return %L.copy(key = %L(%L))",
                            item.templatePropertyName,
                            item.propertyName,
                            item.templateParameters.joinToString(", "),
                        )
                        .build(),
                )
                allDefinitionReferences += CodeBlock.of(item.templatePropertyName)
            }
        }

        if (hasTemplateItem) {
            objectBuilder.addFunction(
                FunSpec.builder("materializeTemplate")
                    .addModifiers(KModifier.PRIVATE)
                    .addParameter("template", STRING)
                    .addParameter(
                        ParameterSpec.builder(
                            "arguments",
                            ClassName("kotlin", "Pair").parameterizedBy(STRING, STRING),
                        ).addModifiers(KModifier.VARARG).build(),
                    )
                    .returns(STRING)
                    .addStatement("var resolved = template")
                    .beginControlFlow("arguments.forEach { (name, value) ->")
                    .addStatement("resolved = resolved.replace(\"{\$name}\", value)")
                    .endControlFlow()
                    .addStatement("return resolved")
                    .build(),
            )
        }

        objectBuilder.addProperty(
            PropertySpec.builder(
                "all",
                LIST.parameterizedBy(ConfigCenterKeyDefinition::class.asTypeName()),
            ).initializer(
                if (allDefinitionReferences.isEmpty()) {
                    CodeBlock.of("emptyList()")
                } else {
                    CodeBlock.builder()
                        .add("listOf(\n")
                        .indent()
                        .add(allDefinitionReferences.joinToString(",\n") { "%L" }, *allDefinitionReferences.toTypedArray())
                        .add("\n")
                        .unindent()
                        .add(")")
                        .build()
                },
            ).build(),
        )

        val providerBuilder = TypeSpec.classBuilder(generatedProviderName)
            .addAnnotation(ClassName("org.koin.core.annotation", "Single"))
            .addSuperinterface(ConfigCenterDefinitionProvider::class)
            .addProperty(
                PropertySpec.builder(
                    "definitions",
                    LIST.parameterizedBy(ConfigCenterKeyDefinition::class.asTypeName()),
                )
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%L.all", generatedObjectName)
                    .build(),
            )

        FileSpec.builder(packageName, generatedObjectName)
            .addType(objectBuilder.build())
            .addType(providerBuilder.build())
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                dependencies = Dependencies(
                    aggregating = false,
                    *declaration.originFiles(),
                ),
            )
    }
}

private data class ConfigItemModel(
    val propertyName: String,
    val constantName: String,
    val key: String,
    val comment: String?,
    val defaultValue: String?,
    val required: Boolean,
    val valueType: String,
) {
    val templatePropertyName: String
        get() = "${propertyName}Template"

    val definitionFunctionName: String
        get() = "${propertyName}Definition"

    fun definitionInitializerCode(
        keyReference: String,
    ): CodeBlock {
        return CodeBlock.of(
            "%T(namespace = NAMESPACE, key = %L, valueType = %S, comment = %L, defaultValue = %L, required = %L)",
            ConfigCenterKeyDefinition::class,
            keyReference,
            valueType,
            comment?.let { CodeBlock.of("%S", it) } ?: CodeBlock.of("null"),
            defaultValue?.let { CodeBlock.of("%S", it) } ?: CodeBlock.of("null"),
            required,
        )
    }

    val templateParameters: List<String>
        get() = key.findTemplateParameters()
}

private fun KSPropertyDeclaration.toModelOrNull(
    logger: KSPLogger,
): ConfigItemModel? {
    val annotation = findAnnotation(ConfigCenterItem::class.qualifiedName.orEmpty()) ?: return null
    val propertyName = simpleName.asString().trim()
    if (propertyName.isBlank()) {
        logger.error("@ConfigCenterItem 标注的属性名不能为空", this)
        return null
    }
    val key = annotation.argumentValue("key")
        ?.toString()
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: propertyName
    val comment = annotation.argumentValue("comment")
        ?.toString()
        ?.trim()
        ?.takeIf(String::isNotBlank)
    val defaultValue = annotation.argumentValue("defaultValue")
        ?.toString()
        ?.trim()
        ?.takeIf(String::isNotBlank)
    val required = annotation.argumentValue("required") as? Boolean ?: false
    val resolvedType = type.resolve()
    val valueType = if (resolvedType.arguments.isEmpty()) {
        resolvedType.declaration.qualifiedName?.asString() ?: resolvedType.toString()
    } else {
        resolvedType.toString()
    }
    val templateParameters = key.findTemplateParameters()
    return ConfigItemModel(
        propertyName = propertyName,
        constantName = if (templateParameters.isEmpty()) {
            propertyName.toUpperSnakeCase()
        } else {
            "${propertyName.toUpperSnakeCase()}_TEMPLATE"
        },
        key = key,
        comment = comment,
        defaultValue = defaultValue,
        required = required,
        valueType = valueType,
    )
}

private fun KSAnnotated.findAnnotation(
    qualifiedName: String,
): KSAnnotation? {
    return annotations.firstOrNull { annotation ->
        annotation.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
    }
}

private fun KSAnnotation.argumentValue(
    name: String,
): Any? {
    return arguments.firstOrNull { argument -> argument.name?.asString() == name }?.value
}

private fun KSClassDeclaration.originFiles(): Array<KSFile> {
    return containingFile?.let { file -> arrayOf(file) } ?: emptyArray()
}

private fun String.toUpperSnakeCase(): String {
    return replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
        .replace(Regex("[^A-Za-z0-9]+"), "_")
        .trim('_')
        .uppercase()
}

private fun String.findTemplateParameters(): List<String> {
    return TEMPLATE_PARAMETER_REGEX.findAll(this)
        .map { matchResult -> matchResult.groupValues[1] }
        .distinct()
        .toList()
}

private val TEMPLATE_PARAMETER_REGEX = Regex("\\{([A-Za-z_][A-Za-z0-9_]*)}")
