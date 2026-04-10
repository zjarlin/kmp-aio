package site.addzero.kcloud.plugins.codegencontext.screen.contexts

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel

/**
 * Codegen Context 页面“设备功能”面板头部操作区的交互插槽。
 *
 * 面板布局和功能表单仍保留在 `DeviceFunctionsPanel`，
 * 这里专门承接新增功能入口，方便宿主后续补权限、模板导入或批量生成能力。
 */
interface CodegenContextDeviceFunctionsHeaderSpi {
    @Composable
    fun Render(
        viewModel: CodegenContextViewModel,
    )
}

/**
 * Codegen Context 页面“设备功能”面板头部操作区的默认实现。
 *
 * 当前默认只提供新增功能入口；
 * 如果后续要接入批量导入、模板复制或更多动作，只替换这一实现即可。
 */
@Single
class DefaultCodegenContextDeviceFunctionsHeaderSpi : CodegenContextDeviceFunctionsHeaderSpi {
    @Composable
    override fun Render(
        viewModel: CodegenContextViewModel,
    ) {
        WorkbenchActionButton(
            text = "新增功能",
            onClick = viewModel::addDeviceFunction,
        )
    }
}
