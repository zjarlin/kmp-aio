package site.addzero.kcloud.plugins.codegencontext.screen.contexts

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextScreenState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel

/**
 * Codegen Context 页面“基础信息”面板操作区的交互插槽。
 *
 * 面板表单布局仍保留在 `ContextDraftPanel`，这里专门承接保存、预检、导出动作，
 * 方便宿主后续替换动作组合、禁用条件或接入更多流程入口。
 */
interface CodegenContextDraftPanelActionsSpi {
    @Composable
    fun Render(
        state: CodegenContextScreenState,
        viewModel: CodegenContextViewModel,
    )
}

/**
 * Codegen Context 页面“基础信息”面板操作区的默认实现。
 *
 * 当前默认顺序是保存、预检、导出；
 * 如果后续要补充发布、回滚或环境切换等操作，只替换这一实现即可。
 */
@Single
class DefaultCodegenContextDraftPanelActionsSpi : CodegenContextDraftPanelActionsSpi {
    @Composable
    override fun Render(
        state: CodegenContextScreenState,
        viewModel: CodegenContextViewModel,
    ) {
        WorkbenchActionButton(
            text = if (state.saving) "保存中" else "保存",
            onClick = viewModel::save,
            enabled = !state.saving && !state.previewing && !state.exporting,
        )
        WorkbenchActionButton(
            text = if (state.previewing) "预检中" else "预检",
            onClick = viewModel::preview,
            enabled = !state.saving && !state.previewing && !state.exporting,
            variant = WorkbenchButtonVariant.Outline,
        )
        WorkbenchActionButton(
            text = if (state.exporting) "导出中" else "导出",
            onClick = viewModel::exportSelected,
            enabled = !state.saving && !state.previewing && !state.exporting,
            variant = WorkbenchButtonVariant.Secondary,
        )
    }
}
