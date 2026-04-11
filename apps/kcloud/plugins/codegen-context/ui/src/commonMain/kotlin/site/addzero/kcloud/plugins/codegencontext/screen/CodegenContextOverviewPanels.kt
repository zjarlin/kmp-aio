package site.addzero.kcloud.plugins.codegencontext.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.cupertino.workbench.components.field.CupertinoBooleanField
import site.addzero.cupertino.workbench.components.field.CupertinoOption
import site.addzero.cupertino.workbench.components.field.CupertinoSelectionField
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.form.CupertinoFormGrid
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.codegencontext.api.context.CODEGEN_CONTEXT_REFERENCE_BRIDGE_IMPL_PATH
import site.addzero.kcloud.plugins.codegencontext.api.context.CODEGEN_CONTEXT_REFERENCE_KEIL_GROUP_NAME
import site.addzero.kcloud.plugins.codegencontext.api.context.CODEGEN_CONTEXT_REFERENCE_KEIL_TARGET_NAME
import site.addzero.kcloud.plugins.codegencontext.api.context.CODEGEN_CONTEXT_REFERENCE_KEIL_UVPROJX_PATH
import site.addzero.kcloud.plugins.codegencontext.api.context.CODEGEN_CONTEXT_REFERENCE_MXPROJECT_PATH
import site.addzero.kcloud.plugins.codegencontext.api.context.CODEGEN_CONTEXT_REFERENCE_PROJECT_DIR
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportResultDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataFirmwareSyncDto
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextScreenState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataIssueSeverity
import site.addzero.kcloud.plugins.codegencontext.screen.contexts.CodegenContextDraftPanelActionsSpi

@Composable
internal fun ContextDraftPanel(
    state: CodegenContextScreenState,
    viewModel: CodegenContextViewModel,
) {
    val draftPanelActionsSpi = koinInject<CodegenContextDraftPanelActionsSpi>()
    val draft = state.draft
    val protocolTemplates = state.protocolTemplates
    val selectedProtocolTemplateDescription =
        protocolTemplates
            .firstOrNull { item -> item.id == draft.protocolTemplateId }
            ?.description
    CupertinoPanel(
        title = "模型信息",
        subtitle = "这里只维护建模定义和导出所需最小信息；页面不再暴露额外生成参数和多余导出开关。",
        actions = {
            draftPanelActionsSpi.Render(
                state = state,
                viewModel = viewModel,
            )
        },
    ) {
        CupertinoFormGrid {
            item {
                CupertinoTextField(
                    label = "模型编码",
                    value = draft.code,
                    onValueChange = { value -> viewModel.updateDraft { it.copy(code = value) } },
                    placeholder = "例如 MCU_DEVICE_DEFAULT",
                    description = "稳定编码，用于保存和导出标识。",
                )
            }
            item {
                CupertinoTextField(
                    label = "模型名称",
                    value = draft.name,
                    onValueChange = { value -> viewModel.updateDraft { it.copy(name = value) } },
                    placeholder = "例如 控制器寄存器协议",
                    description = "人可读标题。",
                )
            }
            fullWidth {
                CupertinoTextField(
                    label = "关联节点 nodeId",
                    value = draft.nodeId,
                    onValueChange = { value -> viewModel.updateDraft { it.copy(nodeId = value) } },
                    placeholder = "例如 device/1/2",
                    description = "用于从 host-config 项目/设备树解析 RTU、TCP、MQTT 默认参数；保存后后端会按 nodeId 回填。",
                )
            }
            item {
                CupertinoSelectionField(
                    label = "协议模板",
                    options = protocolTemplates.map { option -> CupertinoOption(option.id, option.name, option.code) },
                    selectedValue = draft.protocolTemplateId.takeIf { id -> id > 0L },
                    onSelected = viewModel::selectProtocolTemplate,
                    allowClear = true,
                    description = selectedProtocolTemplateDescription ?: "协议模板决定可编辑的建模上下文。",
                )
            }
            item {
                CupertinoBooleanField(
                    label = "启用",
                    checked = draft.enabled,
                    onCheckedChange = { value -> viewModel.updateDraft { it.copy(enabled = value) } },
                    description = "停用后不会参与导出。",
                )
            }
            fullWidth {
                CupertinoTextField(
                    label = "模型说明",
                    value = draft.description.orEmpty(),
                    onValueChange = { value -> viewModel.updateDraft { it.copy(description = value) } },
                    singleLine = false,
                )
            }
        }
    }
}

