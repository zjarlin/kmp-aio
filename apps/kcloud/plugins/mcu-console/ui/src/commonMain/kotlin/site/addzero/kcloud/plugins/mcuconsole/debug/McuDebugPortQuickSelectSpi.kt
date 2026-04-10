package site.addzero.kcloud.plugins.mcuconsole.debug

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialPortDescriptor

/**
 * McuDebug 页面“可见串口”区域里快速选择入口的交互插槽。
 *
 * 串口列表布局仍保留在 `McuDebugScreen`，
 * 这里专门承接每个串口条目的快速选中动作，方便宿主后续替换为卡片、菜单或更多端口动作。
 */
interface McuDebugPortQuickSelectSpi {
    @Composable
    fun Render(
        state: McuDebugScreenState,
        descriptor: McuSerialPortDescriptor,
        viewModel: McuDebugViewModel,
    )
}

/**
 * McuDebug 页面“可见串口”区域里快速选择入口的默认实现。
 *
 * 当前默认把每个串口渲染成一个可点击按钮；
 * 如果后续要补更多串口状态展示或行内操作，只替换这一实现即可。
 */
@Single
class DefaultMcuDebugPortQuickSelectSpi : McuDebugPortQuickSelectSpi {
    @Composable
    override fun Render(
        state: McuDebugScreenState,
        descriptor: McuSerialPortDescriptor,
        viewModel: McuDebugViewModel,
    ) {
        WorkbenchActionButton(
            text = descriptor.systemPortPath.ifBlank { descriptor.systemPortName },
            onClick = { viewModel.selectPort(descriptor) },
            variant = if (descriptor.systemPortPath == state.portName || descriptor.systemPortName == state.portName) {
                WorkbenchButtonVariant.Default
            } else {
                WorkbenchButtonVariant.Outline
            },
        )
    }
}
