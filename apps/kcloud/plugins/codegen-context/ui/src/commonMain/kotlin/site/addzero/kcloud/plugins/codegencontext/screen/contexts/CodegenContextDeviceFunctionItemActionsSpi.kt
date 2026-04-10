package site.addzero.kcloud.plugins.codegencontext.screen.contexts

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel

/**
 * Codegen Context 页面“设备功能”单项操作区的交互插槽。
 *
 * 单项卡片的表单内容仍保留在 `DeviceFunctionsPanel`，
 * 这里专门承接每个功能卡片头部的删除动作，便于宿主按场景替换为归档、复制或更多菜单。
 */
interface CodegenContextDeviceFunctionItemActionsSpi {
    @Composable
    fun Render(
        index: Int,
        viewModel: CodegenContextViewModel,
    )
}

/**
 * Codegen Context 页面“设备功能”单项操作区的默认实现。
 *
 * 当前默认只提供删除功能动作；
 * 如果后续要接入复制、排序或更多单项能力，只替换这一实现即可。
 */
@Single
class DefaultCodegenContextDeviceFunctionItemActionsSpi : CodegenContextDeviceFunctionItemActionsSpi {
    @Composable
    override fun Render(
        index: Int,
        viewModel: CodegenContextViewModel,
    ) {
        WorkbenchActionButton(
            text = "删除功能",
            onClick = { viewModel.removeDeviceFunction(index) },
            variant = WorkbenchButtonVariant.Destructive,
        )
    }
}
