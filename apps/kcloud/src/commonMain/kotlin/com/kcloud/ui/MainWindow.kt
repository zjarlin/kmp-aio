package com.kcloud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcloud.app.KCloudFeatureRegistry
import com.kcloud.app.KCloudShellState
import com.kcloud.feature.KCloudMenuNode
import com.kcloud.feature.ShellSettingsService
import com.kcloud.feature.ShellThemeMode
import com.kcloud.ui.theme.KCloudTheme
import org.koin.compose.koinInject

@Composable
fun MainWindow(
    featureRegistry: KCloudFeatureRegistry = koinInject(),
    shellState: KCloudShellState = koinInject(),
    shellSettingsService: ShellSettingsService = koinInject()
) {
    val selectedMenuId by shellState.selectedMenuId.collectAsState()
    val expandedMenuIds by shellState.expandedMenuIds.collectAsState()
    val themeMode by shellSettingsService.themeMode.collectAsState()

    val selectedNode = featureRegistry.findLeaf(selectedMenuId)

    KCloudTheme(
        darkTheme = when (themeMode) {
            ShellThemeMode.LIGHT -> false
            ShellThemeMode.DARK -> true
            ShellThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                FeatureSidebar(
                    nodes = featureRegistry.menuTree,
                    selectedId = selectedMenuId,
                    expandedIds = expandedMenuIds,
                    onLeafClick = shellState::selectMenu,
                    onGroupToggle = shellState::toggleGroup,
                    modifier = Modifier.width(240.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f)) {
                        selectedNode?.entry?.content?.invoke()
                            ?: EmptyShellContent()
                    }
                    ShellStatusBar(
                        currentTitle = selectedNode?.title ?: "未选择页面",
                        modifier = Modifier.height(60.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureSidebar(
    nodes: List<KCloudMenuNode>,
    selectedId: String,
    expandedIds: Set<String>,
    onLeafClick: (String) -> Unit,
    onGroupToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        Text(
            text = "KCloud",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            nodes.forEach { node ->
                FeatureMenuRow(
                    node = node,
                    selectedId = selectedId,
                    expandedIds = expandedIds,
                    onLeafClick = onLeafClick,
                    onGroupToggle = onGroupToggle
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "模块化壳层",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "功能由 feature 聚合",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FeatureMenuRow(
    node: KCloudMenuNode,
    selectedId: String,
    expandedIds: Set<String>,
    onLeafClick: (String) -> Unit,
    onGroupToggle: (String) -> Unit
) {
    if (!node.visible) {
        return
    }

    val isSelected = node.id == selectedId
    val isExpanded = node.id in expandedIds
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable {
                if (node.children.isEmpty()) {
                    onLeafClick(node.id)
                } else {
                    onGroupToggle(node.id)
                }
            }
            .padding(start = (12 + node.level * 18).dp, top = 10.dp, end = 12.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (node.children.isNotEmpty()) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }

        node.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = node.title,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Text(
            text = node.title,
            color = contentColor,
            fontWeight = if (node.children.isEmpty()) FontWeight.Medium else FontWeight.SemiBold
        )
    }

    if (isExpanded) {
        node.children.forEach { child ->
            FeatureMenuRow(
                node = child,
                selectedId = selectedId,
                expandedIds = expandedIds,
                onLeafClick = onLeafClick,
                onGroupToggle = onGroupToggle
            )
        }
    }
}

@Composable
private fun ShellStatusBar(
    currentTitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "本地插件已聚合",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun EmptyShellContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无可用页面",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
