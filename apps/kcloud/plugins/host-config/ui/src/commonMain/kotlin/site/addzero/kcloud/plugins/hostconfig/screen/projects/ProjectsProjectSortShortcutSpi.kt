package site.addzero.kcloud.plugins.hostconfig.screen.projects

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant

/**
 * Projects 页面工程编辑表单里“仅更新排序”入口的交互插槽。
 *
 * 表单字段布局仍保留在 `ProjectEditorForm`，
 * 这里专门承接排序快捷更新动作，方便宿主后续替换为批量排序或权限校验入口。
 */
internal interface ProjectsProjectSortShortcutSpi {
    @Composable
    fun Render(
        actions: ProjectsProjectSortShortcutActions,
    )
}

/**
 * Projects 页面工程编辑表单里“仅更新排序”入口的默认实现。
 *
 * 当前默认只提供一个次级按钮触发排序更新；
 * 如果后续要改成更多快捷动作，只替换这一实现即可。
 */
@Single
internal class DefaultProjectsProjectSortShortcutSpi : ProjectsProjectSortShortcutSpi {
    @Composable
    override fun Render(
        actions: ProjectsProjectSortShortcutActions,
    ) {
        WorkbenchActionButton(
            text = "仅更新排序",
            onClick = actions::updateSort,
            variant = WorkbenchButtonVariant.Secondary,
        )
    }
}

/**
 * Projects 页面工程编辑表单排序快捷动作桥接。
 */
internal class ProjectsProjectSortShortcutActions(
    private val onUpdateSort: () -> Unit,
) {
    fun updateSort() {
        onUpdateSort()
    }
}
