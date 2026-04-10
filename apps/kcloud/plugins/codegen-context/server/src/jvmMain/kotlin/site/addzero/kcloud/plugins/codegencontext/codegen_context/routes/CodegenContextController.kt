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

/**
 * 提供代码生成上下文接口。
 *
 * @property contextService 上下文服务。
 */
@Single
@RestController
@RequestMapping("/api/codegen-context/v1/contexts")
class CodegenContextController(
    private val contextService: CodegenContextService,
) {
    /**
     * 列出上下文。
     */
    @GetMapping
    fun listContexts(): List<CodegenContextSummaryDto> =
        contextService.listContexts()

    /**
     * 获取上下文。
     *
     * @param contextId 路径中的上下文 ID。
     */
    @GetMapping("/{contextId}")
    fun getContext(
        @PathVariable("contextId") contextId: Long,
    ): CodegenMetadataDraftDto =
        contextService.getContextDraft(contextId)

    /**
     * 列出上下文定义。
     *
     * @param protocolTemplateId 路径中的协议模板 ID。
     */
    @GetMapping("/protocols/{protocolTemplateId}/definitions")
    fun listContextDefinitions(
        @PathVariable("protocolTemplateId") protocolTemplateId: Long,
    ): List<CodegenContextDefinitionDto> =
        contextService.listContextDefinitions(protocolTemplateId)

    /**
     * 保存上下文。
     *
     * @param request 前端提交的元数据草稿。
     */
    @PostMapping("/save")
    fun saveContext(
        @RequestBody request: CodegenMetadataDraftDto,
    ): CodegenMetadataDraftDto =
        contextService.saveContextDraft(request)

    /**
     * 预检元数据草稿。
     *
     * @param request 前端提交的元数据草稿。
     */
    @PostMapping("/preview")
    fun previewContext(
        @RequestBody request: CodegenMetadataDraftDto,
    ): CodegenMetadataPreviewDto =
        contextService.previewContextDraft(request)

    /**
     * 删除上下文。
     *
     * @param contextId 路径中的上下文 ID。
     */
    @DeleteMapping("/{contextId}")
    fun deleteContext(
        @PathVariable("contextId") contextId: Long,
    ) {
        contextService.deleteContext(contextId)
    }

    /**
     * 导出上下文。
     *
     * @param contextId 路径中的上下文 ID。
     */
    @PostMapping("/{contextId}/export")
    fun exportContext(
        @PathVariable("contextId") contextId: Long,
    ): CodegenMetadataExportResultDto =
        contextService.exportContext(contextId)
}
