package site.addzero.coding.playground.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

class GeneratedCodeIndexProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    companion object {
        private const val annotationFqName = "site.addzero.coding.playground.annotations.GeneratedManagedDeclaration"
        private const val optionPackage = "coding.playground.indexPackage"
        private const val defaultPackage = "site.addzero.coding.playground.generated"
    }

    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) {
            return emptyList()
        }

        val symbols = resolver.getSymbolsWithAnnotation(annotationFqName).toList()
        val deferred = symbols.filterNot { it is KSClassDeclaration && it.validate() }
        val declarations = symbols.filterIsInstance<KSClassDeclaration>().filter { it.validate() }
        processed = deferred.isEmpty()

        if (declarations.isEmpty()) {
            return deferred
        }

        var hasError = false
        val records = mutableListOf<DeclarationRecord>()
        val declarationIds = linkedSetOf<String>()
        val fqNames = linkedSetOf<String>()

        declarations.forEach { declaration ->
            val record = declaration.toRecord()
            if (record == null) {
                hasError = true
                return@forEach
            }

            if (!declarationIds.add(record.declarationId)) {
                logger.error("重复的 declarationId：${record.declarationId}", declaration)
                hasError = true
            }
            if (!fqNames.add(record.fqName)) {
                logger.error("重复的全限定名：${record.fqName}", declaration)
                hasError = true
            }

            records += record
        }

        if (hasError) {
            return deferred
        }

        val packageName = options[optionPackage]?.takeIf { it.isNotBlank() }
            ?: records.firstOrNull()?.packageName
            ?: defaultPackage

        val sourceFiles = declarations.mapNotNull { it.containingFile }.distinct().toTypedArray()
        buildGeneratedCodeIndex(packageName, records).writeTo(
            codeGenerator = codeGenerator,
            dependencies = Dependencies(aggregating = true, *sourceFiles),
        )
        return deferred
    }

    private fun buildGeneratedCodeIndex(
        packageName: String,
        records: List<DeclarationRecord>,
    ): com.squareup.kotlinpoet.FileSpec {
        val contractClass = ClassName("site.addzero.coding.playground.annotations", "GeneratedCodeIndexContract")
        val fileDescriptorClass = ClassName("site.addzero.coding.playground.annotations", "GeneratedCodeFileDescriptor")
        val declarationDescriptorClass = ClassName("site.addzero.coding.playground.annotations", "GeneratedCodeDeclarationDescriptor")
        val fileDescriptorList = LIST.parameterizedBy(fileDescriptorClass)
        val declarationDescriptorList = LIST.parameterizedBy(declarationDescriptorClass)
        val fileRecords = records.groupBy { it.fileId }.values.map { group ->
            FileRecord(
                fileId = group.first().fileId,
                targetId = group.first().targetId,
                packageName = group.first().packageName,
                fileName = group.first().fileName,
                declarationIds = group.map { it.declarationId },
            )
        }

        val objectSpec = TypeSpec.objectBuilder("GeneratedCodeIndex")
            .addSuperinterface(contractClass)
            .addProperty(
                PropertySpec.builder("fileDescriptors", fileDescriptorList, KModifier.PRIVATE)
                    .initializer(buildFileDescriptorInitializer(fileRecords, fileDescriptorClass))
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("declarationDescriptors", declarationDescriptorList, KModifier.PRIVATE)
                    .initializer(buildDeclarationDescriptorInitializer(records, declarationDescriptorClass))
                    .build(),
            )
            .addFunction(
                FunSpec.builder("files")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(fileDescriptorList)
                    .addStatement("return fileDescriptors")
                    .build(),
            )
            .addFunction(
                FunSpec.builder("declarations")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(declarationDescriptorList)
                    .addStatement("return declarationDescriptors")
                    .build(),
            )
            .addFunction(
                FunSpec.builder("findByFqName")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("fqName", String::class)
                    .returns(declarationDescriptorClass.copy(nullable = true))
                    .addStatement("return declarationDescriptors.firstOrNull { it.fqName == fqName }")
                    .build(),
            )
            .addFunction(
                FunSpec.builder("findByDeclarationId")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("declarationId", String::class)
                    .returns(declarationDescriptorClass.copy(nullable = true))
                    .addStatement("return declarationDescriptors.firstOrNull { it.declarationId == declarationId }")
                    .build(),
            )
            .build()

        return com.squareup.kotlinpoet.FileSpec.builder(packageName, "GeneratedCodeIndex")
            .addType(objectSpec)
            .build()
    }

    private fun buildFileDescriptorInitializer(
        records: List<FileRecord>,
        descriptorClass: ClassName,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            add("listOf(\n")
            records.forEachIndexed { index, record ->
                add(
                    "    %T(fileId = %S, targetId = %S, packageName = %S, fileName = %S, declarationIds = listOf(",
                    descriptorClass,
                    record.fileId,
                    record.targetId,
                    record.packageName,
                    record.fileName,
                )
                record.declarationIds.forEachIndexed { declarationIndex, declarationId ->
                    if (declarationIndex > 0) {
                        add(", ")
                    }
                    add("%S", declarationId)
                }
                add("))")
                if (index < records.lastIndex) {
                    add(",")
                }
                add("\n")
            }
            add(")")
        }.build()
    }

    private fun buildDeclarationDescriptorInitializer(
        records: List<DeclarationRecord>,
        descriptorClass: ClassName,
    ): CodeBlock {
        return CodeBlock.builder().apply {
            add("listOf(\n")
            records.forEachIndexed { index, record ->
                add(
                    "    %T(declarationId = %S, fileId = %S, fqName = %S, kind = %S, presetType = %S)",
                    descriptorClass,
                    record.declarationId,
                    record.fileId,
                    record.fqName,
                    record.kind,
                    record.presetType,
                )
                if (index < records.lastIndex) {
                    add(",")
                }
                add("\n")
            }
            add(")")
        }.build()
    }

    private fun KSClassDeclaration.toRecord(): DeclarationRecord? {
        val annotation = annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationFqName
        } ?: run {
            logger.error("缺少托管注解：$annotationFqName", this)
            return null
        }

        val args = annotation.arguments.associateBy({ it.name?.asString().orEmpty() }, { it.value })
        val declarationId = args["declarationId"] as? String
        val fileId = args["fileId"] as? String
        val presetType = args["presetType"] as? String
        if (declarationId.isNullOrBlank() || fileId.isNullOrBlank() || presetType.isNullOrBlank()) {
            logger.error("托管注解参数不完整，至少需要 declarationId、fileId、presetType", this)
            return null
        }

        val fqName = qualifiedName?.asString()
        if (fqName.isNullOrBlank()) {
            logger.error("托管声明必须具有全限定名", this)
            return null
        }

        val containingFile = containingFile
        if (containingFile == null) {
            logger.error("无法定位声明所在文件", this)
            return null
        }

        return DeclarationRecord(
            declarationId = declarationId,
            fileId = fileId,
            targetId = args["targetId"] as? String ?: "",
            fqName = fqName,
            packageName = packageName.asString(),
            fileName = containingFile.fileName,
            kind = kindName(),
            presetType = presetType,
        )
    }

    private fun KSClassDeclaration.kindName(): String {
        return when (classKind) {
            ClassKind.INTERFACE -> "INTERFACE"
            ClassKind.OBJECT -> "OBJECT"
            ClassKind.ENUM_CLASS -> "ENUM_CLASS"
            ClassKind.ANNOTATION_CLASS -> "ANNOTATION_CLASS"
            ClassKind.CLASS -> if (Modifier.DATA in modifiers) "DATA_CLASS" else "CLASS"
            else -> classKind.name
        }
    }
}

private data class DeclarationRecord(
    val declarationId: String,
    val fileId: String,
    val targetId: String,
    val fqName: String,
    val packageName: String,
    val fileName: String,
    val kind: String,
    val presetType: String,
)

private data class FileRecord(
    val fileId: String,
    val targetId: String,
    val packageName: String,
    val fileName: String,
    val declarationIds: List<String>,
)
