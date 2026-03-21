package site.addzero.liquiddemo.demos

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import site.addzero.appsidebar.AppSidebarItem
import site.addzero.liquiddemo.SidebarDemoScene
import site.addzero.liquiddemo.SidebarDemoShell

internal fun adminBackendSidebarScene(): SidebarDemoScene {
    return SidebarDemoScene(
        id = "admin-backend",
        title = "Admin Backend",
        subtitle = "适合后台管理、租户中台、权限系统和运营工作台。",
        initialSelectedId = "orders",
        shell = SidebarDemoShell.AdminWorkbench,
        items = listOf(
            AppSidebarItem(
                id = "dashboard",
                title = "仪表盘",
                icon = Icons.Rounded.Dashboard,
                order = 0,
                children = listOf(
                    AppSidebarItem(
                        id = "overview",
                        title = "总览",
                        icon = Icons.Rounded.Dashboard,
                        order = 0,
                    ),
                    AppSidebarItem(
                        id = "orders",
                        title = "订单中心",
                        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                        order = 10,
                        badge = "12",
                    ),
                ),
            ),
            AppSidebarItem(
                id = "catalog",
                title = "商品",
                icon = Icons.Rounded.Inventory2,
                order = 10,
                children = listOf(
                    AppSidebarItem(
                        id = "products",
                        title = "商品列表",
                        icon = Icons.Rounded.Inventory2,
                        order = 0,
                    ),
                    AppSidebarItem(
                        id = "inventory",
                        title = "库存",
                        icon = Icons.Rounded.Inventory2,
                        order = 10,
                    ),
                ),
            ),
            AppSidebarItem(
                id = "users",
                title = "用户与权限",
                icon = Icons.Rounded.People,
                order = 20,
                children = listOf(
                    AppSidebarItem(
                        id = "members",
                        title = "成员",
                        icon = Icons.Rounded.People,
                        order = 0,
                    ),
                    AppSidebarItem(
                        id = "roles",
                        title = "角色",
                        icon = Icons.Rounded.AdminPanelSettings,
                        order = 10,
                    ),
                ),
            ),
            AppSidebarItem(
                id = "settings",
                title = "系统设置",
                icon = Icons.Rounded.Settings,
                order = 100,
            ),
        ),
        footerSlot = {
            Text(
                text = "Production",
                color = DemoTokens.textPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "当前租户：Acme · 区域：Hangzhou",
                color = DemoTokens.textMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        detail = { selected ->
            Text(
                text = selected?.title ?: "订单中心",
                color = DemoTokens.textPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "这个场景重点不是侧栏本身，而是把后台常见的页面头部、全局工具动作和右侧 Inspector 一起收成稳定骨架。",
                color = DemoTokens.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "默认 header 会把页面级操作和全局壳层动作分开，不再让每个后台页面自己拼一遍语言切换、主题切换、通知和用户入口。",
                color = DemoTokens.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
    )
}
