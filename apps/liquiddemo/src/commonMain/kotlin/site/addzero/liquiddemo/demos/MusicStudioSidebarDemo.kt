package site.addzero.liquiddemo.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import site.addzero.appsidebar.AppSidebarItem
import site.addzero.liquiddemo.SidebarDemoScene

internal fun musicStudioSidebarScene(): SidebarDemoScene {
    return SidebarDemoScene(
        id = "music",
        title = "Music Studio",
        subtitle = "适合创作工具、素材库、模型控制台这类以工作区为中心的产品。",
        initialSelectedId = "generate",
        items = listOf(
            AppSidebarItem(
                id = "create",
                title = "创作",
                icon = Icons.Rounded.AutoAwesome,
                order = 0,
                children = listOf(
                    AppSidebarItem(
                        id = "generate",
                        title = "生成音乐",
                        icon = Icons.Rounded.AutoAwesome,
                        order = 0,
                        keywords = listOf("suno", "prompt", "music"),
                    ),
                    AppSidebarItem(
                        id = "voices",
                        title = "声线管理",
                        icon = Icons.Rounded.Tune,
                        order = 10,
                        badge = "3",
                    ),
                ),
            ),
            AppSidebarItem(
                id = "library",
                title = "素材库",
                icon = Icons.Rounded.LibraryMusic,
                order = 10,
                children = listOf(
                    AppSidebarItem(
                        id = "tracks",
                        title = "曲目资产",
                        icon = Icons.Rounded.LibraryMusic,
                        order = 0,
                    ),
                    AppSidebarItem(
                        id = "lyrics",
                        title = "歌词草稿",
                        icon = Icons.Rounded.Tune,
                        order = 10,
                        badge = "12",
                    ),
                ),
            ),
            AppSidebarItem(
                id = "settings",
                title = "设置",
                icon = Icons.Rounded.Settings,
                order = 100,
            ),
        ),
        headerSlot = {
            DemoLabel(text = "创作者工作流")
        },
        footerSlot = {
            DemoFooterBlock(
                title = "Suno Runtime",
                detail = "当前连接正常，剩余积分 2。",
            )
        },
        detail = { selected ->
            DemoHeadline(
                title = selected?.title ?: "生成音乐",
                subtitle = "这类侧栏最重要的是：树结构清楚、搜索马上可用、常用叶子节点一眼到位。",
            )
            DemoBody(
                text = "默认已经把“分组节点只负责收纳、叶子节点负责实际跳转”的交互写死了。业务方只需要提供标题、图标、排序和点击行为。",
            )
            DemoBulletGroup(
                title = "适合的页面骨架",
                items = listOf(
                    "左侧侧栏管理工作流分区，右侧是主工作区",
                    "顶部可以再放筛选、二级 Tab、工具条",
                    "底部插槽适合账号状态、同步状态、当前环境",
                ),
            )
        },
    )
}

@Composable
private fun ColumnScope.DemoHeadline(
    title: String,
    subtitle: String,
) {
    Text(
        text = title,
        color = DemoTokens.textPrimary,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
    )
    Text(
        text = subtitle,
        color = DemoTokens.textMuted,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun ColumnScope.DemoBody(
    text: String,
) {
    Text(
        text = text,
        color = DemoTokens.textSecondary,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun ColumnScope.DemoBulletGroup(
    title: String,
    items: List<String>,
) {
    Text(
        text = title,
        color = DemoTokens.textPrimary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    items.forEach { item ->
        Text(
            text = "• $item",
            color = DemoTokens.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DemoLabel(
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = text,
            color = DemoTokens.textPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.demoLabelFrame(),
        )
    }
}

@Composable
private fun DemoFooterBlock(
    title: String,
    detail: String,
) {
    Text(
        text = title,
        color = DemoTokens.textPrimary,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        text = detail,
        color = DemoTokens.textMuted,
        style = MaterialTheme.typography.bodySmall,
    )
}
