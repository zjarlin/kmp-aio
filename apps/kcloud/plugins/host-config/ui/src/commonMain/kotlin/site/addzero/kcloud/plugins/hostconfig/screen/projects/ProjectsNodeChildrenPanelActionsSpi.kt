package site.addzero.kcloud.plugins.hostconfig.screen.projects

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState

/**
 * Projects 页面“下级节点”面板头部操作区的交互插槽。
 *
 * 面板内容仍保留在 `NodeChildrenPanel`，
 * 这里专门承接设备标签分页这一组翻页动作，避免把页头按钮写死在面板布局里。
 */
interface ProjectsNodeChildrenPanelActionsSpi {
    @Composable
    fun Render(
        state: ProjectsScreenState,
        actions: ProjectsNodeChildrenPanelActions,
    )
}

/**
 * Projects 页面“下级节点”面板头部操作区的默认实现。
 *
 * 当前默认只在设备节点下暴露上一页/下一页两个分页动作；
 * 如果后续要改成页码器、筛选器或快捷跳转，只替换这一实现即可。
 */
@Single
class DefaultProjectsNodeChildrenPanelActionsSpi : ProjectsNodeChildrenPanelActionsSpi {
    @Composable
    override fun Render(
        state: ProjectsScreenState,
        actions: ProjectsNodeChildrenPanelActions,
    ) {
        if (state.selectedNode?.kind != HostConfigNodeKind.DEVICE) {
            return
        }
        WorkbenchActionButton(
            text = "上一页",
            onClick = actions::loadPreviousTagPage,
            variant = WorkbenchButtonVariant.Outline,
            enabled = state.tagOffset > 0,
        )
        WorkbenchActionButton(
            text = "下一页",
            onClick = actions::loadNextTagPage,
            variant = WorkbenchButtonVariant.Outline,
            enabled = state.tagOffset + state.tagSize < state.tagPage.t.toInt(),
        )
    }
}

/**
 * Projects 页面“下级节点”面板分页动作桥接。
 *
 * 这里先把标签分页相关动作收口成一个小型操作对象，
 * 这样 slot 可以直接拿到明确的页面动作，而不是继续透传多条离散回调。
 */
internal class ProjectsNodeChildrenPanelActions(
    private val onPrevTagPage: () -> Unit,
    private val onNextTagPage: () -> Unit,
) {
    fun loadPreviousTagPage() {
        onPrevTagPage()
    }

    fun loadNextTagPage() {
        onNextTagPage()
    }
}
