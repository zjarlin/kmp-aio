package site.addzero.liquiddemo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandMore

data class LiquidGlassSidebarItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val children: List<LiquidGlassSidebarItem> = emptyList(),
    val initiallyExpanded: Boolean = true,
    val selectable: Boolean = true,
)

private val LiquidGlassSidebarItem.isBranch: Boolean
    get() = children.isNotEmpty()

@Composable
fun LiquidGlassSidebarMenu(
    title: String,
    items: List<LiquidGlassSidebarItem>,
    selectedId: String,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    LiquidGlassCard(
        modifier = modifier,
        spec = LiquidGlassDefaults.sidebar,
    ) {
        val expandedState = remember(items) {
            mutableStateMapOf<String, Boolean>().apply {
                items.registerInitialExpandedState(this)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                color = LiquidGlassDefaults.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Apple 风格更强调漂浮、单色图标、激活项高亮而不是满屏色块。",
                color = LiquidGlassDefaults.textMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items.forEach { item ->
                TreeSidebarItem(
                    item = item,
                    level = 0,
                    selectedId = selectedId,
                    expandedState = expandedState,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.TreeSidebarItem(
    item: LiquidGlassSidebarItem,
    level: Int,
    selectedId: String,
    expandedState: MutableMap<String, Boolean>,
    onSelect: (String) -> Unit,
) {
    val selected = item.selectable && item.id == selectedId
    val descendantSelected = item.children.any { it.containsSelection(selectedId) }
    val expanded = expandedState[item.id] ?: item.initiallyExpanded
    val spec = when {
        selected -> LiquidGlassDefaults.sidebarItemSelected
        descendantSelected -> LiquidGlassDefaults.sidebarItem.copy(
            edge = 0.014f,
            surfaceColor = Color.White.copy(alpha = 0.010f),
            tint = LiquidGlassDefaults.accent.copy(alpha = 0.024f),
        )
        else -> LiquidGlassDefaults.sidebarItem
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(spec)
            .clip(spec.shape)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = {
                    if (item.selectable) {
                        onSelect(item.id)
                    } else if (item.isBranch) {
                        expandedState[item.id] = !expanded
                    }
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(spec.shape),
        ) {
            if (selected || descendantSelected) {
                Box(modifier = selectedSidebarTopGlow())
                Box(modifier = selectedSidebarEndGlow())
                Box(modifier = selectedSidebarCornerCaustic())
                Box(modifier = selectedSidebarSweepHighlight())
            }
        }
        Row(
            modifier = Modifier.sidebarTreeItemPadding(level),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BranchToggle(
                isBranch = item.isBranch,
                expanded = expanded,
                onToggle = {
                    expandedState[item.id] = !expanded
                },
            )
            SidebarLeadingIcon(item.icon)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = item.title,
                    color = LiquidGlassDefaults.textPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = when {
                        selected -> FontWeight.Bold
                        descendantSelected -> FontWeight.SemiBold
                        else -> FontWeight.Medium
                    },
                )
                if (item.subtitle != null) {
                    Text(
                        text = item.subtitle,
                        color = when {
                            selected || descendantSelected -> LiquidGlassDefaults.textSecondary
                            else -> LiquidGlassDefaults.textMuted
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }

    if (item.isBranch && expanded) {
        item.children.forEach { child ->
            TreeSidebarItem(
                item = child,
                level = level + 1,
                selectedId = selectedId,
                expandedState = expandedState,
                onSelect = onSelect,
            )
        }
    }
}

@Composable
private fun BranchToggle(
    isBranch: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    if (!isBranch) {
        Box(modifier = Modifier.size(12.dp))
        return
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onToggle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (expanded) {
                Icons.Rounded.ExpandMore
            } else {
                Icons.AutoMirrored.Rounded.KeyboardArrowRight
            },
            contentDescription = null,
            tint = LiquidGlassDefaults.textMuted,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun SidebarLeadingIcon(
    icon: ImageVector?,
) {
    if (icon == null) {
        Box(modifier = Modifier.size(18.dp))
        return
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = LiquidGlassDefaults.textPrimary,
        modifier = Modifier.size(18.dp),
    )
}

/** 树节点内容缩进：按层级递增左内边距，形成真正的树结构。 */
private fun Modifier.sidebarTreeItemPadding(
    level: Int,
): Modifier = padding(
    start = (14 + level * 18).dp,
    top = 14.dp,
    end = 14.dp,
    bottom = 14.dp,
)

private fun List<LiquidGlassSidebarItem>.registerInitialExpandedState(
    expandedState: MutableMap<String, Boolean>,
) {
    forEach { item ->
        if (item.isBranch) {
            expandedState[item.id] = item.initiallyExpanded
            item.children.registerInitialExpandedState(expandedState)
        }
    }
}

private fun LiquidGlassSidebarItem.containsSelection(
    selectedId: String,
): Boolean {
    if (id == selectedId) {
        return true
    }
    return children.any { it.containsSelection(selectedId) }
}

/** 激活项左上角高光：只在内部聚光，不形成整块选中面板。 */
private fun BoxScope.selectedSidebarTopGlow(): Modifier =
    Modifier.align(Alignment.TopStart)
        .offset(x = (-2).dp, y = (-2).dp)
        .size(64.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.13f),
                    Color(0xFFFFF8EB).copy(alpha = 0.05f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(18.dp)

/** 激活项右侧折射辉光：让选中态更像液体鼓起，而不是边框发白。 */
private fun BoxScope.selectedSidebarEndGlow(): Modifier =
    Modifier.align(Alignment.CenterEnd)
        .offset(x = 8.dp)
        .size(70.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidGlassDefaults.accentStrong.copy(alpha = 0.10f),
                    LiquidGlassDefaults.accent.copy(alpha = 0.04f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(20.dp)

/** 左上角弧形焦散：改用一团软高光代替直角线段，避免出现矩形尖角。 */
private fun BoxScope.selectedSidebarCornerCaustic(): Modifier =
    Modifier.align(Alignment.TopStart)
        .offset(x = (-10).dp, y = (-10).dp)
        .size(88.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.16f),
                    Color(0xFFFFF8EB).copy(alpha = 0.06f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(22.dp)

/** 选中项右下扫光：补一点流动感，但不形成边框。 */
private fun BoxScope.selectedSidebarSweepHighlight(): Modifier =
    Modifier.align(Alignment.BottomEnd)
        .offset(x = (-8).dp, y = (-6).dp)
        .fillMaxWidth(0.16f)
        .height(14.dp)
        .rotate(-14f)
        .background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    LiquidGlassDefaults.accentStrong.copy(alpha = 0.05f),
                    Color.White.copy(alpha = 0.05f),
                ),
            ),
            shape = RoundedCornerShape(999.dp),
        )
        .blur(8.dp)
