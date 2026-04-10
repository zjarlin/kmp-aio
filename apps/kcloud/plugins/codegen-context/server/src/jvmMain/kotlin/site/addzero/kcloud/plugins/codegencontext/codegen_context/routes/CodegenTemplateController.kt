package site.addzero.kcloud.plugins.codegencontext.codegen_context.routes

import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto
import site.addzero.kcloud.plugins.codegencontext.codegen_context.service.CodegenTemplateService

/**
 * 提供代码生成模板接口。
 *
 * @property templateService 模板服务。
 */
@Single
@RestController
@RequestMapping("/api/codegen-context/v1/templates")
class CodegenTemplateController(
    private val templateService: CodegenTemplateService,
) {
    /**
     * 列出协议模板。
     */
    @GetMapping("/protocols")
    fun listProtocolTemplates(): List<ProtocolTemplateOptionDto> =
        templateService.listProtocolTemplates()
}
