package site.addzero.kcloud.plugins.hostconfig.screen.projects

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchIconButton
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.screen.NodeActionDropdownMenu
import site.addzero.kcloud.plugins.hostconfig.screen.NodeActionMenuSeed
import site.addzero.kcloud.plugins.hostconfig.screen.NodeActionType

/**
 * Projects 页面工程树节点尾部操作区的交互插槽。
 *
 * 这个 slot 负责节点尾部“更多操作”按钮和动作菜单，
 * 这样页面树仍由 `WorkbenchTreeSidebar` 负责布局，节点级交互则可按宿主替换。
 */
interface ProjectsNodeTrailingActionsSpi {
    @Composable
    fun Render(
        node: HostConfigTreeNode,
        nodeActionMenu: NodeActionMenuSeed?,
        onSelectNode: (String) -> Unit,
        onOpenNodeActionMenu: (HostConfigTreeNode) -> Unit,
        onDismissNodeActionMenu: () -> Unit,
        onNodeAction: (HostConfigTreeNode, NodeActionType) -> Unit,
    )
}

/**
 * Projects 页面工程树节点尾部操作区的默认实现。
 *
 * 当前默认行为是点击更多按钮后展开动作菜单，并把动作继续回传给页面状态处理逻辑。
 * 如果后续宿主要改成右键、悬浮动作条或权限裁剪菜单，只替换这一实现即可。
 */
@Single
class DefaultProjectsNodeTrailingActionsSpi : ProjectsNodeTrailingActionsSpi {
    @Composable
    override fun Render(
        node: HostConfigTreeNode,
        nodeActionMenu: NodeActionMenuSeed?,
        onSelectNode: (String) -> Unit,
        onOpenNodeActionMenu: (HostConfigTreeNode) -> Unit,
        onDismissNodeActionMenu: () -> Unit,
        onNodeAction: (HostConfigTreeNode, NodeActionType) -> Unit,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            WorkbenchIconButton(
                onClick = {
                    onSelectNode(node.id)
                    onOpenNodeActionMenu(node)
                },
                tooltip = "节点操作",
            ) {
                Icon(imageVector = Icons.Filled.MoreHoriz, contentDescription = null)
            }
            nodeActionMenu?.takeIf { it.node.id == node.id }?.let { seed ->
                NodeActionDropdownMenu(
                    seed = seed,
                    onDismissRequest = onDismissNodeActionMenu,
                    onAction = { actionType ->
                        onDismissNodeActionMenu()
                        onNodeAction(seed.node, actionType)
                    },
                )
            }
        }
    }
}
