package site.addzero.liquiddemo.demos

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import site.addzero.appsidebar.AppSidebarItem
import site.addzero.liquiddemo.SidebarDemoScene

internal fun settingsConsoleSidebarScene(): SidebarDemoScene {
    return SidebarDemoScene(
        id = "settings",
        title = "Settings Console",
        subtitle = "适合系统设置、AI 提供者、账号权限、连接配置等配置型界面。",
        initialSelectedId = "providers",
        items = listOf(
            AppSidebarItem(
                id = "general",
                title = "通用",
                icon = Icons.Rounded.Settings,
                order = 0,
                children = listOf(
                    AppSidebarItem("appearance", "界面外观", Icons.Rounded.Tune, order = 0),
                    AppSidebarItem("shortcuts", "快捷键", Icons.Rounded.Tune, order = 10),
                ),
            ),
            AppSidebarItem(
                id = "ai",
                title = "AI",
                icon = Icons.Rounded.AutoAwesome,
                order = 10,
                children = listOf(
                    AppSidebarItem("providers", "提供者", Icons.Rounded.Cloud, order = 0, badge = "1"),
                    AppSidebarItem("models", "模型", Icons.Rounded.AutoAwesome, order = 10),
                ),
            ),
            AppSidebarItem(
                id = "security",
                title = "安全",
                icon = Icons.Rounded.Security,
                order = 20,
            ),
        ),
        footerSlot = {
            Text(
                text = "Draft Changes",
                color = DemoTokens.textPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "当前还有 2 项未保存改动。",
                color = DemoTokens.textMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        detail = { selected ->
            Text(
                text = selected?.title ?: "提供者",
                color = DemoTokens.textPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "设置类侧栏和工作区类侧栏其实骨架差不多，区别主要只在文案和树节点组织方式，不该每个项目都重写一遍。",
                color = DemoTokens.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "这个 demo 重点演示：同一套骨架可以直接切到配置中台，不需要再额外写一套“设置专用侧栏”。",
                color = DemoTokens.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
    )
}
