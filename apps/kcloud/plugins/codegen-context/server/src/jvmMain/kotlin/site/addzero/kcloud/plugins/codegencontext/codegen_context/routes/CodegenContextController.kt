package site.addzero.kcloud.plugins.codegencontext.codegen_context.routes

import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportResultDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataPreviewDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextSummaryDto
import site.addzero.kcloud.plugins.codegencontext.codegen_context.service.CodegenContextService

@Single
@RestController
@RequestMapping("/api/codegen-context/v1/contexts")
/**
 * 提供代码生成上下文接口。
 *
 * @property contextService 上下文服务。
 */
class CodegenContextController(
    private val contextService: CodegenContextService,
) {
    @GetMapping
    /**
     * 列出上下文。
     */
    fun listContexts(): List<CodegenContextSummaryDto> =
        contextService.listContexts()

    @GetMapping("/{contextId}")
    /**
     * 获取上下文。
     *
     * @param @PathVariable("contextId") 路径variable上下文ID。
     */
    fun getContext(
        @PathVariable("contextId") contextId: Long,
    ): CodegenMetadataDraftDto =
        contextService.getContextDraft(contextId)

    @GetMapping("/protocols/{protocolTemplateId}/definitions")
    /**
     * 列出上下文定义。
     *
     * @param @PathVariable("protocolTemplateId") 路径variable协议模板ID。
     */
    fun listContextDefinitions(
        @PathVariable("protocolTemplateId") protocolTemplateId: Long,
    ): List<CodegenContextDefinitionDto> =
        contextService.listContextDefinitions(protocolTemplateId)

    @PostMapping("/save")
    /**
     * 保存上下文。
     *
     * @param @RequestBody 请求体。
     */
    fun saveContext(
        @RequestBody request: CodegenMetadataDraftDto,
    ): CodegenMetadataDraftDto =
        contextService.saveContextDraft(request)

    @PostMapping("/preview")
    /**
     * 预检元数据草稿。
     *
     * @param @RequestBody 请求体。
     */
    fun previewContext(
        @RequestBody request: CodegenMetadataDraftDto,
    ): CodegenMetadataPreviewDto =
        contextService.previewContextDraft(request)

    @DeleteMapping("/{contextId}")
    /**
     * 删除上下文。
     *
     * @param @PathVariable("contextId") 路径variable上下文ID。
     */
    fun deleteContext(
        @PathVariable("contextId") contextId: Long,
    ) {
        contextService.deleteContext(contextId)
    }

    @PostMapping("/{contextId}/export")
    /**
     * 导出上下文。
     *
     * @param @PathVariable("contextId") 路径variable上下文ID。
     */
    fun exportContext(
        @PathVariable("contextId") contextId: Long,
    ): CodegenMetadataExportResultDto =
        contextService.exportContext(contextId)
}
