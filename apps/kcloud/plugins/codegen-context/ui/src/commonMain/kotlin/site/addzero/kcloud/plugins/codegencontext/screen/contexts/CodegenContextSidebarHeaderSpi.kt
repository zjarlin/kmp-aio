package site.addzero.kcloud.plugins.codegencontext.screen.contexts

import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextScreenState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel

/**
 * Codegen Context 页面左侧草稿目录头部交互插槽。
 *
 * 页面根布局继续保留在 `CodegenContextScreen`，这个 slot 只负责目录区顶部动作，
 * 方便后续按宿主能力替换刷新、新建、删除策略，而不改页面分栏结构。
 */
interface CodegenContextSidebarHeaderSpi {
    @Composable
    fun Render(
        state: CodegenContextScreenState,
        viewModel: CodegenContextViewModel,
    )
}

/**
 * Codegen Context 页面左侧草稿目录头部的默认实现。
 *
 * 当前默认暴露刷新、新建、删除三个入口；
 * 如果后续要接入权限裁剪、二次确认或更多目录动作，只替换这一实现即可。
 */
@Single
class DefaultCodegenContextSidebarHeaderSpi : CodegenContextSidebarHeaderSpi {
    @Composable
    override fun Render(
        state: CodegenContextScreenState,
        viewModel: CodegenContextViewModel,
    ) {
        WorkbenchActionButton(
            text = if (state.loading) "加载中" else "刷新",
            onClick = viewModel::refresh,
            imageVector = Icons.Outlined.Refresh,
            variant = WorkbenchButtonVariant.Outline,
        )
        WorkbenchActionButton(text = "新建", onClick = viewModel::newContext)
        WorkbenchActionButton(
            text = "删除",
            onClick = viewModel::deleteSelected,
            enabled = state.selectedContextId != null && !state.deleting,
            variant = WorkbenchButtonVariant.Destructive,
        )
    }
}
