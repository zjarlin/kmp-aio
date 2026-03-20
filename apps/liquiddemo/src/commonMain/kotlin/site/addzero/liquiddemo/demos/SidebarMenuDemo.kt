package site.addzero.liquiddemo.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
    var selectedId by remember { mutableStateOf("inbox") }
    val sidebarItems = remember {
        listOf(
            LiquidGlassSidebarItem("inbox", "Inbox", "Pinned", Icons.Rounded.Home),
            LiquidGlassSidebarItem("library", "Library", "Collections", Icons.Rounded.Folder),
            LiquidGlassSidebarItem("team", "Team", "People", Icons.Rounded.Person),
            LiquidGlassSidebarItem("settings", "Settings", "Preferences", Icons.Rounded.Settings),
        )
    }
    val selected = sidebarItems.first { it.id == selectedId }

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
                text = "当前选中的是 ${selected.title}。Apple 的做法更像浮在内容之上的轻面板，因此这里保持了单色图标、弱边框和低饱和高亮。",
                color = LiquidGlassDefaults.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
