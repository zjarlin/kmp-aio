package site.addzero.kcloud.plugins.hostconfig.screen.projects

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant

/**
 * Projects 页面标签值文本区域里“新增一行”入口的交互插槽。
 *
 * 标签值文本表单布局仍保留在 `TagEditorForm`，
 * 这里专门承接新增映射行动作，方便宿主后续替换为批量导入或模板填充入口。
 */
internal interface ProjectsTagValueTextAppendActionSpi {
    @Composable
    fun Render(
        actions: ProjectsTagValueTextActions,
    )
}

/**
 * Projects 页面标签值文本区域里“新增一行”入口的默认实现。
 *
 * 当前默认只提供一个追加映射行按钮；
 * 如果后续要补更多尾部动作，只替换这一实现即可。
 */
@Single
internal class DefaultProjectsTagValueTextAppendActionSpi : ProjectsTagValueTextAppendActionSpi {
    @Composable
    override fun Render(
        actions: ProjectsTagValueTextActions,
    ) {
        WorkbenchActionButton(
            text = "新增一行",
            onClick = actions::appendValueText,
            variant = WorkbenchButtonVariant.Outline,
        )
    }
}
