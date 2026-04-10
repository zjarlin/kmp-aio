package site.addzero.kcloud.plugins.hostconfig.screen.protocols

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.components.panel.CupertinoSectionTitle
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.hostconfig.protocols.ProtocolsScreenState
import site.addzero.kcloud.plugins.hostconfig.protocols.ProtocolsViewModel

/**
 * Protocols 页面左侧目录头部交互插槽。
 *
 * 页面主分栏和目录树继续由 `ProtocolsScreen` 保持，
 * 这里专门承接目录头部的状态提示和刷新动作，方便宿主后续替换工具区能力。
 */
interface ProtocolsSidebarHeaderSpi {
    @Composable
    fun Render(
        state: ProtocolsScreenState,
        viewModel: ProtocolsViewModel,
    )
}

/**
 * Protocols 页面左侧目录头部的默认实现。
 *
 * 当前默认保留目录标题、错误提示和刷新按钮三部分；
 * 如果后续要接入更多筛选或权限控制，只替换这一实现即可。
 */
@Single
class DefaultProtocolsSidebarHeaderSpi : ProtocolsSidebarHeaderSpi {
    @Composable
    override fun Render(
        state: ProtocolsScreenState,
        viewModel: ProtocolsViewModel,
    ) {
        CupertinoSectionTitle("协议字典目录")
        state.errorMessage?.let { message ->
            CupertinoStatusStrip(message)
        }
        WorkbenchActionButton(
            text = if (state.loading) "加载中" else "刷新",
            onClick = viewModel::refresh,
            variant = WorkbenchButtonVariant.Outline,
        )
    }
}
