package site.addzero.liquiddemo.demos

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import site.addzero.appsidebar.AppSidebarItem
import site.addzero.liquiddemo.SidebarDemoScene

internal fun projectWorkbenchSidebarScene(): SidebarDemoScene {
    return SidebarDemoScene(
        id = "project",
        title = "Project Workbench",
        subtitle = "适合资产管理、项目管理、知识库和带层级目录的后台。",
        initialSelectedId = "project-overview",
        items = listOf(
            AppSidebarItem(
                id = "project-home",
                title = "项目空间",
                icon = Icons.Rounded.Home,
                order = 0,
                children = listOf(
                    AppSidebarItem(
                        id = "project-overview",
                        title = "总览",
                        icon = Icons.Rounded.Home,
                        order = 0,
                    ),
                    AppSidebarItem(
                        id = "assets",
                        title = "资产库",
                        icon = Icons.Rounded.Inventory2,
                        order = 10,
                        badge = "18",
                    ),
                ),
            ),
            AppSidebarItem(
                id = "folders",
                title = "文件夹",
                icon = Icons.Rounded.Folder,
                order = 10,
                children = listOf(
                    AppSidebarItem("specs", "产品文档", Icons.Rounded.Folder, order = 0),
                    AppSidebarItem("delivery", "交付清单", Icons.Rounded.Folder, order = 10),
                    AppSidebarItem("archive", "归档", Icons.Rounded.Folder, order = 20),
                ),
            ),
            AppSidebarItem(
                id = "team",
                title = "团队",
                icon = Icons.Rounded.Groups,
                order = 20,
            ),
            AppSidebarItem(
                id = "settings",
                title = "设置",
                icon = Icons.Rounded.Settings,
                order = 100,
            ),
        ),
        headerSlot = {
            Text(
                text = "默认适合文件树 / 项目树 / 资源树",
                color = DemoTokens.textMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        footerSlot = {
            Text(
                text = "Workspace Sync",
                color = DemoTokens.textPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "刚刚同步了 18 个文件，状态正常。",
                color = DemoTokens.textMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        detail = { selected ->
            Text(
                text = selected?.title ?: "总览",
                color = DemoTokens.textPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "这种场景最怕的是结构太花。这里默认就是克制暗色桌面风，更适合作为商用后台和工程工作台的主导航。",
                color = DemoTokens.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "如果你不想让调用方操心视觉，只需要把数据树喂进来，搜索、折叠、默认选中和树缩进都已经封好了。",
                color = DemoTokens.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
    )
}
