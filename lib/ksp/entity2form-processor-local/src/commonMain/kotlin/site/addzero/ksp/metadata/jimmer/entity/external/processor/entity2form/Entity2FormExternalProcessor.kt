package site.addzero.ksp.metadata.jimmer.entity.external.processor.entity2form

import site.addzero.entity2form.processor.context.Settings
import site.addzero.generator.FormCodeGenerator
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessContext
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorIds
import site.addzero.lsi.processor.ProcessorSpi

class Entity2FormExternalProcessor : ProcessorSpi<JimmerEntityProcessContext, Unit> {
    override val id: String = JimmerEntityProcessorIds.ENTITY2_FORM
    override val dependsOn: Set<String> = setOf(JimmerEntityProcessorIds.ENTITY2_ISO)
    override lateinit var ctx: JimmerEntityProcessContext

    override fun onFinish() {
        val context = ctx
        if (context.entities.isEmpty()) {
            return
        }

        Settings.fromOptions(context.options)
        val outputDir = withPkg(Settings.sharedComposeSourceDir, Settings.formPackageName)
        val generatedFormClasses = mutableSetOf<String>()
        val formCodeGenerator = FormCodeGenerator(context.logger)

        context.logger.warn("开始生成表单类...")
        context.entities
            .asSequence()
            .sortedBy { it.qualifiedName }
            .forEach { entity ->
                if (!generatedFormClasses.add(entity.qualifiedName)) {
                    return@forEach
                }
                try {
                    formCodeGenerator.writeFormFileWithStrategy(entity, outputDir, Settings.formPackageName)
                } catch (error: Exception) {
                    context.logger.error("生成表单失败: ${entity.simpleName}, 错误: ${error.message}")
                }
            }
        context.logger.warn("表单类生成完成，共生成 ${generatedFormClasses.size} 个")
    }

    private fun withPkg(root: String, pkg: String): String {
        return "$root/${pkg.replace(".", "/")}"
    }
}
