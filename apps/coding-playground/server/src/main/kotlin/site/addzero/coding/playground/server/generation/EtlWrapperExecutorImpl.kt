package site.addzero.coding.playground.server.generation

import org.koin.core.annotation.Single
import kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory
import site.addzero.coding.playground.shared.dto.EtlWrapperMetaDto
import site.addzero.coding.playground.shared.dto.GenerationTargetMetaDto
import site.addzero.coding.playground.shared.dto.RenderedTemplateDto
import site.addzero.coding.playground.shared.dto.TemplateMetaDto
import site.addzero.coding.playground.shared.service.EtlWrapperExecutor

@Single
class EtlWrapperExecutorImpl : EtlWrapperExecutor {
    override suspend fun apply(
        wrapper: EtlWrapperMetaDto?,
        rendered: RenderedTemplateDto,
        template: TemplateMetaDto?,
        target: GenerationTargetMetaDto?,
        variables: Map<String, String>,
    ): RenderedTemplateDto {
        if (wrapper == null || !wrapper.enabled) {
            return rendered
        }
        val scriptEngine = KotlinJsr223DefaultScriptEngineFactory().scriptEngine
        scriptEngine.put("__content", rendered.content)
        scriptEngine.put("__template", template)
        scriptEngine.put("__target", target)
        scriptEngine.put("__variables", variables)
        val script = buildString {
            appendLine("import site.addzero.coding.playground.shared.dto.GenerationTargetMetaDto")
            appendLine("import site.addzero.coding.playground.shared.dto.TemplateMetaDto")
            appendLine()
            appendLine("fun __run(")
            appendLine("    content: String,")
            appendLine("    template: TemplateMetaDto?,")
            appendLine("    target: GenerationTargetMetaDto?,")
            appendLine("    variables: Map<String, String>,")
            appendLine("): String {")
            appendLine(wrapper.scriptBody.prependIndent("    "))
            appendLine("}")
            appendLine()
            appendLine("__run(")
            appendLine("    bindings[\"__content\"] as String,")
            appendLine("    bindings[\"__template\"] as TemplateMetaDto?,")
            appendLine("    bindings[\"__target\"] as GenerationTargetMetaDto?,")
            appendLine("    bindings[\"__variables\"] as Map<String, String>,")
            appendLine(")")
        }
        val transformed = try {
            scriptEngine.eval(script)
        } catch (throwable: Throwable) {
            throw IllegalStateException(
                "ETL script '${wrapper.key}' failed: ${throwable.message.orEmpty()}",
                throwable,
            )
        }
        if (transformed !is String) {
            throw IllegalStateException("ETL script '${wrapper.key}' must return String")
        }
        return rendered.copy(content = transformed)
    }
}
