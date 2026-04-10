package site.addzero.kcloud.plugins.codegencontext.screen.contexts

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel

/**
 * Codegen Context 页面“物模型字段”面板头部操作区的交互插槽。
 *
 * 面板布局和字段表单仍保留在 `ThingPropertiesPanel`，
 * 这里专门承接新增字段入口，方便宿主后续补模板导入、批量生成或字段同步能力。
 */
interface CodegenContextThingPropertiesHeaderSpi {
    @Composable
    fun Render(
        viewModel: CodegenContextViewModel,
    )
}

/**
 * Codegen Context 页面“物模型字段”面板头部操作区的默认实现。
 *
 * 当前默认只提供新增字段入口；
 * 如果后续要补更多头部动作，只替换这一实现即可。
 */
@Single
class DefaultCodegenContextThingPropertiesHeaderSpi : CodegenContextThingPropertiesHeaderSpi {
    @Composable
    override fun Render(
        viewModel: CodegenContextViewModel,
    ) {
        WorkbenchActionButton(
            text = "新增字段",
            onClick = viewModel::addThingProperty,
        )
    }
}
