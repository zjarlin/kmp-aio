package site.addzero.vibepocket.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcloud.feature.KCloudMenuNode

@Composable
fun VibePocketFeatureSidebar(
    nodes: List<KCloudMenuNode>,
    selectedId: String,
    expandedIds: Set<String>,
    onLeafClick: (String) -> Unit,
    onGroupToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        nodes.forEach { node ->
            FeatureMenuRow(
                node = node,
                selectedId = selectedId,
                expandedIds = expandedIds,
                onLeafClick = onLeafClick,
                onGroupToggle = onGroupToggle,
            )
        }
    }
}

@Composable
private fun FeatureMenuRow(
    node: KCloudMenuNode,
    selectedId: String,
    expandedIds: Set<String>,
    onLeafClick: (String) -> Unit,
    onGroupToggle: (String) -> Unit,
) {
    if (!node.visible) {
        return
    }

    val isLeaf = node.children.isEmpty()
    val isSelected = node.id == selectedId
    val isExpanded = node.id in expandedIds
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        node.children.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
        else -> Color.Transparent
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isLeaf -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable {
                if (isLeaf) {
                    onLeafClick(node.id)
                } else {
                    onGroupToggle(node.id)
                }
            }
            .padding(start = (12 + node.level * 14).dp, top = 11.dp, end = 12.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (node.children.isNotEmpty()) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
        } else {
            Spacer(modifier = Modifier.size(26.dp))
        }

        node.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = node.title,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(10.dp))
        }

        Text(
            text = node.title,
            style = if (isLeaf) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleSmall,
            color = contentColor,
            fontWeight = if (isLeaf) FontWeight.Medium else FontWeight.SemiBold,
        )
    }

    if (isExpanded) {
        node.children.forEach { child ->
            FeatureMenuRow(
                node = child,
                selectedId = selectedId,
                expandedIds = expandedIds,
                onLeafClick = onLeafClick,
                onGroupToggle = onGroupToggle,
            )
        }
    }
}
