package site.addzero.kcloud.plugins.mcuconsole.flash

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant

/**
 * McuFlash 页面“烧录配置”主操作区的交互插槽。
 *
 * 表单字段和布局仍保留在 `McuFlashConfigPanel`，
 * 这里只承接“开始烧录 / 发送复位”这组核心动作，方便后续替换按钮组合或增加确认流程。
 */
interface McuFlashConfigActionsSpi {
    @Composable
    fun RowScope.Render(
        state: McuFlashScreenState,
        viewModel: McuFlashViewModel,
    )
}

/**
 * McuFlash 页面“烧录配置”主操作区的默认实现。
 *
 * 当前默认保留开始烧录和发送复位两个动作，并沿用页面状态控制按钮可用性；
 * 如果后续需要改成多阶段确认或补充更多命令，只替换这一实现即可。
 */
@Single
class DefaultMcuFlashConfigActionsSpi : McuFlashConfigActionsSpi {
    @Composable
    override fun RowScope.Render(
        state: McuFlashScreenState,
        viewModel: McuFlashViewModel,
    ) {
        WorkbenchActionButton(
            text = if (state.busy) "提交中" else "开始烧录",
            onClick = viewModel::startFlash,
            enabled = !state.loading && !state.busy && !state.running && state.selectedProfile != null,
            modifier = Modifier.weight(1f),
        )
        WorkbenchActionButton(
            text = "发送复位",
            onClick = viewModel::resetTarget,
            variant = WorkbenchButtonVariant.Outline,
            enabled = !state.loading && !state.busy && !state.running,
            modifier = Modifier.weight(1f),
        )
    }
}
