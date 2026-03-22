package site.addzero.appsidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppSidebarStyle {
    Default,
    FlushWorkbench,
}

@Composable
fun AppSidebar(
    title: String,
    items: List<AppSidebarItem>,
    modifier: Modifier = Modifier,
    state: AppSidebarState = rememberAppSidebarState(),
    style: AppSidebarStyle = AppSidebarStyle.Default,
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
    val tokens = remember(style) {
        style.resolveTokens()
    }
    val visibleItems = remember(sortedItems, state.searchQuery) {
        sortedItems.filterSidebarItems(state.searchQuery)
    }

    LaunchedEffect(sortedItems) {
        state.ensureInitialized(sortedItems)
    }

    Box(
        modifier = modifier.sidebarContainer(tokens),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(tokens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SidebarHeader(
                title = title,
                supportText = supportText,
                headerSlot = headerSlot,
                tokens = tokens,
            )

            if (searchEnabled) {
                SidebarSearchField(
                    value = state.searchQuery,
                    placeholder = searchPlaceholder,
                    onValueChange = state::updateSearchQuery,
                    onClear = state::clearSearch,
                    tokens = tokens,
                )
            }

            Box(
                modifier = Modifier.weight(1f),
            ) {
                if (visibleItems.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(vertical = tokens.emptyVerticalPadding),
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
                                tokens = tokens,
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
    tokens: SidebarStyleTokens,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = tokens.textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
        if (supportText != null) {
            Text(
                text = supportText,
                color = tokens.textMuted,
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
    tokens: SidebarStyleTokens,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().searchFieldContainer(tokens),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = tokens.textPrimary,
        ),
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = tokens.textMuted,
            )
        },
        trailingIcon = {
            if (value.isNotBlank()) {
                Text(
                    text = "清空",
                    color = tokens.textMuted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onClear),
                )
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                color = tokens.textFaint,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = tokens.searchBackground,
            unfocusedContainerColor = tokens.searchBackground,
            disabledContainerColor = tokens.searchBackground,
            focusedTextColor = tokens.textPrimary,
            unfocusedTextColor = tokens.textPrimary,
            cursorColor = tokens.textPrimary,
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
    tokens: SidebarStyleTokens,
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
                tokens = tokens,
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
            modifier = Modifier.fillMaxWidth().sidebarItemPadding(level, tokens),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SidebarBranchToggle(
                isBranch = item.isBranch,
                expanded = expanded,
                tokens = tokens,
                onToggle = {
                    state.toggleExpanded(item.id)
                },
            )
            SidebarIcon(
                icon = item.icon,
                tokens = tokens,
            )
            Text(
                text = item.title,
                color = tokens.textPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            SidebarBadge(
                badge = item.badge,
                tokens = tokens,
            )
        }
    }

    if (item.isBranch && expanded) {
        item.children.forEach { child ->
            SidebarTreeItem(
                item = child,
                level = level + 1,
                state = state,
                tokens = tokens,
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
    tokens: SidebarStyleTokens,
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
                tint = tokens.textMuted,
                modifier = Modifier.size(16.dp),
            )
        }
}

@Composable
private fun SidebarIcon(
    icon: ImageVector?,
    tokens: SidebarStyleTokens,
) {
    if (icon == null) {
        Spacer(modifier = Modifier.width(18.dp))
        return
    }

    androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tokens.textPrimary,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun SidebarBadge(
    badge: String?,
    tokens: SidebarStyleTokens,
) {
    if (badge == null) {
        return
    }

    Box(
        modifier = Modifier.badgeFrame(tokens),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badge,
            color = tokens.textPrimary,
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
        color = AppSidebarStyle.Default.resolveTokens().textMuted,
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = "可以试试更短的关键词，或者直接从完整菜单里选。",
        color = AppSidebarStyle.Default.resolveTokens().textFaint,
        style = MaterialTheme.typography.bodySmall,
    )
}

/** 侧栏容器：固定成商用暗色壳体，默认就能压住复杂工作台背景。 */
private fun Modifier.sidebarContainer(
    tokens: SidebarStyleTokens,
): Modifier {
    return fillMaxHeight()
        .background(tokens.containerBrush, tokens.containerShape)
        .border(1.dp, tokens.containerBorder, tokens.containerShape)
        .padding(1.dp)
        .background(tokens.containerBackground, tokens.containerShape)
}

/** 搜索框容器：保持低对比、低噪声，只给一个稳定的输入感。 */
private fun Modifier.searchFieldContainer(
    tokens: SidebarStyleTokens,
): Modifier {
    return height(48.dp)
        .background(tokens.searchBackground, tokens.searchShape)
        .border(1.dp, tokens.searchBorder, tokens.searchShape)
}

/** 树节点内边距：按层级递增左缩进，形成稳定的信息树。 */
private fun Modifier.sidebarItemPadding(
    level: Int,
    tokens: SidebarStyleTokens,
): Modifier {
    return padding(
        start = tokens.itemStartPadding + tokens.itemIndentStep * level,
        top = tokens.itemVerticalPadding,
        end = tokens.itemEndPadding,
        bottom = tokens.itemVerticalPadding,
    )
}

/** 节点底板：选中项更饱满，祖先项只给轻微提示，不制造视觉噪声。 */
private fun Modifier.sidebarItemFrame(
    selected: Boolean,
    descendantSelected: Boolean,
    tokens: SidebarStyleTokens,
): Modifier {
    val border = when {
        selected -> tokens.selectedBorder
        descendantSelected -> tokens.ancestorBorder
        else -> Color.Transparent
    }
    val baseModifier = when {
        selected -> background(tokens.selectedBackgroundBrush, tokens.itemShape)
        descendantSelected -> background(tokens.ancestorBackground, tokens.itemShape)
        else -> this
    }

    return baseModifier.border(1.dp, border, tokens.itemShape)
}

/** 角标胶囊：把数量和状态压成很轻的一颗暗色药丸。 */
private fun Modifier.badgeFrame(
    tokens: SidebarStyleTokens,
): Modifier {
    return background(tokens.badgeBackground, CircleShape)
        .padding(horizontal = 8.dp, vertical = 4.dp)
}

@Immutable
private data class SidebarStyleTokens(
    val contentPadding: PaddingValues,
    val emptyVerticalPadding: Dp,
    val containerShape: Shape,
    val searchShape: Shape,
    val itemShape: Shape,
    val containerBackground: Color,
    val containerBorder: Color,
    val containerBrush: Brush,
    val searchBackground: Color,
    val searchBorder: Color,
    val selectedBackgroundBrush: Brush,
    val selectedBorder: Color,
    val ancestorBackground: Color,
    val ancestorBorder: Color,
    val badgeBackground: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val textFaint: Color,
    val itemStartPadding: Dp,
    val itemIndentStep: Dp,
    val itemVerticalPadding: Dp,
    val itemEndPadding: Dp,
)

private fun AppSidebarStyle.resolveTokens(): SidebarStyleTokens {
    return when (this) {
        AppSidebarStyle.Default -> SidebarStyleTokens(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            emptyVerticalPadding = 8.dp,
            containerShape = RoundedCornerShape(26.dp),
            searchShape = RoundedCornerShape(16.dp),
            itemShape = RoundedCornerShape(16.dp),
            containerBackground = Color(0xFF091221).copy(alpha = 0.94f),
            containerBorder = Color.White.copy(alpha = 0.08f),
            containerBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0E1A30).copy(alpha = 0.94f),
                    Color(0xFF091221).copy(alpha = 0.98f),
                ),
            ),
            searchBackground = Color.White.copy(alpha = 0.05f),
            searchBorder = Color.White.copy(alpha = 0.06f),
            selectedBackgroundBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF336DFF).copy(alpha = 0.34f),
                    Color(0xFF2448A8).copy(alpha = 0.26f),
                ),
            ),
            selectedBorder = Color(0xFF9BC1FF).copy(alpha = 0.26f),
            ancestorBackground = Color.White.copy(alpha = 0.05f),
            ancestorBorder = Color.White.copy(alpha = 0.05f),
            badgeBackground = Color.White.copy(alpha = 0.10f),
            textPrimary = Color.White.copy(alpha = 0.96f),
            textMuted = Color.White.copy(alpha = 0.68f),
            textFaint = Color.White.copy(alpha = 0.46f),
            itemStartPadding = 12.dp,
            itemIndentStep = 18.dp,
            itemVerticalPadding = 12.dp,
            itemEndPadding = 12.dp,
        )

        AppSidebarStyle.FlushWorkbench -> SidebarStyleTokens(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            emptyVerticalPadding = 0.dp,
            containerShape = RoundedCornerShape(0.dp),
            searchShape = RoundedCornerShape(12.dp),
            itemShape = RoundedCornerShape(12.dp),
            containerBackground = Color(0xFF081220).copy(alpha = 0.98f),
            containerBorder = Color.White.copy(alpha = 0.04f),
            containerBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF102136).copy(alpha = 0.98f),
                    Color(0xFF081220).copy(alpha = 1f),
                ),
            ),
            searchBackground = Color(0xFF112136).copy(alpha = 0.92f),
            searchBorder = Color.White.copy(alpha = 0.05f),
            selectedBackgroundBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF3A7BFF).copy(alpha = 0.42f),
                    Color(0xFF2749A8).copy(alpha = 0.34f),
                ),
            ),
            selectedBorder = Color(0xFFA9C8FF).copy(alpha = 0.22f),
            ancestorBackground = Color.White.copy(alpha = 0.04f),
            ancestorBorder = Color.Transparent,
            badgeBackground = Color.White.copy(alpha = 0.08f),
            textPrimary = Color.White.copy(alpha = 0.97f),
            textMuted = Color.White.copy(alpha = 0.72f),
            textFaint = Color.White.copy(alpha = 0.48f),
            itemStartPadding = 10.dp,
            itemIndentStep = 16.dp,
            itemVerticalPadding = 10.dp,
            itemEndPadding = 10.dp,
        )
    }
}
