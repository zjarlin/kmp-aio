package com.kcloud.features.quicktransfer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcloud.features.quicktransfer.QuickTransferService
import com.kcloud.features.quicktransfer.QuickTransferState
import com.kcloud.features.quicktransfer.QuickTransferSyncStatus
import com.kcloud.features.quicktransfer.ui.components.StageProgress
import com.kcloud.features.quicktransfer.ui.components.StageStatus
import com.kcloud.features.quicktransfer.ui.components.StagedProgressIndicator
import com.kcloud.features.quicktransfer.ui.components.SyncStage
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun QuickTransferScreen(
    service: QuickTransferService = koinInject()
) {
    val appState by service.state.collectAsState()
    val scope = rememberCoroutineScope()
    var statusMessage by remember {
        mutableStateOf("这里负责调度当前同步引擎；如果引擎尚未初始化，会给出明确提示。")
    }

    val engineAvailable = service.isEngineAvailable()
    val stages = remember(appState) { buildStageProgress(appState) }
    val currentStage = remember(appState.syncStatus) { appState.syncStatus.toCurrentStage() }

    fun updateStatus(message: String) {
        statusMessage = message
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("快速迁移", style = MaterialTheme.typography.headlineSmall)
        Text(
            statusMessage,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                title = "同步状态",
                value = appState.syncStatus.displayName(),
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "待上传 / 待下载",
                value = "${appState.pendingUploads} / ${appState.pendingDownloads}",
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "冲突数",
                value = appState.conflictCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                updateStatus(service.triggerSync().message)
                            }
                        },
                        enabled = engineAvailable
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.height(0.dp))
                        Text("立即同步")
                    }

                    Button(
                        onClick = {
                            updateStatus(service.pause().message)
                        },
                        enabled = engineAvailable && appState.syncStatus != QuickTransferSyncStatus.PAUSED
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Text("暂停")
                    }

                    Button(
                        onClick = {
                            updateStatus(service.resume().message)
                        },
                        enabled = engineAvailable && appState.syncStatus == QuickTransferSyncStatus.PAUSED
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text("继续")
                    }
                }

                Text(
                    text = if (engineAvailable) {
                        "同步引擎已接入，页面现在通过 quick-transfer service 订阅实时状态。"
                    } else {
                        "同步引擎当前未初始化，所以这里只能展示状态壳与控制入口。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                StagedProgressIndicator(
                    stages = stages,
                    currentStage = currentStage
                )
            }
        }

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
                Text("当前上下文", fontWeight = FontWeight.SemiBold)
                Text("当前操作：${appState.currentOperation ?: "暂无"}")
                Text("在线状态：${if (appState.isOnline) "在线" else "离线"}")
                Text("整体进度：${(appState.overallProgress * 100).toInt()}%")
                Text("最近同步：${appState.lastSyncTime?.formatTimestamp() ?: "暂无"}")
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HintChip("拖拽上传入口", "拖文件后会写入同步目录并入队")
            HintChip("冲突处理", "冲突数会通过 quick-transfer service 同步到当前页面")
            HintChip("本地聚合服务", "server 侧仍保留 `/api/sync/*` 供外部调试")
        }
    }
}

@Composable
private fun StatusCard(
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
private fun HintChip(
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun buildStageProgress(appState: QuickTransferState): List<StageProgress> {
    val currentStage = appState.syncStatus.toCurrentStage()
    val currentStatus = appState.syncStatus
    val progress = appState.overallProgress.coerceIn(0f, 1f)
    val currentItem = appState.currentOperation.orEmpty()
    val totalItems = appState.pendingUploads + appState.pendingDownloads + appState.conflictCount

    return listOf(
        stageProgress(SyncStage.SCANNING, currentStage, currentStatus, progress, currentItem, totalItems),
        stageProgress(SyncStage.ANALYZING, currentStage, currentStatus, progress, currentItem, totalItems),
        stageProgress(SyncStage.TRANSFERRING, currentStage, currentStatus, progress, currentItem, totalItems),
        stageProgress(SyncStage.VERIFYING, currentStage, currentStatus, progress, currentItem, totalItems),
        stageProgress(SyncStage.FINALIZING, currentStage, currentStatus, progress, currentItem, totalItems),
        stageProgress(SyncStage.CONFLICTS, currentStage, currentStatus, progress, currentItem, totalItems)
    )
}

private fun stageProgress(
    stage: SyncStage,
    currentStage: SyncStage?,
    syncStatus: QuickTransferSyncStatus,
    progress: Float,
    currentItem: String,
    totalItems: Int
): StageProgress {
    val status = when {
        stage == currentStage && syncStatus == QuickTransferSyncStatus.ERROR -> StageStatus.ERROR
        stage == currentStage -> StageStatus.IN_PROGRESS
        currentStage == null && stage == SyncStage.SCANNING -> StageStatus.PENDING
        currentStage != null && stage.ordinal < currentStage.ordinal -> StageStatus.COMPLETED
        currentStage == SyncStage.CONFLICTS && stage == SyncStage.CONFLICTS && syncStatus == QuickTransferSyncStatus.CONFLICT -> StageStatus.IN_PROGRESS
        else -> StageStatus.PENDING
    }

    return StageProgress(
        stage = stage,
        status = status,
        progress = if (stage == currentStage) progress else if (status == StageStatus.COMPLETED) 1f else 0f,
        currentItem = if (stage == currentStage) currentItem else "",
        itemCount = totalItems,
        currentIndex = if (stage == currentStage && totalItems > 0) {
            (progress * totalItems).toInt().coerceAtLeast(1)
        } else {
            0
        }
    )
}

private fun QuickTransferSyncStatus.toCurrentStage(): SyncStage? {
    return when (this) {
        QuickTransferSyncStatus.IDLE,
        QuickTransferSyncStatus.PAUSED,
        QuickTransferSyncStatus.OFFLINE -> null
        QuickTransferSyncStatus.SCANNING -> SyncStage.SCANNING
        QuickTransferSyncStatus.UPLOADING,
        QuickTransferSyncStatus.DOWNLOADING -> SyncStage.TRANSFERRING
        QuickTransferSyncStatus.CONFLICT -> SyncStage.CONFLICTS
        QuickTransferSyncStatus.ERROR -> SyncStage.TRANSFERRING
    }
}

private fun QuickTransferSyncStatus.displayName(): String {
    return when (this) {
        QuickTransferSyncStatus.IDLE -> "空闲"
        QuickTransferSyncStatus.SCANNING -> "扫描中"
        QuickTransferSyncStatus.UPLOADING -> "上传中"
        QuickTransferSyncStatus.DOWNLOADING -> "下载中"
        QuickTransferSyncStatus.CONFLICT -> "冲突"
        QuickTransferSyncStatus.ERROR -> "错误"
        QuickTransferSyncStatus.PAUSED -> "已暂停"
        QuickTransferSyncStatus.OFFLINE -> "离线"
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
        append(':')
        append(dateTime.second.toString().padStart(2, '0'))
    }
}
