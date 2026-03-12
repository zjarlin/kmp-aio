package com.kcloud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kcloud.model.Conflict
import com.kcloud.model.ConflictStrategy
import java.text.SimpleDateFormat
import java.util.*

/**
 * 冲突解决对话框 - 完整的文件冲突解决 UI
 *
 * @param conflict 冲突信息
 * @param onResolve 解决回调
 * @param onCancel 取消回调
 * @param onSkip 跳过此冲突回调
 */
@Composable
fun ConflictResolutionDialog(
    conflict: Conflict,
    onResolve: (strategy: ConflictStrategy, rememberChoice: Boolean) -> Unit,
    onCancel: () -> Unit,
    onSkip: () -> Unit = onCancel
) {
    var selectedStrategy by remember { mutableStateOf<ConflictStrategy?>(null) }
    var rememberChoice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.CompareArrows,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "文件冲突",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "以下文件在本地和云端都有修改，请选择要保留的版本：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 文件路径
                FilePathCard(path = conflict.path)

                Spacer(modifier = Modifier.height(16.dp))

                // 版本对比
                VersionComparison(
                    conflict = conflict,
                    selectedStrategy = selectedStrategy,
                    onStrategySelected = { selectedStrategy = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 记住选择
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberChoice = !rememberChoice }
                ) {
                    Checkbox(
                        checked = rememberChoice,
                        onCheckedChange = { rememberChoice = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "记住我的选择（适用于后续类似冲突）",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedStrategy?.let { strategy ->
                        onResolve(strategy, rememberChoice)
                    }
                },
                enabled = selectedStrategy != null
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("跳过")
            }
        }
    )
}

/**
 * 文件路径卡片
 */
@Composable
private fun FilePathCard(
    path: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = path,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 版本对比区域
 */
@Composable
private fun VersionComparison(
    conflict: Conflict,
    selectedStrategy: ConflictStrategy?,
    onStrategySelected: (ConflictStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 本地版本
        VersionCard(
            title = "本地版本",
            icon = Icons.Default.Computer,
            version = conflict.localVersion,
            size = conflict.localSize,
            modifiedTime = conflict.localMtime,
            dateFormat = dateFormat,
            isSelected = selectedStrategy == ConflictStrategy.OVERWRITE,
            onClick = { onStrategySelected(ConflictStrategy.OVERWRITE) },
            color = MaterialTheme.colorScheme.primaryContainer
        )

        // 远程版本
        VersionCard(
            title = "云端版本",
            icon = Icons.Default.Cloud,
            version = conflict.remoteVersion,
            size = conflict.remoteSize,
            modifiedTime = conflict.remoteMtime,
            dateFormat = dateFormat,
            isSelected = selectedStrategy == ConflictStrategy.SKIP,
            onClick = { onStrategySelected(ConflictStrategy.SKIP) },
            color = MaterialTheme.colorScheme.tertiaryContainer
        )

        // 保留两者
        VersionCard(
            title = "保留两者",
            icon = Icons.Default.ContentCopy,
            version = "创建副本",
            size = conflict.localSize + conflict.remoteSize,
            modifiedTime = null,
            dateFormat = dateFormat,
            isSelected = selectedStrategy == ConflictStrategy.RENAME,
            onClick = { onStrategySelected(ConflictStrategy.RENAME) },
            color = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

/**
 * 版本卡片
 */
@Composable
private fun VersionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    version: String,
    size: Long,
    modifiedTime: Long?,
    dateFormat: SimpleDateFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选择指示器
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "版本: $version",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "大小: ${formatFileSize(size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                modifiedTime?.let { time ->
                    Text(
                        text = "修改: ${dateFormat.format(Date(time))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 选中标记
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

/**
 * 批量冲突解决对话框
 *
 * @param conflicts 冲突列表
 * @param onResolveAll 批量解决回调
 * @param onResolveOne 单个解决回调
 * @param onCancel 取消回调
 */
@Composable
fun BatchConflictResolutionDialog(
    conflicts: List<Conflict>,
    onResolveAll: (strategy: ConflictStrategy) -> Unit,
    onResolveOne: (conflictId: String, strategy: ConflictStrategy) -> Unit,
    onCancel: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var applyToAll by remember { mutableStateOf(false) }
    var selectedStrategy by remember { mutableStateOf<ConflictStrategy?>(null) }

    val currentConflict = conflicts.getOrNull(currentIndex)

    if (currentConflict == null || applyToAll) {
        // 显示批量选择对话框
        BatchStrategySelectionDialog(
            conflictCount = conflicts.size,
            onSelectStrategy = { strategy ->
                if (applyToAll) {
                    onResolveAll(strategy)
                } else {
                    selectedStrategy = strategy
                }
            },
            onCancel = onCancel
        )
        return
    }

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            BadgedBox(
                badge = {
                    Badge { Text(conflicts.size.toString()) }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        title = {
            Text(
                text = "解决冲突 (${currentIndex + 1}/${conflicts.size})",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            ConflictResolutionDialog(
                conflict = currentConflict,
                onResolve = { strategy, remember ->
                    if (remember) {
                        applyToAll = true
                        selectedStrategy = strategy
                    }
                    onResolveOne(currentConflict.id, strategy)
                    currentIndex++
                },
                onCancel = onCancel,
                onSkip = {
                    currentIndex++
                }
            )
        },
        confirmButton = { },
        dismissButton = { }
    )
}

/**
 * 批量策略选择对话框
 */
@Composable
private fun BatchStrategySelectionDialog(
    conflictCount: Int,
    onSelectStrategy: (ConflictStrategy) -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.SelectAll,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("批量解决冲突") },
        text = {
            Column {
                Text("检测到 $conflictCount 个文件冲突，请选择统一处理方式：")
                Spacer(modifier = Modifier.height(16.dp))

                StrategyOption(
                    icon = Icons.Default.Computer,
                    title = "全部使用本地版本",
                    description = "本地文件将覆盖云端版本",
                    onClick = { onSelectStrategy(ConflictStrategy.OVERWRITE) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                StrategyOption(
                    icon = Icons.Default.Cloud,
                    title = "全部使用云端版本",
                    description = "下载云端版本覆盖本地文件",
                    onClick = { onSelectStrategy(ConflictStrategy.SKIP) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                StrategyOption(
                    icon = Icons.Default.ContentCopy,
                    title = "全部保留两者",
                    description = "冲突文件将重命名后同时保留",
                    onClick = { onSelectStrategy(ConflictStrategy.RENAME) }
                )
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        }
    )
}

/**
 * 策略选项
 */
@Composable
private fun StrategyOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
