package site.addzero.kcloud.plugins.mcuconsole.flash

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant

/**
 * McuFlash 页面头部工具区的交互插槽。
 *
 * 页面头部面板布局仍保留在 `McuFlashHeader`，
 * 这里专门承接刷新页面、刷新探针、刷新状态三组动作，方便宿主按场景替换工具条。
 */
interface McuFlashHeaderActionsSpi {
    @Composable
    fun Render(
        state: McuFlashScreenState,
        viewModel: McuFlashViewModel,
    )
}

/**
 * McuFlash 页面头部工具区的默认实现。
 *
 * 当前默认保留三类刷新动作，并沿用页面状态控制启用条件；
 * 如果后续要接入串口探测、日志面板或权限裁剪，只替换这一实现即可。
 */
@Single
class DefaultMcuFlashHeaderActionsSpi : McuFlashHeaderActionsSpi {
    @Composable
    override fun Render(
        state: McuFlashScreenState,
        viewModel: McuFlashViewModel,
    ) {
        WorkbenchActionButton(
            text = if (state.loading) "加载中" else "刷新页面",
            onClick = viewModel::refresh,
            variant = WorkbenchButtonVariant.Outline,
            enabled = !state.busy,
        )
        WorkbenchActionButton(
            text = "刷新探针",
            onClick = viewModel::refreshProbes,
            variant = WorkbenchButtonVariant.Secondary,
            enabled = !state.busy,
        )
        WorkbenchActionButton(
            text = "刷新状态",
            onClick = viewModel::refreshStatus,
            variant = WorkbenchButtonVariant.Secondary,
            enabled = !state.loading && !state.busy,
        )
    }
}
