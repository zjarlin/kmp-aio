package site.addzero.kcloud.plugins.mcuconsole.debug

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant

/**
 * McuDebug 页面头部工具区的交互插槽。
 *
 * 页面主布局和日志面板仍保留在 `McuDebugScreen`，
 * 这里专门承接连接、停止、刷新串口和清空日志这组头部动作，
 * 方便宿主后续替换工具条组合或增加调试相关扩展入口。
 */
interface McuDebugToolbarActionsSpi {
    @Composable
    fun Render(
        state: McuDebugScreenState,
        viewModel: McuDebugViewModel,
    )
}

/**
 * McuDebug 页面头部工具区的默认实现。
 *
 * 当前默认保留连接、停止、刷新串口和清空日志四个动作；
 * 如果后续要改成分组工具条或更多调试命令，只替换这一实现即可。
 */
@Single
class DefaultMcuDebugToolbarActionsSpi : McuDebugToolbarActionsSpi {
    @Composable
    override fun Render(
        state: McuDebugScreenState,
        viewModel: McuDebugViewModel,
    ) {
        WorkbenchActionButton(
            text = if (state.streaming) "重新连接" else "开始连接",
            onClick = viewModel::startStreaming,
            variant = WorkbenchButtonVariant.Default,
            enabled = !state.connecting,
        )
        WorkbenchActionButton(
            text = "停止",
            onClick = viewModel::stopStreaming,
            variant = WorkbenchButtonVariant.Outline,
            enabled = state.streaming || state.connecting,
        )
        WorkbenchActionButton(
            text = "刷新串口",
            onClick = viewModel::refreshPorts,
            variant = WorkbenchButtonVariant.Secondary,
            enabled = !state.connecting,
        )
        WorkbenchActionButton(
            text = "清空日志",
            onClick = viewModel::clearLogs,
            variant = WorkbenchButtonVariant.Secondary,
            enabled = state.logs.isNotEmpty(),
        )
    }
}
