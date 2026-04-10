package site.addzero.kcloud.plugins.hostconfig.screen.projects

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.kcloud.plugins.hostconfig.screen.TagDraft
import site.addzero.kcloud.plugins.hostconfig.screen.TagValueTextDraft

/**
 * Projects 页面标签值文本区域里单行“删除映射”入口的交互插槽。
 *
 * 行内字段布局仍保留在 `TagEditorForm`，
 * 这里专门承接单条值文本映射的删除动作，方便宿主后续换成更多行内操作。
 */
internal interface ProjectsTagValueTextRemoveActionSpi {
    @Composable
    fun Render(
        index: Int,
        actions: ProjectsTagValueTextActions,
    )
}

/**
 * Projects 页面标签值文本区域里单行“删除映射”入口的默认实现。
 *
 * 当前默认只提供危险删除按钮；
 * 如果后续要补复制、上移下移等行内动作，只替换这一实现即可。
 */
@Single
internal class DefaultProjectsTagValueTextRemoveActionSpi : ProjectsTagValueTextRemoveActionSpi {
    @Composable
    override fun Render(
        index: Int,
        actions: ProjectsTagValueTextActions,
    ) {
        WorkbenchActionButton(
            text = "删除映射",
            onClick = { actions.removeValueTextAt(index) },
            variant = WorkbenchButtonVariant.Destructive,
        )
    }
}

/**
 * Projects 页面标签值文本区域动作桥接。
 *
 * 值文本行的增删依赖当前 `TagDraft` 本地草稿，
 * 因此先收口成小型操作对象，再交给 slot 渲染，避免按钮区继续直接操作表单结构。
 */
internal class ProjectsTagValueTextActions(
    private val draft: TagDraft,
    private val onDraftChange: (TagDraft) -> Unit,
) {
    fun removeValueTextAt(index: Int) {
        onDraftChange(
            draft.copy(
                valueTexts = draft.valueTexts.toMutableList().apply {
                    removeAt(index)
                },
            ),
        )
    }

    fun appendValueText() {
        onDraftChange(
            draft.copy(
                valueTexts = draft.valueTexts + TagValueTextDraft("", ""),
            ),
        )
    }
}
