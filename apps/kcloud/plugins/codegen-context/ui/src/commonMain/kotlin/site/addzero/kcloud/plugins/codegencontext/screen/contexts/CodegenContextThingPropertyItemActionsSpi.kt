package site.addzero.kcloud.plugins.codegencontext.screen.contexts

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel

/**
 * Codegen Context 页面“物模型字段”单项操作区的交互插槽。
 *
 * 单项字段卡片的表单内容仍保留在 `ThingPropertiesPanel`，
 * 这里专门承接删除字段动作，方便宿主后续替换为复制、归档或更多字段操作。
 */
interface CodegenContextThingPropertyItemActionsSpi {
    @Composable
    fun Render(
        index: Int,
        viewModel: CodegenContextViewModel,
    )
}

/**
 * Codegen Context 页面“物模型字段”单项操作区的默认实现。
 *
 * 当前默认只提供删除字段动作；
 * 如果后续要扩展更多字段级入口，只替换这一实现即可。
 */
@Single
class DefaultCodegenContextThingPropertyItemActionsSpi : CodegenContextThingPropertyItemActionsSpi {
    @Composable
    override fun Render(
        index: Int,
        viewModel: CodegenContextViewModel,
    ) {
        WorkbenchActionButton(
            text = "删除字段",
            onClick = { viewModel.removeThingProperty(index) },
            variant = WorkbenchButtonVariant.Destructive,
        )
    }
}
