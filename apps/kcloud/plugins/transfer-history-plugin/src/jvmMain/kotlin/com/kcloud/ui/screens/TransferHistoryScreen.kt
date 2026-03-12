package com.kcloud.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcloud.plugins.transferhistory.TransferHistoryQueueItem
import com.kcloud.plugins.transferhistory.TransferHistoryQueueOperation
import com.kcloud.plugins.transferhistory.TransferHistoryQueueStatus
import com.kcloud.plugins.transferhistory.TransferHistoryService
import org.koin.compose.koinInject

@Composable
fun TransferHistoryScreen(
    service: TransferHistoryService = koinInject()
) {
    val snapshot by service.snapshot.collectAsState()
    val stats = snapshot.stats
    val queueItems = snapshot.queueItems
    var status by remember { mutableStateOf("这里展示的是本地数据库里的迁移统计与同步队列。") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("迁移记录", style = MaterialTheme.typography.headlineSmall)
        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard("文件总数", stats.totalFiles.toString(), Modifier.weight(1f))
            StatsCard("已同步", stats.syncedFiles.toString(), Modifier.weight(1f))
            StatsCard("冲突", stats.conflicts.toString(), Modifier.weight(1f))
            StatsCard("失败队列", stats.queueFailed.toString(), Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard("待上传", stats.pendingUploads.toString(), Modifier.weight(1f))
            StatsCard("待下载", stats.pendingDownloads.toString(), Modifier.weight(1f))
            StatsCard("队列待处理", stats.queuePending.toString(), Modifier.weight(1f))
            StatsCard("队列运行中", stats.queueRunning.toString(), Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    status = service.clearCompletedQueue().message
                }
            ) {
                Text("清理已完成队列")
            }
            Button(
                onClick = {
                    status = "当前队列 ${queueItems.size} 条，文件记录 ${stats.totalFiles} 条"
                }
            ) {
                Text("刷新摘要")
            }
        }

        if (queueItems.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Text(
                    text = "当前还没有同步队列记录。等文件进入数据库或同步任务开始后，这里会有内容。",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(queueItems, key = { item -> item.id }) { item ->
                    QueueItemCard(item)
                }
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QueueItemCard(item: TransferHistoryQueueItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("任务 #${item.id}", fontWeight = FontWeight.SemiBold)
                Text(item.status.displayName(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("操作：${item.operation.displayName()}")
            Text("文件 ID：${item.fileId}")
            LinearProgressIndicator(
                progress = {
                    if (item.totalBytes > 0) {
                        (item.progressBytes.toFloat() / item.totalBytes).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = buildString {
                    append("进度 ${item.progressBytes} / ${item.totalBytes} B")
                    append(" · 重试 ${item.retryCount}")
                    if (!item.errorMessage.isNullOrBlank()) {
                        append(" · 错误 ${item.errorMessage}")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun TransferHistoryQueueStatus.displayName(): String {
    return when (this) {
        TransferHistoryQueueStatus.PENDING -> "等待中"
        TransferHistoryQueueStatus.RUNNING -> "运行中"
        TransferHistoryQueueStatus.PAUSED -> "已暂停"
        TransferHistoryQueueStatus.COMPLETED -> "已完成"
        TransferHistoryQueueStatus.FAILED -> "失败"
        TransferHistoryQueueStatus.CANCELLED -> "已取消"
    }
}

private fun TransferHistoryQueueOperation.displayName(): String {
    return when (this) {
        TransferHistoryQueueOperation.UPLOAD -> "上传"
        TransferHistoryQueueOperation.DOWNLOAD -> "下载"
        TransferHistoryQueueOperation.DELETE_LOCAL -> "删除本地"
        TransferHistoryQueueOperation.DELETE_REMOTE -> "删除远程"
    }
}
