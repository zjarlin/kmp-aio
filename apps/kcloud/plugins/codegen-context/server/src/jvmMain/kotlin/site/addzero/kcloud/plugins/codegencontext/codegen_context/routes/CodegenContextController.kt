package site.addzero.kcloud.plugins.codegencontext.codegen_context.routes

import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextSummaryDto
import site.addzero.kcloud.plugins.codegencontext.api.context.GenerateContractsResponseDto
import site.addzero.kcloud.plugins.codegencontext.codegen_context.service.CodegenContextService

@Single
@RestController
@RequestMapping("/api/codegen-context/v1/contexts")
class CodegenContextController(
    private val contextService: CodegenContextService,
) {
    @GetMapping
    fun listContexts(): List<CodegenContextSummaryDto> =
        contextService.listContexts()

    @GetMapping("/{contextId}")
    fun getContext(
        @PathVariable("contextId") contextId: Long,
    ): CodegenContextDetailDto =
        contextService.getContext(contextId)

    @PostMapping("/save")
    fun saveContext(
        @RequestBody request: CodegenContextDetailDto,
    ): CodegenContextDetailDto =
        contextService.saveContext(request)

    @DeleteMapping("/{contextId}")
    fun deleteContext(
        @PathVariable("contextId") contextId: Long,
    ) {
        contextService.deleteContext(contextId)
    }

    @PostMapping("/{contextId}/generate")
    fun generateContext(
        @PathVariable("contextId") contextId: Long,
    ): GenerateContractsResponseDto =
        contextService.generateContracts(contextId)
}
