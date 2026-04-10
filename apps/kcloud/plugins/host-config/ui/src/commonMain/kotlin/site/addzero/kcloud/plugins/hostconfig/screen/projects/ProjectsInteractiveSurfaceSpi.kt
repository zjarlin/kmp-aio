package site.addzero.kcloud.plugins.hostconfig.screen.projects

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.core.annotation.Single

/**
 * Projects 页面通用交互表面的插槽。
 *
 * 页面里的子节点卡片、模块总览卡片、节点动作菜单项都属于“布局保持本地，点击行为可替换”的交互块；
 * 这个 slot 只负责把点击语义挂到既有布局上，避免把页面里的 `clickable` 细节直接写死在布局函数里。
 */
internal interface ProjectsInteractiveSurfaceSpi {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        actions: ProjectsInteractiveSurfaceActions = ProjectsInteractiveSurfaceActions(),
        content: @Composable (Modifier) -> Unit,
    )
}

/**
 * Projects 页面通用交互表面的默认实现。
 *
 * 当前默认行为只是按启用状态挂上点击能力，不改页面自己的排版、背景、边框和内容；
 * 后续如果宿主要补权限拦截、埋点、统一提示或其他交互策略，只替换这一实现即可。
 */
@Single
internal class DefaultProjectsInteractiveSurfaceSpi : ProjectsInteractiveSurfaceSpi {
    @Composable
    override fun Render(
        modifier: Modifier,
        enabled: Boolean,
        actions: ProjectsInteractiveSurfaceActions,
        content: @Composable (Modifier) -> Unit,
    ) {
        val interactiveModifier = if (!actions.hasClickAction()) {
            modifier
        } else {
            modifier.clickable(
                enabled = enabled,
                onClick = actions::triggerClick,
            )
        }
        content(interactiveModifier)
    }
}

/**
 * Projects 页面通用交互表面的动作桥接。
 *
 * 这类交互块本身不需要感知整个页面视图模型，
 * 但仍然要避免在布局函数里散落裸 lambda，所以先收口成一个最小 actions 对象再交给 slot。
 */
internal class ProjectsInteractiveSurfaceActions(
    private val onClick: (() -> Unit)? = null,
) {
    fun hasClickAction(): Boolean {
        return onClick != null
    }

    fun triggerClick() {
        onClick?.invoke()
    }
}
