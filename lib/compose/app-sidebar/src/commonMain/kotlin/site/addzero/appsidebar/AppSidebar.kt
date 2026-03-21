package site.addzero.appsidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun AppSidebar(
    title: String,
    items: List<AppSidebarItem>,
    modifier: Modifier = Modifier,
    state: AppSidebarState = rememberAppSidebarState(),
    supportText: String? = null,
    searchEnabled: Boolean = true,
    searchPlaceholder: String = "搜索菜单",
    headerSlot: @Composable ColumnScope.() -> Unit = {},
    footerSlot: @Composable ColumnScope.() -> Unit = {},
    emptySlot: @Composable ColumnScope.(String) -> Unit = { query ->
        DefaultSidebarEmpty(
            query = query,
        )
    },
    onItemClick: (AppSidebarItem) -> Unit = {},
) {
    val sortedItems = remember(items) {
        items.sortSidebarItems()
    }
    val visibleItems = remember(sortedItems, state.searchQuery) {
        sortedItems.filterSidebarItems(state.searchQuery)
    }

    LaunchedEffect(sortedItems) {
        state.ensureInitialized(sortedItems)
    }

    Box(
        modifier = modifier.sidebarContainer(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SidebarHeader(
                title = title,
                supportText = supportText,
                headerSlot = headerSlot,
            )

            if (searchEnabled) {
                SidebarSearchField(
                    value = state.searchQuery,
                    placeholder = searchPlaceholder,
                    onValueChange = state::updateSearchQuery,
                    onClear = state::clearSearch,
                )
            }

            Box(
                modifier = Modifier.weight(1f),
            ) {
                if (visibleItems.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        content = {
                            emptySlot(state.searchQuery)
                        },
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        visibleItems.forEach { item ->
                            SidebarTreeItem(
                                item = item,
                                level = 0,
                                state = state,
                                searchActive = state.searchQuery.isNotBlank(),
                                onItemClick = onItemClick,
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = footerSlot,
            )
        }
    }
}

@Composable
private fun SidebarHeader(
    title: String,
    supportText: String?,
    headerSlot: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = SidebarTokens.textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
        if (supportText != null) {
            Text(
                text = supportText,
                color = SidebarTokens.textMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        headerSlot()
    }
}

@Composable
private fun SidebarSearchField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().searchFieldContainer(),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = SidebarTokens.textPrimary,
        ),
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = SidebarTokens.textMuted,
            )
        },
        trailingIcon = {
            if (value.isNotBlank()) {
                Text(
                    text = "清空",
                    color = SidebarTokens.textMuted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onClear),
                )
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                color = SidebarTokens.textFaint,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SidebarTokens.searchBackground,
            unfocusedContainerColor = SidebarTokens.searchBackground,
            disabledContainerColor = SidebarTokens.searchBackground,
            focusedTextColor = SidebarTokens.textPrimary,
            unfocusedTextColor = SidebarTokens.textPrimary,
            cursorColor = SidebarTokens.textPrimary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
    )
}

