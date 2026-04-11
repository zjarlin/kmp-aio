package site.addzero.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import java.io.File
import site.addzero.controller2iso2dataprovider.processor.context.Settings

class Controller2Iso2DataProviderProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return Controller2Iso2DataProviderProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options,
        )
    }
}

/**
 * 扫描实现 BaseTreeApi<E> 的控制器，生成 Iso 到 tree API 的映射表。
 */
class Controller2Iso2DataProviderProcessor(
    @Suppress("unused")
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        Settings.fromOptions(options)

        val controllers =
            resolver.getSymbolsWithAnnotation("org.springframework.web.bind.annotation.RestController")
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.validate() }
                .toList()

        if (controllers.isEmpty()) {
            logger.info("未找到任何 @RestController 注解的类")
            return emptyList()
        }

        val controllerInfos =
            controllers
                .filter(::isBaseTreeApiImplementation)
                .mapNotNull(::extractControllerInfo)

        if (controllerInfos.isEmpty()) {
            logger.info("未找到任何 BaseTreeApi 实现")
            return emptyList()
        }

        generateIso2DataProvider(controllerInfos)
        return emptyList()
    }

    private fun isBaseTreeApiImplementation(controller: KSClassDeclaration): Boolean {
        return controller.superTypes.any { superType ->
            val declaration = superType.resolve().declaration
            declaration.qualifiedName?.asString()?.contains("BaseTreeApi") == true
        }
    }

    private fun extractControllerInfo(controller: KSClassDeclaration): ControllerInfo? {
        val baseTreeApiSuperType =
            controller.superTypes.firstOrNull { superType ->
                val declaration = superType.resolve().declaration
                declaration.qualifiedName?.asString()?.contains("BaseTreeApi") == true
            }
                ?: return null

        val entityType = baseTreeApiSuperType.resolve().arguments.firstOrNull()?.type?.resolve() ?: return null
        val entityClassName = entityType.declaration.simpleName.asString()
        val isoClassName = if (entityClassName.endsWith("Iso")) entityClassName else "${entityClassName}Iso"

        return ControllerInfo(
            entityClassName = entityClassName,
            isoQualifiedName = "${Settings.isomorphicPackageName}.$isoClassName",
        )
    }

    private fun generateIso2DataProvider(controllerInfos: List<ControllerInfo>) {
        val packageName =
            options["iso2DataProviderPackage"]?.takeIf(String::isNotBlank)
                ?: Settings.iso2DataProviderPackage

        val outputDir = File(Settings.sharedComposeSourceDir, packageName.replace(".", "/"))
        outputDir.mkdirs()

        val imports =
            controllerInfos.joinToString("\n") { controllerInfo ->
                "import ${controllerInfo.isoQualifiedName}"
            }

        val mappings =
            controllerInfos.joinToString(",\n") { controllerInfo ->
                val isoClassName = controllerInfo.isoQualifiedName.substringAfterLast(".")
                val apiMethodName = controllerInfo.entityClassName.replaceFirstChar { it.lowercase() }
                "        ${isoClassName}::class to ${Settings.apiClientAggregatorObjectName}.${apiMethodName}Api::tree"
            }

        val code =
            """
            |package $packageName
            |
            |import ${Settings.apiClientPackageName}.${Settings.apiClientAggregatorObjectName}
            |$imports
            |
            |object Iso2DataProvider {
            |    val isoToDataProvider = mapOf(
            |$mappings
            |    )
            |}
            """.trimMargin()

        File(outputDir, "Iso2DataProvider.kt").writeText(code)
        logger.info("生成 Iso2DataProvider.kt，包含 ${controllerInfos.size} 个映射")
    }

    private data class ControllerInfo(
        val entityClassName: String,
        val isoQualifiedName: String,
    )
}
