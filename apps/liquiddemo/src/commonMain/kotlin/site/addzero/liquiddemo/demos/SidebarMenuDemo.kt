package site.addzero.liquiddemo.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.liquiddemo.components.LiquidGlassCard
import site.addzero.liquiddemo.components.LiquidGlassCardHeader
import site.addzero.liquiddemo.components.LiquidGlassDefaults
import site.addzero.liquiddemo.components.LiquidGlassSidebarItem
import site.addzero.liquiddemo.components.LiquidGlassSidebarMenu

@Composable
fun SidebarMenuDemo(
    modifier: Modifier = Modifier,
) {
    var selectedId by remember { mutableStateOf("library-assets") }
    val sidebarItems = remember {
        listOf(
            LiquidGlassSidebarItem(
                id = "workspace",
                title = "Workspace",
                subtitle = "内容树",
                icon = Icons.Rounded.Folder,
                selectable = false,
                children = listOf(
                    LiquidGlassSidebarItem("inbox", "Inbox", "Pinned", Icons.Rounded.Home),
                    LiquidGlassSidebarItem(
                        id = "library",
                        title = "Library",
                        subtitle = "Collections",
                        icon = Icons.Rounded.Folder,
                        selectable = false,
                        children = listOf(
                            LiquidGlassSidebarItem("library-docs", "Documents", "Specs", Icons.Rounded.Folder),
                            LiquidGlassSidebarItem("library-assets", "Assets", "Materials", Icons.Rounded.Folder),
                        ),
                    ),
                    LiquidGlassSidebarItem("team", "Team", "People", Icons.Rounded.Person),
                ),
            ),
            LiquidGlassSidebarItem(
                id = "preferences",
                title = "Preferences",
                subtitle = "系统设置",
                icon = Icons.Rounded.Settings,
                selectable = false,
                children = listOf(
                    LiquidGlassSidebarItem("settings", "Settings", "General", Icons.Rounded.Settings),
                    LiquidGlassSidebarItem("ai", "AI", "Providers", Icons.Rounded.AutoAwesome),
                ),
            ),
        )
    }
    val selected = sidebarItems.findSidebarItem(selectedId) ?: sidebarItems.firstSelectableItem()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        LiquidGlassSidebarMenu(
            title = "Sidebar",
            items = sidebarItems,
            selectedId = selectedId,
            modifier = Modifier.width(250.dp),
            onSelect = { selectedId = it },
        )

        LiquidGlassCard(
            modifier = Modifier.weight(1f),
            spec = LiquidGlassDefaults.heroCard,
        ) {
            LiquidGlassCardHeader(
                title = selected.title,
                subtitle = "这个 demo 展示了组件库里的侧边菜单成品，外层面板和激活项都是固定好的高阶材质。",
                badge = "Sidebar",
            )
            Text(
                text = "当前选中的是 ${selected.title}。这个版本已经支持树形侧边栏：父节点负责展开/收起，叶子节点负责实际选中，因此更适合文件、设置和工作区目录这类层级结构。",
                color = LiquidGlassDefaults.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun List<LiquidGlassSidebarItem>.findSidebarItem(
    id: String,
): LiquidGlassSidebarItem? = asSequence()
    .mapNotNull { it.findSidebarItem(id) }
    .firstOrNull()

private fun LiquidGlassSidebarItem.findSidebarItem(
    id: String,
): LiquidGlassSidebarItem? {
    if (this.id == id) {
        return this
    }
    return children.asSequence()
        .mapNotNull { it.findSidebarItem(id) }
        .firstOrNull()
}

private fun List<LiquidGlassSidebarItem>.firstSelectableItem(): LiquidGlassSidebarItem =
    asSequence()
        .mapNotNull { it.firstSelectableItemOrNull() }
        .first()

private fun LiquidGlassSidebarItem.firstSelectableItemOrNull(): LiquidGlassSidebarItem? {
    if (selectable) {
        return this
    }
    return children.asSequence()
        .mapNotNull { it.firstSelectableItemOrNull() }
        .firstOrNull()
}
