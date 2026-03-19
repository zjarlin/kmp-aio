package com.kcloud.features.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kcloud.model.Conflict
import com.kcloud.model.ConflictStrategy
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun ConflictResolutionDialog(
    conflict: Conflict,
    onResolve: (strategy: ConflictStrategy, rememberChoice: Boolean) -> Unit,
    onCancel: () -> Unit,
    onSkip: () -> Unit = onCancel,
) {
    var selectedStrategy by remember { mutableStateOf<ConflictStrategy?>(null) }
    var rememberChoice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
        },
        title = {
            Text(
                text = "文件冲突",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "以下文件在本地和云端都有修改，请选择要保留的版本：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))
                FilePathCard(path = conflict.path)
                Spacer(modifier = Modifier.height(16.dp))

                VersionComparison(
                    conflict = conflict,
                    selectedStrategy = selectedStrategy,
                    onStrategySelected = { selectedStrategy = it },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberChoice = !rememberChoice },
                ) {
                    Checkbox(
                        checked = rememberChoice,
                        onCheckedChange = { rememberChoice = it },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "记住我的选择（适用于后续类似冲突）",
                        style = MaterialTheme.typography.bodySmall,
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
                enabled = selectedStrategy != null,
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("跳过")
            }
        },
    )
}

@Composable
private fun FilePathCard(
    path: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = path,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun VersionComparison(
    conflict: Conflict,
    selectedStrategy: ConflictStrategy?,
    onStrategySelected: (ConflictStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        VersionCard(
            title = "本地版本",
            icon = Icons.Default.Computer,
            version = conflict.localVersion,
            size = conflict.localSize,
            modifiedTime = conflict.localMtime,
            isSelected = selectedStrategy == ConflictStrategy.OVERWRITE,
            onClick = { onStrategySelected(ConflictStrategy.OVERWRITE) },
            color = MaterialTheme.colorScheme.primaryContainer,
        )

        VersionCard(
            title = "云端版本",
            icon = Icons.Default.Cloud,
            version = conflict.remoteVersion,
            size = conflict.remoteSize,
            modifiedTime = conflict.remoteMtime,
            isSelected = selectedStrategy == ConflictStrategy.SKIP,
            onClick = { onStrategySelected(ConflictStrategy.SKIP) },
            color = MaterialTheme.colorScheme.tertiaryContainer,
        )

        VersionCard(
            title = "保留两者",
            icon = Icons.Default.ContentCopy,
            version = "创建副本",
            size = conflict.localSize + conflict.remoteSize,
            modifiedTime = null,
            isSelected = selectedStrategy == ConflictStrategy.RENAME,
            onClick = { onStrategySelected(ConflictStrategy.RENAME) },
            color = MaterialTheme.colorScheme.secondaryContainer,
        )
    }
}

@Composable
private fun VersionCard(
    title: String,
    icon: ImageVector,
    version: String,
    size: Long,
    modifiedTime: Long?,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color else MaterialTheme.colorScheme.surface,
        ),
        border = if (isSelected) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
            )
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "版本: $version",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "大小: ${formatFileSize(size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                modifiedTime?.let { time ->
                    Text(
                text = "修改: ${time.formatTimestamp()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

@Composable
fun BatchConflictResolutionDialog(
    conflicts: List<Conflict>,
    onResolveAll: (strategy: ConflictStrategy) -> Unit,
    onResolveOne: (conflictId: String, strategy: ConflictStrategy) -> Unit,
    onCancel: () -> Unit,
) {
    var currentIndex by remember { mutableStateOf(0) }
    var applyToAll by remember { mutableStateOf(false) }
    var selectedStrategy by remember { mutableStateOf<ConflictStrategy?>(null) }

    val currentConflict = conflicts.getOrNull(currentIndex)

    if (currentConflict == null || applyToAll) {
        BatchStrategySelectionDialog(
            conflictCount = conflicts.size,
            onSelectStrategy = { strategy ->
                if (applyToAll) {
                    onResolveAll(strategy)
                } else {
                    selectedStrategy = strategy
                }
            },
            onCancel = onCancel,
        )
        return
    }

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            BadgedBox(
                badge = {
                    Badge { Text(conflicts.size.toString()) }
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            }
        },
        title = {
            Text(
                text = "解决冲突 (${currentIndex + 1}/${conflicts.size})",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            ConflictResolutionDialog(
                conflict = currentConflict,
                onResolve = { strategy, rememberChoice ->
                    if (rememberChoice) {
                        applyToAll = true
                        selectedStrategy = strategy
                    }
                    onResolveOne(currentConflict.id, strategy)
                    currentIndex++
                },
                onCancel = onCancel,
                onSkip = {
                    currentIndex++
                },
            )
        },
        confirmButton = {},
        dismissButton = {},
    )
}

@Composable
private fun BatchStrategySelectionDialog(
    conflictCount: Int,
    onSelectStrategy: (ConflictStrategy) -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.SelectAll,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
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
                    onClick = { onSelectStrategy(ConflictStrategy.OVERWRITE) },
                )

                Spacer(modifier = Modifier.height(8.dp))

                StrategyOption(
                    icon = Icons.Default.Cloud,
                    title = "全部使用云端版本",
                    description = "下载云端版本覆盖本地文件",
                    onClick = { onSelectStrategy(ConflictStrategy.SKIP) },
                )

                Spacer(modifier = Modifier.height(8.dp))

                StrategyOption(
                    icon = Icons.Default.ContentCopy,
                    title = "全部保留两者",
                    description = "冲突文件将重命名后同时保留",
                    onClick = { onSelectStrategy(ConflictStrategy.RENAME) },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun StrategyOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun Long.formatTimestamp(): String {
    val dateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    return buildString {
        append(dateTime.year)
        append('-')
        append(dateTime.month.toString().padStart(2, '0'))
        append('-')
        append(dateTime.day.toString().padStart(2, '0'))
        append(' ')
        append(dateTime.hour.toString().padStart(2, '0'))
        append(':')
        append(dateTime.minute.toString().padStart(2, '0'))
    }
}