@Composable
internal fun ExportWorkbenchPanel(
    state: CodegenContextScreenState,
    viewModel: CodegenContextViewModel,
) {
    CupertinoPanel(
        title = "代码导出",
        subtitle = "导出时后端固定生成 RTU/TCP/MQTT 的 C contract、dispatch 和 Markdown；页面只保留工程落地信息。",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CupertinoStatusStrip("页面只负责建模和导出触发；传输实现细节和导出策略由后端统一处理。")
            CupertinoStatusStrip("如果填写了 nodeId，后端会优先按 host-config 的项目/设备树配置补齐默认参数，再参与导出。")
            CupertinoStatusStrip("点击“导出”后会直接触发 Modbus/MQTT 相关 C 与 Markdown 生成，并尝试执行工程同步。")
            FirmwareSyncPanel(
                firmwareSync = state.draft.exportSettings.firmwareSync,
                onUpdate = viewModel::updateFirmwareSync,
            )
            ExportResultPanel(state.exportResult)
        }
    }
}

@Composable
private fun FirmwareSyncPanel(
    firmwareSync: CodegenMetadataFirmwareSyncDto,
    onUpdate: (transform: (CodegenMetadataFirmwareSyncDto) -> CodegenMetadataFirmwareSyncDto) -> Unit,
) {
    CupertinoPanel(
        title = "工程落地",
        subtitle = "这里只保留导出和同步真正需要的路径配置。",
    ) {
        CupertinoFormGrid {
            fullWidth {
                CupertinoTextField(
                    label = "C 工程根目录",
                    value = firmwareSync.cOutputProjectDir,
                    onValueChange = { value -> onUpdate { it.copy(cOutputProjectDir = value) } },
                    placeholder = CODEGEN_CONTEXT_REFERENCE_PROJECT_DIR,
                )
            }
            item {
                CupertinoTextField(
                    label = "bridge 实现路径",
                    value = firmwareSync.bridgeImplPath,
                    onValueChange = { value -> onUpdate { it.copy(bridgeImplPath = value) } },
                    placeholder = CODEGEN_CONTEXT_REFERENCE_BRIDGE_IMPL_PATH,
                )
            }
            item {
                CupertinoTextField(
                    label = "Keil uvprojx",
                    value = firmwareSync.keilUvprojxPath,
                    onValueChange = { value -> onUpdate { it.copy(keilUvprojxPath = value) } },
                    placeholder = CODEGEN_CONTEXT_REFERENCE_KEIL_UVPROJX_PATH,
                )
            }
            item {
                CupertinoTextField(
                    label = "Keil target",
                    value = firmwareSync.keilTargetName,
                    onValueChange = { value -> onUpdate { it.copy(keilTargetName = value) } },
                    placeholder = CODEGEN_CONTEXT_REFERENCE_KEIL_TARGET_NAME,
                )
            }
            item {
                CupertinoTextField(
                    label = "Keil group",
                    value = firmwareSync.keilGroupName,
                    onValueChange = { value -> onUpdate { it.copy(keilGroupName = value) } },
                    placeholder = CODEGEN_CONTEXT_REFERENCE_KEIL_GROUP_NAME,
                )
            }
            item {
                CupertinoTextField(
                    label = "CubeMX mxproject",
                    value = firmwareSync.mxprojectPath,
                    onValueChange = { value -> onUpdate { it.copy(mxprojectPath = value) } },
                    placeholder = CODEGEN_CONTEXT_REFERENCE_MXPROJECT_PATH,
                )
            }
        }
    }
}

@Composable
private fun ExportResultPanel(
    exportResult: CodegenMetadataExportResultDto?,
) {
    CupertinoPanel(
        title = "导出结果",
        subtitle = "这里只展示实际生成的 C/Markdown 文件和工程同步结果。",
    ) {
        if (exportResult == null) {
            CupertinoStatusStrip("还没有导出结果。")
            return@CupertinoPanel
        }
        CupertinoStatusStrip(exportResult.message)
        if (exportResult.issues.isNotEmpty()) {
            exportResult.issues.forEach { issue ->
                val tone =
                    when (issue.severity) {
                        CodegenMetadataIssueSeverity.ERROR -> Color(0xFFFFE8E6)
                        CodegenMetadataIssueSeverity.WARNING -> Color(0xFFFFF5D6)
                        CodegenMetadataIssueSeverity.INFO -> Color(0xFFEAF4FF)
                    }
                CupertinoStatusStrip("${issue.location}: ${issue.message}", tone = tone)
            }
        }
        if (exportResult.generatedArtifacts.isNotEmpty()) {
            CupertinoPanel(title = "生成文件", subtitle = "导出器已写入的 C/Markdown 文件。") {
                exportResult.generatedArtifacts.forEach { item ->
                    CupertinoStatusStrip("${item.transport?.name ?: "-"} / ${item.artifactKind} · ${item.path}")
                }
            }
        }
        if (exportResult.projectSyncResults.isNotEmpty()) {
            CupertinoPanel(title = "工程同步", subtitle = "Keil / CubeMX / bridge 同步结果。") {
                exportResult.projectSyncResults.forEach { item ->
                    CupertinoStatusStrip("${item.toolId} / ${item.transport} · ${item.message}")
                }
            }
        }
    }
}
