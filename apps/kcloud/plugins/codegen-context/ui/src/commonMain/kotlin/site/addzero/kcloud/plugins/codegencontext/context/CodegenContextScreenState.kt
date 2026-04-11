package site.addzero.kcloud.plugins.codegencontext.context

import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataPreviewDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextSummaryDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportResultDto
import site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto

/**
 * 表示元数据建模页面状态。
 */
data class CodegenContextScreenState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val exporting: Boolean = false,
    val deleting: Boolean = false,
    val previewing: Boolean = false,
    val errorMessage: String? = null,
    val previewErrorMessage: String? = null,
    val statusMessage: String? = null,
    val protocolTemplates: List<ProtocolTemplateOptionDto> = emptyList(),
    val contexts: List<CodegenContextSummaryDto> = emptyList(),
    val selectedContextId: Long? = null,
    val selectedWorkbenchTab: CodegenContextWorkbenchTab = CodegenContextWorkbenchTab.DEVICE_FUNCTIONS,
    val availableContextDefinitions: List<CodegenContextDefinitionDto> = emptyList(),
    val draft: CodegenMetadataDraftDto = CodegenMetadataDraftDto(),
    val preview: CodegenMetadataPreviewDto? = null,
    val exportResult: CodegenMetadataExportResultDto? = null,
)