@Composable
private fun ColumnScope.SidebarTreeItem(
    item: AppSidebarItem,
    level: Int,
    state: AppSidebarState,
    searchActive: Boolean,
    onItemClick: (AppSidebarItem) -> Unit,
) {
    val selected = item.id == state.selectedId
    val descendantSelected = item.children.any { child ->
        child.containsSelection(state.selectedId)
    }
    val expanded = if (searchActive) {
        true
    } else {
        state.expandedItems[item.id] ?: item.initiallyExpanded
    }

    Box(
        modifier = Modifier.fillMaxWidth()
            .sidebarItemFrame(
                selected = selected,
                descendantSelected = descendantSelected,
            )
            .clickable(
                onClick = {
                    if (item.isBranch) {
                        state.toggleExpanded(item.id)
                    } else {
                        state.select(item)
                        item.onClick?.invoke()
                        onItemClick(item)
                    }
                },
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().sidebarItemPadding(level),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SidebarBranchToggle(
                isBranch = item.isBranch,
                expanded = expanded,
                onToggle = {
                    state.toggleExpanded(item.id)
                },
            )
            SidebarIcon(icon = item.icon)
            Text(
                text = item.title,
                color = SidebarTokens.textPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            SidebarBadge(item.badge)
        }
    }

    if (item.isBranch && expanded) {
        item.children.forEach { child ->
            SidebarTreeItem(
                item = child,
                level = level + 1,
                state = state,
                searchActive = searchActive,
                onItemClick = onItemClick,
            )
        }
    }
}

@Composable
private fun SidebarBranchToggle(
    isBranch: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    if (!isBranch) {
        Spacer(modifier = Modifier.size(14.dp))
        return
    }

    Box(
        modifier = Modifier.size(16.dp).clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = if (expanded) {
                Icons.Rounded.ExpandMore
            } else {
                Icons.AutoMirrored.Rounded.KeyboardArrowRight
            },
            contentDescription = null,
            tint = SidebarTokens.textMuted,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun SidebarIcon(
    icon: ImageVector?,
) {
    if (icon == null) {
        Spacer(modifier = Modifier.width(18.dp))
        return
    }

    androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = null,
        tint = SidebarTokens.textPrimary,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun SidebarBadge(
    badge: String?,
) {
    if (badge == null) {
        return
    }

    Box(
        modifier = Modifier.badgeFrame(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badge,
            color = SidebarTokens.textPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ColumnScope.DefaultSidebarEmpty(
    query: String,
) {
    Text(
        text = "没有找到 “$query” 对应的导航项。",
        color = SidebarTokens.textMuted,
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = "可以试试更短的关键词，或者直接从完整菜单里选。",
        color = SidebarTokens.textFaint,
        style = MaterialTheme.typography.bodySmall,
    )
}

/** 侧栏容器：固定成商用暗色壳体，默认就能压住复杂工作台背景。 */
private fun Modifier.sidebarContainer(): Modifier {
    return fillMaxHeight()
        .background(SidebarTokens.containerBrush, SidebarTokens.containerShape)
        .border(1.dp, SidebarTokens.containerBorder, SidebarTokens.containerShape)
        .padding(1.dp)
        .background(SidebarTokens.containerBackground, SidebarTokens.containerShape)
}

/** 搜索框容器：保持低对比、低噪声，只给一个稳定的输入感。 */
private fun Modifier.searchFieldContainer(): Modifier {
    return height(48.dp)
        .background(SidebarTokens.searchBackground, SidebarTokens.searchShape)
        .border(1.dp, SidebarTokens.searchBorder, SidebarTokens.searchShape)
}

/** 树节点内边距：按层级递增左缩进，形成稳定的信息树。 */
private fun Modifier.sidebarItemPadding(level: Int): Modifier {
    return padding(
        start = (12 + level * 18).dp,
        top = 12.dp,
        end = 12.dp,
        bottom = 12.dp,
    )
}

/** 节点底板：选中项更饱满，祖先项只给轻微提示，不制造视觉噪声。 */
private fun Modifier.sidebarItemFrame(
    selected: Boolean,
    descendantSelected: Boolean,
): Modifier {
    val border = when {
        selected -> SidebarTokens.selectedBorder
        descendantSelected -> SidebarTokens.ancestorBorder
        else -> Color.Transparent
    }
    val baseModifier = when {
        selected -> background(SidebarTokens.selectedBackgroundBrush, SidebarTokens.itemShape)
        descendantSelected -> background(SidebarTokens.ancestorBackground, SidebarTokens.itemShape)
        else -> this
    }

    return baseModifier.border(1.dp, border, SidebarTokens.itemShape)
}

/** 角标胶囊：把数量和状态压成很轻的一颗暗色药丸。 */
private fun Modifier.badgeFrame(): Modifier {
    return background(SidebarTokens.badgeBackground, CircleShape)
        .padding(horizontal = 8.dp, vertical = 4.dp)
}

private object SidebarTokens {
    val containerShape = RoundedCornerShape(26.dp)
    val searchShape = RoundedCornerShape(16.dp)
    val itemShape = RoundedCornerShape(16.dp)

    val containerBackground = Color(0xFF091221).copy(alpha = 0.94f)
    val containerBorder = Color.White.copy(alpha = 0.08f)
    val containerBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0E1A30).copy(alpha = 0.94f),
            Color(0xFF091221).copy(alpha = 0.98f),
        ),
    )

    val searchBackground = Color.White.copy(alpha = 0.05f)
    val searchBorder = Color.White.copy(alpha = 0.06f)
    val selectedBackgroundBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF336DFF).copy(alpha = 0.34f),
            Color(0xFF2448A8).copy(alpha = 0.26f),
        ),
    )
    val selectedBorder = Color(0xFF9BC1FF).copy(alpha = 0.26f)
    val ancestorBackground = Color.White.copy(alpha = 0.05f)
    val ancestorBorder = Color.White.copy(alpha = 0.05f)
    val badgeBackground = Color.White.copy(alpha = 0.10f)

    val textPrimary = Color.White.copy(alpha = 0.96f)
    val textMuted = Color.White.copy(alpha = 0.68f)
    val textFaint = Color.White.copy(alpha = 0.46f)
}
