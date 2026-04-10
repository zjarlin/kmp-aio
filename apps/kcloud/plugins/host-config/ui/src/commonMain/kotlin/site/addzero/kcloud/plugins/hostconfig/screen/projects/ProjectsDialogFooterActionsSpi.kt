package site.addzero.kcloud.plugins.hostconfig.screen.projects

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant

/**
 * Projects 页面各类对话框底部操作区的交互插槽。
 *
 * 对话框表单内容和布局继续保留在 `ProjectsDialogs`，
 * 这里专门承接“取消 + 主动作”这一组页内弹窗操作，
 * 方便宿主后续统一替换按钮文案、主次样式或补二次确认流程。
 */
interface ProjectsDialogFooterActionsSpi {
    @Composable
    fun Render(
        state: ProjectsDialogFooterActionState,
        actions: ProjectsDialogFooterActions,
    )
}

/**
 * Projects 页面各类对话框底部操作区的默认实现。
 *
 * 当前默认统一使用“取消 + 主动作”双按钮结构；
 * 如果后续要按宿主场景改成危险确认、三级操作或权限裁剪，只替换这一实现即可。
 */
@Single
class DefaultProjectsDialogFooterActionsSpi : ProjectsDialogFooterActionsSpi {
    @Composable
    override fun Render(
        state: ProjectsDialogFooterActionState,
        actions: ProjectsDialogFooterActions,
    ) {
        WorkbenchActionButton(
            text = state.cancelText,
            onClick = actions::dismiss,
            variant = WorkbenchButtonVariant.Outline,
        )
        WorkbenchActionButton(
            text = state.confirmText,
            onClick = actions::confirm,
            enabled = state.confirmEnabled,
            variant = state.confirmVariant,
        )
    }
}

/**
 * Projects 页面弹窗底部操作区状态。
 *
 * 这里收口弹窗 footer 的稳定视觉语义，避免每个 dialog 都继续平铺按钮文案和启用条件。
 */
internal data class ProjectsDialogFooterActionState(
    val cancelText: String = "取消",
    val confirmText: String,
    val confirmEnabled: Boolean,
    val confirmVariant: WorkbenchButtonVariant = WorkbenchButtonVariant.Default,
)

/**
 * Projects 页面弹窗底部操作桥接。
 *
 * 弹窗真正的确认逻辑往往依赖页面本地 draft、选择值或关闭状态，
 * 因此先收口成一个操作对象，再交给 slot 渲染，避免继续透传零散 lambda。
 */
internal class ProjectsDialogFooterActions(
    private val onDismiss: () -> Unit,
    private val onConfirm: () -> Unit,
) {
    fun dismiss() {
        onDismiss()
    }

    fun confirm() {
        onConfirm()
    }
}
