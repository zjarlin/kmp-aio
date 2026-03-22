package site.addzero.coding.playground.server.generation

import org.koin.core.annotation.Single
import site.addzero.coding.playground.shared.dto.EtlWrapperMetaDto
import site.addzero.coding.playground.shared.dto.RenderedTemplateDto
import site.addzero.coding.playground.shared.service.EtlWrapperExecutor

@Single
class EtlWrapperExecutorImpl : EtlWrapperExecutor {
    override suspend fun apply(
        wrapper: EtlWrapperMetaDto?,
        rendered: RenderedTemplateDto,
        variables: Map<String, String>,
    ): RenderedTemplateDto {
        if (wrapper == null || !wrapper.enabled) {
            return rendered
        }
        val transformed = if (wrapper.scriptBody.contains("{{content}}")) {
            wrapper.scriptBody.replace("{{content}}", rendered.content)
        } else {
            wrapper.scriptBody + rendered.content
        }
        return rendered.copy(content = transformed)
    }
}
