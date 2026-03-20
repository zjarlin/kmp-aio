package site.addzero.liquiddemo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class LiquidGlassSidebarItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
)

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
                val selected = item.id == selectedId
                val spec = if (selected) {
                    LiquidGlassDefaults.sidebarItemSelected
                } else {
                    LiquidGlassDefaults.sidebarItem
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(spec)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = { onSelect(item.id) },
                        )
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = LiquidGlassDefaults.textPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = item.title,
                            color = LiquidGlassDefaults.textPrimary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                        if (item.subtitle != null) {
                            Text(
                                text = item.subtitle,
                                color = if (selected) {
                                    LiquidGlassDefaults.textSecondary
                                } else {
                                    LiquidGlassDefaults.textMuted
                                },
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
