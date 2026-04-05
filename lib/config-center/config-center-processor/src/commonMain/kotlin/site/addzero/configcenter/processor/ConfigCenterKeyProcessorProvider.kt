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
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import site.addzero.configcenter.ConfigCenterItem
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
    private val declarations = linkedSetOf<KSClassDeclaration>()

    override fun process(
        resolver: Resolver,
    ): List<KSAnnotated> {
        val annotationName = ConfigCenterNamespace::class.qualifiedName.orEmpty()
        resolver.getSymbolsWithAnnotation(annotationName)
            .filterIsInstance<KSClassDeclaration>()
            .forEach(declarations::add)
        return emptyList()
    }

    override fun finish() {
        declarations.forEach(::generateSpec)
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

        items.forEach { item ->
            if (item.comment != null) {
                objectBuilder.addKdoc("")
            }
            val constantBuilder = PropertySpec.builder(item.constantName, STRING)
                .addModifiers(KModifier.CONST)
                .initializer("%S", item.key)
            item.comment?.let { comment ->
                constantBuilder.addKdoc("%L\n", comment)
            }
            objectBuilder.addProperty(constantBuilder.build())

            if (item.templateParameters.isNotEmpty()) {
                val functionBuilder = FunSpec.builder(item.propertyName)
                    .returns(STRING)
                    .addParameters(
                        item.templateParameters.map { parameterName ->
                            ParameterSpec.builder(parameterName, STRING).build()
                        },
                    )
                    .addStatement(
                        "return materializeTemplate(%L, %L)",
                        item.constantName,
                        item.templateParameters.joinToString(", ") { parameterName ->
                            "\"$parameterName\" to $parameterName"
                        },
                    )
                item.comment?.let { comment ->
                    functionBuilder.addKdoc("%L\n", comment)
                }
                objectBuilder.addFunction(functionBuilder.build())
            }
        }

        if (items.any { it.templateParameters.isNotEmpty() }) {
            objectBuilder.addFunction(
                FunSpec.builder("materializeTemplate")
                    .addModifiers(KModifier.PRIVATE)
                    .addParameter("template", STRING)
                    .addParameter("arguments", STRING, KModifier.VARARG)
                    .returns(STRING)
                    .addStatement("var resolved = template")
                    .beginControlFlow("arguments.forEach { argument ->")
                    .addStatement("val parts = argument.split('=', limit = 2)")
                    .addStatement("if (parts.size == 2) resolved = resolved.replace(\"{\${parts[0]}}\", parts[1])")
                    .endControlFlow()
                    .addStatement("return resolved")
                    .build(),
            )
        }

        FileSpec.builder(packageName, generatedObjectName)
            .addType(objectBuilder.build())
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
) {
    val templateParameters
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
