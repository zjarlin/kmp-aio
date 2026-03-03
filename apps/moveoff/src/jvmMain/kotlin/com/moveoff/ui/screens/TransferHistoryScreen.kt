package com.moveoff.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moveoff.model.TransferRecord
import com.moveoff.model.TransferStatus
import com.moveoff.progress.ProgressFormatter
import com.moveoff.ui.MainViewModel
import com.moveoff.ui.components.EmptyState
import com.moveoff.ui.theme.SuccessLight
import com.moveoff.ui.theme.ErrorLight
import com.moveoff.ui.theme.WarningLight
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransferHistoryScreen(viewModel: MainViewModel) {
    val tasks by viewModel.progressTracker.tasks.collectAsState()

    // Sample history data
    var historyRecords by remember { mutableStateOf(generateSampleHistory()) }
    var filterStatus by remember { mutableStateOf<TransferStatus?>(null) }

    val filteredRecords = historyRecords.filter {
        filterStatus == null || it.status == filterStatus
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "迁移记录",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "查看所有文件迁移任务的历史记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Filter chips
            Row {
                FilterChip(
                    selected = filterStatus == null,
                    onClick = { filterStatus = null },
                    label = { Text("全部") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filterStatus == TransferStatus.COMPLETED,
                    onClick = { filterStatus = TransferStatus.COMPLETED },
                    label = { Text("成功") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filterStatus == TransferStatus.FAILED,
                    onClick = { filterStatus = TransferStatus.FAILED },
                    label = { Text("失败") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "总任务",
                value = historyRecords.size.toString(),
                icon = Icons.Default.List,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "成功",
                value = historyRecords.count { it.status == TransferStatus.COMPLETED }.toString(),
                icon = Icons.Default.CheckCircle,
                color = SuccessLight,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "失败",
                value = historyRecords.count { it.status == TransferStatus.FAILED }.toString(),
                icon = Icons.Default.Warning,
                color = ErrorLight,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History List
        if (filteredRecords.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Info,
                message = "暂无记录",
                description = if (filterStatus != null) "该状态下没有记录" else "还没有文件迁移记录"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecords.sortedByDescending { it.createdAt }) { record ->
                    HistoryItem(record = record)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun HistoryItem(record: TransferRecord) {
    val statusColor = when (record.status) {
        TransferStatus.COMPLETED -> SuccessLight
        TransferStatus.FAILED -> ErrorLight
        TransferStatus.CANCELLED -> WarningLight
        else -> MaterialTheme.colorScheme.primary
    }

    val statusIcon = when (record.status) {
        TransferStatus.COMPLETED -> Icons.Default.CheckCircle
        TransferStatus.FAILED -> Icons.Default.Warning
        TransferStatus.CANCELLED -> Icons.Default.Close
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // File Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = "${record.sourcePath} → ${record.targetPath}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1
                )
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date(record.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // File Size
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ProgressFormatter.formatBytes(record.fileSize),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = ProgressFormatter.formatStatusName(
                        com.moveoff.progress.TaskStatus.valueOf(record.status.name)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}

// Sample data
private fun generateSampleHistory(): List<TransferRecord> {
    val statuses = listOf(TransferStatus.COMPLETED, TransferStatus.COMPLETED, TransferStatus.COMPLETED, TransferStatus.FAILED, TransferStatus.CANCELLED)
    val fileNames = listOf("document.pdf", "image.jpg", "video.mp4", "backup.zip", "project.zip", "data.json", "report.xlsx")

    return (1..20).map { index ->
        val status = statuses.random()
        TransferRecord(
            id = "record_$index",
            taskId = "task_$index",
            sourcePath = "/Users/zjarlin/Documents/${fileNames.random()}",
            targetPath = "/home/user/moveoff/${fileNames.random()}",
            fileName = fileNames.random(),
            fileSize = (1024L..1024L * 1024 * 100).random(),
            serverId = "server_1",
            status = status,
            createdAt = System.currentTimeMillis() - (0L..86400000L * 7).random(),
            completedAt = if (status == TransferStatus.COMPLETED) System.currentTimeMillis() - (0L..86400000L).random() else null,
            errorMessage = if (status == TransferStatus.FAILED) "连接超时" else null
        )
    }
}
