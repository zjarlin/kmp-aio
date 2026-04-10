package site.addzero.kcloud.plugins.hostconfig.screen.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Upload
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState
import site.addzero.kcloud.plugins.hostconfig.screen.ProjectsSidebarActions

/**
 * Projects 页面左侧树工具区的交互插槽。
 *
 * 页面骨架和树布局仍留在 `ProjectsWorkbenchContent` 里，
 * 这里专门承接“新建工程 / 导入导出 SQLite / 刷新”这一组用户动作，
 * 方便宿主后续替换按钮组合、文案或权限策略，而不改页面结构。
 */
interface ProjectsSidebarActionGridSpi {
    @Composable
    fun Render(
        state: ProjectsScreenState,
        actions: ProjectsSidebarActions,
    )
}

/**
 * Projects 页面左侧树工具区的默认实现。
 *
 * 当前默认保留四个核心入口：新建工程、导出 SQLite、导入 SQLite、刷新。
 * 后续如果要按宿主场景裁剪按钮或接入额外动作，只替换这一实现即可。
 */
@Single
class DefaultProjectsSidebarActionGridSpi : ProjectsSidebarActionGridSpi {
    @Composable
    override fun Render(
        state: ProjectsScreenState,
        actions: ProjectsSidebarActions,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                WorkbenchActionButton(
                    text = "新建工程",
                    onClick = actions::createProject,
                    imageVector = Icons.Outlined.Add,
                    modifier = Modifier.weight(1f),
                    variant = WorkbenchButtonVariant.Default,
                    enabled = !state.busy,
                )
                WorkbenchActionButton(
                    text = "导出 SQLite",
                    onClick = actions::exportProjectSqlite,
                    modifier = Modifier.weight(1f),
                    variant = WorkbenchButtonVariant.Outline,
                    enabled = state.selectedProjectId != null && !state.busy,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                WorkbenchActionButton(
                    text = "导入 SQLite",
                    onClick = actions::importProjectSqlite,
                    imageVector = Icons.Outlined.Upload,
                    modifier = Modifier.weight(1f),
                    variant = WorkbenchButtonVariant.Secondary,
                    enabled = !state.busy,
                )
                WorkbenchActionButton(
                    text = if (state.loading) "加载中" else "刷新",
                    onClick = actions::refresh,
                    imageVector = Icons.Outlined.Refresh,
                    modifier = Modifier.weight(1f),
                    variant = WorkbenchButtonVariant.Ghost,
                    enabled = !state.busy,
                )
            }
        }
    }
}
