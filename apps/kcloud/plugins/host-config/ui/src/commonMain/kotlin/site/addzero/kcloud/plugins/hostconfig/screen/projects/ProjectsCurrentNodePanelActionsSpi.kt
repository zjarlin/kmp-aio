package site.addzero.kcloud.plugins.hostconfig.screen.projects

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState

/**
 * Projects 页面“当前节点”面板头部操作区的交互插槽。
 *
 * 面板内容和节点详情布局仍保留在 `CurrentNodePanel`，
 * 这里专门承接“编辑 / 取消编辑 / 折叠”这组页面级动作，
 * 方便宿主后续替换工具区策略，而不改节点详情骨架。
 */
interface ProjectsCurrentNodePanelActionsSpi {
    @Composable
    fun Render(
        state: ProjectsScreenState,
        collapsed: Boolean,
        isEditing: Boolean,
        actions: ProjectsCurrentNodePanelActions,
    )
}

/**
 * Projects 页面“当前节点”面板头部操作区的默认实现。
 *
 * 当前默认行为是选中节点后允许切换编辑态，并始终提供折叠/展开入口；
 * 如果后续要补权限判断、草稿锁定或更多节点动作，只替换这一实现即可。
 */
@Single
class DefaultProjectsCurrentNodePanelActionsSpi : ProjectsCurrentNodePanelActionsSpi {
    @Composable
    override fun Render(
        state: ProjectsScreenState,
        collapsed: Boolean,
        isEditing: Boolean,
        actions: ProjectsCurrentNodePanelActions,
    ) {
        state.selectedNodeId?.let { nodeId ->
            WorkbenchActionButton(
                text = if (isEditing) "取消编辑" else "编辑",
                onClick = { actions.toggleEditing(nodeId, isEditing) },
                variant = WorkbenchButtonVariant.Secondary,
            )
        }
        WorkbenchActionButton(
            text = if (collapsed) "展开" else "折叠",
            onClick = actions::toggleCollapsed,
            variant = WorkbenchButtonVariant.Outline,
        )
    }
}

/**
 * Projects 页面“当前节点”面板操作桥接。
 *
 * 这组动作依赖页面自己的编辑态和折叠态，不适合直接塞进通用单例；
 * 因此先用一个小型操作对象收口，避免 slot 继续持有多条散落的 lambda。
 */
internal class ProjectsCurrentNodePanelActions(
    private val onStartEditing: (String) -> Unit,
    private val onStopEditing: () -> Unit,
    private val onToggleCollapsed: () -> Unit,
) {
    fun toggleEditing(
        nodeId: String,
        isEditing: Boolean,
    ) {
        if (isEditing) {
            onStopEditing()
        } else {
            onStartEditing(nodeId)
        }
    }

    fun toggleCollapsed() {
        onToggleCollapsed()
    }
}
