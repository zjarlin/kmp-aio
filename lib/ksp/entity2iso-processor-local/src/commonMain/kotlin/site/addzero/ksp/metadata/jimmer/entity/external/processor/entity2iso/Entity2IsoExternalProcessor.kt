package site.addzero.ksp.metadata.jimmer.entity.external.processor.entity2iso

import generator.IsoCodeGenerator
import site.addzero.entity2iso.processor.context.Settings
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessContext
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorIds
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorOptions
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerGeneratedSourceWriter
import site.addzero.lsi.processor.ProcessorSpi

class Entity2IsoExternalProcessor : ProcessorSpi<JimmerEntityProcessContext, Unit> {
    override val id: String = JimmerEntityProcessorIds.ENTITY2_ISO
    override lateinit var ctx: JimmerEntityProcessContext

    override fun onFinish() {
        val context = ctx
        if (context.entities.isEmpty()) {
            return
        }

        Settings.fromOptions(context.options)
        val packageName =
            context.options[JimmerEntityProcessorOptions.ISO_PACKAGE_NAME]
                ?.takeIf(String::isNotBlank)
                ?: context.options[JimmerEntityProcessorOptions.ISO_PACKAGE_NAME_LEGACY]
                    ?.takeIf(String::isNotBlank)
                ?: Settings.isomorphicPkg
        val outputDir =
            context.options[JimmerEntityProcessorOptions.ISO_OUTPUT_DIR]
                ?.takeIf(String::isNotBlank)
                ?: Settings.isomorphicGenDir
        val classSuffix =
            context.options[JimmerEntityProcessorOptions.ISO_CLASS_SUFFIX]
                ?.takeIf(String::isNotBlank)
                ?: "Iso"
        val serializableEnabled =
            context.options[JimmerEntityProcessorOptions.ISO_SERIALIZABLE_ENABLED]
                ?.equals("true", ignoreCase = true)
                ?: Settings.isomorphicSerializableEnabled

        context.logger.warn("开始生成同构体类...")
        var generatedCount = 0

        context.entities
            .asSequence()
            .sortedBy { it.qualifiedName }
            .forEach { entity ->
                try {
                    val isoCode =
                        IsoCodeGenerator.generateIsoCode(
                            entity = entity,
                            packageName = packageName,
                            classSuffix = classSuffix,
                            serializableEnabled = serializableEnabled,
                        )
                    JimmerGeneratedSourceWriter.writeKotlinFile(
                        rootOutputDir = outputDir,
                        packageName = packageName,
                        fileName = "${entity.simpleName}$classSuffix.kt",
                        content = isoCode,
                    )
                    generatedCount++
                } catch (error: Exception) {
                    context.logger.error("生成同构体失败: ${entity.simpleName}, 错误: ${error.message}")
                }
            }

        context.logger.warn("同构体类生成完成，共生成 $generatedCount 个")
    }
}
